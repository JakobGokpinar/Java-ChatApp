package com.chatapp.backend.controller;

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

    // Send a message
    @PostMapping("/send")
    public ResponseEntity<ApiResponse<String>> sendMessage(@RequestParam String sender,
                                                           @RequestParam String receiver,
                                                           @RequestParam String message) {
        ApiResponse<String> response = messageService.sendMessage(sender, receiver, message);
        return ResponseEntity.ok(response);
    }

    // Get messages between two users
    @PostMapping("/get")
    public ResponseEntity<ApiResponse<List<MessageResponse>>> getMessages(@RequestParam String user1,
                                                                          @RequestParam String receiver) {
        ApiResponse<List<MessageResponse>> response = messageService.getMessages(user1, receiver);
        return ResponseEntity.ok(response);
    }

    // Check notification count
    @PostMapping("/check-notif")
    public ResponseEntity<ApiResponse<Integer>> checkNotification(@RequestParam String receiver,
                                                                  @RequestParam String chatter) {
        ApiResponse<Integer> response = messageService.checkNotification(receiver, chatter);
        return ResponseEntity.ok(response);
    }
}