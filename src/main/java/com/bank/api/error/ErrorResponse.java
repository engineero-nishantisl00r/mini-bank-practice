package com.bank.api.error;

public class ErrorResponse {

    public String timestamp;
    public String errorCode;
    public String message;

    public ErrorResponse(String errorCode, String message) {
        this.timestamp = java.time.LocalDateTime.now().toString();
        this.errorCode = errorCode;
        this.message = message;
    }
}
