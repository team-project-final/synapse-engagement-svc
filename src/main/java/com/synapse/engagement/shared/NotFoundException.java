package com.synapse.engagement.shared;

public class NotFoundException extends BusinessException {
    public NotFoundException(String message) {
        super("NOT_FOUND", 404, message);
    }
}
