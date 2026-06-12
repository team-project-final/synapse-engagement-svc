package com.synapse.engagement.gamification.controller;

import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
@Testcontainers(disabledWithoutDocker = true)
class GamificationControllerIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @SuppressWarnings("resource")
    @Container
    static final GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine")
            .withExposedPorts(6379);

    @DynamicPropertySource
    static void containerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
    }

    @Autowired
    private TestRestTemplate rest;

    @Test
    @DisplayName("getProfile_신규사용자_should기본프로필반환")
    void getProfile_신규사용자_should기본프로필반환() {
        UUID userId = UUID.randomUUID();

        ResponseEntity<String> response = rest.exchange(
                "/api/v1/gamification/profile",
                HttpMethod.GET,
                requestEntity(userId),
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("\"userId\":\"" + userId + "\"");
        assertThat(response.getBody()).contains("\"level\":1");
        assertThat(response.getBody()).contains("\"totalXp\":0");
        assertThat(response.getBody()).contains("\"nextLevelXp\":100");
        assertThat(response.getBody()).contains("\"recentBadges\":[]");
    }

    @Test
    @DisplayName("getXpHistory_신규사용자_should빈목록반환")
    void getXpHistory_신규사용자_should빈목록반환() {
        ResponseEntity<String> response = rest.exchange(
                "/api/v1/gamification/xp/history",
                HttpMethod.GET,
                requestEntity(UUID.randomUUID()),
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo("[]");
    }

    @Test
    @DisplayName("getLeaderboard_빈상태_should빈목록반환")
    void getLeaderboard_빈상태_should빈목록반환() {
        ResponseEntity<String> response = rest.exchange(
                "/api/v1/gamification/leaderboard",
                HttpMethod.GET,
                requestEntity(null),
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo("[]");
    }

    private static HttpEntity<Object> requestEntity(UUID userId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (userId != null) {
            headers.set("X-User-Id", userId.toString());
        }
        return new HttpEntity<>(null, headers);
    }
}
