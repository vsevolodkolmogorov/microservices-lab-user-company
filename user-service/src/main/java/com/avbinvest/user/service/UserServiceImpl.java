package com.avbinvest.user.service;

import com.avbinvest.user.dto.CompanyDTO;
import com.avbinvest.user.dto.UserRequestDTO;
import com.avbinvest.user.dto.UserResponseDTO;
import com.avbinvest.user.exception.CompanyNotFoundException;
import com.avbinvest.user.exception.UserNotFoundException;
import com.avbinvest.user.module.User;
import com.avbinvest.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Optional;

import static com.avbinvest.user.util.UserConverter.*;
import static com.avbinvest.user.util.UserConverter.convertEntityToDto;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RestTemplate restTemplate;

    @Value("${services.company-service-url}")
    private String companyServiceUrl;

    /**
     * @param dto
     * @return UserResponseDTO
     */
    @Override
    public UserResponseDTO createUser(UserRequestDTO dto) {
        CompanyDTO company = fetchCompanyById(dto.getCompanyId());
        User user = userRepository.save(convertDtoToEntity(dto));
        addUserToCompany(user.getCompanyId(), user.getId());
        return convertEntityToDto(user, company);
    }

    /**
     * @param id
     * @param dto
     * @return UserResponseDTO
     */
    @Override
    public UserResponseDTO updateUser(Long id, UserRequestDTO dto) {
        User user = userRepository.getUserById(id).orElseThrow(() -> new UserNotFoundException(id));
        CompanyDTO company;
        if (dto.getCompanyId() != null) {
            company = fetchCompanyById(dto.getCompanyId());
            removeUserFromCompany(user.getCompanyId(), user.getId());
            addUserToCompany(dto.getCompanyId(), user.getId());
        } else {
            company = fetchCompanyById(user.getCompanyId());
        }
        patchUser(user, dto);
        return convertEntityToDto(userRepository.save(user), company);
    }

    /**
     * @param id
     * @return UserResponseDTO
     */
    @Override
    public UserResponseDTO getUserById(Long id) {
        User user = userRepository.getUserById(id).orElseThrow(() -> new UserNotFoundException(id));
        CompanyDTO company = fetchCompanyById(user.getCompanyId());
        return convertEntityToDto(user, company);
    }

    /**
     * @return List<UserResponseDTO>
     */
    @Override
    public List<UserResponseDTO> getAllUsers() {
        List<User> usersList = userRepository.findAll();
        return usersList.stream()
                .map(user -> {
                    CompanyDTO company = fetchCompanyById(user.getCompanyId());
                    return convertEntityToDto(user, company);
                })
                .toList();
    }

    /**
     * @param id
     */
    @Override
    public void deleteUser(Long id) {
        User user = userRepository.getUserById(id).orElseThrow(() -> new UserNotFoundException(id));
        userRepository.delete(user);
    }

    /**
     * @param ids
     * @return List<UserResponseDTO>
     */
    @Override
    public List<UserResponseDTO> getUsersByIds(List<Long> ids) {
        List<User> users = userRepository.findAllByIdIn(ids);

        if (users.isEmpty()) return null;

        return users.stream()
                .map(user -> {
                    CompanyDTO company = fetchCompanyById(user.getCompanyId());
                    return convertEntityToDto(user, company);
                })
                .toList();
    }

    /**
     * @param id
     * @return CompanyDTO
     */
    @Override
    public CompanyDTO fetchCompanyById(Long id) {
        String url = companyServiceUrl + id;

        try {
            return restTemplate.getForObject(url, CompanyDTO.class);
        } catch (HttpClientErrorException.NotFound ex) {
            throw new CompanyNotFoundException(id);
        }
    }

    /**
     * @param companyId
     * @param userId
     */
    @Override
    public void addUserToCompany(Long companyId, Long userId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Long> request = new HttpEntity<>(userId, headers);

        String url = String.format(companyServiceUrl + "%d/employees", companyId);

        ResponseEntity<Void> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                request,
                Void.class
        );

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Failed to add user to company");
        }
    }

    /**
     * @param companyId
     * @param userId
     */
    @Override
    public void removeUserFromCompany(Long companyId, Long userId) {
        String url = String.format(companyServiceUrl + "%d/employees/%d", companyId, userId);

        try {
            restTemplate.exchange(url, HttpMethod.DELETE, null, Void.class);
        } catch (HttpClientErrorException.NotFound ex) {
            throw new RuntimeException("Failed to add user to company: " + ex.getMessage());
        }
    }

    private void patchUser(User user, UserRequestDTO dto) {
        Optional.ofNullable(dto.getCompanyId()).ifPresent(user::setCompanyId);
        Optional.ofNullable(dto.getLastName())
                .filter(s -> !s.isBlank())
                .ifPresent(user::setLastName);
        Optional.ofNullable(dto.getFirstName())
                .filter(s -> !s.isBlank())
                .ifPresent(user::setFirstName);
        Optional.ofNullable(dto.getPhoneNumber())
                .filter(s -> !s.isBlank())
                .ifPresent(user::setPhoneNumber);
    }
}
