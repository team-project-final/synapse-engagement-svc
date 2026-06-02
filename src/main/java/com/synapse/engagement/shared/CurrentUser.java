package com.synapse.engagement.shared;

import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;

public final class CurrentUser {
    private CurrentUser() {
    }

    public static Long require(Jwt jwt) {
        // Synapse 서비스들은 JWT subject를 공통 userId로 사용한다. 여기서 숫자 변환까지 통일한다.
        if (jwt == null || jwt.getSubject() == null || jwt.getSubject().isBlank()) {
            throw new UnauthorizedException("JWT subject is required");
        }
        try {
            return Long.valueOf(jwt.getSubject());
        } catch (NumberFormatException ex) {
            throw new UnauthorizedException("JWT subject must be a numeric user id");
        }
    }

    public static Long requireAdmin(Jwt jwt) {
        var userId = require(jwt);
        // Step 8 관리자 API는 Spring Security URL rule만으로 끝내지 않고,
        // 컨트롤러 경계에서 JWT role claim을 직접 확인해 테스트와 운영 정책을 맞춘다.
        if (!hasAdminRole(jwt)) {
            throw new ForbiddenException("ADMIN role is required");
        }
        return userId;
    }

    private static boolean hasAdminRole(Jwt jwt) {
        Object roles = jwt.getClaims().get("roles");
        if (roles instanceof Collection<?> collection && collection.stream()
                .map(Objects::toString)
                .anyMatch("ADMIN"::equalsIgnoreCase)) {
            return true;
        }
        Object role = jwt.getClaims().get("role");
        if (role != null && "ADMIN".equalsIgnoreCase(role.toString())) {
            return true;
        }
        String scope = jwt.getClaimAsString("scope");
        return scope != null && Arrays.stream(scope.split("\\s+"))
                .anyMatch("admin"::equalsIgnoreCase);
    }
}
