package com.chatapp.backend.config;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtils {

    // Get the username of the currently authenticated user from JWT
    public static String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated()) {
            return authentication.getName();
        }

        return null;
    }
}