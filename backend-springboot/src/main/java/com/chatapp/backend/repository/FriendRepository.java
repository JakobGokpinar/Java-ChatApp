package com.chatapp.backend.repository;

import com.chatapp.backend.model.Friend;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FriendRepository extends JpaRepository<Friend, Integer> {

    // Get all friends of a user (where user is either person1 or person2)
    @Query("SELECT f FROM Friend f WHERE f.person1 = :username OR f.person2 = :username")
    List<Friend> findFriendsByUsername(@Param("username") String username);

    // Check if friendship exists (in either direction)
    @Query("SELECT CASE WHEN COUNT(f) > 0 THEN true ELSE false END FROM Friend f " +
            "WHERE (f.person1 = :user1 AND f.person2 = :user2) OR (f.person1 = :user2 AND f.person2 = :user1)")
    boolean areFriends(@Param("user1") String user1, @Param("user2") String user2);
}