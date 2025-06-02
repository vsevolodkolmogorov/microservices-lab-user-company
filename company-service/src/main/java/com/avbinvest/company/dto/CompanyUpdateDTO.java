package com.avbinvest.company.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
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
public class CompanyUpdateDTO {

    @Size(min = 3, max = 100)
    private String name;

    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal budget;

    private List<Long> employeeIds;
}

