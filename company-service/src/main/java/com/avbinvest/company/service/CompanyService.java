package com.avbinvest.company.service;


import com.avbinvest.company.dto.CompanyCreateDTO;
import com.avbinvest.company.dto.CompanyResponseDTO;
import com.avbinvest.company.dto.CompanyUpdateDTO;
import com.avbinvest.company.dto.UserDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;


public interface CompanyService {
    CompanyResponseDTO createCompany(CompanyCreateDTO dto);
    CompanyResponseDTO updateCompany(Long id, CompanyUpdateDTO dto);
    CompanyResponseDTO getCompanyById(Long id, boolean includeEmployees);
    Page<CompanyResponseDTO> getAllCompanies(Pageable pageable, boolean includeEmployees);
    List<UserDTO> fetchUsersByIds(List<Long> ids);
    void deleteCompany(Long id);
    void addEmployee(Long companyId, Long userId);
    void removeEmployee(Long companyId, Long userId);
}
