package com.chatapp.backend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "friendships")
public class Friendship {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 50)
    private String user1;

    @Column(nullable = false, length = 50)
    private String user2;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FriendshipStatus status;

    @Column(name = "initiated_by", nullable = false, length = 50)
    private String initiatedBy;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Enum for status
    public enum FriendshipStatus {
        PENDING, ACCEPTED, REJECTED
    }

    // Constructors
    public Friendship() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.status = FriendshipStatus.PENDING;
    }

    public Friendship(String user1, String user2, String initiatedBy) {
        this.user1 = user1;
        this.user2 = user2;
        this.initiatedBy = initiatedBy;
        this.status = FriendshipStatus.PENDING;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUser1() {
        return user1;
    }

    public void setUser1(String user1) {
        this.user1 = user1;
    }

    public String getUser2() {
        return user2;
    }

    public void setUser2(String user2) {
        this.user2 = user2;
    }

    public FriendshipStatus getStatus() {
        return status;
    }

    public void setStatus(FriendshipStatus status) {
        this.status = status;
        this.updatedAt = LocalDateTime.now();
    }

    public String getInitiatedBy() {
        return initiatedBy;
    }

    public void setInitiatedBy(String initiatedBy) {
        this.initiatedBy = initiatedBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}