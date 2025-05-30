package com.avbinvest.user.service;

import com.avbinvest.user.feignClient.CompanyClient;
import com.avbinvest.user.dto.CompanyDTO;
import com.avbinvest.user.dto.UserRequestDTO;
import com.avbinvest.user.dto.UserResponseDTO;
import com.avbinvest.user.exception.CompanyNotFoundException;
import com.avbinvest.user.exception.ConflictException;
import com.avbinvest.user.exception.RestRequestFailedException;
import com.avbinvest.user.exception.UserNotFoundException;
import com.avbinvest.user.module.User;
import com.avbinvest.user.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.avbinvest.user.util.UserConverter.*;
import static com.avbinvest.user.util.UserConverter.convertEntityToDto;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RestTemplate restTemplate;
    private final CompanyClient companyClient;

    @Override
    public UserResponseDTO createUser(UserRequestDTO dto) {
        log.info("[UserService] Creating user with phone: {}", dto.getPhoneNumber());

        if (dto.getPhoneNumber() != null) {
            validatePhoneNumberUniqueness(dto.getPhoneNumber());
        }

        CompanyDTO company = null;

        if (dto.getCompanyId() != null) {
            log.info("[UserService] Fetching company with ID {} for user", dto.getCompanyId());
            company = fetchCompanyById(dto.getCompanyId());
        }

        User user = userRepository.save(convertDtoToEntity(dto));
        log.info("[UserService] User created with ID {}", user.getId());

        if (dto.getCompanyId() != null) {
            addUserToCompany(user.getId(), dto.getCompanyId());
        }

        return convertEntityToDto(user, company);
    }

    @Override
    public UserResponseDTO updateUser(Long id, UserRequestDTO dto) {
        log.info("[UserService] Updating user with ID {}", id);
        User user = userRepository.getUserById(id).orElseThrow(() -> new UserNotFoundException(id));

        if (dto.getPhoneNumber() != null) {
            validatePhoneNumberUniqueness(dto.getPhoneNumber(), id);
        }
        CompanyDTO company = null;

        if (dto.getCompanyId() != null) {
            log.info("[UserService] Fetching new company with ID {} for user update", dto.getCompanyId());
            company = fetchCompanyById(dto.getCompanyId());

            if (user.getCompanyId() != null && !user.getCompanyId().equals(dto.getCompanyId())) {
                log.info("[UserService] Removing user {} from old company {}", id, user.getCompanyId());
                removeUserFromCompany(user.getId(), user.getCompanyId());

                log.info("[UserService] Adding user {} to new company {}", id, dto.getCompanyId());
                addUserToCompany(user.getId(), dto.getCompanyId());
            }
        } else if (user.getCompanyId() != null) {
            company = fetchCompanyById(user.getCompanyId());
        }

        patchUser(user, dto);
        User updatedUser = userRepository.save(user);
        log.info("[UserService] User with ID {} successfully updated", id);
        return convertEntityToDto(updatedUser, company);
    }

    @Override
    public UserResponseDTO getUserById(Long id) {
        log.info("[UserService] Fetching user with ID {}", id);
        User user = userRepository.getUserById(id).orElseThrow(() -> new UserNotFoundException(id));
        CompanyDTO company = fetchCompanyIfExists(user.getCompanyId());
        return convertEntityToDto(user, company);
    }

    @Override
    public List<UserResponseDTO> getAllUsers() {
        log.info("[UserService] Fetching all users");
        List<User> usersList = userRepository.findAll();
        return usersList.stream()
                .map(this::mapUserWithCompany)
                .toList();
    }

    @Override
    public List<UserResponseDTO> getUsersByIds(List<Long> ids) {
        log.info("[UserService] Fetching users by IDs: {}", ids);
        List<User> users = userRepository.findAllByIdIn(ids);

        if (users.isEmpty()) {
            log.warn("[UserService] No users found for IDs: {}", ids);
            return null;
        }

        return users.stream()
                .map(this::mapUserWithCompany)
                .toList();
    }

    @Override
    public void deleteUser(Long id) {
        log.info("[UserService] Deleting user with ID {}", id);
        User user = userRepository.getUserById(id).orElseThrow(() -> new UserNotFoundException(id));
        removeUserFromCompany(user.getId(), user.getCompanyId());
        userRepository.delete(user);
        log.info("[UserService] User with ID {} deleted", id);
    }


    @Override
    public CompanyDTO fetchCompanyById(Long id) {
        try {
            return companyClient.getCompanyById(id, false);
        } catch (HttpClientErrorException.NotFound ex) {
            log.warn("[UserService] Company with ID {} not found", id);
            throw new CompanyNotFoundException(id);
        }
    }

    @Override
    public UserResponseDTO addUserToCompany(Long userId, Long companyId) {
        log.info("[UserService] Adding user {} to company {}", userId, companyId);
        CompanyDTO company = fetchCompanyById(companyId);
        User user = userRepository.getUserById(userId).orElseThrow(() -> new UserNotFoundException(userId));

        if (user.getCompanyId() != null && !Objects.equals(user.getCompanyId(), companyId)) {
            log.warn("[UserService] User {} already belongs to another company {}", userId, user.getCompanyId());
            throw new ConflictException("User belongs to other company with id " + user.getCompanyId());
        }

        companyClient.addEmployee(companyId, userId);

        user.setCompanyId(companyId);
        log.info("[UserService] User {} successfully added to company {}", userId, companyId);
        return convertEntityToDto(userRepository.save(user), company);
    }

    @Override
    public void removeUserFromCompany(Long userId, Long companyId) {
        log.info("[UserService] Removing user {} from company {}", userId, companyId);
        fetchCompanyById(companyId);
        User user = userRepository.getUserById(userId).orElseThrow(() -> new UserNotFoundException(userId));

        validateUserCompanyMembership(user, companyId);

        companyClient.removeEmployee(companyId, userId);

        user.setCompanyId(null);
        userRepository.save(user);
        log.info("[UserService] User {} successfully removed from company {}", userId, companyId);
    }

    // --- Private methods ---

    private void performCompanyServiceCall(String url, HttpMethod method, String errorMessage) {
        try {
            ResponseEntity<Void> response = restTemplate.exchange(url, method, null, Void.class);
            if (!response.getStatusCode().is2xxSuccessful()) {
                log.error("[UserService] Company service call failed. Status: {}", response.getStatusCode());
                throw new RestRequestFailedException(errorMessage);
            }
        } catch (HttpClientErrorException ex) {
            log.error("[UserService] Company service call error: {}", ex.getMessage());
            throw new RestRequestFailedException(errorMessage + ": " + ex.getMessage());
        }
    }

    private CompanyDTO fetchCompanyIfExists(Long companyId) {
        return companyId != null ? fetchCompanyById(companyId) : null;
    }

    private UserResponseDTO mapUserWithCompany(User user) {
        CompanyDTO company = fetchCompanyIfExists(user.getCompanyId());
        return convertEntityToDto(user, company);
    }


    private void validateUserCompanyMembership(User user, Long expectedCompanyId) {
        if (!Objects.equals(user.getCompanyId(), expectedCompanyId)) {
            log.warn("[UserService] User {} does not belong to company {}", user.getId(), expectedCompanyId);
            throw new ConflictException("User does not belong to this company with id: " + expectedCompanyId);
        }
    }

    private void validatePhoneNumberUniqueness(String phoneNumber) {
        User userCheck = userRepository.findUserByPhoneNumber(phoneNumber);
        if (userCheck != null) {
            log.warn("[UserService] Phone number {} is already used by another user", phoneNumber);
            throw new ConflictException("User with such phone: " + phoneNumber + " already exists in the system");
        }
    }

    private void validatePhoneNumberUniqueness(String phoneNumber, Long currentUserId) {
        User userCheck = userRepository.findUserByPhoneNumber(phoneNumber);
        if (userCheck != null && !userCheck.getId().equals(currentUserId)) {
            log.warn("[UserService] Phone number {} is already used by another user", phoneNumber);
            throw new ConflictException("User with such phone: " + phoneNumber + " already exists in the system");
        }
    }


    private void patchUser(User user, UserRequestDTO dto) {
        Optional.ofNullable(dto.getCompanyId())
                .ifPresent(user::setCompanyId);

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

