package com.chatapp.backend.dto.response;

public class FriendResponse {
    private String friendUsername;
    private String lastMessage;
    private int unreadCount;

    public FriendResponse(String friendUsername) {
        this.friendUsername = friendUsername;
        this.lastMessage = "";
        this.unreadCount = 0;
    }

    public FriendResponse(String friendUsername, String lastMessage, int unreadCount) {
        this.friendUsername = friendUsername;
        this.lastMessage = lastMessage;
        this.unreadCount = unreadCount;
    }

    public String getFriendUsername() {
        return friendUsername;
    }

    public void setFriendUsername(String friendUsername) {
        this.friendUsername = friendUsername;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public int getUnreadCount() {
        return unreadCount;
    }

    public void setUnreadCount(int unreadCount) {
        this.unreadCount = unreadCount;
    }
}