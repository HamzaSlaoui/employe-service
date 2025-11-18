package com.emsi.employe_service.exception;

public class EmployeNotFoundException extends RuntimeException {

    public EmployeNotFoundException(String message) {
        super(message);
    }
}
