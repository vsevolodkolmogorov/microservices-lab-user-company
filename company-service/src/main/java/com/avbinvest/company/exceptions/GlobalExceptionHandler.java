package com.avbinvest.company.exceptions;

import com.avbinvest.company.dto.ErrorResponseDTO;
import jakarta.validation.ValidationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CompanyNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleUserNotFound(CompanyNotFoundException ex) {
        ErrorResponseDTO errorResponse = new ErrorResponseDTO("CompanyNotFound", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponseDTO> handleValidation(ValidationException ex) {
        ErrorResponseDTO errorResponse = new ErrorResponseDTO("Validation failed", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }
}
