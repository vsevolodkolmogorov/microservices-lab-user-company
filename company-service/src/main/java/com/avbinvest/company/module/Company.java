package com.avbinvest.company.module;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * Entity representing a Company.
 *
 * Contains basic company information including its name, budget,
 * and a list of employee IDs associated with the company.
 */
@Builder
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class Company {

    /**
     * Unique identifier of the company.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long Id;

    /**
     * Name of the company.
     */
    private String name;

    /**
     * Company's budget.
     */
    private BigDecimal budget;

    /**
     * List of employee IDs associated with this company.
     */
    @ElementCollection
    private List<Long> employeeIds;
}
