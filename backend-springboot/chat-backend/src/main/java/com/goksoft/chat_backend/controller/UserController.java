package com.goksoft.chat_backend.controller;

import com.goksoft.chat_backend.model.User;
import com.goksoft.chat_backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    // Search users by username (for "Add Friend" search)
    @PostMapping("/search")
    public ResponseEntity<List<String>> searchUsers(@RequestParam String username) {
        List<User> allUsers = userRepository.findAll();
        List<String> matchingUsers = new ArrayList<>();

        // Filter users whose username contains the search term
        for (User user : allUsers) {
            if (user.getUsername().contains(username)) {
                matchingUsers.add(user.getUsername());
            }
        }

        // Return max 20 results
        if (matchingUsers.size() > 20) {
            matchingUsers = matchingUsers.subList(0, 20);
        }

        return ResponseEntity.ok(matchingUsers);
    }

    // Get profile photo
    @GetMapping("/photo/{username}")
    public ResponseEntity<byte[]> getProfilePhoto(@PathVariable String username) {
        var user = userRepository.findById(username);

        if (user.isPresent() && user.get().getPhoto() != null) {
            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_PNG)
                    .body(user.get().getPhoto());
        } else {
            // Return empty if no photo (frontend will use default)
            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_PNG)
                    .body(new byte[0]);
        }
    }

    // Change profile photo
    @PostMapping("/photo")
    public ResponseEntity<String> changePhoto(@RequestParam String username,
                                              @RequestParam("photo") MultipartFile photo) {
        try {
            var user = userRepository.findById(username);

            if (user.isPresent()) {
                User existingUser = user.get();
                existingUser.setPhoto(photo.getBytes());
                userRepository.save(existingUser);

                return ResponseEntity.ok("File uploaded successfully.");
            } else {
                return ResponseEntity.ok("User not found.");
            }
        } catch (Exception e) {
            return ResponseEntity.ok("File upload failed, please try again.");
        }
    }
}