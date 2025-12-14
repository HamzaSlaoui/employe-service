package com.emsi.employe_service.exception;

public class DuplicateResourceException extends RuntimeException {
    private String message;

    public DuplicateResourceException(String message) {
        super(message);
    }
}