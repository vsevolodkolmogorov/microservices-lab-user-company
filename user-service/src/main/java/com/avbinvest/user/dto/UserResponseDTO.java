package com.avbinvest.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponseDTO {

    private Long Id;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private CompanyDTO company;
}
