package goksoft.chat.app.service;

import com.google.gson.reflect.TypeToken;
import goksoft.chat.app.api.ApiClient;
import goksoft.chat.app.model.dto.ApiResponse;
import goksoft.chat.app.util.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class MessageService {

    private static final Logger logger = LoggerFactory.getLogger(MessageService.class);
    private final ApiClient apiClient;

    public MessageService(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    /**
     * Get messages between current user and receiver
     * Backend returns: [[sender, message], [sender, message], ...]
     */
    public CompletableFuture<List<List<String>>> getMessages(String receiver) {
        String url = "/messages/get?receiver=" + receiver;
        return apiClient.post(url, "")
                .thenApply(json -> {
                    return JsonUtil.fromJson(json, new TypeToken<List<List<String>>>(){});
                })
                .exceptionally(ex -> {
                    logger.error("Error fetching messages", ex);
                    return List.of();
                });
    }

    /**
     * Send a message to receiver
     */
    public CompletableFuture<ApiResponse<String>> sendMessage(String receiver, String message) {
        String url = "/messages/send?receiver=" + receiver + "&message=" + message;
        return apiClient.post(url, "")
                .thenApply(json -> JsonUtil.fromJson(json, new TypeToken<ApiResponse<String>>(){}))
                .exceptionally(ex -> {
                    logger.error("Error sending message", ex);
                    return new ApiResponse<>(false, "Connection error", null);
                });
    }

    /**
     * Check notification count for a specific chatter
     * Backend returns: notification count as plain text string
     */
    public CompletableFuture<Integer> checkNotification(String chatter) {
        String url = "/messages/check-notif?chatter=" + chatter;
        return apiClient.post(url, "")
                .thenApply(responseStr -> {
                    try {
                        return Integer.parseInt(responseStr.trim());
                    } catch (NumberFormatException e) {
                        logger.warn("Invalid notification count format: {}", responseStr);
                        return 0;
                    }
                })
                .exceptionally(ex -> {
                    logger.error("Error checking notifications", ex);
                    return 0;
                });
    }
}