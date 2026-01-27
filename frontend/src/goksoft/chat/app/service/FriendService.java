package goksoft.chat.app.service;

import goksoft.chat.app.api.ApiClient;
import goksoft.chat.app.model.dto.ApiResponse;
import goksoft.chat.app.util.JsonUtil;
import com.google.gson.reflect.TypeToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class FriendService {

    private static final Logger logger = LoggerFactory.getLogger(FriendService.class);
    private final ApiClient apiClient;

    public FriendService(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    // Get friends list (JWT token provides username automatically)
    public CompletableFuture<ApiResponse<List<String>>> getFriends() {
        return apiClient.post("/friends/get", "")  // Changed from GET to POST, changed endpoint
                .thenApply(responseJson -> {
                    return JsonUtil.fromJson(
                            responseJson,
                            new TypeToken<ApiResponse<List<String>>>(){}
                    );
                })
                .exceptionally(ex -> {
                    logger.error("Failed to get friends: {}", ex.getMessage());
                    return new ApiResponse<>(false, "Connection error", Collections.emptyList());
                });
    }

    // Get friend requests (JWT token provides username automatically)
    public CompletableFuture<ApiResponse<List<String>>> getFriendRequests() {
        return apiClient.post("/friends/requests", "")  // POST, no params needed
                .thenApply(responseJson -> {
                    return JsonUtil.fromJson(
                            responseJson,
                            new TypeToken<ApiResponse<List<String>>>(){}
                    );
                })
                .exceptionally(ex -> {
                    logger.error("Failed to get friend requests: {}", ex.getMessage());
                    return new ApiResponse<>(false, "Connection error", Collections.emptyList());
                });
    }

    // Send friend request (sender from JWT, receiver from param)
    public CompletableFuture<ApiResponse<String>> sendFriendRequest(String receiver) {
        return apiClient.post("/friends/send-request?receiver=" + receiver, "")
                .thenApply(responseJson -> {
                    return JsonUtil.fromJson(
                            responseJson,
                            new TypeToken<ApiResponse<String>>(){}
                    );
                })
                .exceptionally(ex -> {
                    logger.error("Failed to send friend request: {}", ex.getMessage());
                    return new ApiResponse<>(false, "Connection error", null);
                });
    }

    // Accept friend request (accepter from JWT, requester from param)
    public CompletableFuture<ApiResponse<String>> acceptFriendRequest(String requester) {  // Changed param name
        return apiClient.post("/friends/accept?requester=" + requester, "")  // Fixed param name
                .thenApply(responseJson -> {
                    return JsonUtil.fromJson(
                            responseJson,
                            new TypeToken<ApiResponse<String>>(){}
                    );
                })
                .exceptionally(ex -> {
                    logger.error("Failed to accept friend request: {}", ex.getMessage());
                    return new ApiResponse<>(false, "Connection error", null);
                });
    }

    // Reject friend request (rejecter from JWT, requester from param)
    public CompletableFuture<ApiResponse<String>> rejectFriendRequest(String requester) {  // Changed param name
        return apiClient.post("/friends/reject?requester=" + requester, "")  // Fixed param name
                .thenApply(responseJson -> {
                    return JsonUtil.fromJson(
                            responseJson,
                            new TypeToken<ApiResponse<String>>(){}
                    );
                })
                .exceptionally(ex -> {
                    logger.error("Failed to reject friend request: {}", ex.getMessage());
                    return new ApiResponse<>(false, "Connection error", null);
                });
    }
}