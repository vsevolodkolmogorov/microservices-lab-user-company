package com.avbinvest.company.exceptions;

/**
 * Exception indicating a conflict in the application state or business logic.
 * Typically, thrown when an operation violates unique constraints or state consistency.
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
