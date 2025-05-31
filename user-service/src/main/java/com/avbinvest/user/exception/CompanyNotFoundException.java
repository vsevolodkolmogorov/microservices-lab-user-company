package com.avbinvest.user.exception;

/**
 * Exception thrown when a company with the specified ID is not found.
 * This is a runtime exception used to indicate that a requested company does not exist.
 */
public class CompanyNotFoundException extends RuntimeException {

    /**
     * Constructs a new {@code CompanyNotFoundException} with a detailed message including the company ID.
     *
     * @param id the ID of the company that was not found
     */
    public CompanyNotFoundException(Long id) {
        super("Company not found with id: " + id);
    }
}
