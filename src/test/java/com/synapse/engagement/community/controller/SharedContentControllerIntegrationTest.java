package com.synapse.engagement.community.controller;

import com.synapse.engagement.community.dto.request.ShareContentRequest;
import com.synapse.engagement.community.entity.ContentType;
import com.synapse.engagement.support.TestJwt;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
@Testcontainers(disabledWithoutDocker = true)
class SharedContentControllerIntegrationTest {

    private static final Pattern TOKEN_PATTERN = Pattern.compile("\"shareToken\"\\s*:\\s*\"([^\"]+)\"");
    private static final Pattern ID_PATTERN = Pattern.compile("\"id\"\\s*:\\s*\"([^\"]+)\"");

    @Container
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void databaseProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private TestRestTemplate rest;

    @Test
    @DisplayName("shareAndFind_정상요청_should토큰으로공개조회")
    void shareAndFind_정상요청_should토큰으로공개조회() {
        String token = share(UUID.randomUUID(), "Spring Deck");

        ResponseEntity<String> response = rest.getForEntity(
                "/api/v1/community/share/{token}",
                String.class,
                token);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("\"shareToken\":\"" + token + "\"");
        assertThat(response.getBody()).contains("\"title\":\"Spring Deck\"");
    }

    @Test
    @DisplayName("search_키워드_should공유콘텐츠목록반환")
    void search_키워드_should공유콘텐츠목록반환() {
        share(UUID.randomUUID(), "Java Search Deck");

        ResponseEntity<String> response = rest.getForEntity(
                "/api/v1/community/search?q=Search&contentType=DECK",
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("\"title\":\"Java Search Deck\"");
    }

    @Test
    @DisplayName("fork_공유토큰_should현재사용자소유복사본생성")
    void fork_공유토큰_should현재사용자소유복사본생성() {
        String token = share(UUID.randomUUID(), "Fork Me");
        UUID currentUserId = UUID.randomUUID();

        ResponseEntity<String> response = rest.postForEntity(
                "/api/v1/community/share/{token}/fork",
                requestEntity(currentUserId, null),
                String.class,
                token);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("\"ownerId\":\"" + currentUserId + "\"");
        assertThat(response.getBody()).contains("\"title\":\"Fork Me\"");
    }

    @Test
    @DisplayName("delete_비소유자요청_should403ProblemDetail")
    void delete_비소유자요청_should403ProblemDetail() {
        String token = share(UUID.randomUUID(), "Owner Only");
        UUID sharedContentId = findSharedContentId(token);

        ResponseEntity<String> response = rest.exchange(
                "/api/v1/community/share/{id}",
                HttpMethod.DELETE,
                requestEntity(UUID.randomUUID(), null),
                String.class,
                sharedContentId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).contains("\"code\":\"ENGM-302\"");
    }

    private String share(UUID ownerId, String title) {
        ShareContentRequest request = new ShareContentRequest(
                ContentType.DECK,
                UUID.randomUUID(),
                title,
                "Description",
                java.util.List.of("spring", "java"));

        ResponseEntity<String> response = rest.postForEntity(
                "/api/v1/community/share",
                requestEntity(ownerId, request),
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        Matcher matcher = TOKEN_PATTERN.matcher(response.getBody());
        assertThat(matcher.find()).isTrue();
        return matcher.group(1);
    }

    private UUID findSharedContentId(String token) {
        ResponseEntity<String> response = rest.getForEntity(
                "/api/v1/community/share/{token}",
                String.class,
                token);
        Matcher matcher = ID_PATTERN.matcher(response.getBody());
        assertThat(matcher.find()).isTrue();
        return UUID.fromString(matcher.group(1));
    }

    private static HttpEntity<Object> requestEntity(UUID userId, Object body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-User-Id", userId.toString());
        headers.setBearerAuth(TestJwt.accessToken(userId.toString()));
        return new HttpEntity<>(body, headers);
    }
}

