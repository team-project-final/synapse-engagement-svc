package com.synapse.engagement.community.controller;

import com.synapse.engagement.community.dto.request.GroupCreateRequest;
import com.synapse.engagement.community.dto.request.GroupMemberInviteRequest;
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
class GroupMemberControllerIntegrationTest {

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
    @DisplayName("join_공개그룹_should즉시ACTIVE")
    void join_공개그룹_should즉시ACTIVE() {
        UUID ownerId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID groupId = createGroup(ownerId, "Public Group", true);

        ResponseEntity<String> response = rest.postForEntity(
                "/api/v1/groups/{groupId}/members/join",
                requestEntity(userId, null),
                String.class,
                groupId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("\"userId\":\"" + userId + "\"");
        assertThat(response.getBody()).contains("\"role\":\"MEMBER\"");
        assertThat(response.getBody()).contains("\"status\":\"ACTIVE\"");
    }

    @Test
    @DisplayName("approve_비공개그룹가입신청_shouldACTIVE전환")
    void approve_비공개그룹가입신청_shouldACTIVE전환() {
        UUID ownerId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID groupId = createGroup(ownerId, "Private Group", false);
        UUID memberId = join(groupId, userId);

        ResponseEntity<String> response = rest.exchange(
                "/api/v1/groups/{groupId}/members/{memberId}/approve",
                HttpMethod.PUT,
                requestEntity(ownerId, null),
                String.class,
                groupId,
                memberId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("\"status\":\"ACTIVE\"");
    }

    @Test
    @DisplayName("invite_소유자요청_should초대멤버ACTIVE")
    void invite_소유자요청_should초대멤버ACTIVE() {
        UUID ownerId = UUID.randomUUID();
        UUID targetUserId = UUID.randomUUID();
        UUID groupId = createGroup(ownerId, "Invite Test", false);
        GroupMemberInviteRequest request = new GroupMemberInviteRequest(targetUserId);

        ResponseEntity<String> response = rest.postForEntity(
                "/api/v1/groups/{groupId}/members/invite",
                requestEntity(ownerId, request),
                String.class,
                groupId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("\"userId\":\"" + targetUserId + "\"");
        assertThat(response.getBody()).contains("\"role\":\"MEMBER\"");
        assertThat(response.getBody()).contains("\"status\":\"ACTIVE\"");
    }

    @Test
    @DisplayName("invite_일반멤버요청_should403")
    void invite_일반멤버요청_should403() {
        UUID ownerId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID targetUserId = UUID.randomUUID();
        UUID groupId = createGroup(ownerId, "Invite Forbidden Test", true);
        join(groupId, userId);
        GroupMemberInviteRequest request = new GroupMemberInviteRequest(targetUserId);

        ResponseEntity<String> response = rest.postForEntity(
                "/api/v1/groups/{groupId}/members/invite",
                requestEntity(userId, request),
                String.class,
                groupId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).contains("\"code\":\"ENGM-102\"");
    }

    @Test
    @DisplayName("delete_관리자가아닌사용자_should403")
    void delete_관리자가아닌사용자_should403() {
        UUID ownerId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID targetId = UUID.randomUUID();
        UUID groupId = createGroup(ownerId, "Kick Test", true);
        join(groupId, userId);
        UUID targetMemberId = join(groupId, targetId);

        ResponseEntity<String> response = rest.exchange(
                "/api/v1/groups/{groupId}/members/{memberId}",
                HttpMethod.DELETE,
                requestEntity(userId, null),
                String.class,
                groupId,
                targetMemberId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).contains("\"code\":\"ENGM-102\"");
    }

    @Test
    @DisplayName("delete_소유자강퇴요청_should204")
    void delete_소유자강퇴요청_should204() {
        UUID ownerId = UUID.randomUUID();
        UUID targetId = UUID.randomUUID();
        UUID groupId = createGroup(ownerId, "Owner Kick Test", true);
        UUID targetMemberId = join(groupId, targetId);

        ResponseEntity<String> response = rest.exchange(
                "/api/v1/groups/{groupId}/members/{memberId}",
                HttpMethod.DELETE,
                requestEntity(ownerId, null),
                String.class,
                groupId,
                targetMemberId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    @DisplayName("join_강퇴후7일이내_should400")
    void join_강퇴후7일이내_should400() {
        UUID ownerId = UUID.randomUUID();
        UUID targetId = UUID.randomUUID();
        UUID groupId = createGroup(ownerId, "Rejoin Block Test", true);
        UUID targetMemberId = join(groupId, targetId);
        rest.exchange(
                "/api/v1/groups/{groupId}/members/{memberId}",
                HttpMethod.DELETE,
                requestEntity(ownerId, null),
                String.class,
                groupId,
                targetMemberId);

        ResponseEntity<String> response = rest.postForEntity(
                "/api/v1/groups/{groupId}/members/join",
                requestEntity(targetId, null),
                String.class,
                groupId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).contains("\"code\":\"ENGM-104\"");
    }

    private UUID createGroup(UUID ownerId, String name, boolean isPublic) {
        ResponseEntity<String> response = rest.postForEntity(
                "/api/v1/groups",
                requestEntity(ownerId, new GroupCreateRequest(name, null, isPublic)),
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        return extractId(response.getBody());
    }

    private UUID join(UUID groupId, UUID userId) {
        ResponseEntity<String> response = rest.postForEntity(
                "/api/v1/groups/{groupId}/members/join",
                requestEntity(userId, null),
                String.class,
                groupId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        return extractId(response.getBody());
    }

    private static UUID extractId(String body) {
        Matcher matcher = ID_PATTERN.matcher(body);
        assertThat(matcher.find()).isTrue();
        return UUID.fromString(matcher.group(1));
    }

    private static HttpEntity<Object> requestEntity(UUID userId, Object body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-User-Id", userId.toString());
        return new HttpEntity<>(body, headers);
    }
}

