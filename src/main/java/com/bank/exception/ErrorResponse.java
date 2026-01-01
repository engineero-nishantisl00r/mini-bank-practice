package com.bank.exception;

public class ErrorResponse {

    public String errorCode;
    public String message;
    public String timestamp;

    public ErrorResponse(String errorCode, String message) {
        this.errorCode = errorCode;
        this.message = message;
        this.timestamp = java.time.LocalDateTime.now().toString();
    }
}
