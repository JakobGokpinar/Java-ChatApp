package com.chatapp.backend.service;

import com.chatapp.backend.dto.response.ApiResponse;
import com.chatapp.backend.model.User;
import com.chatapp.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    // Search users by username
    public ApiResponse<List<String>> searchUsers(String username) {
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

        return ApiResponse.success("Users found", matchingUsers);
    }

    // Get profile photo
    public ApiResponse<byte[]> getProfilePhoto(String username) {
        var user = userRepository.findById(username);

        if (user.isPresent() && user.get().getPhoto() != null) {
            return ApiResponse.success("Photo retrieved", user.get().getPhoto());
        } else {
            // Return empty array if no photo
            return ApiResponse.success("No photo", new byte[0]);
        }
    }

    // Update profile photo
    public ApiResponse<String> updateProfilePhoto(String username, byte[] photoBytes) {
        try {
            var user = userRepository.findById(username);

            if (user.isPresent()) {
                User existingUser = user.get();
                existingUser.setPhoto(photoBytes);
                userRepository.save(existingUser);

                return ApiResponse.success("Photo updated successfully", null);
            } else {
                return ApiResponse.error("User not found");
            }
        } catch (Exception e) {
            return ApiResponse.error("Photo update failed");
        }
    }
}