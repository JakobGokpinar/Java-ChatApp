package com.chatapp.backend.exception;

public class ResourceNotFoundException extends ChatAppException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}