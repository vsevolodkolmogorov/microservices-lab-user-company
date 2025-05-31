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

/**
 * Data Transfer Object for company creation and update requests.
 *
 * Contains basic company information such as name, budget, and associated employee IDs.
 * Validation groups differentiate required fields for creation.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompanyRequestDTO {

    /**
     * Company name. Required for creation.
     */
    @NotBlank(message = "Name must not be blank", groups = OnCreate.class)
    private String name;

    /**
     * Company budget. Required for creation.
     */
    @NotNull(message = "Budget must not be null", groups = OnCreate.class)
    private BigDecimal budget;

    /**
     * List of employee IDs associated with the company.
     */
    @ElementCollection
    private List<Long> employeeIds;
}
