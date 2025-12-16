package com.chatapp.backend.repository;

import com.chatapp.backend.model.FriendRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FriendRequestRepository extends JpaRepository<FriendRequest, Integer> {

    // Get all friend requests for a user (where they are the receiver)
    List<FriendRequest> findByReceiver(String receiver);

    // Delete request between two users (for accept/reject)
    void deleteBySenderAndReceiver(String sender, String receiver);

    // Check if request exists
    boolean existsBySenderAndReceiver(String sender, String receiver);
}