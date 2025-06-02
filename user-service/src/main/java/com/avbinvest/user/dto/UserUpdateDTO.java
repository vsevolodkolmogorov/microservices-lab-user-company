package com.avbinvest.user.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserUpdateDTO {

    @Size(min = 3, max = 30, message = "FirstName length must be between 3 and 100 characters")
    private String firstName;

    @Size(min = 3, max = 30, message = "LastName length must be between 3 and 100 characters")
    private String lastName;

    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Invalid phone number")
    private String phoneNumber;

    @PositiveOrZero(message = "CompanyId must be positive or zero")
    private Long companyId;
}
