package com.chatapp.backend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "messagetable")
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "sender", length = 25)
    private String sender;

    @Column(name = "receiver", length = 25)
    private String receiver;

    @Column(name = "msg", length = 255)
    private String msg;

    @Column(name = "history")
    private LocalDateTime history;

    // Constructors
    public Message() {}

    public Message(String sender, String receiver, String msg) {
        this.sender = sender;
        this.receiver = receiver;
        this.msg = msg;
        this.history = LocalDateTime.now();
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

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public LocalDateTime getHistory() {
        return history;
    }

    public void setHistory(LocalDateTime history) {
        this.history = history;
    }
}