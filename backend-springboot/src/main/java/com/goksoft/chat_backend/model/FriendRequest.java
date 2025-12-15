package com.goksoft.chat_backend.model;

import jakarta.persistence.*;

@Entity
@Table(name = "requeststable")
public class FriendRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "sender", length = 25)
    private String sender;

    @Column(name = "receiver", length = 25)
    private String receiver;

    // Constructors
    public FriendRequest() {}

    public FriendRequest(String sender, String receiver) {
        this.sender = sender;
        this.receiver = receiver;
    }

    // Getters and Setters
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
}