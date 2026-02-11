package com.chatapp.backend.service;

import com.chatapp.backend.dto.response.ApiResponse;
import com.chatapp.backend.exception.ResourceNotFoundException;
import com.chatapp.backend.exception.ValidationException;
import com.chatapp.backend.model.User;
import com.chatapp.backend.repository.FriendshipRepository;
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

    @Autowired
    private FriendshipRepository friendshipRepository;

    public ApiResponse<List<String>> searchUsers(String username, String currentUser) {
        if (username == null || username.isBlank()) {
            throw new ValidationException("Search term is required");
        }

        logger.info("Searching users with term: {}", username);

        List<User> allUsers = userRepository.findAll();
        List<String> matchingUsers = new ArrayList<>();

        for (User user : allUsers) {
            String name = user.getUsername();

            // Skip self
            if (name.equals(currentUser)) continue;

            // Skip existing friends and pending requests
            if (friendshipRepository.findByUsers(currentUser, name).isPresent()) continue;

            if (name.contains(username)) {
                matchingUsers.add(name);
            }
        }

        if (matchingUsers.size() > 20) {
            matchingUsers = matchingUsers.subList(0, 20);
        }

        logger.info("Found {} users matching '{}'", matchingUsers.size(), username);
        return ApiResponse.success("Users found", matchingUsers);
    }
}