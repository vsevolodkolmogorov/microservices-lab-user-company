package com.avbinvest.company.service;

import com.avbinvest.company.dto.CompanyRequestDTO;
import com.avbinvest.company.dto.CompanyResponseDTO;
import com.avbinvest.company.dto.UserDTO;

import java.util.List;

/**
 * Service interface for managing companies and their employees.
 */
public interface CompanyService {

    /**
     * Creates a new company.
     *
     * @param dto the company data transfer object containing creation details
     * @return the created company details
     */
    CompanyResponseDTO createCompany(CompanyRequestDTO dto);

    /**
     * Updates an existing company by its id.
     *
     * @param id the company id
     * @param dto the company data transfer object containing update details
     * @return the updated company details
     */
    CompanyResponseDTO updateCompany(Long id, CompanyRequestDTO dto);

    /**
     * Retrieves a company by its id.
     *
     * @param id the company id
     * @param includeEmployees whether to include employee details in the response
     * @return the company details
     */
    CompanyResponseDTO getCompanyById(Long id, boolean includeEmployees);

    /**
     * Retrieves all companies.
     *
     * @param includeEmployees whether to include employee details in each company
     * @return list of all companies
     */
    List<CompanyResponseDTO> getAllCompanies(boolean includeEmployees);

    /**
     * Deletes a company by its id.
     *
     * @param id the company id
     */
    void deleteCompany(Long id);

    /**
     * Adds an employee to a company.
     *
     * @param companyId the company id
     * @param userId the employee's user id
     */
    void addEmployee(Long companyId, Long userId);

    /**
     * Removes an employee from a company.
     *
     * @param companyId the company id
     * @param userId the employee's user id
     */
    void removeEmployee(Long companyId, Long userId);

    /**
     * Fetches user details by a list of user ids.
     *
     * @param ids list of user ids
     * @return list of user details
     */
    List<UserDTO> fetchUsersByIds(List<Long> ids);
}
