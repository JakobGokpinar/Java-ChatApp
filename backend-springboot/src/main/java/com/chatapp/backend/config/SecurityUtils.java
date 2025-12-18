package com.chatapp.backend.config;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Utility for extracting authenticated user information from JWT token.
 * Used by controllers to get current username without accepting it as a parameter.
 */
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