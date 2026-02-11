package com.chatapp.backend.controller;

import com.chatapp.backend.config.SecurityUtils;
import com.chatapp.backend.dto.response.ApiResponse;
import com.chatapp.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/search")
    public ResponseEntity<ApiResponse<List<String>>> searchUsers(@RequestParam String username) {
        String currentUser = SecurityUtils.getCurrentUsername();
        ApiResponse<List<String>> response = userService.searchUsers(username, currentUser);
        return ResponseEntity.ok(response);
    }

}