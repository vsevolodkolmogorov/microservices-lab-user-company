package com.avbinvest.company.controller;

import com.avbinvest.company.dto.CompanyRequestDTO;
import com.avbinvest.company.dto.CompanyResponseDTO;
import com.avbinvest.company.service.CompanyService;
import com.avbinvest.company.validation.OnCreate;
import com.avbinvest.company.validation.OnUpdate;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for managing companies.
 * Provides endpoints for CRUD operations and managing employees within companies.
 */
@RestController
@RequestMapping("/api/company")
@RequiredArgsConstructor
@Validated
public class CompanyController {

    private final CompanyService companyService;

    /**
     * Retrieves a list of all companies.
     *
     * @return HTTP 200 with the list of companies.
     */
    @GetMapping
    public ResponseEntity<List<CompanyResponseDTO>> getAllCompanies() {
        List<CompanyResponseDTO> companies = companyService.getAllCompanies(true);
        return ResponseEntity.ok(companies);
    }

    /**
     * Retrieves a company by its ID.
     *
     * @param id              the company ID
     * @param includeEmployees whether to include employees in the response
     * @return HTTP 200 with the company data
     */
    @GetMapping("/{id}")
    public ResponseEntity<CompanyResponseDTO> getCompanyById(@PathVariable Long id,
                                                             @RequestParam(defaultValue = "true") boolean includeEmployees) {
        CompanyResponseDTO company = companyService.getCompanyById(id, includeEmployees);
        return ResponseEntity.ok(company);
    }

    /**
     * Creates a new company.
     *
     * @param companyDTO company data for creation, validated on {@code OnCreate} group
     * @return HTTP 201 with the created company data
     */
    @PostMapping
    public ResponseEntity<CompanyResponseDTO> createCompany(@Validated(OnCreate.class) @RequestBody CompanyRequestDTO companyDTO) {
        CompanyResponseDTO company = companyService.createCompany(companyDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(company);
    }

    /**
     * Adds an employee to a company.
     *
     * @param id     the company ID
     * @param userId the user ID to add as employee
     * @return HTTP 204 No Content
     */
    @PostMapping("/{id}/addEmployee")
    public ResponseEntity<Void> addEmployee(@PathVariable Long id, @RequestParam Long userId) {
        companyService.addEmployee(id, userId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Removes an employee from a company.
     *
     * @param id     the company ID
     * @param userId the user ID to remove from the company
     * @return HTTP 204 No Content
     */
    @DeleteMapping("/{id}/removeEmployee")
    public ResponseEntity<Void> removeEmployee(@PathVariable Long id, @RequestParam Long userId) {
        companyService.removeEmployee(id, userId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Updates an existing company.
     *
     * @param id         the company ID
     * @param companyDTO company data for update, validated on {@code OnUpdate} group
     * @return HTTP 200 with the updated company data
     */
    @PutMapping("/{id}")
    public ResponseEntity<CompanyResponseDTO> updateCompany(@PathVariable Long id,
                                                            @Validated(OnUpdate.class) @RequestBody CompanyRequestDTO companyDTO) {
        CompanyResponseDTO company = companyService.updateCompany(id, companyDTO);
        return ResponseEntity.ok(company);
    }

    /**
     * Deletes a company by its ID.
     *
     * @param id the company ID
     * @return HTTP 204 No Content
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCompany(@PathVariable Long id) {
        companyService.deleteCompany(id);
        return ResponseEntity.noContent().build();
    }
}
