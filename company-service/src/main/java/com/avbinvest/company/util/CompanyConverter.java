package com.avbinvest.company.util;

import com.avbinvest.company.dto.CompanyCreateDTO;
import com.avbinvest.company.dto.CompanyResponseDTO;
import com.avbinvest.company.dto.UserDTO;
import com.avbinvest.company.module.Company;

import java.util.List;

/**
 * Utility class for converting between Company entities and DTOs.
 */
public class CompanyConverter {

    public static Company convertDtoToEntity(CompanyCreateDTO dto) {
        return Company.builder()
                .name(dto.getName())
                .budget(dto.getBudget())
                .employeeIds(dto.getEmployeeIds())
                .build();
    }

    public static CompanyResponseDTO convertEntityToDto(Company company, List<UserDTO> userDTO) {
        return CompanyResponseDTO.builder()
                .id(company.getId())
                .name(company.getName())
                .budget(company.getBudget())
                .employeeIds(userDTO)
                .build();
    }
}
