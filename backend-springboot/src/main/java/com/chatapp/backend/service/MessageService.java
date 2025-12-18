package com.chatapp.backend.service;

import com.chatapp.backend.dto.response.ApiResponse;
import com.chatapp.backend.dto.response.MessageResponse;
import com.chatapp.backend.exception.ValidationException;
import com.chatapp.backend.model.Message;
import com.chatapp.backend.repository.MessageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles messaging operations between users.
 * Messages are marked as read when retrieved by the receiver.
 */
@Service
public class MessageService {

    private static final Logger logger = LoggerFactory.getLogger(MessageService.class);

    @Autowired
    private MessageRepository messageRepository;

    @Transactional
    public ApiResponse<String> sendMessage(String sender, String receiver, String messageText) {
        // Validation
        if (sender == null || sender.isBlank()) {
            throw new ValidationException("Sender is required");
        }
        if (receiver == null || receiver.isBlank()) {
            throw new ValidationException("Receiver is required");
        }
        if (messageText == null || messageText.isBlank()) {
            throw new ValidationException("Message cannot be empty");
        }

        logger.info("Sending message: {} → {}", sender, receiver);

        try {
            Message msg = new Message(sender, receiver, messageText);
            messageRepository.save(msg);

            logger.info("Message sent successfully: {} → {}", sender, receiver);
            return ApiResponse.success("Message sent", null);
        } catch (Exception e) {
            logger.error("Failed to send message: {} → {}", sender, receiver, e);
            throw new RuntimeException("Failed to send message");
        }
    }

    @Transactional
    public ApiResponse<List<MessageResponse>> getMessages(String user1, String receiver) {
        logger.info("Fetching messages between: {} and {}", user1, receiver);

        List<Message> messages = messageRepository.findMessagesBetweenUsers(user1, receiver);
        List<MessageResponse> messageResponses = new ArrayList<>();

        for (Message msg : messages) {
            messageResponses.add(new MessageResponse(msg.getSender(), msg.getContent()));
        }

        // Mark all messages from receiver to user1 as read
        messageRepository.markMessagesAsRead(user1, receiver);

        logger.info("Retrieved {} messages between {} and {}", messages.size(), user1, receiver);
        return ApiResponse.success("Messages retrieved", messageResponses);
    }

    public ApiResponse<Integer> checkNotification(String receiver, String sender) {
        logger.debug("Checking unread count: {} ← {}", receiver, sender);

        int unreadCount = messageRepository.countUnreadMessages(receiver, sender);

        logger.debug("Unread messages for {}: {}", receiver, unreadCount);
        return ApiResponse.success("Notification count", unreadCount);
    }
}