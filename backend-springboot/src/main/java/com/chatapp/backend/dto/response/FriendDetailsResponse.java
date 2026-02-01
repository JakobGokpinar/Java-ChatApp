package com.chatapp.backend.dto.response;

/**
 * DTO for returning detailed friend information
 * Matches frontend expectations: [username, notifCount, lastMessage, timeSinceLastMessage]
 */
public record FriendDetailsResponse(
        String username,
        String notificationCount,
        String lastMessage,
        String timeSinceLastMessage
) {
    public FriendDetailsResponse(String username) {
        this(username, "0", "", "");
    }
}