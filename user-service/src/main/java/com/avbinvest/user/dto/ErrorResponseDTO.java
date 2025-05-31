package com.avbinvest.user.dto;

import lombok.*;

/**
 * DTO representing an error response.
 * Contains a brief error code or type and a detailed message.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ErrorResponseDTO {

    /**
     * Short error code or error type identifier.
     */
    private String error;

    /**
     * Detailed error message describing the cause.
     */
    private String message;
}
