package com.chatapp.backend.repository;

import com.chatapp.backend.model.Friendship;
import com.chatapp.backend.model.Friendship.FriendshipStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FriendshipRepository extends JpaRepository<Friendship, Integer> {

    // Find all accepted friendships for a user
    @Query("SELECT f FROM Friendship f WHERE " +
            "(f.user1 = :username OR f.user2 = :username) " +
            "AND f.status = 'ACCEPTED'")
    List<Friendship> findAcceptedFriendships(@Param("username") String username);

    // Find pending friend requests received by a user (initiated by someone else)
    @Query("SELECT f FROM Friendship f WHERE " +
            "(f.user1 = :username OR f.user2 = :username) " +
            "AND f.status = 'PENDING' " +
            "AND f.initiatedBy != :username")
    List<Friendship> findPendingRequests(@Param("username") String username);

    // Find friendship between two users (regardless of order)
    @Query("SELECT f FROM Friendship f WHERE " +
            "((f.user1 = :user1 AND f.user2 = :user2) OR " +
            "(f.user1 = :user2 AND f.user2 = :user1))")
    Optional<Friendship> findByUsers(@Param("user1") String user1,
                                     @Param("user2") String user2);

    // Check if two users are already friends (ACCEPTED status)
    @Query("SELECT CASE WHEN COUNT(f) > 0 THEN true ELSE false END FROM Friendship f WHERE " +
            "((f.user1 = :user1 AND f.user2 = :user2) OR " +
            "(f.user1 = :user2 AND f.user2 = :user1)) " +
            "AND f.status = 'ACCEPTED'")
    boolean areFriends(@Param("user1") String user1, @Param("user2") String user2);

    // Check if friendship exists with specific status
    @Query("SELECT CASE WHEN COUNT(f) > 0 THEN true ELSE false END FROM Friendship f WHERE " +
            "((f.user1 = :user1 AND f.user2 = :user2) OR " +
            "(f.user1 = :user2 AND f.user2 = :user1)) " +
            "AND f.status = :status")
    boolean existsByUsersAndStatus(@Param("user1") String user1,
                                   @Param("user2") String user2,
                                   @Param("status") FriendshipStatus status);
}