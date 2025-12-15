package com.goksoft.chat_backend.model;

import jakarta.persistence.*;

@Entity
@Table(name = "notiftable")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "sender", length = 25)
    private String sender;

    @Column(name = "receiver", length = 25)
    private String receiver;

    @Column(name = "counts")
    private Integer counts;

    // Constructors
    public Notification() {}

    public Notification(String sender, String receiver, Integer counts) {
        this.sender = sender;
        this.receiver = receiver;
        this.counts = counts;
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

    public Integer getCounts() {
        return counts;
    }

    public void setCounts(Integer counts) {
        this.counts = counts;
    }
}