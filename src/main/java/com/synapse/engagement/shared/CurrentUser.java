package com.synapse.engagement.shared;

import org.springframework.security.oauth2.jwt.Jwt;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.UUID;

public final class CurrentUser {
    private CurrentUser() {
    }

    public static Long require(Jwt jwt) {
        // Synapse 서비스들은 JWT subject를 공통 userId로 사용한다.
        // 발급자(platform)는 subject를 UUID 문자열로 넣으므로, 숫자가 아니면 Kafka 소비 경로
        // (EngagementKafkaEventHandler.resolveUserId)와 '동일한' 결정적 해시로 Long을 도출해
        // HTTP/Kafka 두 경로가 동일 사용자에 대해 같은 userId를 갖도록 통일한다.
        if (jwt == null || jwt.getSubject() == null || jwt.getSubject().isBlank()) {
            throw new UnauthorizedException("JWT subject is required");
        }
        return resolveUserId(jwt.getSubject());
    }

    /**
     * JWT subject(platform UUID 문자열)를 그대로 반환한다.
     * 내부 PK(require)는 해시 Long을 쓰지만, outbound 이벤트에는 이 원본 subject(UUID)를 실어야
     * platform NotificationService의 UUID.fromString(userId)가 성공한다(F10).
     */
    public static String subject(Jwt jwt) {
        if (jwt == null || jwt.getSubject() == null || jwt.getSubject().isBlank()) {
            throw new UnauthorizedException("JWT subject is required");
        }
        return jwt.getSubject();
    }

    /**
     * 외부 userId(JWT subject 또는 이벤트 필드)를 내부 Long userId로 변환한다.
     * 숫자면 그대로, 아니면(UUID 등) nameUUIDFromBytes 기반 결정적 해시.
     * HTTP·Kafka 양 경로가 이 메서드를 공유해 신원 도출이 분기되지 않도록 한다.
     */
    public static Long resolveUserId(String externalUserId) {
        var value = externalUserId == null ? "" : externalUserId;
        try {
            return Long.valueOf(value);
        } catch (NumberFormatException ex) {
            var uuid = UUID.nameUUIDFromBytes(value.getBytes(StandardCharsets.UTF_8));
            return uuid.getMostSignificantBits() & Long.MAX_VALUE;
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
