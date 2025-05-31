package com.avbinvest.company.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * Data Transfer Object representing company details in responses.
 *
 * Contains company ID, name, budget, and a list of employees.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompanyResponseDTO {

    /**
     * Unique identifier of the company.
     */
    private Long id;

    /**
     * Company name.
     */
    private String name;

    /**
     * Company's budget.
     */
    private BigDecimal budget;

    /**
     * List of employees associated with the company.
     */
    private List<UserDTO> employeeIds;
}
