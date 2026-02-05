package goksoft.chat.app.api;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ApiClient Tests")
class ApiClientTest {

    private ApiClient apiClient;

    @BeforeEach
    void setUp() {
        apiClient = new ApiClient();
    }

    // ===== TOKEN MANAGEMENT =====

    @Nested
    @DisplayName("Token Management")
    class TokenManagement {

        @Test
        @DisplayName("New client has no token")
        void newClient_hasNoToken() {
            assertFalse(apiClient.hasToken());
            assertNull(apiClient.getToken());
        }

        @Test
        @DisplayName("setToken stores token correctly")
        void setToken_storesToken() {
            apiClient.setToken("jwt-abc-123");

            assertTrue(apiClient.hasToken());
            assertEquals("jwt-abc-123", apiClient.getToken());
        }

        @Test
        @DisplayName("clearToken removes token")
        void clearToken_removesToken() {
            apiClient.setToken("jwt-abc-123");
            apiClient.clearToken();

            assertFalse(apiClient.hasToken());
            assertNull(apiClient.getToken());
        }

        @Test
        @DisplayName("hasToken returns false for null token")
        void hasToken_nullToken_returnsFalse() {
            apiClient.setToken(null);

            assertFalse(apiClient.hasToken());
        }

        @Test
        @DisplayName("hasToken returns false for empty token")
        void hasToken_emptyToken_returnsFalse() {
            apiClient.setToken("");

            assertFalse(apiClient.hasToken());
        }

        @Test
        @DisplayName("Token can be replaced")
        void setToken_replacesExistingToken() {
            apiClient.setToken("old-token");
            apiClient.setToken("new-token");

            assertEquals("new-token", apiClient.getToken());
        }

        @Test
        @DisplayName("Multiple clear calls are safe")
        void clearToken_multipleCalls_safe() {
            apiClient.clearToken();
            apiClient.clearToken();

            assertFalse(apiClient.hasToken());
            assertNull(apiClient.getToken());
        }
    }
}