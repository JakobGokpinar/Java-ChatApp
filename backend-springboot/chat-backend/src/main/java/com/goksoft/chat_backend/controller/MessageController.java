package com.goksoft.chat_backend.controller;

import com.goksoft.chat_backend.model.Message;
import com.goksoft.chat_backend.model.Notification;
import com.goksoft.chat_backend.repository.MessageRepository;
import com.goksoft.chat_backend.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/*
    POST /api/messages/send → Send message (replaces sendMessage.php)
    POST /api/messages/get → Get messages (replaces getMessage.php)
    POST /api/messages/check-notif
 */
@RestController
@RequestMapping("/api/messages")
@CrossOrigin(origins = "*")
public class MessageController {

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    // Send a message
    @PostMapping("/send")
    @Transactional
    public ResponseEntity<String> sendMessage(@RequestParam String sender,
                                              @RequestParam String receiver,
                                              @RequestParam String message) {
        try {
            // Save message
            Message msg = new Message(sender, receiver, message);
            messageRepository.save(msg);

            // Update notification count
            addNotification(sender, receiver, 1);

            return ResponseEntity.ok("message sent");
        } catch (Exception e) {
            return ResponseEntity.ok("message failed");
        }
    }

    // Get messages between two users
    @PostMapping("/get")
    @Transactional
    public ResponseEntity<List<String[]>> getMessages(@RequestParam String user1,
                                                      @RequestParam String receiver) {
        List<Message> messages = messageRepository.findMessagesBetweenUsers(user1, receiver);
        List<String[]> result = new ArrayList<>();

        for (Message msg : messages) {
            result.add(new String[]{msg.getSender(), msg.getMsg()});
        }

        // Reset notification count to 0
        setNotification(user1, receiver, 0);

        return ResponseEntity.ok(result);
    }

    // Check notification count
    @PostMapping("/check-notif")
    public ResponseEntity<Integer> checkNotification(@RequestParam String receiver,
                                                     @RequestParam String chatter) {
        var notification = notificationRepository.findBySenderAndReceiver(chatter, receiver);

        if (notification.isPresent()) {
            return ResponseEntity.ok(notification.get().getCounts());
        } else {
            return ResponseEntity.ok(0);
        }
    }

    // Helper: Add or increment notification
    private void addNotification(String sender, String receiver, int count) {
        var existingNotif = notificationRepository.findBySenderAndReceiver(sender, receiver);

        if (existingNotif.isPresent()) {
            // Update existing
            Notification notif = existingNotif.get();
            notif.setCounts(notif.getCounts() + count);
            notificationRepository.save(notif);
        } else {
            // Create new
            Notification notif = new Notification(sender, receiver, count);
            notificationRepository.save(notif);
        }
    }

    // Helper: Set notification count
    private void setNotification(String receiver, String sender, int count) {
        var existingNotif = notificationRepository.findBySenderAndReceiver(sender, receiver);

        if (existingNotif.isPresent()) {
            Notification notif = existingNotif.get();
            notif.setCounts(count);
            notificationRepository.save(notif);
        }
    }
}