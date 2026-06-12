package com.synapse.engagement.shared;

public class ConflictException extends BusinessException {
    public ConflictException(String message) {
        super("CONFLICT", 409, message);
    }
}
