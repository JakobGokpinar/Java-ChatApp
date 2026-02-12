package goksoft.chat.app.service;

import com.google.gson.reflect.TypeToken;
import goksoft.chat.app.api.ApiClient;
import goksoft.chat.app.model.dto.ApiResponse;
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

    public CompletableFuture<List<String>> searchUsers(String username) {
        String url = "/users/search?username=" + username;
        return apiClient.post(url, "")
                .thenApply(json -> {
                    ApiResponse<List<String>> response = JsonUtil.fromJson(
                            json, new TypeToken<ApiResponse<List<String>>>() {}
                    );
                    if (response.isSuccess() && response.getData() != null) {
                        return response.getData();
                    }
                    return List.<String>of();
                })
                .exceptionally(ex -> {
                    logger.error("Error searching users", ex);
                    return List.of();
                });
    }

}