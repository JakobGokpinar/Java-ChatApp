package com.chatapp.backend.service;

import com.chatapp.backend.dto.response.ApiResponse;
import com.chatapp.backend.exception.DuplicateResourceException;
import com.chatapp.backend.exception.ResourceNotFoundException;
import com.chatapp.backend.exception.ValidationException;
import com.chatapp.backend.model.Friendship;
import com.chatapp.backend.model.Friendship.FriendshipStatus;
import com.chatapp.backend.repository.FriendshipRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages friendship relationships and requests.
 * Uses unified Friendship model with status: PENDING → ACCEPTED/REJECTED
 */
@Service
public class FriendService {

    private static final Logger logger = LoggerFactory.getLogger(FriendService.class);

    @Autowired
    private FriendshipRepository friendshipRepository;

    public ApiResponse<List<String>> getFriends(String username) {
        logger.info("Fetching friends for user: {}", username);

        List<Friendship> friendships = friendshipRepository.findAcceptedFriendships(username);
        List<String> friendNames = new ArrayList<>();

        for (Friendship friendship : friendships) {
            if (friendship.getUser1().equals(username)) {
                friendNames.add(friendship.getUser2());
            } else {
                friendNames.add(friendship.getUser1());
            }
        }

        logger.info("Found {} friends for user: {}", friendNames.size(), username);
        return ApiResponse.success("Friends retrieved", friendNames);
    }

    public ApiResponse<List<String>> getFriendRequests(String username) {
        logger.info("Fetching friend requests for user: {}", username);

        List<Friendship> requests = friendshipRepository.findPendingRequests(username);
        List<String> senderNames = new ArrayList<>();

        for (Friendship request : requests) {
            senderNames.add(request.getInitiatedBy());
        }

        logger.info("Found {} friend requests for user: {}", senderNames.size(), username);
        return ApiResponse.success("Friend requests retrieved", senderNames);
    }

    @Transactional
    public ApiResponse<String> sendFriendRequest(String sender, String receiver) {
        // Validation
        if (sender == null || sender.isBlank()) {
            throw new ValidationException("Sender username is required");
        }
        if (receiver == null || receiver.isBlank()) {
            throw new ValidationException("Receiver username is required");
        }
        if (sender.equals(receiver)) {
            throw new ValidationException("Cannot send friend request to yourself");
        }

        logger.info("Friend request: {} → {}", sender, receiver);

        // Check if already friends
        if (friendshipRepository.areFriends(sender, receiver)) {
            logger.warn("Friend request failed - already friends: {} and {}", sender, receiver);
            throw new DuplicateResourceException("You are already friends");
        }

        // Check if pending request already exists
        if (friendshipRepository.existsByUsersAndStatus(sender, receiver, FriendshipStatus.PENDING)) {
            logger.warn("Friend request failed - request already exists: {} → {}", sender, receiver);
            throw new DuplicateResourceException("Friend request already sent");
        }

        // Create new friendship request
        Friendship friendship = new Friendship(sender, receiver, sender);
        friendshipRepository.save(friendship);

        logger.info("Friend request sent successfully: {} → {}", sender, receiver);
        return ApiResponse.success("Friend request sent", null);
    }

    @Transactional
    public ApiResponse<String> acceptFriendRequest(String accepter, String requester) {
        logger.info("Accepting friend request: {} accepting {}", accepter, requester);

        // Find the friendship
        var friendshipOpt = friendshipRepository.findByUsers(accepter, requester);

        if (friendshipOpt.isEmpty()) {
            logger.warn("Accept failed - friend request not found: {} ← {}", accepter, requester);
            throw new ResourceNotFoundException("Friend request not found");
        }

        Friendship friendship = friendshipOpt.get();

        // Verify it's pending
        if (friendship.getStatus() != FriendshipStatus.PENDING) {
            logger.warn("Accept failed - request already processed: {} ← {}", accepter, requester);
            throw new ValidationException("Friend request already processed");
        }

        // Verify the accepter is not the one who initiated it
        if (friendship.getInitiatedBy().equals(accepter)) {
            logger.warn("Accept failed - cannot accept own request: {}", accepter);
            throw new ValidationException("Cannot accept your own friend request");
        }

        // Update status to accepted
        friendship.setStatus(FriendshipStatus.ACCEPTED);
        friendshipRepository.save(friendship);

        logger.info("Friend request accepted: {} and {} are now friends", accepter, requester);
        return ApiResponse.success("Friend request accepted", null);
    }

    @Transactional
    public ApiResponse<String> rejectFriendRequest(String rejecter, String requester) {
        logger.info("Rejecting friend request: {} rejecting {}", rejecter, requester);

        // Find the friendship
        var friendshipOpt = friendshipRepository.findByUsers(rejecter, requester);

        if (friendshipOpt.isEmpty()) {
            logger.warn("Reject failed - friend request not found: {} ← {}", rejecter, requester);
            throw new ResourceNotFoundException("Friend request not found");
        }

        Friendship friendship = friendshipOpt.get();

        // Update status to rejected
        friendship.setStatus(FriendshipStatus.REJECTED);
        friendshipRepository.save(friendship);

        logger.info("Friend request rejected: {} rejected {}", rejecter, requester);
        return ApiResponse.success("Friend request rejected", null);
    }

    // Helper method using Java 21 switch expression
    private String getFriendshipStatusMessage(FriendshipStatus status) {
        return switch (status) {
            case PENDING -> "Friend request pending";
            case ACCEPTED -> "You are friends";
            case REJECTED -> "Friend request was rejected";
        };
    }
}