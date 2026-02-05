package goksoft.chat.app.service;

import goksoft.chat.app.api.ApiClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("UserService Tests")
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private ApiClient apiClient;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(apiClient);
    }

    // ===== SEARCH USERS =====

    @Nested
    @DisplayName("searchUsers")
    class SearchUsers {

        @Test
        @DisplayName("Returns matching usernames")
        void searchUsers_success_returnsUsernames() {
            // Given - backend returns List<String>
            String json = """
                    ["alice", "alice_wonder", "malice"]
                    """;
            when(apiClient.post(contains("/users/search"), anyString()))
                    .thenReturn(CompletableFuture.completedFuture(json));

            // When
            List<String> results = userService.searchUsers("alice").join();

            // Then
            assertEquals(3, results.size());
            assertEquals("alice", results.get(0));
            assertEquals("alice_wonder", results.get(1));
            assertEquals("malice", results.get(2));
        }

        @Test
        @DisplayName("Returns empty list when no matches")
        void searchUsers_noMatches_returnsEmptyList() {
            // Given
            String json = "[]";
            when(apiClient.post(contains("/users/search"), anyString()))
                    .thenReturn(CompletableFuture.completedFuture(json));

            // When
            List<String> results = userService.searchUsers("nonexistent").join();

            // Then
            assertTrue(results.isEmpty());
        }

        @Test
        @DisplayName("Sends correct endpoint with username parameter")
        void searchUsers_sendsCorrectEndpoint() {
            // Given
            when(apiClient.post(anyString(), anyString()))
                    .thenReturn(CompletableFuture.completedFuture("[]"));

            // When
            userService.searchUsers("jakob").join();

            // Then
            verify(apiClient).post(eq("/users/search?username=jakob"), eq(""));
        }

        @Test
        @DisplayName("Connection error returns empty list via exceptionally")
        void searchUsers_connectionError_returnsEmptyList() {
            // Given
            when(apiClient.post(contains("/users/search"), anyString()))
                    .thenReturn(CompletableFuture.failedFuture(
                            new RuntimeException("Network error")));

            // When
            List<String> results = userService.searchUsers("test").join();

            // Then
            assertTrue(results.isEmpty());
        }

        @Test
        @DisplayName("Single result is returned correctly")
        void searchUsers_singleResult_returnsSingleElement() {
            // Given
            String json = """
                    ["exactmatch"]
                    """;
            when(apiClient.post(contains("/users/search"), anyString()))
                    .thenReturn(CompletableFuture.completedFuture(json));

            // When
            List<String> results = userService.searchUsers("exactmatch").join();

            // Then
            assertEquals(1, results.size());
            assertEquals("exactmatch", results.get(0));
        }

        @Test
        @DisplayName("Partial search term finds matching users")
        void searchUsers_partialMatch_returnsMatches() {
            // Given
            String json = """
                    ["jakob", "test_jakob_dev"]
                    """;
            when(apiClient.post(contains("/users/search"), anyString()))
                    .thenReturn(CompletableFuture.completedFuture(json));

            // When
            List<String> results = userService.searchUsers("jak").join();

            // Then
            assertEquals(2, results.size());
            assertTrue(results.stream().allMatch(name -> name.contains("jakob")));
        }
    }

    // ===== GET PROFILE PHOTO URL =====

    @Nested
    @DisplayName("getProfilePhotoUrl")
    class GetProfilePhotoUrl {

        @Test
        @DisplayName("Returns correct URL with username")
        void getProfilePhotoUrl_returnsCorrectUrl() {
            // When
            String url = userService.getProfilePhotoUrl("alice");

            // Then - URL should end with /users/photo/alice
            assertTrue(url.endsWith("/users/photo/alice"));
        }

        @Test
        @DisplayName("URL contains base URL from Environment")
        void getProfilePhotoUrl_containsBaseUrl() {
            // When
            String url = userService.getProfilePhotoUrl("bob");

            // Then - should contain /api/ since it uses Environment.getBaseUrl()
            assertTrue(url.contains("/api/users/photo/bob"));
        }

        @Test
        @DisplayName("Different usernames produce different URLs")
        void getProfilePhotoUrl_differentUsers_differentUrls() {
            // When
            String url1 = userService.getProfilePhotoUrl("alice");
            String url2 = userService.getProfilePhotoUrl("bob");

            // Then
            assertNotEquals(url1, url2);
            assertTrue(url1.endsWith("/alice"));
            assertTrue(url2.endsWith("/bob"));
        }
    }
}