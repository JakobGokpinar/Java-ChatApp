package com.chatapp.backend.exception;

public class DuplicateResourceException extends ChatAppException {

    public DuplicateResourceException(String message) {
        super(message);
    }
}