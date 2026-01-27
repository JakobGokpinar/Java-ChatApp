package goksoft.chat.app.service;

import goksoft.chat.app.api.AuthApi;
import goksoft.chat.app.config.AppConfig;
import goksoft.chat.app.model.dto.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import retrofit2.Call;
import retrofit2.Response;

import java.io.IOException;


public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);
    private final AuthApi authApi;
    private String currentToken;
    private User currentUser;

    public AuthService() {
        this.authApi = AppConfig.getInstance().createService(AuthApi.class);
    }

    /**
     * Login user
     */
    public ApiResponse<LoginResponse> login(String username, String password) {
        try {
            LoginRequest request = new LoginRequest(username, password);
            Call<ApiResponse<LoginResponse>> call = authApi.login(request);
            Response<ApiResponse<LoginResponse>> response = call.execute();

            if (response.isSuccessful() && response.body() != null) {
                ApiResponse<LoginResponse> apiResponse = response.body();

                if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                    // Store token and user
                    this.currentToken = apiResponse.getData().getToken();
                    this.currentUser = apiResponse.getData().getUser();
                    logger.info("Login successful for user: {}", username);
                }

                return apiResponse;
            } else {
                logger.error("Login failed with status: {}", response.code());
                return new ApiResponse<>(false, "Login failed: " + response.message(), null);
            }

        } catch (IOException e) {
            logger.error("Login error", e);
            return new ApiResponse<>(false, "Network error: " + e.getMessage(), null);
        }
    }

    /**
     * Register new user
     */
    public ApiResponse<User> register(String username, String email, String password) {
        try {
            RegisterRequest request = new RegisterRequest(username, email, password);
            Call<ApiResponse<User>> call = authApi.register(request);
            Response<ApiResponse<User>> response = call.execute();

            if (response.isSuccessful() && response.body() != null) {
                ApiResponse<User> apiResponse = response.body();
                logger.info("Registration successful for user: {}", username);
                return apiResponse;
            } else {
                logger.error("Registration failed with status: {}", response.code());
                return new ApiResponse<>(false, "Registration failed: " + response.message(), null);
            }

        } catch (IOException e) {
            logger.error("Registration error", e);
            return new ApiResponse<>(false, "Network error: " + e.getMessage(), null);
        }
    }

    public void logout() {
        this.currentToken = null;
        this.currentUser = null;
        logger.info("User logged out");
    }

    public String getToken() {
        return currentToken;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public boolean isLoggedIn() {
        return currentToken != null && currentUser != null;
    }

    public String getAuthHeader() {
        return currentToken != null ? "Bearer " + currentToken : null;
    }
}