package com.chatapp.backend.service;

import com.chatapp.backend.config.JwtUtil;
import com.chatapp.backend.dto.request.LoginRequest;
import com.chatapp.backend.dto.request.RegisterRequest;
import com.chatapp.backend.dto.response.ApiResponse;
import com.chatapp.backend.dto.response.UserResponse;
import com.chatapp.backend.exception.AuthenticationException;
import com.chatapp.backend.exception.DuplicateResourceException;
import com.chatapp.backend.exception.ValidationException;
import com.chatapp.backend.model.User;
import com.chatapp.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthService authService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User("alice", "$2a$10$hashedPassword");
    }

    @Test
    void login_WithValidCredentials_ReturnsTokenAndUserResponse() {
        // Given
        LoginRequest request = new LoginRequest("alice", "password123");
        when(userRepository.findById("alice")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password123", testUser.getPassword())).thenReturn(true);
        when(jwtUtil.generateToken("alice")).thenReturn("mock-jwt-token");

        // When
        ApiResponse<UserResponse> response = authService.login(request);

        // Then
        assertThat(response.success()).isTrue();
        assertThat(response.data().username()).isEqualTo("alice");
        assertThat(response.data().token()).isEqualTo("mock-jwt-token");
        verify(userRepository).findById("alice");
        verify(passwordEncoder).matches("password123", testUser.getPassword());
        verify(jwtUtil).generateToken("alice");
    }

    @Test
    void login_WithInvalidUsername_ThrowsAuthenticationException() {
        // Given
        LoginRequest request = new LoginRequest("unknown", "password123");
        when(userRepository.findById("unknown")).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(AuthenticationException.class)
                .hasMessage("Invalid username or password");
    }

    @Test
    void login_WithInvalidPassword_ThrowsAuthenticationException() {
        // Given
        LoginRequest request = new LoginRequest("alice", "wrongPassword");
        when(userRepository.findById("alice")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("wrongPassword", testUser.getPassword())).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(AuthenticationException.class)
                .hasMessage("Invalid username or password");
    }

    @Test
    void login_WithBlankUsername_ThrowsValidationException() {
        // Given
        LoginRequest request = new LoginRequest("", "password123");

        // When & Then
        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(ValidationException.class)
                .hasMessage("Username is required");
    }

    @Test
    void register_WithValidData_CreatesUserSuccessfully() {
        // Given
        RegisterRequest request = new RegisterRequest("bob", "password123");
        when(userRepository.existsByUsername("bob")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("$2a$10$hashedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArguments()[0]);

        // When
        ApiResponse<UserResponse> response = authService.register(request);

        // Then
        assertThat(response.success()).isTrue();
        assertThat(response.data().username()).isEqualTo("bob");
        verify(userRepository).existsByUsername("bob");
        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_WithExistingUsername_ThrowsDuplicateResourceException() {
        // Given
        RegisterRequest request = new RegisterRequest("alice", "password123");
        when(userRepository.existsByUsername("alice")).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessage("Username already exists");
    }

    @Test
    void register_WithShortUsername_ThrowsValidationException() {
        // Given
        RegisterRequest request = new RegisterRequest("ab", "password123");

        // When & Then
        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(ValidationException.class)
                .hasMessage("Username must be at least 3 characters");
    }

    @Test
    void register_WithShortPassword_ThrowsValidationException() {
        // Given
        RegisterRequest request = new RegisterRequest("bob", "abc");

        // When & Then
        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(ValidationException.class)
                .hasMessage("Password must be at least 4 characters");
    }
}