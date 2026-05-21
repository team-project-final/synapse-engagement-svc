package com.synapse.engagement.global.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
    COM_INVALID_REQUEST("COM_001", HttpStatus.BAD_REQUEST, "Invalid request."),
    COM_INTERNAL_SERVER_ERROR("COM_999", HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error."),
    GAM_INVALID_XP_REQUEST("GAM_001", HttpStatus.BAD_REQUEST, "Invalid gamification request.");

    private final String code;
    private final HttpStatus status;
    private final String message;

    ErrorCode(String code, HttpStatus status, String message) {
        this.code = code;
        this.status = status;
        this.message = message;
    }

    public String code() {
        return code;
    }

    public HttpStatus status() {
        return status;
    }

    public String message() {
        return message;
    }
}
