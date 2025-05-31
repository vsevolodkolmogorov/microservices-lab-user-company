package com.avbinvest.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Data Transfer Object representing a Company.
 * Used for transferring company data between services or layers.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompanyDTO {

    /**
     * Unique identifier of the company.
     */
    private Long id;

    /**
     * Name of the company.
     */
    private String name;

    /**
     * Budget allocated for the company.
     */
    private BigDecimal budget;
}
