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

import java.util.ArrayList;
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
     * Backend returns: ApiResponse<List<FriendDetailDto>>
     * We convert to List<List<String>> for compatibility with existing UI code
     */
    public CompletableFuture<List<List<String>>> getFriendsWithDetails() {
        return apiClient.post("/friends/get-details", "")
                .thenApply(this::parseFriendDetailsResponse)
                .exceptionally(ex -> {
                    logger.error("Error fetching friends with details", ex);
                    return List.of();
                });
    }

    /**
     * Parse friend details JSON response into List<List<String>> format
     * Extracted method to reduce complexity
     */
    private List<List<String>> parseFriendDetailsResponse(String json) {
        try {
            JsonObject responseObj = JsonParser.parseString(json).getAsJsonObject();

            if (!responseObj.get("success").getAsBoolean()) {
                logger.warn("Failed to fetch friends: {}",
                        responseObj.get("message").getAsString());
                return List.of();
            }

            JsonArray dataArray = responseObj.getAsJsonArray("data");
            List<List<String>> result = new ArrayList<>();

            for (JsonElement elem : dataArray) {
                JsonObject friendObj = elem.getAsJsonObject();
                List<String> friendData = List.of(
                        friendObj.get("username").getAsString(),
                        friendObj.get("notificationCount").getAsString(),
                        friendObj.get("lastMessage").getAsString(),
                        friendObj.get("timeSinceLastMessage").getAsString()
                );
                result.add(friendData);
            }

            return result;
        } catch (Exception e) {
            logger.error("Error parsing friend details response", e);
            return List.of();
        }
    }

    /**
     * Get list of friend requests
     */
    public CompletableFuture<List<String>> getFriendRequests() {
        return apiClient.post("/friends/requests", "")
                .thenApply(this::parseFriendRequestsResponse)
                .exceptionally(ex -> {
                    logger.error("Error fetching friend requests", ex);
                    return new ArrayList<>();
                });
    }

    /**
     * Parse friend requests response - unwrap ApiResponse object
     */
    private List<String> parseFriendRequestsResponse(String json) {
        try {
            JsonObject responseObj = JsonParser.parseString(json).getAsJsonObject();

            if (responseObj.has("data") && responseObj.get("data").isJsonArray()) {
                JsonArray dataArray = responseObj.getAsJsonArray("data");
                List<String> usernames = new ArrayList<>();

                for (JsonElement element : dataArray) {
                    usernames.add(element.getAsString());
                }

                return usernames;
            }

            return new ArrayList<>();

        } catch (Exception e) {
            logger.error("Failed to parse friend requests response", e);
            return new ArrayList<>();
        }
    }

    /**
     * Send friend request to another user
     */
    public CompletableFuture<ApiResponse<String>> sendFriendRequest(String receiver) {
        String url = "/friends/send-request?receiver=" + receiver;
        return apiClient.post(url, "")
                .thenApply(json -> JsonUtil.fromJson(json, new TypeToken<ApiResponse<String>>() {
                }))
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
                .thenApply(json -> JsonUtil.fromJson(json, new TypeToken<ApiResponse<String>>() {
                }))
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
                .thenApply(json -> JsonUtil.fromJson(json, new TypeToken<ApiResponse<String>>() {
                }))
                .exceptionally(ex -> {
                    logger.error("Error rejecting friend request", ex);
                    return new ApiResponse<>(false, "Connection error", null);
                });
    }
}