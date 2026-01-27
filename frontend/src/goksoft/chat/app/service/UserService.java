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

public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    private final ApiClient apiClient;

    public UserService(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    // Search users by username
    public CompletableFuture<ApiResponse<List<String>>> searchUsers(String searchQuery) {
        return apiClient.post("/users/search?username=" + searchQuery, "")  // Changed to POST
                .thenApply(responseJson -> {
                    return JsonUtil.fromJson(
                            responseJson,
                            new TypeToken<ApiResponse<List<String>>>(){}
                    );
                })
                .exceptionally(ex -> {
                    logger.error("Failed to search users: {}", ex.getMessage());
                    return new ApiResponse<>(false, "Connection error", Collections.emptyList());
                });
    }

    // Get profile photo (public endpoint, no auth required)
    public CompletableFuture<byte[]> getProfilePhoto(String username) {
        // Note: This returns raw bytes, not JSON
        // Will handle differently when integrate with UI
        return CompletableFuture.completedFuture(new byte[0]); // Placeholder for now
    }
}