package com.avbinvest.company.util;

import com.avbinvest.company.dto.CompanyRequestDTO;
import com.avbinvest.company.dto.CompanyResponseDTO;
import com.avbinvest.company.dto.UserDTO;
import com.avbinvest.company.module.Company;

import java.util.List;

public class CompanyConverter {
    public static Company convertDtoToEntity(CompanyRequestDTO dto) {
        return Company.builder()
                .name(dto.getName())
                .budget(dto.getBudget())
                .employeeIds(dto.getEmployeeIds())
                .build();
    }

    public static CompanyResponseDTO convertEntityToDto(Company company, List<UserDTO> userDTO) {
        return CompanyResponseDTO.builder()
                .Id(company.getId())
                .name(company.getName())
                .budget(company.getBudget())
                .employeeIds(userDTO)
                .build();
    }
}
