package com.chatapp.backend.service;

import com.chatapp.backend.dto.response.ApiResponse;
import com.chatapp.backend.dto.response.MessageResponse;
import com.chatapp.backend.model.Message;
import com.chatapp.backend.model.Notification;
import com.chatapp.backend.repository.MessageRepository;
import com.chatapp.backend.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class MessageService {

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    // Send a message
    @Transactional
    public ApiResponse<String> sendMessage(String sender, String receiver, String messageText) {
        try {
            // Save message
            Message msg = new Message(sender, receiver, messageText);
            messageRepository.save(msg);

            // Update notification count
            updateNotificationCount(sender, receiver, 1);

            return ApiResponse.success("message sent", null);
        } catch (Exception e) {
            return ApiResponse.error("message failed");
        }
    }

    // Get messages between two users
    @Transactional
    public ApiResponse<List<MessageResponse>> getMessages(String user1, String receiver) {
        List<Message> messages = messageRepository.findMessagesBetweenUsers(user1, receiver);
        List<MessageResponse> messageResponses = new ArrayList<>();

        for (Message msg : messages) {
            messageResponses.add(new MessageResponse(msg.getSender(), msg.getMsg()));
        }

        // Reset notification count to 0
        resetNotificationCount(user1, receiver);

        return ApiResponse.success("Messages retrieved", messageResponses);
    }

    // Check notification count
    public ApiResponse<Integer> checkNotification(String receiver, String chatter) {
        var notification = notificationRepository.findBySenderAndReceiver(chatter, receiver);

        if (notification.isPresent()) {
            return ApiResponse.success("Notification count", notification.get().getCounts());
        } else {
            return ApiResponse.success("Notification count", 0);
        }
    }

    // Helper: Update notification count
    private void updateNotificationCount(String sender, String receiver, int increment) {
        var existingNotif = notificationRepository.findBySenderAndReceiver(sender, receiver);

        if (existingNotif.isPresent()) {
            // Update existing
            Notification notif = existingNotif.get();
            notif.setCounts(notif.getCounts() + increment);
            notificationRepository.save(notif);
        } else {
            // Create new
            Notification notif = new Notification(sender, receiver, increment);
            notificationRepository.save(notif);
        }
    }

    // Helper: Reset notification count
    private void resetNotificationCount(String receiver, String sender) {
        var existingNotif = notificationRepository.findBySenderAndReceiver(sender, receiver);

        if (existingNotif.isPresent()) {
            Notification notif = existingNotif.get();
            notif.setCounts(0);
            notificationRepository.save(notif);
        }
    }
}