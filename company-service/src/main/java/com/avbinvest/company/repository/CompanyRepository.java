package com.avbinvest.company.repository;

import com.avbinvest.company.module.Company;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repository interface for {@link Company} entity.
 *
 * Extends JpaRepository to provide basic CRUD operations.
 * Additional methods allow fetching company by name or id.
 */
public interface CompanyRepository extends JpaRepository<Company, Long> {

    /**
     * Finds a company by its exact name.
     *
     * @param name the company name
     * @return the company entity if found, otherwise null
     */
    Company getCompanyByName(String name);

    /**
     * Finds a company by its id.
     *
     * @param id the company id
     * @return an Optional containing the company if found, or empty otherwise
     */
    Optional<Company> getCompanyById(Long id);
}
