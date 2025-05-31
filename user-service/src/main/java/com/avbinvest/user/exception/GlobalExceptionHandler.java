package com.avbinvest.user.exception;

import com.avbinvest.user.dto.ErrorResponseDTO;
import jakarta.validation.ValidationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.stream.Collectors;

/**
 * Global exception handler for REST controllers.
 * Handles common exceptions and returns meaningful HTTP responses
 * with error details in a consistent format.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles UserNotFoundException and returns 404 Not Found.
     */
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleUserNotFound(UserNotFoundException ex) {
        ErrorResponseDTO errorResponse = new ErrorResponseDTO("UserNotFound", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    /**
     * Handles ConflictException and returns 409 Conflict.
     */
    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ErrorResponseDTO> handleConflictException(ConflictException ex) {
        ErrorResponseDTO errorResponse = new ErrorResponseDTO("ConflictException", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    /**
     * Handles RestRequestFailedException and returns 400 Bad Request.
     */
    @ExceptionHandler(RestRequestFailedException.class)
    public ResponseEntity<ErrorResponseDTO> handleRestRequestFailedException(RestRequestFailedException ex) {
        ErrorResponseDTO errorResponse = new ErrorResponseDTO("RestRequestFailedException", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * Handles CompanyNotFoundException and returns 404 Not Found.
     */
    @ExceptionHandler(CompanyNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleCompanyNotFound(CompanyNotFoundException ex) {
        ErrorResponseDTO errorResponse = new ErrorResponseDTO("CompanyNotFound", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    /**
     * Handles validation errors from method argument validation
     * and returns 400 Bad Request with concatenated field error messages.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<String> handleValidationExceptions(MethodArgumentNotValidException ex) {
        String errorMessage = ex.getBindingResult().getFieldErrors().stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .collect(Collectors.joining(", "));
        return ResponseEntity.badRequest().body("Validation failed: " + errorMessage);
    }

    /**
     * Handles generic ValidationException and returns 400 Bad Request.
     */
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponseDTO> handleValidation(ValidationException ex) {
        ErrorResponseDTO errorResponse = new ErrorResponseDTO("Validation failed", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }
}
