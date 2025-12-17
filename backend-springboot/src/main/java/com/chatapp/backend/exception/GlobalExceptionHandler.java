package com.chatapp.backend.exception;

import com.chatapp.backend.dto.response.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // Handle authentication exceptions (401)
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationException(
            AuthenticationException ex, WebRequest request) {

        logger.warn("Authentication failed: {} - Path: {}",
                ex.getMessage(), request.getDescription(false));

        ErrorResponse error = new ErrorResponse(
                ex.getMessage(),
                "AUTHENTICATION_ERROR",
                HttpStatus.UNAUTHORIZED.value()
        );

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    // Handle resource not found (404)
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(
            ResourceNotFoundException ex, WebRequest request) {

        logger.warn("Resource not found: {} - Path: {}",
                ex.getMessage(), request.getDescription(false));

        ErrorResponse error = new ErrorResponse(
                ex.getMessage(),
                "RESOURCE_NOT_FOUND",
                HttpStatus.NOT_FOUND.value()
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    // Handle validation errors (400)
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            ValidationException ex, WebRequest request) {

        logger.warn("Validation error: {} - Path: {}",
                ex.getMessage(), request.getDescription(false));

        ErrorResponse error = new ErrorResponse(
                ex.getMessage(),
                "VALIDATION_ERROR",
                HttpStatus.BAD_REQUEST.value()
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    // Handle duplicate resource (409)
    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateResourceException(
            DuplicateResourceException ex, WebRequest request) {

        logger.warn("Duplicate resource: {} - Path: {}",
                ex.getMessage(), request.getDescription(false));

        ErrorResponse error = new ErrorResponse(
                ex.getMessage(),
                "DUPLICATE_RESOURCE",
                HttpStatus.CONFLICT.value()
        );

        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    // Handle generic exceptions (500)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(
            Exception ex, WebRequest request) {

        logger.error("Unexpected error: {} - Path: {}",
                ex.getMessage(), request.getDescription(false), ex);

        ErrorResponse error = new ErrorResponse(
                "An unexpected error occurred",
                "INTERNAL_SERVER_ERROR",
                HttpStatus.INTERNAL_SERVER_ERROR.value()
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    // Helper method using Java 21 pattern matching
    private String getErrorCode(Exception ex) {
        return switch (ex) {
            case AuthenticationException e -> "AUTHENTICATION_ERROR";
            case ResourceNotFoundException e -> "RESOURCE_NOT_FOUND";
            case ValidationException e -> "VALIDATION_ERROR";
            case DuplicateResourceException e -> "DUPLICATE_RESOURCE";
            default -> "INTERNAL_SERVER_ERROR";
        };
    }
}