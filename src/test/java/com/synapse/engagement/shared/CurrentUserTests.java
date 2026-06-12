package com.synapse.engagement.shared;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CurrentUserTests {
    @Test
    void requireAdminAcceptsAdminRoleClaim() {
        var jwt = jwt("9001")
                .claim("roles", java.util.List.of("ADMIN"))
                .build();

        assertThat(CurrentUser.requireAdmin(jwt)).isEqualTo(9001L);
    }

    @Test
    void requireAdminRejectsScopeThatOnlyContainsAdminAsSubstring() {
        var jwt = jwt("9002")
                .claim("scope", "profile:notadmin")
                .build();

        assertThatThrownBy(() -> CurrentUser.requireAdmin(jwt))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("ADMIN role");
    }

    @Test
    void requireAdminAcceptsAdminScopeToken() {
        var jwt = jwt("9003")
                .claim("scope", "profile:read admin")
                .build();

        assertThat(CurrentUser.requireAdmin(jwt)).isEqualTo(9003L);
    }

    @Test
    void requireResolvesUuidSubjectToDeterministicLongMatchingKafkaPath() {
        // platform이 발급하는 UUID subject도 거부하지 않고, Kafka 소비 경로와 동일한
        // 결정적 해시로 Long을 도출한다(동일 UUID → 동일 userId).
        var subject = "019ea9bc-fc6f-70ce-a2a2-24c9f9c4409e";
        var expected = CurrentUser.resolveUserId(subject);

        assertThat(CurrentUser.require(jwt(subject).build())).isEqualTo(expected);
        assertThat(CurrentUser.require(jwt(subject).build()))
                .isEqualTo(CurrentUser.require(jwt(subject).build()));
    }

    private static Jwt.Builder jwt(String subject) {
        return Jwt.withTokenValue("test-token")
                .header("alg", "none")
                .subject(subject);
    }
}
