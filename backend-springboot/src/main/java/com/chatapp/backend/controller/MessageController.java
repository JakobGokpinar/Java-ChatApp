package com.chatapp.backend.controller;

import com.chatapp.backend.config.SecurityUtils;
import com.chatapp.backend.dto.response.ApiResponse;
import com.chatapp.backend.dto.response.MessageResponse;
import com.chatapp.backend.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/messages")
@CrossOrigin(origins = "*")
public class MessageController {

    @Autowired
    private MessageService messageService;

    // Send a message - sender from JWT
    @PostMapping("/send")
    public ResponseEntity<ApiResponse<String>> sendMessage(@RequestParam String receiver,
                                                           @RequestParam String message) {
        String sender = SecurityUtils.getCurrentUsername();
        ApiResponse<String> response = messageService.sendMessage(sender, receiver, message);
        return ResponseEntity.ok(response);
    }

    // Get messages - user1 from JWT
    @PostMapping("/get")
    public ResponseEntity<ApiResponse<List<MessageResponse>>> getMessages(@RequestParam String receiver) {
        String user1 = SecurityUtils.getCurrentUsername();
        ApiResponse<List<MessageResponse>> response = messageService.getMessages(user1, receiver);
        return ResponseEntity.ok(response);
    }

    // Check notification - receiver from JWT
    @PostMapping("/check-notif")
    public ResponseEntity<ApiResponse<Integer>> checkNotification(@RequestParam String chatter) {
        String receiver = SecurityUtils.getCurrentUsername();
        ApiResponse<Integer> response = messageService.checkNotification(receiver, chatter);
        return ResponseEntity.ok(response);
    }
}