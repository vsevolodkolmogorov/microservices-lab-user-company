package com.avbinvest.user.module;

import jakarta.persistence.*;
import lombok.*;

/**
 * Represents a user entity in the system.
 * <p>
 * Maps to the "users" table in the database. Contains basic user information
 * including personal details and association with a company.
 * </p>
 */
@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class User {

    /**
     * Primary key identifier for the user.
     * Generated automatically by the database.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    /**
     * User's first name.
     */
    private String firstName;

    /**
     * User's last name.
     */
    private String lastName;

    /**
     * User's phone number in international format.
     */
    private String phoneNumber;

    /**
     * Identifier of the company the user belongs to.
     * Can be null if the user is not assigned to any company.
     */
    private Long companyId;
}
