package com.avbinvest.company.exceptions;

/**
 * Exception thrown when a company with the specified ID is not found.
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
