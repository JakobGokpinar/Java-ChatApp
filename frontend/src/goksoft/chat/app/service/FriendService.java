package goksoft.chat.app.service;

import goksoft.chat.app.api.FriendsApi;
import goksoft.chat.app.config.AppConfig;
import goksoft.chat.app.model.dto.ApiResponse;
import goksoft.chat.app.model.dto.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import retrofit2.Call;
import retrofit2.Response;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * Friend management service
 */
public class FriendService {

    private static final Logger logger = LoggerFactory.getLogger(FriendService.class);
    private final FriendsApi friendsApi;
    private final AuthService authService;

    public FriendService(AuthService authService) {
        this.friendsApi = AppConfig.getInstance().createService(FriendsApi.class);
        this.authService = authService;
    }

    /**
     * Get all friends
     */
    public ApiResponse<List<User>> getFriends() {
        try {
            String authHeader = authService.getAuthHeader();
            if (authHeader == null) {
                return new ApiResponse<>(false, "Not authenticated", Collections.emptyList());
            }

            Call<ApiResponse<List<User>>> call = friendsApi.getFriends(authHeader);
            Response<ApiResponse<List<User>>> response = call.execute();

            if (response.isSuccessful() && response.body() != null) {
                return response.body();
            } else {
                logger.error("Get friends failed with status: {}", response.code());
                return new ApiResponse<>(false, "Failed to get friends: " + response.message(), Collections.emptyList());
            }

        } catch (IOException e) {
            logger.error("Get friends error", e);
            return new ApiResponse<>(false, "Network error: " + e.getMessage(), Collections.emptyList());
        }
    }

    /**
     * Send friend request
     */
    public ApiResponse<String> sendFriendRequest(Long userId) {
        try {
            String authHeader = authService.getAuthHeader();
            if (authHeader == null) {
                return new ApiResponse<>(false, "Not authenticated", null);
            }

            Call<ApiResponse<String>> call = friendsApi.sendFriendRequest(authHeader, userId);
            Response<ApiResponse<String>> response = call.execute();

            if (response.isSuccessful() && response.body() != null) {
                return response.body();
            } else {
                return new ApiResponse<>(false, "Failed to send request: " + response.message(), null);
            }

        } catch (IOException e) {
            logger.error("Send friend request error", e);
            return new ApiResponse<>(false, "Network error: " + e.getMessage(), null);
        }
    }

    /**
     * Accept friend request
     */
    public ApiResponse<String> acceptFriendRequest(Long userId) {
        try {
            String authHeader = authService.getAuthHeader();
            if (authHeader == null) {
                return new ApiResponse<>(false, "Not authenticated", null);
            }

            Call<ApiResponse<String>> call = friendsApi.acceptFriendRequest(authHeader, userId);
            Response<ApiResponse<String>> response = call.execute();

            if (response.isSuccessful() && response.body() != null) {
                return response.body();
            } else {
                return new ApiResponse<>(false, "Failed to accept request: " + response.message(), null);
            }

        } catch (IOException e) {
            logger.error("Accept friend request error", e);
            return new ApiResponse<>(false, "Network error: " + e.getMessage(), null);
        }
    }

    /**
     * Remove friend
     */
    public ApiResponse<String> removeFriend(Long userId) {
        try {
            String authHeader = authService.getAuthHeader();
            if (authHeader == null) {
                return new ApiResponse<>(false, "Not authenticated", null);
            }

            Call<ApiResponse<String>> call = friendsApi.removeFriend(authHeader, userId);
            Response<ApiResponse<String>> response = call.execute();

            if (response.isSuccessful() && response.body() != null) {
                return response.body();
            } else {
                return new ApiResponse<>(false, "Failed to remove friend: " + response.message(), null);
            }

        } catch (IOException e) {
            logger.error("Remove friend error", e);
            return new ApiResponse<>(false, "Network error: " + e.getMessage(), null);
        }
    }
}