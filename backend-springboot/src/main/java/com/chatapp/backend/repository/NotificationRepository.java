package com.chatapp.backend.repository;

import com.chatapp.backend.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Integer> {

    // Find notification by sender and receiver
    Optional<Notification> findBySenderAndReceiver(String sender, String receiver);

    // Check if notification exists
    boolean existsBySenderAndReceiver(String sender, String receiver);
}