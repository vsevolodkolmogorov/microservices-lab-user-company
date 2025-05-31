package com.avbinvest.company.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object representing a user.
 *
 * Contains basic user information such as ID, first name, last name, and phone number.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDTO {
    /**
     * Unique identifier of the user.
     */
    private Long Id;

    /**
     * User's first name.
     */
    private String firstName;

    /**
     * User's last name.
     */
    private String lastName;

    /**
     * User's phone number.
     */
    private String phoneNumber;
}
