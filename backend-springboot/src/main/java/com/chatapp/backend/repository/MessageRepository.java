package com.chatapp.backend.repository;

import com.chatapp.backend.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Integer> {

    // Get all messages between two users (in both directions)
    @Query("SELECT m FROM Message m WHERE " +
            "(m.sender = :user1 AND m.receiver = :user2) OR " +
            "(m.sender = :user2 AND m.receiver = :user1) " +
            "ORDER BY m.createdAt ASC")
    List<Message> findMessagesBetweenUsers(@Param("user1") String user1,
                                           @Param("user2") String user2);

    // Count unread messages from a specific sender to receiver
    @Query("SELECT COUNT(m) FROM Message m WHERE " +
            "m.receiver = :receiver AND m.sender = :sender AND m.isRead = false")
    int countUnreadMessages(@Param("receiver") String receiver,
                            @Param("sender") String sender);

    // Mark all messages from sender to receiver as read
    @Modifying
    @Query("UPDATE Message m SET m.isRead = true WHERE " +
            "m.receiver = :receiver AND m.sender = :sender AND m.isRead = false")
    void markMessagesAsRead(@Param("receiver") String receiver,
                            @Param("sender") String sender);
}