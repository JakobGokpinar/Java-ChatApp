package goksoft.chat.app.service;

import goksoft.chat.app.api.ApiClient;
import goksoft.chat.app.model.dto.ApiResponse;
import goksoft.chat.app.model.dto.LoginRequest;
import goksoft.chat.app.model.dto.LoginResponse;
import goksoft.chat.app.model.dto.RegisterRequest;
import goksoft.chat.app.model.dto.User;
import goksoft.chat.app.util.JsonUtil;
import com.google.gson.reflect.TypeToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;

public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);
    private final ApiClient apiClient;

    public AuthService(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public CompletableFuture<ApiResponse<LoginResponse>> login(String username, String password) {
        LoginRequest request = new LoginRequest(username, password);
        String jsonBody = JsonUtil.toJson(request);

        return apiClient.post("/auth/login", jsonBody)
                .thenApply(responseJson -> {
                    ApiResponse<LoginResponse> response = JsonUtil.fromJson(
                            responseJson,
                            new TypeToken<ApiResponse<LoginResponse>>(){}
                    );

                    if (response.isSuccess() && response.getData() != null) {
                        String token = response.getData().getToken();
                        apiClient.setToken(token);
                        logger.info("Login successful: {}", username);
                    } else {
                        logger.error("Login failed for user: {}", username);
                    }

                    return response;
                })
                .exceptionally(ex -> {
                    logger.error("Login error: {}", ex.getMessage());
                    return new ApiResponse<>(false, "Connection error: " + ex.getMessage(), null);
                });
    }

    public CompletableFuture<ApiResponse<User>> register(String username, String password) {
        RegisterRequest request = new RegisterRequest(username, null, password);
        String jsonBody = JsonUtil.toJson(request);

        return apiClient.post("/auth/register", jsonBody)
                .thenApply(responseJson -> {
                    ApiResponse<User> response = JsonUtil.fromJson(
                            responseJson,
                            new TypeToken<ApiResponse<User>>(){}
                    );

                    if (response.isSuccess()) {
                        logger.info("Registration successful: {}", username);
                    } else {
                        logger.error("Registration failed for user: {}", username);
                    }

                    return response;
                })
                .exceptionally(ex -> {
                    logger.error("Registration error: {}", ex.getMessage());
                    return new ApiResponse<>(false, "Connection error: " + ex.getMessage(), null);
                });
    }

    public void logout() {
        apiClient.clearToken();
        logger.info("User logged out");
    }
}