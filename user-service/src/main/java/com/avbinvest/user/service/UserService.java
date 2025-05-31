package com.avbinvest.user.service;

import com.avbinvest.user.dto.CompanyDTO;
import com.avbinvest.user.dto.UserRequestDTO;
import com.avbinvest.user.dto.UserResponseDTO;

import java.util.List;

/**
 * Service interface for managing users and their association with companies.
 * Defines business operations related to user creation, update, retrieval,
 * deletion, and company membership management.
 */
public interface UserService {

    /**
     * Creates a new user based on the provided data transfer object.
     *
     * @param dto the user data for creation
     * @return the created user's response DTO
     */
    UserResponseDTO createUser(UserRequestDTO dto);

    /**
     * Updates an existing user identified by the given ID using the provided data.
     *
     * @param id  the ID of the user to update
     * @param dto the updated user data
     * @return the updated user's response DTO
     */
    UserResponseDTO updateUser(Long id, UserRequestDTO dto);

    /**
     * Retrieves a user by their unique identifier.
     *
     * @param id the ID of the user to retrieve
     * @return the user response DTO
     */
    UserResponseDTO getUserById(Long id);

    /**
     * Retrieves all users in the system.
     *
     * @return list of all user response DTOs
     */
    List<UserResponseDTO> getAllUsers();

    /**
     * Deletes a user by their unique identifier.
     *
     * @param id the ID of the user to delete
     */
    void deleteUser(Long id);

    /**
     * Removes a user from a specific company.
     *
     * @param companyId the ID of the company
     * @param userId    the ID of the user to remove
     */
    void removeUserFromCompany(Long companyId, Long userId);

    /**
     * Adds a user to a specific company.
     *
     * @param companyId the ID of the company
     * @param userId    the ID of the user to add
     * @return the updated user response DTO
     */
    UserResponseDTO addUserToCompany(Long companyId, Long userId);

    /**
     * Retrieves a list of users by their IDs.
     *
     * @param ids list of user IDs to retrieve
     * @return list of user response DTOs
     */
    List<UserResponseDTO> getUsersByIds(List<Long> ids);

    /**
     * Fetches company information by its ID.
     *
     * @param id the ID of the company
     * @return the company data transfer object
     */
    CompanyDTO fetchCompanyById(Long id);
}
