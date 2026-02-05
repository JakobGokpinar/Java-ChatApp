package goksoft.chat.app.service;

import goksoft.chat.app.api.ApiClient;
import goksoft.chat.app.model.dto.ApiResponse;
import goksoft.chat.app.model.dto.LoginResponse;
import goksoft.chat.app.model.dto.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("AuthService Tests")
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private ApiClient apiClient;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(apiClient);
    }

    // ===== LOGIN =====

    @Nested
    @DisplayName("Login")
    class Login {

        @Test
        @DisplayName("Successful login returns response and sets token")
        void login_success_returnsResponseAndSetsToken() {
            // Given
            String successJson = """
                    {
                        "success": true,
                        "message": "Login successful",
                        "data": {
                            "token": "jwt-test-token-123",
                            "user": { "id": 1, "username": "jakob" }
                        }
                    }
                    """;
            when(apiClient.post(eq("/auth/login"), anyString()))
                    .thenReturn(CompletableFuture.completedFuture(successJson));

            // When
            ApiResponse<LoginResponse> response = authService.login("jakob", "password123").join();

            // Then
            assertTrue(response.isSuccess());
            assertEquals("Login successful", response.getMessage());
            assertNotNull(response.getData());
            assertEquals("jwt-test-token-123", response.getData().getToken());
            assertEquals("jakob", response.getData().getUser().getUsername());

            // Verify token was set on ApiClient
            verify(apiClient).setToken("jwt-test-token-123");
        }

        @Test
        @DisplayName("Failed login returns error response without setting token")
        void login_invalidCredentials_returnsErrorResponse() {
            // Given
            String failJson = """
                    {
                        "success": false,
                        "message": "Invalid credentials",
                        "data": null
                    }
                    """;
            when(apiClient.post(eq("/auth/login"), anyString()))
                    .thenReturn(CompletableFuture.completedFuture(failJson));

            // When
            ApiResponse<LoginResponse> response = authService.login("jakob", "wrong").join();

            // Then
            assertFalse(response.isSuccess());
            assertEquals("Invalid credentials", response.getMessage());
            assertNull(response.getData());

            // Token should NOT be set
            verify(apiClient, never()).setToken(anyString());
        }

        @Test
        @DisplayName("Connection error returns error ApiResponse via exceptionally")
        void login_connectionError_returnsErrorResponse() {
            // Given
            when(apiClient.post(eq("/auth/login"), anyString()))
                    .thenReturn(CompletableFuture.failedFuture(
                            new RuntimeException("Connection refused")));

            // When
            ApiResponse<LoginResponse> response = authService.login("jakob", "pass").join();

            // Then
            assertFalse(response.isSuccess());
            assertTrue(response.getMessage().contains("Connection error"));
        }

        @Test
        @DisplayName("Login sends correct endpoint")
        void login_sendsCorrectEndpoint() {
            // Given
            String successJson = """
                    {
                        "success": true,
                        "message": "OK",
                        "data": { "token": "tok", "user": { "id": 1, "username": "test" } }
                    }
                    """;
            when(apiClient.post(anyString(), anyString()))
                    .thenReturn(CompletableFuture.completedFuture(successJson));

            // When
            authService.login("test", "pass").join();

            // Then
            verify(apiClient).post(eq("/auth/login"), contains("\"username\":\"test\""));
        }

        @Test
        @DisplayName("Login sends JSON body with username and password")
        void login_sendsJsonBody() {
            // Given
            String successJson = """
                    {
                        "success": true,
                        "message": "OK",
                        "data": { "token": "tok", "user": { "id": 1, "username": "alice" } }
                    }
                    """;
            when(apiClient.post(anyString(), anyString()))
                    .thenReturn(CompletableFuture.completedFuture(successJson));

            // When
            authService.login("alice", "secret123").join();

            // Then - verify JSON body contains both fields
            verify(apiClient).post(eq("/auth/login"), argThat(json ->
                    json.contains("\"username\":\"alice\"") &&
                            json.contains("\"password\":\"secret123\"")
            ));
        }
    }

    // ===== REGISTER =====

    @Nested
    @DisplayName("Register")
    class Register {

        @Test
        @DisplayName("Successful registration returns user data")
        void register_success_returnsUserData() {
            // Given
            String successJson = """
                    {
                        "success": true,
                        "message": "Registration successful",
                        "data": { "id": 42, "username": "newuser" }
                    }
                    """;
            when(apiClient.post(eq("/auth/register"), anyString()))
                    .thenReturn(CompletableFuture.completedFuture(successJson));

            // When
            ApiResponse<User> response = authService.register("newuser", "pass123").join();

            // Then
            assertTrue(response.isSuccess());
            assertEquals("Registration successful", response.getMessage());
            assertNotNull(response.getData());
            assertEquals("newuser", response.getData().getUsername());
            assertEquals(42L, response.getData().getId());
        }

        @Test
        @DisplayName("Duplicate username returns error response")
        void register_duplicateUsername_returnsError() {
            // Given
            String failJson = """
                    {
                        "success": false,
                        "message": "Username already exists",
                        "data": null
                    }
                    """;
            when(apiClient.post(eq("/auth/register"), anyString()))
                    .thenReturn(CompletableFuture.completedFuture(failJson));

            // When
            ApiResponse<User> response = authService.register("existing", "pass").join();

            // Then
            assertFalse(response.isSuccess());
            assertEquals("Username already exists", response.getMessage());
            assertNull(response.getData());
        }

        @Test
        @DisplayName("Connection error returns error ApiResponse via exceptionally")
        void register_connectionError_returnsErrorResponse() {
            // Given
            when(apiClient.post(eq("/auth/register"), anyString()))
                    .thenReturn(CompletableFuture.failedFuture(
                            new RuntimeException("Server unavailable")));

            // When
            ApiResponse<User> response = authService.register("user", "pass").join();

            // Then
            assertFalse(response.isSuccess());
            assertTrue(response.getMessage().contains("Connection error"));
        }

        @Test
        @DisplayName("Register sends null email in request body")
        void register_sendsNullEmail() {
            // Given
            String successJson = """
                    {
                        "success": true,
                        "message": "OK",
                        "data": { "id": 1, "username": "test" }
                    }
                    """;
            when(apiClient.post(anyString(), anyString()))
                    .thenReturn(CompletableFuture.completedFuture(successJson));

            // When
            authService.register("test", "pass").join();

            // Then - RegisterRequest(username, null, password) → email excluded by Gson
            verify(apiClient).post(eq("/auth/register"), argThat(json ->
                    json.contains("\"username\":\"test\"") &&
                            json.contains("\"password\":\"pass\"")
            ));
        }
    }

    // ===== LOGOUT =====

    @Nested
    @DisplayName("Logout")
    class Logout {

        @Test
        @DisplayName("Logout clears token on ApiClient")
        void logout_clearsToken() {
            // When
            authService.logout();

            // Then
            verify(apiClient).clearToken();
        }

        @Test
        @DisplayName("Logout can be called multiple times safely")
        void logout_multipleCalls_safe() {
            // When
            authService.logout();
            authService.logout();

            // Then
            verify(apiClient, times(2)).clearToken();
        }
    }
}