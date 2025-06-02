package com.avbinvest.user.service;

import com.avbinvest.user.dto.UserUpdateDTO;
import com.avbinvest.user.feignClient.CompanyClient;
import com.avbinvest.user.dto.CompanyDTO;
import com.avbinvest.user.dto.UserCreateDTO;
import com.avbinvest.user.dto.UserResponseDTO;
import com.avbinvest.user.exception.CompanyNotFoundException;
import com.avbinvest.user.exception.ConflictException;
import com.avbinvest.user.exception.UserNotFoundException;
import com.avbinvest.user.module.User;
import com.avbinvest.user.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.avbinvest.user.util.UserConverter.*;

/**
 * Service implementation for managing users.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final CompanyClient companyClient;

    @Override
    public UserResponseDTO createUser(UserCreateDTO dto) {
        log.info("[UserService] Creating user with phone: {}", dto.getPhoneNumber());

        validatePhoneNumber(dto.getPhoneNumber());

        CompanyDTO company = fetchCompanyIfPresent(dto.getCompanyId());

        User user = userRepository.save(convertDtoToEntity(dto));

        addUserToCompanyIfPresent(user.getId(), dto.getCompanyId());

        UserResponseDTO response = convertEntityToDto(user, company);
        log.info("[UserService] User created with ID {}", user.getId());
        return response;
    }

    @Override
    public UserResponseDTO updateUser(Long id, UserUpdateDTO dto) {
        log.info("[UserService] Updating user with ID {}", id);

        User user = findUserOrThrow(id);

        validatePhoneNumberForUpdate(dto.getPhoneNumber(), id);

        CompanyDTO company = resolveCompanyForUpdate(user, dto);

        patchUser(user, dto);

        User updatedUser = userRepository.save(user);

        UserResponseDTO response = convertEntityToDto(updatedUser, company);
        log.info("[UserService] User with ID {} successfully updated", id);
        return response;
    }

    @Override
    public UserResponseDTO getUserById(Long id) {

        User user = findUserOrThrow(id);
        CompanyDTO company = fetchCompanyIfExists(user.getCompanyId());

        UserResponseDTO response = convertEntityToDto(user, company);
        log.info("[UserService] Fetched user with ID {}", id);
        return response;
    }

    @Override
    public Page<UserResponseDTO> getAllUsers(Pageable pageable) {

        Page<User> usersPage = userRepository.findAll(pageable);

        Page<UserResponseDTO> response = usersPage.map(this::mapUserWithCompany);

        log.info("[UserService] Fetched {} companies out of total {}", response.getNumberOfElements(), response.getTotalElements());
        return response;
    }

    @Override
    public Page<UserResponseDTO> getUsersByIds(List<Long> ids, Pageable pageable) {
        Page<User> usersPage = userRepository.findAllByIdIn(ids, pageable);

        if (usersPage.isEmpty()) {
            log.warn("[UserService] No users found for IDs: {}", ids);
            return Page.empty(pageable);
        }

        List<UserResponseDTO> dtoList = usersPage.getContent().stream()
                .map(this::mapUserWithCompany)
                .toList();

        log.info("[UserService] Fetched users by IDs, count: {}", dtoList.size());

        return new PageImpl<>(dtoList, pageable, usersPage.getTotalElements());
    }

    @Override
    public void deleteUser(Long id) {

        User user = findUserOrThrow(id);

        removeUserFromCompanyIfExists(user);

        userRepository.delete(user);

        log.info("[UserService] User with ID {} deleted", id);
    }

    @Override
    public UserResponseDTO addUserToCompany(Long userId, Long companyId) {

        CompanyDTO company = fetchCompanyByIdOrThrow(companyId);
        User user = findUserOrThrow(userId);

        validateUserCompanyConflict(user, companyId);

        companyClient.addEmployee(companyId, userId);

        user.setCompanyId(companyId);
        User savedUser = userRepository.save(user);

        UserResponseDTO response = convertEntityToDto(savedUser, company);

        log.info("[UserService] User {} successfully added to company {}", userId, companyId);

        return response;
    }

    @Override
    public void removeUserFromCompany(Long userId, Long companyId) {

        fetchCompanyByIdOrThrow(companyId);
        User user = findUserOrThrow(userId);

        validateUserCompanyMembership(user, companyId);

        companyClient.removeEmployee(companyId, userId);

        user.setCompanyId(null);
        userRepository.save(user);

        log.info("[UserService] User {} successfully removed from company {}", userId, companyId);
    }

    // --- Private method

    private User findUserOrThrow(Long id) {
        return userRepository.getUserById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
    }

    private CompanyDTO fetchCompanyByIdOrThrow(Long companyId) {
        try {
            return companyClient.getCompanyById(companyId, false);
        } catch (HttpClientErrorException.NotFound ex) {
            log.warn("[UserService] Company with ID {} not found", companyId);
            throw new CompanyNotFoundException(companyId);
        }
    }

    private void validatePhoneNumber(String phoneNumber) {
        if (phoneNumber == null) return;
        checkPhoneNumberUniqueness(phoneNumber);
    }

    private void validatePhoneNumberForUpdate(String phoneNumber, Long currentUserId) {
        if (phoneNumber == null) return;
        checkPhoneNumberUniquenessForUpdate(phoneNumber, currentUserId);
    }

    private void checkPhoneNumberUniqueness(String phoneNumber) {
        User existingUser = userRepository.findUserByPhoneNumber(phoneNumber);
        if (existingUser != null) {
            log.warn("[UserService] Phone number {} is already used by another user", phoneNumber);
            throw new ConflictException("User with such phone: " + phoneNumber + " already exists in the system");
        }
    }

    private void checkPhoneNumberUniquenessForUpdate(String phoneNumber, Long currentUserId) {
        User existingUser = userRepository.findUserByPhoneNumber(phoneNumber);
        if (existingUser != null && !existingUser.getId().equals(currentUserId)) {
            log.warn("[UserService] Phone number {} is already used by another user", phoneNumber);
            throw new ConflictException("User with such phone: " + phoneNumber + " already exists in the system");
        }
    }

    private void validateUserCompanyConflict(User user, Long companyId) {
        if (user.getCompanyId() != null && !Objects.equals(user.getCompanyId(), companyId)) {
            log.warn("[UserService] User {} already belongs to another company {}", user.getId(), user.getCompanyId());
            throw new ConflictException("User belongs to other company with id " + user.getCompanyId());
        }
    }

    private void validateUserCompanyMembership(User user, Long expectedCompanyId) {
        if (!Objects.equals(user.getCompanyId(), expectedCompanyId)) {
            log.warn("[UserService] User {} does not belong to company {}", user.getId(), expectedCompanyId);
            throw new ConflictException("User does not belong to this company with id: " + expectedCompanyId);
        }
    }

    private CompanyDTO fetchCompanyIfExists(Long companyId) {
        if (companyId == null) return null;
        return fetchCompanyByIdOrThrow(companyId);
    }

    private UserResponseDTO mapUserWithCompany(User user) {
        CompanyDTO company = fetchCompanyIfExists(user.getCompanyId());
        return convertEntityToDto(user, company);
    }

    private void patchUser(User user, UserUpdateDTO dto) {
        Optional.ofNullable(dto.getCompanyId()).ifPresent(user::setCompanyId);
        Optional.ofNullable(dto.getLastName()).filter(s -> !s.isBlank()).ifPresent(user::setLastName);
        Optional.ofNullable(dto.getFirstName()).filter(s -> !s.isBlank()).ifPresent(user::setFirstName);
        Optional.ofNullable(dto.getPhoneNumber()).filter(s -> !s.isBlank()).ifPresent(user::setPhoneNumber);
    }

    private CompanyDTO fetchCompanyIfPresent(Long companyId) {
        return Optional.ofNullable(companyId)
                .map(this::fetchCompanyByIdOrThrow)
                .orElse(null);
    }

    private void addUserToCompanyIfPresent(Long userId, Long companyId) {
        if (companyId != null) {
            addUserToCompany(userId, companyId);
        }
    }

    private CompanyDTO resolveCompanyForUpdate(User user, UserUpdateDTO dto) {
        if (dto.getCompanyId() != null) {
            handleCompanyChangeIfNeeded(user, dto.getCompanyId());
            return fetchCompanyByIdOrThrow(dto.getCompanyId());
        }
        if (user.getCompanyId() != null) {
            return fetchCompanyByIdOrThrow(user.getCompanyId());
        }
        return null;
    }

    private void handleCompanyChangeIfNeeded(User user, Long newCompanyId) {
        if (user.getCompanyId() != null && !user.getCompanyId().equals(newCompanyId)) {
            removeUserFromCompany(user.getId(), user.getCompanyId());
            addUserToCompany(user.getId(), newCompanyId);
        }
    }

    private void removeUserFromCompanyIfExists(User user) {
        Optional.ofNullable(user.getCompanyId())
                .ifPresent(companyId -> removeUserFromCompany(user.getId(), companyId));
    }
}
