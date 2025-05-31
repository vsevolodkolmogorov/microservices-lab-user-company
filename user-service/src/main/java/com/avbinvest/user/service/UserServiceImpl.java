package com.avbinvest.user.service;

import com.avbinvest.user.feignClient.CompanyClient;
import com.avbinvest.user.dto.CompanyDTO;
import com.avbinvest.user.dto.UserRequestDTO;
import com.avbinvest.user.dto.UserResponseDTO;
import com.avbinvest.user.exception.CompanyNotFoundException;
import com.avbinvest.user.exception.ConflictException;
import com.avbinvest.user.exception.UserNotFoundException;
import com.avbinvest.user.module.User;
import com.avbinvest.user.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.avbinvest.user.util.UserConverter.*;
import static com.avbinvest.user.util.UserConverter.convertEntityToDto;

/**
 * Service implementation for managing users.
 * <p>
 * Provides operations for creating, updating, retrieving, and deleting users,
 * as well as managing their association with companies via {@link CompanyClient}.
 * Ensures business rules like phone number uniqueness and company membership consistency.
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final CompanyClient companyClient;

    /**
     * Creates a new user.
     * Validates uniqueness of phone number and associates user with a company if provided.
     *
     * @param dto User data transfer object containing creation info.
     * @return UserResponseDTO representing the created user.
     * @throws ConflictException if phone number is already used.
     * @throws CompanyNotFoundException if provided company ID does not exist.
     */
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

    /**
     * Updates an existing user identified by {@code id}.
     * Validates phone number uniqueness and handles company reassignment if company ID changes.
     *
     * @param id  Identifier of the user to update.
     * @param dto User data transfer object containing update info.
     * @return UserResponseDTO representing the updated user.
     * @throws UserNotFoundException if user with given ID does not exist.
     * @throws ConflictException if phone number is already used by another user.
     * @throws CompanyNotFoundException if new company ID does not exist.
     */
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

    /**
     * Retrieves a user by their ID.
     *
     * @param id Identifier of the user to retrieve.
     * @return UserResponseDTO of the requested user.
     * @throws UserNotFoundException if user is not found.
     */
    @Override
    public UserResponseDTO getUserById(Long id) {
        log.info("[UserService] Fetching user with ID {}", id);
        User user = userRepository.getUserById(id).orElseThrow(() -> new UserNotFoundException(id));
        CompanyDTO company = fetchCompanyIfExists(user.getCompanyId());
        return convertEntityToDto(user, company);
    }

    /**
     * Retrieves all users in the system.
     *
     * @return List of UserResponseDTO representing all users.
     */
    @Override
    public List<UserResponseDTO> getAllUsers() {
        log.info("[UserService] Fetching all users");
        List<User> usersList = userRepository.findAll();
        return usersList.stream()
                .map(this::mapUserWithCompany)
                .toList();
    }

    /**
     * Retrieves users by a list of IDs.
     *
     * @param ids List of user IDs to fetch.
     * @return List of UserResponseDTO matching the provided IDs, or null if none found.
     */
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

    /**
     * Deletes a user by their ID.
     * Also removes the user from their associated company if any.
     *
     * @param id Identifier of the user to delete.
     * @throws UserNotFoundException if user is not found.
     */
    @Override
    public void deleteUser(Long id) {
        log.info("[UserService] Deleting user with ID {}", id);
        User user = userRepository.getUserById(id).orElseThrow(() -> new UserNotFoundException(id));
        removeUserFromCompany(user.getId(), user.getCompanyId());
        userRepository.delete(user);
        log.info("[UserService] User with ID {} deleted", id);
    }

    /**
     * Fetches a company by its ID via {@link CompanyClient}.
     *
     * @param id Company identifier.
     * @return CompanyDTO representing the company.
     * @throws CompanyNotFoundException if company is not found.
     */
    @Override
    public CompanyDTO fetchCompanyById(Long id) {
        try {
            return companyClient.getCompanyById(id, false);
        } catch (HttpClientErrorException.NotFound ex) {
            log.warn("[UserService] Company with ID {} not found", id);
            throw new CompanyNotFoundException(id);
        }
    }

    /**
     * Adds a user to a company.
     * Validates company existence and user membership constraints.
     *
     * @param userId    ID of the user to add.
     * @param companyId ID of the company to add the user to.
     * @return UserResponseDTO of the updated user.
     * @throws UserNotFoundException if user not found.
     * @throws CompanyNotFoundException if company not found.
     * @throws ConflictException if user already belongs to a different company.
     */
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

    /**
     * Removes a user from a company.
     * Validates company and user association before removal.
     *
     * @param userId    ID of the user to remove.
     * @param companyId ID of the company to remove the user from.
     * @throws UserNotFoundException if user not found.
     * @throws CompanyNotFoundException if company not found.
     * @throws ConflictException if user does not belong to the specified company.
     */
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

    // --- Private helper methods with minimal javadoc for internal use ---

    /**
     * Fetches a company if {@code companyId} is not null.
     *
     * @param companyId ID of the company or null.
     * @return CompanyDTO or null if companyId is null.
     */
    private CompanyDTO fetchCompanyIfExists(Long companyId) {
        return companyId != null ? fetchCompanyById(companyId) : null;
    }

    /**
     * Maps a User entity to UserResponseDTO along with company info if exists.
     *
     * @param user User entity.
     * @return UserResponseDTO with company data.
     */
    private UserResponseDTO mapUserWithCompany(User user) {
        CompanyDTO company = fetchCompanyIfExists(user.getCompanyId());
        return convertEntityToDto(user, company);
    }

    /**
     * Validates that a user belongs to the expected company.
     *
     * @param user              User entity.
     * @param expectedCompanyId Expected company ID.
     * @throws ConflictException if user does not belong to expected company.
     */
    private void validateUserCompanyMembership(User user, Long expectedCompanyId) {
        if (!Objects.equals(user.getCompanyId(), expectedCompanyId)) {
            log.warn("[UserService] User {} does not belong to company {}", user.getId(), expectedCompanyId);
            throw new ConflictException("User does not belong to this company with id: " + expectedCompanyId);
        }
    }

    /**
     * Validates phone number uniqueness for a new user.
     *
     * @param phoneNumber Phone number to check.
     * @throws ConflictException if phone number is already used.
     */
    private void validatePhoneNumberUniqueness(String phoneNumber) {
        User userCheck = userRepository.findUserByPhoneNumber(phoneNumber);
        if (userCheck != null) {
            log.warn("[UserService] Phone number {} is already used by another user", phoneNumber);
            throw new ConflictException("User with such phone: " + phoneNumber + " already exists in the system");
        }
    }

    /**
     * Validates phone number uniqueness when updating existing user.
     *
     * @param phoneNumber   Phone number to check.
     * @param currentUserId ID of the user being updated.
     * @throws ConflictException if phone number is used by another user.
     */
    private void validatePhoneNumberUniqueness(String phoneNumber, Long currentUserId) {
        User userCheck = userRepository.findUserByPhoneNumber(phoneNumber);
        if (userCheck != null && !userCheck.getId().equals(currentUserId)) {
            log.warn("[UserService] Phone number {} is already used by another user", phoneNumber);
            throw new ConflictException("User with such phone: " + phoneNumber + " already exists in the system");
        }
    }

    /**
     * Applies non-null and non-blank fields from DTO to the user entity.
     *
     * @param user User entity to patch.
     * @param dto  UserRequestDTO with update fields.
     */
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

