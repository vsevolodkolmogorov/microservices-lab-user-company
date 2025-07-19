package com.avbinvest.user.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ErrorResponseDTO {

    private String error;
    private String message;
}
