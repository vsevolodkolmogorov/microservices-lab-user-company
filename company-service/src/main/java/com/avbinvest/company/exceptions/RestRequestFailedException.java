package com.avbinvest.company.exceptions;

/**
 * Exception thrown when a REST request fails due to client or server errors.
 */
public class RestRequestFailedException extends RuntimeException {

    /**
     * Constructs a new RestRequestFailedException with the specified detail message.
     *
     * @param message the detail message describing the failure
     */
    public RestRequestFailedException(String message) {
        super(message);
    }
}
