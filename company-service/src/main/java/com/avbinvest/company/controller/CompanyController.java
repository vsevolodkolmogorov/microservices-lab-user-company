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

@RestController
@RequestMapping("/api/company")
@RequiredArgsConstructor
@Validated
public class CompanyController {

    private final CompanyService companyService;

    @GetMapping
    public ResponseEntity<List<CompanyResponseDTO>> getAllCompanies() {
        List<CompanyResponseDTO> usersList = companyService.getAllCompanies();
        return ResponseEntity.ok(usersList);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CompanyResponseDTO> getCompanyById(@PathVariable Long id) {
        CompanyResponseDTO user = companyService.getCompanyById(id);
        return ResponseEntity.ok(user);
    }

    @PostMapping
    public ResponseEntity<CompanyResponseDTO> createCompany(@Validated(OnCreate.class) @RequestBody CompanyRequestDTO userDTO) {
        CompanyResponseDTO user = companyService.createCompany(userDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    @PostMapping("/{id}/employees")
    public ResponseEntity<Void> addEmployee(@PathVariable Long id, @RequestBody Long userId) {
        companyService.addEmployee(id,userId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}/employees/{userId}")
    public ResponseEntity<Void> removeEmployee(@PathVariable Long id, @PathVariable Long userId) {
        companyService.removeEmployee(id,userId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<CompanyResponseDTO> updateCompany(@PathVariable Long id, @Validated(OnUpdate.class) @RequestBody CompanyRequestDTO userDTO) {
        CompanyResponseDTO user = companyService.updateCompany(id, userDTO);
        return ResponseEntity.ok(user);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCompany(@PathVariable Long id) {
        companyService.deleteCompany(id);
        return ResponseEntity.noContent().build();
    }
}
