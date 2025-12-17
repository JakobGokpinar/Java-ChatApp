package com.chatapp.backend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "messages")
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 50)
    private String sender;

    @Column(nullable = false, length = 50)
    private String receiver;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "is_read", nullable = false)
    private Boolean isRead;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public Message() {
        this.isRead = false;
        this.createdAt = LocalDateTime.now();
    }

    public Message(String sender, String receiver, String content) {
        this.sender = sender;
        this.receiver = receiver;
        this.content = content;
        this.isRead = false;
        this.createdAt = LocalDateTime.now();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Boolean getIsRead() {
        return isRead;
    }

    public void setIsRead(Boolean isRead) {
        this.isRead = isRead;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}