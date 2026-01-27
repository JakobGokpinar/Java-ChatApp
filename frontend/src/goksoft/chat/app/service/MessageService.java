package goksoft.chat.app.service;

import goksoft.chat.app.api.ApiClient;
import goksoft.chat.app.model.dto.ApiResponse;
import goksoft.chat.app.util.JsonUtil;
import com.google.gson.reflect.TypeToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class MessageService {

    private static final Logger logger = LoggerFactory.getLogger(MessageService.class);
    private final ApiClient apiClient;

    public MessageService(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public CompletableFuture<ApiResponse<List<Map<String, String>>>> getMessages(String receiver) {
        return apiClient.post("/messages/get?receiver=" + receiver, "")  // Changed to POST
                .thenApply(responseJson -> {
                    return JsonUtil.fromJson(
                            responseJson,
                            new TypeToken<ApiResponse<List<Map<String, String>>>>(){}
                    );
                })
                .exceptionally(ex -> {
                    logger.error("Failed to get messages: {}", ex.getMessage());
                    return new ApiResponse<>(false, "Connection error", Collections.emptyList());
                });
    }

    public CompletableFuture<ApiResponse<String>> sendMessage(String receiver, String message) {
        return apiClient.post("/messages/send?receiver=" + receiver + "&message=" + message, "")
                .thenApply(responseJson -> {
                    return JsonUtil.fromJson(
                            responseJson,
                            new TypeToken<ApiResponse<String>>(){}
                    );
                })
                .exceptionally(ex -> {
                    logger.error("Failed to send message: {}", ex.getMessage());
                    return new ApiResponse<>(false, "Connection error", null);
                });
    }

    public CompletableFuture<ApiResponse<Integer>> checkNotification(String chatter) {
        return apiClient.post("/messages/check-notif?chatter=" + chatter, "")
                .thenApply(responseJson -> {
                    return JsonUtil.fromJson(
                            responseJson,
                            new TypeToken<ApiResponse<Integer>>(){}
                    );
                })
                .exceptionally(ex -> {
                    logger.error("Failed to check notification: {}", ex.getMessage());
                    return new ApiResponse<>(false, "Connection error", 0);
                });
    }
}