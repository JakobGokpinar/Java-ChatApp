package com.chatapp.backend.controller;

import com.chatapp.backend.dto.request.LoginRequest;
import com.chatapp.backend.dto.request.RegisterRequest;
import com.chatapp.backend.dto.response.ApiResponse;
import com.chatapp.backend.dto.response.UserResponse;
import com.chatapp.backend.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<UserResponse>> login(@RequestParam String username,
                                                           @RequestParam String password) {
        LoginRequest request = new LoginRequest(username, password);
        ApiResponse<UserResponse> response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponse>> register(@RequestParam String username,
                                                              @RequestParam String password) {
        RegisterRequest request = new RegisterRequest(username, password);
        ApiResponse<UserResponse> response = authService.register(request);
        return ResponseEntity.ok(response);
    }
}