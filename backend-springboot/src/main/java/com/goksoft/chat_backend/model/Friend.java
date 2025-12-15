package com.goksoft.chat_backend.model;

import jakarta.persistence.*;

@Entity
@Table(name = "friends")
public class Friend {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "person1", length = 25)
    private String person1;

    @Column(name = "person2", length = 25)
    private String person2;

    // Constructors
    public Friend() {}

    public Friend(String person1, String person2) {
        this.person1 = person1;
        this.person2 = person2;
    }

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getPerson1() {
        return person1;
    }

    public void setPerson1(String person1) {
        this.person1 = person1;
    }

    public String getPerson2() {
        return person2;
    }

    public void setPerson2(String person2) {
        this.person2 = person2;
    }
}