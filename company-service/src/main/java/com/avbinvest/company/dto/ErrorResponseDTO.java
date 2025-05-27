package com.avbinvest.company.dto;

import lombok.*;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ErrorResponseDTO {
    private String error;
    private String message;
}
