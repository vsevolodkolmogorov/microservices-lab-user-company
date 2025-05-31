package com.avbinvest.user.exception;

/**
 * Exception thrown when a user with the specified ID is not found.
 * Indicates that the requested user resource does not exist in the system.
 */
public class UserNotFoundException extends RuntimeException {

    /**
     * Constructs a new UserNotFoundException with a detailed message including the user ID.
     *
     * @param id the ID of the user that was not found
     */
    public UserNotFoundException(Long id) {
        super("User not found with id: " + id);
    }
}
