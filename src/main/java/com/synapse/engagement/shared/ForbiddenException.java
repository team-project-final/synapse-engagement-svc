package com.synapse.engagement.shared;

public class ForbiddenException extends BusinessException {
    public ForbiddenException(String message) {
        super("FORBIDDEN", 403, message);
    }
}
