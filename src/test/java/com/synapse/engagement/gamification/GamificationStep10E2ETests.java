package com.synapse.engagement.gamification;

import com.synapse.engagement.support.TestJwt;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class GamificationStep10E2ETests {
    @Autowired
    private MockMvc mvc;

    @Test
    void reviewXpBadgeLevelAndLeaderboardFlowWorksEndToEnd() throws Exception {
        var userId = "10100";
        var token = bearer(userId);

        // 1차 복습: XP 이력 생성 + 최초 XP 배지 획득. 아직 100 XP 미만이라 레벨은 1이다.
        mvc.perform(post("/api/v1/gamification/xp/events")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"eventType":"CARD_REVIEWED","xpAmount":90,"sourceId":"step10-card-1","sourceType":"card-review","eventId":"step10-review-1"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.xp").value(90))
                .andExpect(jsonPath("$.level").value(1))
                .andExpect(jsonPath("$.badges[*].code", hasItem("FIRST_XP")));

        // 2차 복습: 누적 XP가 레벨 임계값을 넘어서 레벨업과 LEVEL_2 배지가 함께 발생한다.
        mvc.perform(post("/api/v1/gamification/xp/events")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"eventType":"CARD_REVIEWED","xpAmount":1910,"sourceId":"step10-card-2","sourceType":"card-review","eventId":"step10-review-2"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.xp").value(2000))
                .andExpect(jsonPath("$.level").value(6))
                .andExpect(jsonPath("$.badges[*].code", hasItem("LEVEL_2")));

        // 같은 eventId/sourceId는 Kafka 재전달이나 클라이언트 재시도 상황에서도 중복 적립되지 않아야 한다.
        mvc.perform(post("/api/v1/gamification/xp/events")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"eventType":"CARD_REVIEWED","xpAmount":1910,"sourceId":"step10-card-2","sourceType":"card-review","eventId":"step10-review-2"}
                                """))
                .andExpect(status().isConflict());

        mvc.perform(get("/api/v1/gamification/me")
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.xp").value(2000))
                .andExpect(jsonPath("$.level").value(6))
                .andExpect(jsonPath("$.badges[*].code", hasItem("FIRST_XP")))
                .andExpect(jsonPath("$.badges[*].code", hasItem("LEVEL_2")));

        mvc.perform(get("/api/v1/gamification/xp/history")
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].eventId", hasItem("step10-review-1")))
                .andExpect(jsonPath("$[*].eventId", hasItem("step10-review-2")));

        mvc.perform(get("/api/v1/gamification/leaderboard?limit=5")
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userId").value(10100))
                .andExpect(jsonPath("$[0].xp").value(2000))
                .andExpect(jsonPath("$[0].level").value(6))
                .andExpect(jsonPath("$[0].rank").value(1));
    }

    private String bearer(String subject) {
        return "Bearer " + TestJwt.accessToken(subject);
    }
}
