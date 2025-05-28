package com.avbinvest.company.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompanyResponseDTO {
    private Long Id;
    private String name;
    private BigDecimal budget;
    private List<UserDTO> employeeIds;
}
