package com.chatapp.backend.controller;

import com.chatapp.backend.dto.response.ApiResponse;
import com.chatapp.backend.service.FriendService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/friends")
@CrossOrigin(origins = "*")
public class FriendController {

    @Autowired
    private FriendService friendService;

    // Get friends list
    @PostMapping("/get")
    public ResponseEntity<ApiResponse<List<String>>> getFriends(@RequestParam String username) {
        ApiResponse<List<String>> response = friendService.getFriends(username);
        return ResponseEntity.ok(response);
    }

    // Get friend requests
    @PostMapping("/requests")
    public ResponseEntity<ApiResponse<List<String>>> getFriendRequests(@RequestParam String username) {
        ApiResponse<List<String>> response = friendService.getFriendRequests(username);
        return ResponseEntity.ok(response);
    }

    // Send friend request
    @PostMapping("/send-request")
    public ResponseEntity<ApiResponse<String>> sendFriendRequest(@RequestParam String sender,
                                                                 @RequestParam String receiver) {
        ApiResponse<String> response = friendService.sendFriendRequest(sender, receiver);
        return ResponseEntity.ok(response);
    }

    // Accept friend request
    @PostMapping("/accept")
    public ResponseEntity<ApiResponse<String>> acceptFriendRequest(@RequestParam String adder,
                                                                   @RequestParam String added) {
        ApiResponse<String> response = friendService.acceptFriendRequest(adder, added);
        return ResponseEntity.ok(response);
    }

    // Reject friend request
    @PostMapping("/reject")
    public ResponseEntity<ApiResponse<String>> rejectFriendRequest(@RequestParam String blocker,
                                                                   @RequestParam String blockedUser) {
        ApiResponse<String> response = friendService.rejectFriendRequest(blocker, blockedUser);
        return ResponseEntity.ok(response);
    }
}