package com.avbinvest.user.util;

import com.avbinvest.user.dto.CompanyDTO;
import com.avbinvest.user.dto.UserRequestDTO;
import com.avbinvest.user.dto.UserResponseDTO;
import com.avbinvest.user.module.User;

/**
 * Utility class for converting between User entities and User DTOs.
 * <p>
 * Provides methods to convert from {@link UserRequestDTO} to {@link User} entity
 * and from {@link User} entity to {@link UserResponseDTO} with optional company data.
 * </p>
 */
public class UserConverter {

    /**
     * Converts a {@link UserRequestDTO} into a {@link User} entity.
     * Maps fields: firstName, lastName, phoneNumber, companyId.
     *
     * @param dto Data transfer object containing user creation or update data.
     * @return User entity constructed from DTO fields.
     */
    public static User convertDtoToEntity(UserRequestDTO dto) {
        return User.builder()
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .phoneNumber(dto.getPhoneNumber())
                .companyId(dto.getCompanyId())
                .build();
    }

    /**
     * Converts a {@link User} entity to a {@link UserResponseDTO}, including
     * the associated company information.
     *
     * @param user    User entity to convert.
     * @param company Company data transfer object or null if none.
     * @return UserResponseDTO with user and company details.
     */
    public static UserResponseDTO convertEntityToDto(User user, CompanyDTO company) {
        return UserResponseDTO.builder()
                .Id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phoneNumber(user.getPhoneNumber())
                .company(company)
                .build();
    }
}
