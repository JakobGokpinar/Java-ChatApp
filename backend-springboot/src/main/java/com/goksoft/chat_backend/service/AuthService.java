package com.goksoft.chat_backend.service;

import com.goksoft.chat_backend.dto.request.LoginRequest;
import com.goksoft.chat_backend.dto.request.RegisterRequest;
import com.goksoft.chat_backend.dto.response.ApiResponse;
import com.goksoft.chat_backend.dto.response.UserResponse;
import com.goksoft.chat_backend.model.User;
import com.goksoft.chat_backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    public ApiResponse<UserResponse> login(LoginRequest request) {

        var userOptional = userRepository.findByUsernameAndPassword(
                request.getUsername(),
                request.getPassword()
        );

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            UserResponse userResponse = new UserResponse(user.getUsername());
            return ApiResponse.success("Login successful", userResponse);
        } else {
            return ApiResponse.error("Invalid username or password");
        }
    }

    public ApiResponse<UserResponse> register(RegisterRequest request) {

        if (userRepository.existsByUsername(request.getUsername())) {
            return ApiResponse.error("Username already exists");
        }

        User newUser = new User(request.getUsername(), request.getPassword());
        userRepository.save(newUser);

        UserResponse userResponse = new UserResponse(newUser.getUsername());
        return ApiResponse.success("Registration successful", userResponse);
    }
}