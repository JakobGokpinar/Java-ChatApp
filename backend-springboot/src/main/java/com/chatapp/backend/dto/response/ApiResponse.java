package com.chatapp.backend.dto.response;

import java.time.LocalDateTime;

/**
 * Standard API response wrapper for all endpoints.
 * Contains success flag, message, data payload, and timestamp.
 *
 * @param <T> Type of data being returned
 */
public record ApiResponse<T>(
        boolean success,
        String message,
        T data,
        LocalDateTime timestamp
) {
    // Factory method for success
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, message, data, LocalDateTime.now());
    }

    // Factory method for success with just data
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, "Success", data, LocalDateTime.now());
    }

    // Factory method for errors
    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, message, null, LocalDateTime.now());
    }
}