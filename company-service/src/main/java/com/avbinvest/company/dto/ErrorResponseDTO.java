package com.avbinvest.company.dto;

import lombok.*;

/**
 * Data Transfer Object representing error response details.
 *
 * Contains an error type and a descriptive message.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ErrorResponseDTO {

    /**
     * Short error code or type.
     */
    private String error;

    /**
     * Detailed error message describing the failure.
     */
    private String message;
}
