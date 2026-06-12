package com.synapse.engagement.shared;

public class UnauthorizedException extends BusinessException {
    public UnauthorizedException(String message) {
        super("UNAUTHORIZED", 401, message);
    }
}
