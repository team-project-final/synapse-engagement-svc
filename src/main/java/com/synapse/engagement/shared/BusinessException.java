package com.synapse.engagement.shared;

public class BusinessException extends RuntimeException {
    private final String code;
    private final int status;

    public BusinessException(String code, int status, String message) {
        super(message);
        this.code = code;
        this.status = status;
    }

    public String code() {
        return code;
    }

    public int status() {
        return status;
    }
}
