package com.synapse.engagement.shared;

public class BadRequestException extends BusinessException {
    public BadRequestException(String message) {
        super("BAD_REQUEST", 400, message);
    }
}
