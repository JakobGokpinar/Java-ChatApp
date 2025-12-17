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

    // Search users - no changes needed, anyone can search
    @PostMapping("/search")
    public ResponseEntity<ApiResponse<List<String>>> searchUsers(@RequestParam String username) {
        ApiResponse<List<String>> response = userService.searchUsers(username);
        return ResponseEntity.ok(response);
    }

    // Get profile photo - can get anyone's photo (public)
    @GetMapping("/photo/{username}")
    public ResponseEntity<byte[]> getProfilePhoto(@PathVariable String username) {
        ApiResponse<byte[]> response = userService.getProfilePhoto(username);
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .body(response.data());
    }

    // Update profile photo - username from JWT (can only update YOUR OWN photo)
    @PostMapping("/photo")
    public ResponseEntity<ApiResponse<String>> updateProfilePhoto(@RequestParam("photo") MultipartFile photo) {
        String username = SecurityUtils.getCurrentUsername();
        try {
            byte[] photoBytes = photo.getBytes();
            ApiResponse<String> response = userService.updateProfilePhoto(username, photoBytes);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error("Photo upload failed"));
        }
    }
}