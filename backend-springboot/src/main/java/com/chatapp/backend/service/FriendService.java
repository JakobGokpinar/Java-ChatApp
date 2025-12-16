package com.chatapp.backend.service;

import com.chatapp.backend.dto.response.ApiResponse;
import com.chatapp.backend.model.Friend;
import com.chatapp.backend.model.FriendRequest;
import com.chatapp.backend.repository.FriendRepository;
import com.chatapp.backend.repository.FriendRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class FriendService {

    @Autowired
    private FriendRepository friendRepository;

    @Autowired
    private FriendRequestRepository friendRequestRepository;

    // Get list of friends
    public ApiResponse<List<String>> getFriends(String username) {
        List<Friend> friendships = friendRepository.findFriendsByUsername(username);
        List<String> friendNames = new ArrayList<>();

        for (Friend friendship : friendships) {
            // Add the friend's name (the one that's NOT the current user)
            if (friendship.getPerson1().equals(username)) {
                friendNames.add(friendship.getPerson2());
            } else {
                friendNames.add(friendship.getPerson1());
            }
        }

        return ApiResponse.success("Friends retrieved", friendNames);
    }

    // Get friend requests
    public ApiResponse<List<String>> getFriendRequests(String username) {
        List<FriendRequest> requests = friendRequestRepository.findByReceiver(username);
        List<String> senderNames = new ArrayList<>();

        for (FriendRequest request : requests) {
            senderNames.add(request.getSender());
        }

        return ApiResponse.success("Friend requests retrieved", senderNames);
    }

    // Send friend request
    @Transactional
    public ApiResponse<String> sendFriendRequest(String sender, String receiver) {
        // Check if already friends
        if (friendRepository.areFriends(sender, receiver)) {
            return ApiResponse.error("already friends");
        }

        // Check if request already exists
        if (friendRequestRepository.existsBySenderAndReceiver(sender, receiver)) {
            return ApiResponse.error("request already exists");
        }

        // Create and save request
        FriendRequest request = new FriendRequest(sender, receiver);
        friendRequestRepository.save(request);

        return ApiResponse.success("request sent", null);
    }

    // Accept friend request
    @Transactional
    public ApiResponse<String> acceptFriendRequest(String adder, String added) {
        // Delete the friend request
        friendRequestRepository.deleteBySenderAndReceiver(added, adder);

        // Create friendship
        Friend friendship = new Friend(adder, added);
        friendRepository.save(friendship);

        return ApiResponse.success("addfriend successful", null);
    }

    // Reject friend request
    @Transactional
    public ApiResponse<String> rejectFriendRequest(String blocker, String blockedUser) {
        // Delete the friend request
        friendRequestRepository.deleteBySenderAndReceiver(blockedUser, blocker);

        return ApiResponse.success("rejection successful", null);
    }
}