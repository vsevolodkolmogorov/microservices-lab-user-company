package com.avbinvest.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for user responses.
 * Represents user data returned by the API, including associated company info.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponseDTO {

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

    /**
     * Company information the user belongs to.
     */
    private CompanyDTO company;
}
