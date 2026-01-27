package goksoft.chat.app.service;

import com.google.gson.reflect.TypeToken;
import goksoft.chat.app.api.ApiClient;
import goksoft.chat.app.model.dto.ApiResponse;
import goksoft.chat.app.util.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class FriendService {

    private static final Logger logger = LoggerFactory.getLogger(FriendService.class);
    private final ApiClient apiClient;

    public FriendService(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    /**
     * Get list of accepted friends with their last message info
     * Backend returns: [[username, notifCount, lastMsg, passedTime], ...]
     */
    public CompletableFuture<List<List<String>>> getFriendsWithDetails() {
        return apiClient.post("/friends/get", "")
                .thenApply(json -> {
                    return JsonUtil.fromJson(json, new TypeToken<List<List<String>>>(){});
                })
                .exceptionally(ex -> {
                    logger.error("Error fetching friends", ex);
                    return List.of();
                });
    }

    /**
     * Get list of friend requests
     * Backend returns: ["username1", "username2", ...]
     */
    public CompletableFuture<List<String>> getFriendRequests() {
        return apiClient.post("/friends/requests", "")
                .thenApply(json -> {
                    return JsonUtil.fromJson(json, new TypeToken<List<String>>(){});
                })
                .exceptionally(ex -> {
                    logger.error("Error fetching friend requests", ex);
                    return List.of();
                });
    }

    /**
     * Send friend request to another user
     */
    public CompletableFuture<ApiResponse<String>> sendFriendRequest(String receiver) {
        String url = "/friends/send-request?receiver=" + receiver;
        return apiClient.post(url, "")
                .thenApply(json -> JsonUtil.fromJson(json, new TypeToken<ApiResponse<String>>(){}))
                .exceptionally(ex -> {
                    logger.error("Error sending friend request", ex);
                    return new ApiResponse<>(false, "Connection error", null);
                });
    }

    /**
     * Accept a friend request
     */
    public CompletableFuture<ApiResponse<String>> acceptFriendRequest(String requester) {
        String url = "/friends/accept?requester=" + requester;
        return apiClient.post(url, "")
                .thenApply(json -> JsonUtil.fromJson(json, new TypeToken<ApiResponse<String>>(){}))
                .exceptionally(ex -> {
                    logger.error("Error accepting friend request", ex);
                    return new ApiResponse<>(false, "Connection error", null);
                });
    }

    /**
     * Reject a friend request
     */
    public CompletableFuture<ApiResponse<String>> rejectFriendRequest(String requester) {
        String url = "/friends/reject?requester=" + requester;
        return apiClient.post(url, "")
                .thenApply(json -> JsonUtil.fromJson(json, new TypeToken<ApiResponse<String>>(){}))
                .exceptionally(ex -> {
                    logger.error("Error rejecting friend request", ex);
                    return new ApiResponse<>(false, "Connection error", null);
                });
    }
}