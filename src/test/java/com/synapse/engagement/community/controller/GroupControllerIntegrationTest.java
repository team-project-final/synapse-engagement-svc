package com.synapse.engagement.community.controller;

import com.synapse.engagement.community.dto.request.GroupCreateRequest;
import com.synapse.engagement.community.dto.request.GroupUpdateRequest;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import com.synapse.engagement.support.TestJwt;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
@Testcontainers(disabledWithoutDocker = true)
class GroupControllerIntegrationTest {

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
    @DisplayName("createGroup_정상요청_should201과그룹정보반환")
    void createGroup_정상요청_should201과그룹정보반환() {
        // Given
        UUID ownerId = UUID.randomUUID();
        GroupCreateRequest request = new GroupCreateRequest("Spring Study", "Java 21 study group", true);

        // When
        ResponseEntity<String> response = rest.postForEntity(
                "/api/v1/groups",
                requestEntity(ownerId, request),
                String.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getHeaders().getLocation().toString()).startsWith("/api/v1/groups/");
        assertThat(response.getBody()).contains("\"name\":\"Spring Study\"");
        assertThat(response.getBody()).contains("\"description\":\"Java 21 study group\"");
        assertThat(response.getBody()).contains("\"isPublic\":true");
        assertThat(response.getBody()).contains("\"ownerId\":\"" + ownerId + "\"");
    }

    @Test
    @DisplayName("listGroups_생성된그룹_should커서페이지응답")
    void listGroups_생성된그룹_should커서페이지응답() {
        // Given
        createGroup(UUID.randomUUID(), "First Group");
        createGroup(UUID.randomUUID(), "Second Group");

        // When
        ResponseEntity<String> response = rest.exchange(
                "/api/v1/groups?size=1",
                HttpMethod.GET,
                requestEntity(UUID.randomUUID(), null),
                String.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("\"items\":[{");
        assertThat(response.getBody()).contains("\"hasNext\":true");
        assertThat(response.getBody()).contains("\"nextCursor\":\"");
    }

    @Test
    @DisplayName("updateGroup_소유자요청_should그룹정보수정")
    void updateGroup_소유자요청_should그룹정보수정() {
        // Given
        UUID ownerId = UUID.randomUUID();
        UUID groupId = createGroup(ownerId, "Before");
        GroupUpdateRequest request = new GroupUpdateRequest("After", "Updated", false);

        // When
        ResponseEntity<String> response = rest.exchange(
                "/api/v1/groups/{groupId}",
                HttpMethod.PUT,
                requestEntity(ownerId, request),
                String.class,
                groupId);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("\"name\":\"After\"");
        assertThat(response.getBody()).contains("\"description\":\"Updated\"");
        assertThat(response.getBody()).contains("\"isPublic\":false");
    }

    @Test
    @DisplayName("updateGroup_비소유자요청_should403ProblemDetail")
    void updateGroup_비소유자요청_should403ProblemDetail() {
        // Given
        UUID groupId = createGroup(UUID.randomUUID(), "Owner Only");
        GroupUpdateRequest request = new GroupUpdateRequest("Blocked", null, true);

        // When
        ResponseEntity<String> response = rest.exchange(
                "/api/v1/groups/{groupId}",
                HttpMethod.PUT,
                requestEntity(UUID.randomUUID(), request),
                String.class,
                groupId);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).contains("\"code\":\"ENGM-002\"");
    }

    @Test
    @DisplayName("deleteGroup_소유자요청_shouldSoftDelete후조회불가")
    void deleteGroup_소유자요청_shouldSoftDelete후조회불가() {
        // Given
        UUID ownerId = UUID.randomUUID();
        UUID groupId = createGroup(ownerId, "Delete Me");

        // When
        ResponseEntity<Void> deleteResponse = rest.exchange(
                "/api/v1/groups/{groupId}",
                HttpMethod.DELETE,
                requestEntity(ownerId, null),
                Void.class,
                groupId);

        // Then
        assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        ResponseEntity<String> getResponse = rest.exchange(
                "/api/v1/groups/{groupId}",
                HttpMethod.GET,
                requestEntity(ownerId, null),
                String.class,
                groupId);
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(getResponse.getBody()).contains("\"code\":\"ENGM-001\"");
    }

    @Test
    @DisplayName("createGroup_인증헤더없음_should401ProblemDetail")
    void createGroup_인증헤더없음_should401ProblemDetail() {
        // Given
        GroupCreateRequest request = new GroupCreateRequest("No User", null, true);

        // When
        ResponseEntity<String> response = rest.postForEntity("/api/v1/groups", jsonEntity(request), String.class);

        // Then
        // JWT 보안 적용 후: 토큰 없으면 Spring Security가 401을 반환(커스텀 본문 없음)
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("createGroup_사용자그룹10개초과_should400ProblemDetail")
    void createGroup_사용자그룹10개초과_should400ProblemDetail() {
        // Given
        UUID ownerId = UUID.randomUUID();
        for (int index = 0; index < 10; index++) {
            createGroup(ownerId, "Group " + index);
        }

        // When
        ResponseEntity<String> response = rest.postForEntity(
                "/api/v1/groups",
                requestEntity(ownerId, new GroupCreateRequest("Too Many", null, true)),
                String.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).contains("\"code\":\"ENGM-003\"");
    }

    private UUID createGroup(UUID ownerId, String name) {
        ResponseEntity<String> response = rest.postForEntity(
                "/api/v1/groups",
                requestEntity(ownerId, new GroupCreateRequest(name, null, true)),
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        Matcher matcher = ID_PATTERN.matcher(response.getBody());
        assertThat(matcher.find()).isTrue();
        return UUID.fromString(matcher.group(1));
    }

    private static HttpEntity<Object> requestEntity(UUID userId, Object body) {
        HttpHeaders headers = jsonHeaders();
        headers.set("X-User-Id", userId.toString());
        headers.setBearerAuth(TestJwt.accessToken(userId.toString()));
        return new HttpEntity<>(body, headers);
    }

    private static HttpEntity<Object> jsonEntity(Object body) {
        return new HttpEntity<>(body, jsonHeaders());
    }

    private static HttpHeaders jsonHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }
}

