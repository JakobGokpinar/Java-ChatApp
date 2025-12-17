package com.chatapp.backend.exception;

public class ChatAppException extends RuntimeException {

    public ChatAppException(String message) {
        super(message);
    }

    public ChatAppException(String message, Throwable cause) {
        super(message, cause);
    }
}