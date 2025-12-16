package com.chatapp.backend.controller;

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

    // Search users by username
    @PostMapping("/search")
    public ResponseEntity<ApiResponse<List<String>>> searchUsers(@RequestParam String username) {
        ApiResponse<List<String>> response = userService.searchUsers(username);
        return ResponseEntity.ok(response);
    }

    // Get profile photo (returns raw bytes for compatibility)
    @GetMapping("/photo/{username}")
    public ResponseEntity<byte[]> getProfilePhoto(@PathVariable String username) {
        ApiResponse<byte[]> response = userService.getProfilePhoto(username);

        // Return raw bytes for frontend compatibility
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .body(response.getData());
    }

    // Update profile photo
    @PostMapping("/photo")
    public ResponseEntity<ApiResponse<String>> updateProfilePhoto(@RequestParam String username,
                                                                  @RequestParam("photo") MultipartFile photo) {
        try {
            byte[] photoBytes = photo.getBytes();
            ApiResponse<String> response = userService.updateProfilePhoto(username, photoBytes);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error("Photo upload failed"));
        }
    }
}