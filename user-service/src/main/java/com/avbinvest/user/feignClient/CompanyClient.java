package com.avbinvest.user.feignClient;

import com.avbinvest.user.dto.CompanyDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

/**
 * Feign client interface for communicating with the Company Service.
 * Provides methods to retrieve company details and manage employees within a company.
 */
@FeignClient(name = "company-service")
public interface CompanyClient {

    /**
     * Retrieves a company by its ID.
     *
     * @param id the ID of the company to retrieve
     * @param includeEmployees flag indicating whether to include employee details in the response
     * @return the company data transfer object containing company information
     */
    @GetMapping("/api/company/{id}")
    CompanyDTO getCompanyById(@PathVariable Long id, @RequestParam(defaultValue = "false") boolean includeEmployees);

    /**
     * Adds an employee to the specified company.
     *
     * @param id the ID of the company
     * @param userId the ID of the user to be added as an employee
     */
    @PostMapping("/api/company/{id}/addEmployee")
    void addEmployee(@PathVariable Long id, @RequestParam Long userId);

    /**
     * Removes an employee from the specified company.
     *
     * @param id the ID of the company
     * @param userId the ID of the user to be removed from employees
     */
    @DeleteMapping("/api/company/{id}/removeEmployee")
    void removeEmployee(@PathVariable Long id, @RequestParam Long userId);
}
