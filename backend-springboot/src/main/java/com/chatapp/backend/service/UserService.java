package com.chatapp.backend.service;

import com.chatapp.backend.dto.response.ApiResponse;
import com.chatapp.backend.exception.ResourceNotFoundException;
import com.chatapp.backend.exception.ValidationException;
import com.chatapp.backend.model.User;
import com.chatapp.backend.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages user-related operations.
 * Handles user search and profile photo management.
 */
@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private UserRepository userRepository;

    public ApiResponse<List<String>> searchUsers(String username) {
        if (username == null || username.isBlank()) {
            throw new ValidationException("Search term is required");
        }

        logger.info("Searching users with term: {}", username);

        List<User> allUsers = userRepository.findAll();
        List<String> matchingUsers = new ArrayList<>();

        for (User user : allUsers) {
            if (user.getUsername().contains(username)) {
                matchingUsers.add(user.getUsername());
            }
        }

        if (matchingUsers.size() > 20) {
            matchingUsers = matchingUsers.subList(0, 20);
        }

        logger.info("Found {} users matching '{}'", matchingUsers.size(), username);
        return ApiResponse.success("Users found", matchingUsers);
    }

    public ApiResponse<byte[]> getProfilePhoto(String username) {
        logger.debug("Fetching photo for user: {}", username);

        var user = userRepository.findById(username);

        if (user.isEmpty()) {
            throw new ResourceNotFoundException("User not found");
        }

        if (user.get().getPhoto() != null) {
            return ApiResponse.success("Photo retrieved", user.get().getPhoto());
        } else {
            return ApiResponse.success("No photo", new byte[0]);
        }
    }

    public ApiResponse<String> updateProfilePhoto(String username, byte[] photoBytes) {
        logger.info("Updating photo for user: {}", username);

        if (photoBytes == null || photoBytes.length == 0) {
            throw new ValidationException("Photo data is required");
        }

        try {
            var user = userRepository.findById(username);

            if (user.isEmpty()) {
                throw new ResourceNotFoundException("User not found");
            }

            User existingUser = user.get();
            existingUser.setPhoto(photoBytes);
            userRepository.save(existingUser);

            logger.info("Photo updated successfully for user: {}", username);
            return ApiResponse.success("Photo updated successfully", null);
        } catch (Exception e) {
            logger.error("Failed to update photo for user: {}", username, e);
            throw new RuntimeException("Failed to update photo");
        }
    }
}