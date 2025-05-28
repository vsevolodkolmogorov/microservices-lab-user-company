package com.avbinvest.company.dto;

import com.avbinvest.company.validation.OnCreate;
import jakarta.persistence.ElementCollection;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class CompanyRequestDTO {

    @NotBlank(message = "Name must not be blank", groups = OnCreate.class)
    private String name;

    @NotNull(message = "Budget must not be null", groups = OnCreate.class)
    private BigDecimal budget;

    @ElementCollection
    private List<Long> employeeIds;
}
