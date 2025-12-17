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

@Service
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    public ApiResponse<UserResponse> login(LoginRequest request) {
        // Validate input
        if (request.getUsername() == null || request.getUsername().isBlank()) {
            logger.warn("Login attempt with blank username");
            throw new ValidationException("Username is required");
        }

        if (request.getPassword() == null || request.getPassword().isBlank()) {
            logger.warn("Login attempt with blank password for user: {}", request.getUsername());
            throw new ValidationException("Password is required");
        }

        logger.info("Login attempt for user: {}", request.getUsername());

        // Find user by username
        var userOptional = userRepository.findById(request.getUsername());

        if (userOptional.isEmpty()) {
            logger.warn("Login failed - user not found: {}", request.getUsername());
            throw new AuthenticationException("Invalid username or password");
        }

        User user = userOptional.get();

        // Verify password using BCrypt
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            logger.warn("Login failed - invalid password for user: {}", request.getUsername());
            throw new AuthenticationException("Invalid username or password");
        }

        String token = jwtUtil.generateToken(user.getUsername());

        logger.info("Login successful for user: {}", request.getUsername());

        UserResponse userResponse = new UserResponse(user.getUsername(), token);
        return ApiResponse.success("Login successful", userResponse);
    }

    public ApiResponse<UserResponse> register(RegisterRequest request) {
        // Validate input
        if (request.getUsername() == null || request.getUsername().isBlank()) {
            logger.warn("Registration attempt with blank username");
            throw new ValidationException("Username is required");
        }

        if (request.getPassword() == null || request.getPassword().isBlank()) {
            logger.warn("Registration attempt with blank password");
            throw new ValidationException("Password is required");
        }

        if (request.getUsername().length() < 3) {
            logger.warn("Registration attempt with username too short: {}", request.getUsername());
            throw new ValidationException("Username must be at least 3 characters");
        }

        if (request.getPassword().length() < 4) {
            logger.warn("Registration attempt with password too short for user: {}", request.getUsername());
            throw new ValidationException("Password must be at least 4 characters");
        }

        logger.info("Registration attempt for username: {}", request.getUsername());

        // Check if username already exists
        if (userRepository.existsByUsername(request.getUsername())) {
            logger.warn("Registration failed - username already exists: {}", request.getUsername());
            throw new DuplicateResourceException("Username already exists");
        }

        // Hash the password using BCrypt
        String hashedPassword = passwordEncoder.encode(request.getPassword());

        // Create new user with hashed password
        User newUser = new User(request.getUsername(), hashedPassword);
        userRepository.save(newUser);

        logger.info("User registered successfully: {}", request.getUsername());

        UserResponse userResponse = new UserResponse(newUser.getUsername());
        return ApiResponse.success("Registration successful", userResponse);
    }
}