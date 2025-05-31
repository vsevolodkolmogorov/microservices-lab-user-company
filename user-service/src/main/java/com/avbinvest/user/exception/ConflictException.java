package com.avbinvest.user.exception;

/**
 * Exception thrown to indicate a conflict situation,
 * typically when attempting to create or update an entity
 * that violates unique constraints or business rules.
 */
public class ConflictException extends RuntimeException {

    /**
     * Constructs a new {@code ConflictException} with the specified detail message.
     *
     * @param message the detail message explaining the conflict
     */
    public ConflictException(String message) {
        super(message);
    }
}
