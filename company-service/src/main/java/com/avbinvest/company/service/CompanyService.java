package com.avbinvest.company.service;

import com.avbinvest.company.dto.CompanyRequestDTO;
import com.avbinvest.company.dto.CompanyResponseDTO;
import com.avbinvest.company.dto.UserDTO;

import java.util.List;

public interface CompanyService {
    CompanyResponseDTO createCompany(CompanyRequestDTO dto);
    CompanyResponseDTO updateCompany(Long id, CompanyRequestDTO dto);
    CompanyResponseDTO getCompanyById(Long id);
    List<CompanyResponseDTO> getAllCompanies();
    void deleteCompany(Long id);
    List<UserDTO> fetchUsersByIds(List<Long> ids);
}
