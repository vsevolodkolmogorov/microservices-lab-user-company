package com.avbinvest.company.exceptions;

import com.avbinvest.company.dto.ErrorResponseDTO;
import jakarta.validation.ValidationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.stream.Collectors;

/**
 * Global exception handler to centralize handling of application exceptions
 * and provide consistent error responses.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles {@link CompanyNotFoundException} exceptions.
     *
     * @param ex the thrown CompanyNotFoundException
     * @return ResponseEntity with NOT_FOUND status and error details
     */
    @ExceptionHandler(CompanyNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleCompanyNotFound(CompanyNotFoundException ex) {
        ErrorResponseDTO errorResponse = new ErrorResponseDTO("CompanyNotFound", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    /**
     * Handles {@link ConflictException} exceptions.
     *
     * @param ex the thrown ConflictException
     * @return ResponseEntity with CONFLICT status and error details
     */
    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ErrorResponseDTO> handleConflictException(ConflictException ex) {
        ErrorResponseDTO errorResponse = new ErrorResponseDTO("ConflictException", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    /**
     * Handles {@link RestRequestFailedException} exceptions.
     *
     * @param ex the thrown RestRequestFailedException
     * @return ResponseEntity with BAD_REQUEST status and error details
     */
    @ExceptionHandler(RestRequestFailedException.class)
    public ResponseEntity<ErrorResponseDTO> handleRestRequestFailedException(RestRequestFailedException ex) {
        ErrorResponseDTO errorResponse = new ErrorResponseDTO("RestRequestFailedException", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * Handles validation exceptions triggered by invalid method arguments.
     *
     * @param ex the thrown MethodArgumentNotValidException
     * @return ResponseEntity with BAD_REQUEST status and concatenated validation messages
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<String> handleValidationExceptions(MethodArgumentNotValidException ex) {
        String errorMessage = ex.getBindingResult().getFieldErrors().stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .collect(Collectors.joining(", "));
        return ResponseEntity.badRequest().body("Validation failed: " + errorMessage);
    }

    /**
     * Handles generic {@link ValidationException} exceptions.
     *
     * @param ex the thrown ValidationException
     * @return ResponseEntity with BAD_REQUEST status and error details
     */
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponseDTO> handleValidation(ValidationException ex) {
        ErrorResponseDTO errorResponse = new ErrorResponseDTO("Validation failed", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }
}
