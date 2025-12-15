package com.goksoft.chat_backend.controller;

import com.goksoft.chat_backend.model.Friend;
import com.goksoft.chat_backend.model.FriendRequest;
import com.goksoft.chat_backend.repository.FriendRepository;
import com.goksoft.chat_backend.repository.FriendRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/*
    POST /api/friends/requests
    POST /api/friends/accept
    POST /api/friends/reject
    POST /api/friends/send-request
*/
@RestController
@RequestMapping("/api/friends")
@CrossOrigin(origins = "*")
public class FriendController {

    @Autowired
    private FriendRepository friendRepository;

    @Autowired
    private FriendRequestRepository friendRequestRepository;

    // Get friend requests for a user
    @PostMapping("/requests")
    public ResponseEntity<List<String>> getRequests(@RequestParam String username) {
        List<FriendRequest> requests = friendRequestRepository.findByReceiver(username);
        List<String> senderNames = new ArrayList<>();

        for (FriendRequest request : requests) {
            senderNames.add(request.getSender());
        }

        return ResponseEntity.ok(senderNames);
    }

    // Accept friend request (becomeFriend)
    @PostMapping("/accept")
    @Transactional
    public ResponseEntity<String> acceptFriend(@RequestParam String adder,
                                               @RequestParam String added) {
        try {
            // Delete the friend request
            friendRequestRepository.deleteBySenderAndReceiver(added, adder);

            // Create friendship
            Friend friendship = new Friend(adder, added);
            friendRepository.save(friendship);

            return ResponseEntity.ok("addfriend successful");
        } catch (Exception e) {
            return ResponseEntity.ok("addfriend unsuccessful");
        }
    }

    // Reject friend request
    @PostMapping("/reject")
    @Transactional
    public ResponseEntity<String> rejectFriend(@RequestParam String blocker,
                                               @RequestParam String blockedUser) {
        try {
            friendRequestRepository.deleteBySenderAndReceiver(blockedUser, blocker);
            return ResponseEntity.ok("rejection successful");
        } catch (Exception e) {
            return ResponseEntity.ok("rejection unsuccessful");
        }
    }

    // Send friend request
    @PostMapping("/send-request")
    public ResponseEntity<String> sendFriendRequest(@RequestParam String sender,
                                                    @RequestParam String receiver) {
        try {
            // Check if request already exists
            if (friendRequestRepository.existsBySenderAndReceiver(sender, receiver)) {
                return ResponseEntity.ok("request already exists");
            }

            // Check if already friends
            if (friendRepository.areFriends(sender, receiver)) {
                return ResponseEntity.ok("already friends");
            }

            FriendRequest request = new FriendRequest(sender, receiver);
            friendRequestRepository.save(request);

            return ResponseEntity.ok("request sent");
        } catch (Exception e) {
            return ResponseEntity.ok("request failed");
        }
    }

    // Get friends list (simplified version)
    @PostMapping("/get")
    public ResponseEntity<List<String>> getFriends(@RequestParam String username) {
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

        return ResponseEntity.ok(friendNames);
    }
}