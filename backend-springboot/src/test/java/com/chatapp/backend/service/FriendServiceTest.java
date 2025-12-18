package com.chatapp.backend.service;

import com.chatapp.backend.dto.response.ApiResponse;
import com.chatapp.backend.exception.DuplicateResourceException;
import com.chatapp.backend.exception.ResourceNotFoundException;
import com.chatapp.backend.exception.ValidationException;
import com.chatapp.backend.model.Friendship;
import com.chatapp.backend.model.Friendship.FriendshipStatus;
import com.chatapp.backend.repository.FriendshipRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FriendServiceTest {

    @Mock
    private FriendshipRepository friendshipRepository;

    @InjectMocks
    private FriendService friendService;

    @Test
    void getFriends_ReturnsAcceptedFriendsList() {
        // Given
        Friendship f1 = new Friendship("alice", "bob", "alice");
        f1.setStatus(FriendshipStatus.ACCEPTED);

        Friendship f2 = new Friendship("charlie", "alice", "charlie");
        f2.setStatus(FriendshipStatus.ACCEPTED);

        when(friendshipRepository.findAcceptedFriendships("alice"))
                .thenReturn(Arrays.asList(f1, f2));

        // When
        ApiResponse<List<String>> response = friendService.getFriends("alice");

        // Then
        assertThat(response.success()).isTrue();
        assertThat(response.data()).containsExactlyInAnyOrder("bob", "charlie");
    }

    @Test
    void sendFriendRequest_WithValidUsers_CreatesRequest() {
        // Given
        when(friendshipRepository.areFriends("alice", "bob")).thenReturn(false);
        when(friendshipRepository.existsByUsersAndStatus("alice", "bob", FriendshipStatus.PENDING))
                .thenReturn(false);
        when(friendshipRepository.save(any(Friendship.class))).thenAnswer(i -> i.getArguments()[0]);

        // When
        ApiResponse<String> response = friendService.sendFriendRequest("alice", "bob");

        // Then
        assertThat(response.success()).isTrue();
        verify(friendshipRepository).save(any(Friendship.class));
    }

    @Test
    void sendFriendRequest_WhenAlreadyFriends_ThrowsDuplicateResourceException() {
        // Given
        when(friendshipRepository.areFriends("alice", "bob")).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> friendService.sendFriendRequest("alice", "bob"))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessage("You are already friends");
    }

    @Test
    void sendFriendRequest_ToSelf_ThrowsValidationException() {
        // When & Then
        assertThatThrownBy(() -> friendService.sendFriendRequest("alice", "alice"))
                .isInstanceOf(ValidationException.class)
                .hasMessage("Cannot send friend request to yourself");
    }

    @Test
    void acceptFriendRequest_WithValidRequest_UpdatesStatusToAccepted() {
        // Given
        Friendship friendship = new Friendship("alice", "bob", "alice");
        friendship.setStatus(FriendshipStatus.PENDING);

        when(friendshipRepository.findByUsers("bob", "alice")).thenReturn(Optional.of(friendship));
        when(friendshipRepository.save(any(Friendship.class))).thenAnswer(i -> i.getArguments()[0]);

        // When
        ApiResponse<String> response = friendService.acceptFriendRequest("bob", "alice");

        // Then
        assertThat(response.success()).isTrue();
        assertThat(friendship.getStatus()).isEqualTo(FriendshipStatus.ACCEPTED);
        verify(friendshipRepository).save(friendship);
    }

    @Test
    void acceptFriendRequest_WhenNotFound_ThrowsResourceNotFoundException() {
        // Given
        when(friendshipRepository.findByUsers("bob", "alice")).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> friendService.acceptFriendRequest("bob", "alice"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Friend request not found");
    }
}