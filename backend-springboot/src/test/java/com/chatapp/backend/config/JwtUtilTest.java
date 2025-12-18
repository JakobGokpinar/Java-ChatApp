package com.chatapp.backend.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class JwtUtilTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
    }

    @Test
    void generateToken_CreatesValidToken() {
        // When
        String token = jwtUtil.generateToken("alice");

        // Then
        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
        assertThat(token.split("\\.")).hasSize(3); // JWT has 3 parts
    }

    @Test
    void extractUsername_FromValidToken_ReturnsCorrectUsername() {
        // Given
        String token = jwtUtil.generateToken("alice");

        // When
        String username = jwtUtil.extractUsername(token);

        // Then
        assertThat(username).isEqualTo("alice");
    }

    @Test
    void validateToken_WithCorrectUsername_ReturnsTrue() {
        // Given
        String token = jwtUtil.generateToken("alice");

        // When
        Boolean isValid = jwtUtil.validateToken(token, "alice");

        // Then
        assertThat(isValid).isTrue();
    }

    @Test
    void validateToken_WithWrongUsername_ReturnsFalse() {
        // Given
        String token = jwtUtil.generateToken("alice");

        // When
        Boolean isValid = jwtUtil.validateToken(token, "bob");

        // Then
        assertThat(isValid).isFalse();
    }
}