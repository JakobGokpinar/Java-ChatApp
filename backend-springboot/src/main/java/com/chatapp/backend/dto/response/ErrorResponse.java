package com.chatapp.backend.dto.response;

import java.time.LocalDateTime;

public class ErrorResponse {
    private boolean success;
    private String message;
    private String error;
    private int status;
    private LocalDateTime timestamp;

    public ErrorResponse(String message, String error, int status) {
        this.success = false;
        this.message = message;
        this.error = error;
        this.status = status;
        this.timestamp = LocalDateTime.now();
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public String getError() {
        return error;
    }

    public int getStatus() {
        return status;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}