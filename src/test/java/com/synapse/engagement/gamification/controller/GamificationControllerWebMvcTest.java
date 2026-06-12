package com.synapse.engagement.gamification.controller;

import com.synapse.engagement.gamification.dto.response.LeaderboardEntryResponse;
import com.synapse.engagement.gamification.dto.response.UserXpResponse;
import com.synapse.engagement.gamification.service.GamificationService;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(GamificationController.class)
class GamificationControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GamificationService gamificationService;

    @Test
    @DisplayName("getProfile_인증헤더없음_should401")
    void getProfile_인증헤더없음_should401() throws Exception {
        mockMvc.perform(get("/api/v1/gamification/profile"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("ENGM-201"));
    }

    @Test
    @DisplayName("getProfile_정상요청_should200")
    void getProfile_정상요청_should200() throws Exception {
        UUID userId = UUID.randomUUID();
        given(gamificationService.getProfile(userId))
                .willReturn(new UserXpResponse(userId, 1, 0, 0, 0, "Novice", 100, List.of()));

        mockMvc.perform(get("/api/v1/gamification/profile")
                        .header("X-User-Id", userId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(userId.toString()))
                .andExpect(jsonPath("$.totalXp").value(0))
                .andExpect(jsonPath("$.level").value(1));
    }

    @Test
    @DisplayName("getXpHistory_size범위초과_should400")
    void getXpHistory_size범위초과_should400() throws Exception {
        mockMvc.perform(get("/api/v1/gamification/xp/history")
                        .header("X-User-Id", UUID.randomUUID().toString())
                        .param("size", "101"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("ENGM-202"));
    }

    @Test
    @DisplayName("getLeaderboard_정상요청_should200")
    void getLeaderboard_정상요청_should200() throws Exception {
        UUID userId = UUID.randomUUID();
        given(gamificationService.getLeaderboard(10))
                .willReturn(List.of(new LeaderboardEntryResponse(1, userId, 1500)));

        mockMvc.perform(get("/api/v1/gamification/leaderboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].rank").value(1))
                .andExpect(jsonPath("$[0].xp").value(1500));
    }

    @Test
    @DisplayName("getLeaderboard_limit범위초과_should400")
    void getLeaderboard_limit범위초과_should400() throws Exception {
        mockMvc.perform(get("/api/v1/gamification/leaderboard")
                        .param("limit", "101"))
                .andExpect(status().isBadRequest());
    }
}
