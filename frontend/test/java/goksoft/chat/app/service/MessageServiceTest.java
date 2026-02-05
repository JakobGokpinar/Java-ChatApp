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

@DisplayName("MessageService Tests")
@ExtendWith(MockitoExtension.class)
class MessageServiceTest {

    @Mock
    private ApiClient apiClient;

    private MessageService messageService;

    @BeforeEach
    void setUp() {
        messageService = new MessageService(apiClient);
    }

    // ===== GET MESSAGES =====

    @Nested
    @DisplayName("getMessages")
    class GetMessages {

        @Test
        @DisplayName("Returns parsed message list between two users")
        void getMessages_success_returnsMessageList() {
            // Given - backend returns List<List<String>> [[sender, message], ...]
            String json = """
                    [
                        ["alice", "Hey there!"],
                        ["bob", "Hi Alice!"],
                        ["alice", "How are you?"]
                    ]
                    """;
            when(apiClient.post(contains("/messages/get"), anyString()))
                    .thenReturn(CompletableFuture.completedFuture(json));

            // When
            List<List<String>> messages = messageService.getMessages("alice").join();

            // Then
            assertEquals(3, messages.size());
            assertEquals("alice", messages.get(0).get(0));
            assertEquals("Hey there!", messages.get(0).get(1));
            assertEquals("bob", messages.get(1).get(0));
            assertEquals("Hi Alice!", messages.get(1).get(1));
            assertEquals("alice", messages.get(2).get(0));
            assertEquals("How are you?", messages.get(2).get(1));
        }

        @Test
        @DisplayName("Returns empty list when no messages exist")
        void getMessages_noMessages_returnsEmptyList() {
            // Given
            String json = "[]";
            when(apiClient.post(contains("/messages/get"), anyString()))
                    .thenReturn(CompletableFuture.completedFuture(json));

            // When
            List<List<String>> messages = messageService.getMessages("alice").join();

            // Then
            assertTrue(messages.isEmpty());
        }

        @Test
        @DisplayName("Sends correct endpoint with receiver parameter")
        void getMessages_sendsCorrectEndpoint() {
            // Given
            when(apiClient.post(anyString(), anyString()))
                    .thenReturn(CompletableFuture.completedFuture("[]"));

            // When
            messageService.getMessages("bob").join();

            // Then
            verify(apiClient).post(eq("/messages/get?receiver=bob"), eq(""));
        }

        @Test
        @DisplayName("Connection error returns empty list via exceptionally")
        void getMessages_connectionError_returnsEmptyList() {
            // Given
            when(apiClient.post(contains("/messages/get"), anyString()))
                    .thenReturn(CompletableFuture.failedFuture(
                            new RuntimeException("Network error")));

            // When
            List<List<String>> messages = messageService.getMessages("alice").join();

            // Then
            assertTrue(messages.isEmpty());
        }

        @Test
        @DisplayName("Handles single message correctly")
        void getMessages_singleMessage_returnsSingleElement() {
            // Given
            String json = """
                    [["jakob", "Hello world!"]]
                    """;
            when(apiClient.post(contains("/messages/get"), anyString()))
                    .thenReturn(CompletableFuture.completedFuture(json));

            // When
            List<List<String>> messages = messageService.getMessages("alice").join();

            // Then
            assertEquals(1, messages.size());
            assertEquals("jakob", messages.get(0).get(0));
            assertEquals("Hello world!", messages.get(0).get(1));
        }
    }

    // ===== SEND MESSAGE =====

    @Nested
    @DisplayName("sendMessage")
    class SendMessage {

        @Test
        @DisplayName("Successful send returns success response")
        void sendMessage_success_returnsSuccessResponse() {
            // Given
            String json = """
                    {
                        "success": true,
                        "message": "Message sent",
                        "data": null
                    }
                    """;
            when(apiClient.post(contains("/messages/send"), anyString()))
                    .thenReturn(CompletableFuture.completedFuture(json));

            // When
            ApiResponse<String> response = messageService.sendMessage("alice", "Hello!").join();

            // Then
            assertTrue(response.isSuccess());
            assertEquals("Message sent", response.getMessage());
        }

        @Test
        @DisplayName("Sends correct endpoint with receiver and message parameters")
        void sendMessage_sendsCorrectEndpoint() {
            // Given
            String json = """
                    { "success": true, "message": "OK", "data": null }
                    """;
            when(apiClient.post(anyString(), anyString()))
                    .thenReturn(CompletableFuture.completedFuture(json));

            // When
            messageService.sendMessage("bob", "Hi there").join();

            // Then
            verify(apiClient).post(eq("/messages/send?receiver=bob&message=Hi there"), eq(""));
        }

        @Test
        @DisplayName("Connection error returns error ApiResponse")
        void sendMessage_connectionError_returnsErrorResponse() {
            // Given
            when(apiClient.post(contains("/messages/send"), anyString()))
                    .thenReturn(CompletableFuture.failedFuture(
                            new RuntimeException("Server down")));

            // When
            ApiResponse<String> response = messageService.sendMessage("alice", "test").join();

            // Then
            assertFalse(response.isSuccess());
            assertEquals("Connection error", response.getMessage());
        }

        @Test
        @DisplayName("Failed send returns error response from backend")
        void sendMessage_backendError_returnsErrorResponse() {
            // Given
            String json = """
                    {
                        "success": false,
                        "message": "Receiver not found",
                        "data": null
                    }
                    """;
            when(apiClient.post(contains("/messages/send"), anyString()))
                    .thenReturn(CompletableFuture.completedFuture(json));

            // When
            ApiResponse<String> response = messageService.sendMessage("unknown", "test").join();

            // Then
            assertFalse(response.isSuccess());
            assertEquals("Receiver not found", response.getMessage());
        }
    }

    // ===== CHECK NOTIFICATION =====

    @Nested
    @DisplayName("checkNotification")
    class CheckNotification {

        @Test
        @DisplayName("Returns unread count as integer")
        void checkNotification_returnsUnreadCount() {
            // Given - backend returns plain text number
            when(apiClient.post(contains("/messages/check-notif"), anyString()))
                    .thenReturn(CompletableFuture.completedFuture("5"));

            // When
            int count = messageService.checkNotification("alice").join();

            // Then
            assertEquals(5, count);
        }

        @Test
        @DisplayName("Returns zero when no unread messages")
        void checkNotification_noUnread_returnsZero() {
            // Given
            when(apiClient.post(contains("/messages/check-notif"), anyString()))
                    .thenReturn(CompletableFuture.completedFuture("0"));

            // When
            int count = messageService.checkNotification("alice").join();

            // Then
            assertEquals(0, count);
        }

        @Test
        @DisplayName("Sends correct endpoint with chatter parameter")
        void checkNotification_sendsCorrectEndpoint() {
            // Given
            when(apiClient.post(anyString(), anyString()))
                    .thenReturn(CompletableFuture.completedFuture("0"));

            // When
            messageService.checkNotification("bob").join();

            // Then
            verify(apiClient).post(eq("/messages/check-notif?chatter=bob"), eq(""));
        }

        @Test
        @DisplayName("Handles whitespace in response")
        void checkNotification_whitespace_parsesCorrectly() {
            // Given - response might have trailing whitespace/newline
            when(apiClient.post(contains("/messages/check-notif"), anyString()))
                    .thenReturn(CompletableFuture.completedFuture("  3  \n"));

            // When
            int count = messageService.checkNotification("alice").join();

            // Then
            assertEquals(3, count);
        }

        @Test
        @DisplayName("Invalid number format returns zero gracefully")
        void checkNotification_invalidFormat_returnsZero() {
            // Given - unexpected response format
            when(apiClient.post(contains("/messages/check-notif"), anyString()))
                    .thenReturn(CompletableFuture.completedFuture("not-a-number"));

            // When
            int count = messageService.checkNotification("alice").join();

            // Then
            assertEquals(0, count);
        }

        @Test
        @DisplayName("Connection error returns zero via exceptionally")
        void checkNotification_connectionError_returnsZero() {
            // Given
            when(apiClient.post(contains("/messages/check-notif"), anyString()))
                    .thenReturn(CompletableFuture.failedFuture(
                            new RuntimeException("Timeout")));

            // When
            int count = messageService.checkNotification("alice").join();

            // Then
            assertEquals(0, count);
        }

        @Test
        @DisplayName("Large notification count is parsed correctly")
        void checkNotification_largeCount_parsesCorrectly() {
            // Given
            when(apiClient.post(contains("/messages/check-notif"), anyString()))
                    .thenReturn(CompletableFuture.completedFuture("999"));

            // When
            int count = messageService.checkNotification("alice").join();

            // Then
            assertEquals(999, count);
        }
    }
}