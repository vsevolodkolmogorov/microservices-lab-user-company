package com.avbinvest.company.controller;

import com.avbinvest.company.dto.CompanyCreateDTO;
import com.avbinvest.company.dto.CompanyResponseDTO;
import com.avbinvest.company.dto.CompanyUpdateDTO;
import com.avbinvest.company.service.CompanyService;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@RestController
@RequestMapping("/api/company")
@RequiredArgsConstructor
@Validated
@Slf4j
public class CompanyController {

    private final CompanyService companyService;

    @GetMapping
    public Page<CompanyResponseDTO> getAllCompanies(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "true") boolean includeEmployees)  {
        log.info("GET /api/company — getAllCompanies() page={} size={}", page, size);
        Pageable pageable = PageRequest.of(page, size);
        return companyService.getAllCompanies(pageable, includeEmployees);
    }

    @GetMapping("/{id}")
    public CompanyResponseDTO getCompanyById(@PathVariable @Min(1) Long id,
                                             @RequestParam(defaultValue = "true") boolean includeEmployees) {
        log.info("GET /api/company/{} — includeEmployees={}", id, includeEmployees);
        return companyService.getCompanyById(id, includeEmployees);
    }

    @PostMapping
    public CompanyResponseDTO createCompany(@Validated @RequestBody CompanyCreateDTO companyDTO) {
        log.info("POST /api/company — createCompany: {}", companyDTO);
        return companyService.createCompany(companyDTO);
    }

    @PostMapping("/{id}/addEmployee")
    public void addEmployee(@PathVariable @Min(1) Long id, @RequestParam @Min(1) Long userId) {
        log.info("POST /api/company/{}/addEmployee — userId={}", id, userId);
        companyService.addEmployee(id, userId);
    }

    @DeleteMapping("/{id}/removeEmployee")
    public void removeEmployee(@PathVariable @Min(1) Long id, @RequestParam @Min(1) Long userId) {
        log.info("DELETE /api/company/{}/removeEmployee — userId={}", id, userId);
        companyService.removeEmployee(id, userId);
    }

    @PutMapping("/{id}")
    public CompanyResponseDTO updateCompany(@PathVariable @Min(1) Long id,
                                            @Validated @RequestBody CompanyUpdateDTO companyDTO) {
        log.info("PUT /api/company/{} — updateCompany: {}", id, companyDTO);
        return companyService.updateCompany(id, companyDTO);
    }

    @DeleteMapping("/{id}")
    public void deleteCompany(@PathVariable @Min(1) Long id) {
        log.info("DELETE /api/company/{}", id);
        companyService.deleteCompany(id);
    }
}
