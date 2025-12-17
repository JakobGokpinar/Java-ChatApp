package com.chatapp.backend.controller;

import com.chatapp.backend.config.SecurityUtils;
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

    // Get friends list - NO username parameter, use JWT token
    @PostMapping("/get")
    public ResponseEntity<ApiResponse<List<String>>> getFriends() {
        String username = SecurityUtils.getCurrentUsername();
        ApiResponse<List<String>> response = friendService.getFriends(username);
        return ResponseEntity.ok(response);
    }

    // Get friend requests - NO username parameter, use JWT token
    @PostMapping("/requests")
    public ResponseEntity<ApiResponse<List<String>>> getFriendRequests() {
        String username = SecurityUtils.getCurrentUsername();
        ApiResponse<List<String>> response = friendService.getFriendRequests(username);
        return ResponseEntity.ok(response);
    }

    // Send friend request - sender from JWT, receiver from param
    @PostMapping("/send-request")
    public ResponseEntity<ApiResponse<String>> sendFriendRequest(@RequestParam String receiver) {
        String sender = SecurityUtils.getCurrentUsername();
        ApiResponse<String> response = friendService.sendFriendRequest(sender, receiver);
        return ResponseEntity.ok(response);
    }

    // Accept friend request - accepter from JWT, requester from param
    @PostMapping("/accept")
    public ResponseEntity<ApiResponse<String>> acceptFriendRequest(@RequestParam String requester) {
        String accepter = SecurityUtils.getCurrentUsername();
        ApiResponse<String> response = friendService.acceptFriendRequest(accepter, requester);
        return ResponseEntity.ok(response);
    }

    // Reject friend request - rejecter from JWT, requester from param
    @PostMapping("/reject")
    public ResponseEntity<ApiResponse<String>> rejectFriendRequest(@RequestParam String requester) {
        String rejecter = SecurityUtils.getCurrentUsername();
        ApiResponse<String> response = friendService.rejectFriendRequest(rejecter, requester);
        return ResponseEntity.ok(response);
    }
}