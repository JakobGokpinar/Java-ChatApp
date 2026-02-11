package goksoft.chat.app.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import goksoft.chat.app.api.ApiClient;
import goksoft.chat.app.model.dto.ApiResponse;
import goksoft.chat.app.util.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class MessageService {

    private static final Logger logger = LoggerFactory.getLogger(MessageService.class);
    private final ApiClient apiClient;

    public MessageService(ApiClient apiClient) {
        this.apiClient = apiClient;
    }


    public CompletableFuture<List<List<String>>> getMessages(String receiver) {
        String url = "/messages/get?receiver=" + receiver;
        return apiClient.post(url, "")
                .thenApply(json -> {
                    try {
                        JsonObject responseObj = JsonParser.parseString(json).getAsJsonObject();
                        if (!responseObj.get("success").getAsBoolean()) {
                            return List.<List<String>>of();
                        }
                        JsonArray dataArray = responseObj.getAsJsonArray("data");
                        List<List<String>> result = new ArrayList<>();
                        for (JsonElement elem : dataArray) {
                            JsonObject msgObj = elem.getAsJsonObject();
                            result.add(List.of(
                                    msgObj.get("sender").getAsString(),
                                    msgObj.get("message").getAsString()
                            ));
                        }
                        return result;
                    } catch (Exception e) {
                        logger.error("Error parsing messages response", e);
                        return List.<List<String>>of();
                    }
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
        String url = "/messages/send?receiver=" + URLEncoder.encode(receiver, StandardCharsets.UTF_8)
                + "&message=" + URLEncoder.encode(message, StandardCharsets.UTF_8);
        return apiClient.post(url, "")
                .thenApply(json -> JsonUtil.fromJson(json, new TypeToken<ApiResponse<String>>() {
                }))
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
                        JsonObject responseObj = JsonParser.parseString(responseStr).getAsJsonObject();
                        if (responseObj.get("success").getAsBoolean()) {
                            return responseObj.get("data").getAsInt();
                        }
                        return 0;
                    } catch (Exception e) {
                        logger.warn("Invalid notification response: {}", responseStr);
                        return 0;
                    }
                })
                .exceptionally(ex -> {
                    logger.error("Error checking notifications", ex);
                    return 0;
                });
    }
}