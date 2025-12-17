package com.chatapp.backend.dto.response;

import java.time.LocalDateTime;

public record ErrorResponse(
        boolean success,
        String message,
        String error,
        int status,
        LocalDateTime timestamp
) {
    // Custom constructor for creating errors
    public ErrorResponse(String message, String error, int status) {
        this(false, message, error, status, LocalDateTime.now());
    }
}