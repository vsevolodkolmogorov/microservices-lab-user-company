package com.avbinvest.user.dto;

import com.avbinvest.user.validation.OnCreate;
import com.avbinvest.user.validation.OnUpdate;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for user requests.
 * Used for creating and updating user information with validation constraints.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRequestDTO {

    /**
     * User's first name.
     * Must not be blank when creating a new user.
     */
    @NotBlank(message = "FirstName must not be blank", groups = OnCreate.class)
    private String FirstName;

    /**
     * User's last name.
     * Must not be blank when creating a new user.
     */
    @NotBlank(message = "LastName must not be blank", groups = OnCreate.class)
    private String LastName;

    /**
     * User's phone number.
     * Must not be blank when creating a new user.
     * Must match the pattern: optional leading '+' followed by 10 to 15 digits.
     */
    @NotBlank(message = "Phone must not be blank", groups = OnCreate.class)
    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Invalid phone number", groups = {OnCreate.class, OnUpdate.class})
    private String phoneNumber;

    /**
     * Optional ID of the company the user belongs to.
     */
    private Long companyId;
}
