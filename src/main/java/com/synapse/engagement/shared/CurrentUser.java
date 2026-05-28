package com.synapse.engagement.shared;

import org.springframework.security.oauth2.jwt.Jwt;

public final class CurrentUser {
    private CurrentUser() {
    }

    public static Long require(Jwt jwt) {
        if (jwt == null || jwt.getSubject() == null || jwt.getSubject().isBlank()) {
            throw new UnauthorizedException("JWT subject is required");
        }
        try {
            return Long.valueOf(jwt.getSubject());
        } catch (NumberFormatException ex) {
            throw new UnauthorizedException("JWT subject must be a numeric user id");
        }
    }
}
