package com.avbinvest.company.dto;

import jakarta.persistence.ElementCollection;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class CompanyCreateDTO {

    @NotBlank(message = "Name must not be blank")
    @Size(min = 3, max = 100, message = "Name length must be between 3 and 100 characters")
    private String name;

    @NotNull(message = "Budget must not be null")
    @DecimalMin(value = "0.0", inclusive = false, message = "Budget must be greater than zero")
    private BigDecimal budget;

    @ElementCollection
    private List<Long> employeeIds;
}
