package com.avbinvest.user.repository;

import com.avbinvest.user.module.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for managing {@link User} entities.
 * Extends Spring Data JPA's {@link JpaRepository} to provide standard CRUD operations,
 * along with custom query methods for specific use cases.
 */
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Retrieves a user by their unique identifier.
     *
     * @param id the id of the user to retrieve
     * @return an {@link Optional} containing the found user or empty if none found
     */
    Optional<User> getUserById(Long id);

    /**
     * Finds all users with IDs contained in the provided list.
     *
     * @param ids list of user IDs to search for
     * @return list of users matching the given IDs
     */
    List<User> findAllByIdIn(List<Long> ids);

    /**
     * Finds a user by their phone number.
     *
     * @param phoneNumber the phone number to search by (case-sensitive)
     * @return the user with the specified phone number, or null if not found
     */
    User findUserByPhoneNumber(String phoneNumber);
}
