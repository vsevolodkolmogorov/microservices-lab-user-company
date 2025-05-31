package com.avbinvest.company.util;

import com.avbinvest.company.dto.CompanyRequestDTO;
import com.avbinvest.company.dto.CompanyResponseDTO;
import com.avbinvest.company.dto.UserDTO;
import com.avbinvest.company.module.Company;

import java.util.List;

/**
 * Utility class for converting between Company entities and DTOs.
 */
public class CompanyConverter {

    /**
     * Converts a {@link CompanyRequestDTO} to a {@link Company} entity.
     *
     * @param dto the company request DTO containing input data
     * @return a new Company entity built from the DTO data
     */
    public static Company convertDtoToEntity(CompanyRequestDTO dto) {
        return Company.builder()
                .name(dto.getName())
                .budget(dto.getBudget())
                .employeeIds(dto.getEmployeeIds())
                .build();
    }

    /**
     * Converts a {@link Company} entity and a list of user DTOs
     * into a {@link CompanyResponseDTO} for output.
     *
     * @param company the Company entity to convert
     * @param userDTO list of {@link UserDTO} representing employees of the company
     * @return a CompanyResponseDTO combining company data and employee information
     */
    public static CompanyResponseDTO convertEntityToDto(Company company, List<UserDTO> userDTO) {
        return CompanyResponseDTO.builder()
                .id(company.getId())
                .name(company.getName())
                .budget(company.getBudget())
                .employeeIds(userDTO)
                .build();
    }
}
