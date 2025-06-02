package com.avbinvest.user.dto;

import jakarta.validation.constraints.NotBlank;
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
public class UserCreateDTO {

    @NotBlank(message = "FirstName must not be blank")
    @Size(min = 3, max = 30, message = "FirstName length must be between 3 and 100 characters")
    private String firstName;

    @NotBlank(message = "LastName must not be blank")
    @Size(min = 3, max = 30, message = "LastName length must be between 3 and 100 characters")
    private String lastName;

    @NotBlank(message = "Phone must not be blank")
    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Invalid phone number")
    private String phoneNumber;

    @PositiveOrZero(message = "CompanyId must be positive or zero")
    private Long companyId;
}
