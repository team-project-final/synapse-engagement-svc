package com.synapse.engagement;

import com.synapse.engagement.support.TestJwt;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class EngagementApiSmokeTests {
    @Autowired
    private MockMvc mvc;

    @Test
    void groupCreateAndListWorks() throws Exception {
        mvc.perform(post("/api/v1/community/groups")
                        .header("Authorization", bearer("1001"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"알고리즘 스터디","description":"매일 1문제","isPublic":true}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.ownerId").value(1001));

        mvc.perform(get("/api/v1/community/groups")
                        .header("Authorization", bearer("1001")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("알고리즘 스터디"));
    }

    @Test
    void xpEventCreatesProfileAndHistory() throws Exception {
        mvc.perform(post("/api/v1/gamification/xp/events")
                        .header("Authorization", bearer("2001"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"eventType":"CARD_REVIEWED","xpAmount":10,"sourceId":"card-1","sourceType":"card","eventId":"event-2001-1"}
                """))
        .andExpect(status().isCreated())
                .andExpect(jsonPath("$.xp").value(10));

        mvc.perform(get("/api/v1/gamification/xp/history")
                        .header("Authorization", bearer("2001")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].eventId").value("event-2001-1"));
    }

    @Test
    void badgesAndLeaderboardUseXpProfiles() throws Exception {
        mvc.perform(post("/api/v1/gamification/xp/events")
                        .header("Authorization", bearer("4001"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"eventType":"CARD_REVIEWED","xpAmount":20,"sourceId":"card-4001","sourceType":"card","eventId":"event-4001-1"}
                """))
        .andExpect(status().isCreated())
                .andExpect(jsonPath("$.badges[0].code").value("FIRST_XP"));

        mvc.perform(post("/api/v1/gamification/xp/events")
                        .header("Authorization", bearer("4002"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"eventType":"NOTE_CREATED","xpAmount":150,"sourceId":"note-4002","sourceType":"note","eventId":"event-4002-1"}
                """))
        .andExpect(status().isCreated())
                .andExpect(jsonPath("$.level").value(2));

        mvc.perform(get("/api/v1/gamification/badges")
                        .header("Authorization", bearer("4001")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].code").value("FIRST_XP"));

        mvc.perform(get("/api/v1/gamification/me")
                        .header("Authorization", bearer("4002")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.level").value(2))
                .andExpect(jsonPath("$.badges.length()").value(2));

        mvc.perform(get("/api/v1/gamification/leaderboard?limit=2")
                        .header("Authorization", bearer("4001")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userId").value(4002))
                .andExpect(jsonPath("$[0].rank").value(1));
    }

    @Test
    void actuatorHealthProbesArePermittedWithoutAuth() throws Exception {
        // #43: 쿠버네티스 프로브가 호출하는 health 하위 경로가 401이면 livenessProbe 실패 →
        // SIGTERM 재시작 루프가 발생한다. 무인증 200을 보장한다.
        mvc.perform(get("/actuator/health/liveness"))
                .andExpect(status().isOk());

        mvc.perform(get("/actuator/health/readiness"))
                .andExpect(status().isOk());
    }

    @Test
    void swaggerApiDocsExposeGamificationEndpoints() throws Exception {
        mvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/api/v1/gamification/me'].get").exists())
                .andExpect(jsonPath("$.paths['/api/v1/gamification/leaderboard'].get").exists())
                .andExpect(jsonPath("$.paths['/api/v1/gamification/badges'].get").exists())
                .andExpect(jsonPath("$.paths['/api/v1/community/groups/{groupId}/invite/{token}/accept'].post").exists())
                .andExpect(jsonPath("$.paths['/api/v1/community/groups/{groupId}/invite/{token}/decline'].post").exists())
                .andExpect(jsonPath("$.paths['/api/v1/community/groups/{groupId}/join-requests'].get").exists())
                .andExpect(jsonPath("$.paths['/api/v1/community/groups/{groupId}/join-requests/{userId}'].patch").exists())
                .andExpect(jsonPath("$.paths['/api/v1/community/reports'].post").exists())
                .andExpect(jsonPath("$.paths['/api/v1/admin/reports'].get").exists())
                .andExpect(jsonPath("$.paths['/api/v1/admin/reports/{reportId}'].patch").exists());
    }

    @Test
    void sharedContentCanBeCreatedAndFound() throws Exception {
        var result = mvc.perform(post("/api/v1/community/share")
                        .header("Authorization", bearer("3001"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"contentType":"DECK","contentId":77,"title":"영단어 덱","description":"기초 단어","tags":["english","basic"]}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.shareToken").exists())
                .andReturn();

        var body = result.getResponse().getContentAsString();
        var token = body.replaceAll(".*\\\"shareToken\\\":\\\"([^\\\"]+)\\\".*", "$1");

        mvc.perform(get("/api/v1/community/share/" + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("영단어 덱"));
    }

    @Test
    void step7InviteDecisionAndJoinRequestManagementWork() throws Exception {
        var group = mvc.perform(post("/api/v1/community/groups")
                        .header("Authorization", bearer("5001"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"비공개 스터디","description":"초대 기반","isPublic":false}
                                """))
                .andExpect(status().isCreated())
                .andReturn();
        var groupId = group.getResponse().getContentAsString()
                .replaceAll(".*\\\"id\\\":([0-9]+).*", "$1");

        var invite = mvc.perform(post("/api/v1/community/groups/" + groupId + "/members/invite")
                        .header("Authorization", bearer("5001"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"userId":5002}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("INVITED"))
                .andExpect(jsonPath("$.inviteToken").isNotEmpty())
                .andReturn();
        var inviteToken = invite.getResponse().getContentAsString()
                .replaceAll(".*\\\"inviteToken\\\":\\\"([^\\\"]+)\\\".*", "$1");

        mvc.perform(post("/api/v1/community/groups/" + groupId + "/invite/" + inviteToken + "/accept")
                        .header("Authorization", bearer("5003")))
                .andExpect(status().isForbidden());

        mvc.perform(post("/api/v1/community/groups/" + groupId + "/invite/" + inviteToken + "/accept")
                        .header("Authorization", bearer("5002")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        mvc.perform(post("/api/v1/community/groups/" + groupId + "/invite/" + inviteToken + "/accept")
                        .header("Authorization", bearer("5002")))
                .andExpect(status().isNotFound());

        mvc.perform(post("/api/v1/community/groups/" + groupId + "/members/join")
                        .header("Authorization", bearer("5004")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"));

        mvc.perform(get("/api/v1/community/groups/" + groupId + "/join-requests")
                        .header("Authorization", bearer("5004")))
                .andExpect(status().isForbidden());

        mvc.perform(get("/api/v1/community/groups/" + groupId + "/join-requests")
                        .header("Authorization", bearer("5001")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userId").value(5004))
                .andExpect(jsonPath("$[0].status").value("PENDING"));

        mvc.perform(patch("/api/v1/community/groups/" + groupId + "/join-requests/5004")
                        .header("Authorization", bearer("5001"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"decision":"APPROVE"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        mvc.perform(post("/api/v1/community/groups/" + groupId + "/members/join")
                        .header("Authorization", bearer("5005")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"));

        mvc.perform(patch("/api/v1/community/groups/" + groupId + "/join-requests/5005")
                        .header("Authorization", bearer("5001"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"decision":"REJECT"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REJECTED"));

        mvc.perform(post("/api/v1/community/groups/" + groupId + "/members/join")
                        .header("Authorization", bearer("5005")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void step8ReportAndAdminModerationWork() throws Exception {
        var group = mvc.perform(post("/api/v1/community/groups")
                        .header("Authorization", bearer("6001"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"신고 대상 그룹","description":"bad","isPublic":true}
                                """))
                .andExpect(status().isCreated())
                .andReturn();
        var groupId = group.getResponse().getContentAsString()
                .replaceAll(".*\\\"id\\\":([0-9]+).*", "$1");

        mvc.perform(post("/api/v1/community/reports")
                        .header("Authorization", bearer("6002"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"targetType":"STUDY_GROUP","targetId":%s,"reason":"부적절한 내용"}
                                """.formatted(groupId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"));

        mvc.perform(post("/api/v1/community/reports")
                        .header("Authorization", bearer("6002"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"targetType":"STUDY_GROUP","targetId":%s,"reason":"중복 신고"}
                                """.formatted(groupId)))
                .andExpect(status().isConflict());

        mvc.perform(get("/api/v1/admin/reports")
                        .header("Authorization", bearer("6002")))
                .andExpect(status().isForbidden());

        var reports = mvc.perform(get("/api/v1/admin/reports")
                        .header("Authorization", bearer("9001", List.of("ADMIN"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("PENDING"))
                .andReturn();
        var reportId = reports.getResponse().getContentAsString()
                .replaceAll(".*\\\"id\\\":([0-9]+).*", "$1");

        mvc.perform(patch("/api/v1/admin/reports/" + reportId)
                        .header("Authorization", bearer("9001", List.of("ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status":"APPROVED","adminNote":"hidden by moderation"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"))
                .andExpect(jsonPath("$.adminNote").value("hidden by moderation"));
    }

    private String bearer(String subject) {
        return "Bearer " + TestJwt.accessToken(subject);
    }

    private String bearer(String subject, List<String> roles) {
        return "Bearer " + TestJwt.accessToken(subject, roles);
    }
}
