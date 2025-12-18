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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Handles user authentication and registration.
 * - Passwords are hashed with BCrypt
 * - Login returns JWT token (24h validity)
 * - Validates credentials and enforces business rules
 */
@Service
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    public ApiResponse<UserResponse> login(LoginRequest request) {
        // Validate input
        if (request.username() == null || request.username().isBlank()) {
            logger.warn("Login attempt with blank username");
            throw new ValidationException("Username is required");
        }

        if (request.password() == null || request.password().isBlank()) {
            logger.warn("Login attempt with blank password for user: {}", request.username());
            throw new ValidationException("Password is required");
        }

        logger.info("Login attempt for user: {}", request.username());

        // Find user by username
        var userOptional = userRepository.findById(request.username());

        if (userOptional.isEmpty()) {
            logger.warn("Login failed - user not found: {}", request.username());
            throw new AuthenticationException("Invalid username or password");
        }

        User user = userOptional.get();

        // Verify password using BCrypt
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            logger.warn("Login failed - invalid password for user: {}", request.username());
            throw new AuthenticationException("Invalid username or password");
        }

        // Generate JWT token
        String token = jwtUtil.generateToken(user.getUsername());

        logger.info("Login successful for user: {}", request.username());

        UserResponse userResponse = new UserResponse(user.getUsername(), token);
        return ApiResponse.success("Login successful", userResponse);
    }

    public ApiResponse<UserResponse> register(RegisterRequest request) {
        // Validate input
        if (request.username() == null || request.username().isBlank()) {
            logger.warn("Registration attempt with blank username");
            throw new ValidationException("Username is required");
        }

        if (request.password() == null || request.password().isBlank()) {
            logger.warn("Registration attempt with blank password");
            throw new ValidationException("Password is required");
        }

        if (request.username().length() < 3) {
            logger.warn("Registration attempt with username too short: {}", request.username());
            throw new ValidationException("Username must be at least 3 characters");
        }

        if (request.password().length() < 4) {
            logger.warn("Registration attempt with password too short for user: {}", request.username());
            throw new ValidationException("Password must be at least 4 characters");
        }

        logger.info("Registration attempt for username: {}", request.username());

        // Check if username already exists
        if (userRepository.existsByUsername(request.username())) {
            logger.warn("Registration failed - username already exists: {}", request.username());
            throw new DuplicateResourceException("Username already exists");
        }

        // Hash the password using BCrypt
        String hashedPassword = passwordEncoder.encode(request.password());

        // Create new user with hashed password
        User newUser = new User(request.username(), hashedPassword);
        userRepository.save(newUser);

        logger.info("User registered successfully: {}", request.username());

        UserResponse userResponse = new UserResponse(newUser.getUsername());
        return ApiResponse.success("Registration successful", userResponse);
    }
}