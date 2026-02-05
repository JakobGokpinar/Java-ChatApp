package goksoft.chat.app.service;

import goksoft.chat.app.api.ApiClient;
import goksoft.chat.app.model.dto.ApiResponse;
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

@DisplayName("FriendService Tests")
@ExtendWith(MockitoExtension.class)
class FriendServiceTest {

    @Mock
    private ApiClient apiClient;

    private FriendService friendService;

    @BeforeEach
    void setUp() {
        friendService = new FriendService(apiClient);
    }

    // ===== GET FRIENDS WITH DETAILS =====

    @Nested
    @DisplayName("getFriendsWithDetails")
    class GetFriendsWithDetails {

        @Test
        @DisplayName("Parses successful response with friend data")
        void getFriendsWithDetails_success_parsesFriendData() {
            // Given - backend returns ApiResponse<List<FriendDetailDto>>
            String json = """
                    {
                        "success": true,
                        "message": "Friends with details retrieved",
                        "data": [
                            {
                                "username": "alice",
                                "notificationCount": "3",
                                "lastMessage": "Hey, how are you?",
                                "timeSinceLastMessage": "5 min ago"
                            },
                            {
                                "username": "bob",
                                "notificationCount": "0",
                                "lastMessage": "See you tomorrow",
                                "timeSinceLastMessage": "2 hours ago"
                            }
                        ]
                    }
                    """;
            when(apiClient.post(eq("/friends/get-details"), anyString()))
                    .thenReturn(CompletableFuture.completedFuture(json));

            // When
            List<List<String>> result = friendService.getFriendsWithDetails().join();

            // Then
            assertEquals(2, result.size());

            // First friend
            assertEquals("alice", result.get(0).get(0));
            assertEquals("3", result.get(0).get(1));
            assertEquals("Hey, how are you?", result.get(0).get(2));
            assertEquals("5 min ago", result.get(0).get(3));

            // Second friend
            assertEquals("bob", result.get(1).get(0));
            assertEquals("0", result.get(1).get(1));
            assertEquals("See you tomorrow", result.get(1).get(2));
            assertEquals("2 hours ago", result.get(1).get(3));
        }

        @Test
        @DisplayName("Returns empty list when response is unsuccessful")
        void getFriendsWithDetails_failedResponse_returnsEmptyList() {
            // Given
            String json = """
                    {
                        "success": false,
                        "message": "Unauthorized",
                        "data": null
                    }
                    """;
            when(apiClient.post(eq("/friends/get-details"), anyString()))
                    .thenReturn(CompletableFuture.completedFuture(json));

            // When
            List<List<String>> result = friendService.getFriendsWithDetails().join();

            // Then
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Returns empty list when no friends")
        void getFriendsWithDetails_noFriends_returnsEmptyList() {
            // Given
            String json = """
                    {
                        "success": true,
                        "message": "Friends with details retrieved",
                        "data": []
                    }
                    """;
            when(apiClient.post(eq("/friends/get-details"), anyString()))
                    .thenReturn(CompletableFuture.completedFuture(json));

            // When
            List<List<String>> result = friendService.getFriendsWithDetails().join();

            // Then
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Connection error returns empty list via exceptionally")
        void getFriendsWithDetails_connectionError_returnsEmptyList() {
            // Given
            when(apiClient.post(eq("/friends/get-details"), anyString()))
                    .thenReturn(CompletableFuture.failedFuture(
                            new RuntimeException("Connection refused")));

            // When
            List<List<String>> result = friendService.getFriendsWithDetails().join();

            // Then
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Malformed JSON returns empty list gracefully")
        void getFriendsWithDetails_malformedJson_returnsEmptyList() {
            // Given
            when(apiClient.post(eq("/friends/get-details"), anyString()))
                    .thenReturn(CompletableFuture.completedFuture("not valid json {{{"));

            // When
            List<List<String>> result = friendService.getFriendsWithDetails().join();

            // Then - parseFriendDetailsResponse catches Exception
            assertTrue(result.isEmpty());
        }
    }

    // ===== GET FRIEND REQUESTS =====

    @Nested
    @DisplayName("getFriendRequests")
    class GetFriendRequests {

        @Test
        @DisplayName("Parses successful response with pending requests")
        void getFriendRequests_success_returnsSenderNames() {
            // Given - backend returns ApiResponse<List<String>>
            String json = """
                    {
                        "success": true,
                        "message": "Friend requests retrieved",
                        "data": ["charlie", "dave", "eve"]
                    }
                    """;
            when(apiClient.post(eq("/friends/requests"), anyString()))
                    .thenReturn(CompletableFuture.completedFuture(json));

            // When
            List<String> result = friendService.getFriendRequests().join();

            // Then
            assertEquals(3, result.size());
            assertEquals("charlie", result.get(0));
            assertEquals("dave", result.get(1));
            assertEquals("eve", result.get(2));
        }

        @Test
        @DisplayName("Returns empty list when no pending requests")
        void getFriendRequests_noPending_returnsEmptyList() {
            // Given
            String json = """
                    {
                        "success": true,
                        "message": "Friend requests retrieved",
                        "data": []
                    }
                    """;
            when(apiClient.post(eq("/friends/requests"), anyString()))
                    .thenReturn(CompletableFuture.completedFuture(json));

            // When
            List<String> result = friendService.getFriendRequests().join();

            // Then
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Returns empty list when data field is missing")
        void getFriendRequests_missingDataField_returnsEmptyList() {
            // Given
            String json = """
                    {
                        "success": true,
                        "message": "OK"
                    }
                    """;
            when(apiClient.post(eq("/friends/requests"), anyString()))
                    .thenReturn(CompletableFuture.completedFuture(json));

            // When
            List<String> result = friendService.getFriendRequests().join();

            // Then
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Connection error returns empty list via exceptionally")
        void getFriendRequests_connectionError_returnsEmptyList() {
            // Given
            when(apiClient.post(eq("/friends/requests"), anyString()))
                    .thenReturn(CompletableFuture.failedFuture(
                            new RuntimeException("Timeout")));

            // When
            List<String> result = friendService.getFriendRequests().join();

            // Then
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Malformed JSON returns empty list gracefully")
        void getFriendRequests_malformedJson_returnsEmptyList() {
            // Given
            when(apiClient.post(eq("/friends/requests"), anyString()))
                    .thenReturn(CompletableFuture.completedFuture("{invalid"));

            // When
            List<String> result = friendService.getFriendRequests().join();

            // Then
            assertTrue(result.isEmpty());
        }
    }

    // ===== SEND FRIEND REQUEST =====

    @Nested
    @DisplayName("sendFriendRequest")
    class SendFriendRequest {

        @Test
        @DisplayName("Successful send returns success response")
        void sendFriendRequest_success() {
            // Given
            String json = """
                    {
                        "success": true,
                        "message": "Friend request sent",
                        "data": null
                    }
                    """;
            when(apiClient.post(contains("/friends/send-request"), anyString()))
                    .thenReturn(CompletableFuture.completedFuture(json));

            // When
            ApiResponse<String> response = friendService.sendFriendRequest("alice").join();

            // Then
            assertTrue(response.isSuccess());
            assertEquals("Friend request sent", response.getMessage());
        }

        @Test
        @DisplayName("Sends correct endpoint with receiver parameter")
        void sendFriendRequest_sendsCorrectEndpoint() {
            // Given
            String json = """
                    { "success": true, "message": "OK", "data": null }
                    """;
            when(apiClient.post(anyString(), anyString()))
                    .thenReturn(CompletableFuture.completedFuture(json));

            // When
            friendService.sendFriendRequest("bob").join();

            // Then
            verify(apiClient).post(eq("/friends/send-request?receiver=bob"), eq(""));
        }

        @Test
        @DisplayName("Duplicate request returns error response")
        void sendFriendRequest_duplicate_returnsError() {
            // Given
            String json = """
                    {
                        "success": false,
                        "message": "Friend request already sent",
                        "data": null
                    }
                    """;
            when(apiClient.post(contains("/friends/send-request"), anyString()))
                    .thenReturn(CompletableFuture.completedFuture(json));

            // When
            ApiResponse<String> response = friendService.sendFriendRequest("alice").join();

            // Then
            assertFalse(response.isSuccess());
            assertEquals("Friend request already sent", response.getMessage());
        }

        @Test
        @DisplayName("Connection error returns error ApiResponse")
        void sendFriendRequest_connectionError_returnsErrorResponse() {
            // Given
            when(apiClient.post(contains("/friends/send-request"), anyString()))
                    .thenReturn(CompletableFuture.failedFuture(
                            new RuntimeException("Network error")));

            // When
            ApiResponse<String> response = friendService.sendFriendRequest("alice").join();

            // Then
            assertFalse(response.isSuccess());
            assertEquals("Connection error", response.getMessage());
        }
    }

    // ===== ACCEPT FRIEND REQUEST =====

    @Nested
    @DisplayName("acceptFriendRequest")
    class AcceptFriendRequest {

        @Test
        @DisplayName("Successful accept returns success response")
        void acceptFriendRequest_success() {
            // Given
            String json = """
                    {
                        "success": true,
                        "message": "Friend request accepted",
                        "data": null
                    }
                    """;
            when(apiClient.post(contains("/friends/accept"), anyString()))
                    .thenReturn(CompletableFuture.completedFuture(json));

            // When
            ApiResponse<String> response = friendService.acceptFriendRequest("charlie").join();

            // Then
            assertTrue(response.isSuccess());
            assertEquals("Friend request accepted", response.getMessage());
        }

        @Test
        @DisplayName("Sends correct endpoint with requester parameter")
        void acceptFriendRequest_sendsCorrectEndpoint() {
            // Given
            String json = """
                    { "success": true, "message": "OK", "data": null }
                    """;
            when(apiClient.post(anyString(), anyString()))
                    .thenReturn(CompletableFuture.completedFuture(json));

            // When
            friendService.acceptFriendRequest("dave").join();

            // Then
            verify(apiClient).post(eq("/friends/accept?requester=dave"), eq(""));
        }

        @Test
        @DisplayName("Connection error returns error ApiResponse")
        void acceptFriendRequest_connectionError_returnsErrorResponse() {
            // Given
            when(apiClient.post(contains("/friends/accept"), anyString()))
                    .thenReturn(CompletableFuture.failedFuture(
                            new RuntimeException("Timeout")));

            // When
            ApiResponse<String> response = friendService.acceptFriendRequest("alice").join();

            // Then
            assertFalse(response.isSuccess());
            assertEquals("Connection error", response.getMessage());
        }
    }

    // ===== REJECT FRIEND REQUEST =====

    @Nested
    @DisplayName("rejectFriendRequest")
    class RejectFriendRequest {

        @Test
        @DisplayName("Successful reject returns success response")
        void rejectFriendRequest_success() {
            // Given
            String json = """
                    {
                        "success": true,
                        "message": "Friend request rejected",
                        "data": null
                    }
                    """;
            when(apiClient.post(contains("/friends/reject"), anyString()))
                    .thenReturn(CompletableFuture.completedFuture(json));

            // When
            ApiResponse<String> response = friendService.rejectFriendRequest("eve").join();

            // Then
            assertTrue(response.isSuccess());
            assertEquals("Friend request rejected", response.getMessage());
        }

        @Test
        @DisplayName("Sends correct endpoint with requester parameter")
        void rejectFriendRequest_sendsCorrectEndpoint() {
            // Given
            String json = """
                    { "success": true, "message": "OK", "data": null }
                    """;
            when(apiClient.post(anyString(), anyString()))
                    .thenReturn(CompletableFuture.completedFuture(json));

            // When
            friendService.rejectFriendRequest("frank").join();

            // Then
            verify(apiClient).post(eq("/friends/reject?requester=frank"), eq(""));
        }

        @Test
        @DisplayName("Connection error returns error ApiResponse")
        void rejectFriendRequest_connectionError_returnsErrorResponse() {
            // Given
            when(apiClient.post(contains("/friends/reject"), anyString()))
                    .thenReturn(CompletableFuture.failedFuture(
                            new RuntimeException("Server down")));

            // When
            ApiResponse<String> response = friendService.rejectFriendRequest("eve").join();

            // Then
            assertFalse(response.isSuccess());
            assertEquals("Connection error", response.getMessage());
        }
    }
}