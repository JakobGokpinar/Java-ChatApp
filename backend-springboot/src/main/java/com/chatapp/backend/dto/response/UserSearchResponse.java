package com.chatapp.backend.dto.response;

import java.util.List;

public class UserSearchResponse {
    private List<String> users;

    public UserSearchResponse(List<String> users) {
        this.users = users;
    }

    public List<String> getUsers() {
        return users;
    }

    public void setUsers(List<String> users) {
        this.users = users;
    }
}