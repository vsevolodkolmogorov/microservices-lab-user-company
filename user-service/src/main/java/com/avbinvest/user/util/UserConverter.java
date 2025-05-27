package com.avbinvest.user.util;

import com.avbinvest.user.dto.UserRequestDTO;
import com.avbinvest.user.dto.UserResponseDTO;
import com.avbinvest.user.module.User;

public class UserConverter {
    public static User convertDtoToEntity(UserRequestDTO dto) {
        return User.builder()
                .FirstName(dto.getFirstName())
                .LastName(dto.getLastName())
                .phoneNumber(dto.getPhoneNumber())
                .companyId(dto.getCompanyId())
                .build();
    }

    public static UserResponseDTO convertEntityToDto(User user) {
        return UserResponseDTO.builder()
                .Id(user.getId())
                .FirstName(user.getFirstName())
                .LastName(user.getLastName())
                .phoneNumber(user.getPhoneNumber())
                .company(null)
                .build();
    }
}
