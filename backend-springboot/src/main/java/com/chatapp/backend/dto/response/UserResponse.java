package com.chatapp.backend.dto.response;

public record UserResponse(String username, String token) {

    // Custom constructor for cases without token
    public UserResponse(String username) {
        this(username, null);
    }
}