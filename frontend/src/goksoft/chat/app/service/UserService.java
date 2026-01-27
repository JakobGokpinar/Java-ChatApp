package goksoft.chat.app.service;

import com.google.gson.reflect.TypeToken;
import goksoft.chat.app.api.ApiClient;
import goksoft.chat.app.config.Environment;
import goksoft.chat.app.util.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    private final ApiClient apiClient;

    public UserService(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    /**
     * Search users by username
     * Backend returns: ["username1", "username2", ...]
     */
    public CompletableFuture<List<String>> searchUsers(String username) {
        String url = "/users/search?username=" + username;
        return apiClient.post(url, "")
                .thenApply(json -> {
                    return JsonUtil.fromJson(json, new TypeToken<List<String>>(){});
                })
                .exceptionally(ex -> {
                    logger.error("Error searching users", ex);
                    return List.of();
                });
    }

    /**
     * Get profile photo URL for a username
     */
    public String getProfilePhotoUrl(String username) {
        return Environment.getBaseUrl() + "/users/photo/" + username;
    }
}