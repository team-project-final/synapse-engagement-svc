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

    private static Jwt.Builder jwt(String subject) {
        return Jwt.withTokenValue("test-token")
                .header("alg", "none")
                .subject(subject);
    }
}
