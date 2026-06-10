package com.synapse.engagement.gamification.api;

import com.synapse.engagement.gamification.api.dto.BadgeResponse;
import com.synapse.engagement.gamification.api.dto.LeaderboardEntryResponse;
import com.synapse.engagement.gamification.api.dto.UserGamificationResponse;
import com.synapse.engagement.gamification.api.dto.XpEventResponse;
import com.synapse.engagement.gamification.application.BadgeService;
import com.synapse.engagement.gamification.application.GamificationService;
import com.synapse.engagement.gamification.application.LeaderboardService;
import com.synapse.engagement.gamification.domain.BadgeConditionType;
import com.synapse.engagement.gamification.domain.EventType;
import com.synapse.engagement.shared.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(GamificationController.class)
@Import({GlobalExceptionHandler.class, GamificationControllerWebMvcTest.AuthenticationPrincipalTestConfig.class})
class GamificationControllerWebMvcTest {
    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private GamificationService gamificationService;

    @MockitoBean
    private BadgeService badgeService;

    @MockitoBean
    private LeaderboardService leaderboardService;

    @Test
    void meRequiresAuthenticatedJwtAndReturnsCurrentUserProfile() throws Exception {
        when(gamificationService.getProfile(100L))
                .thenReturn(new UserGamificationResponse(150, 2, 3, 5, List.of()));

        mvc.perform(get("/api/v1/gamification/me")
                        .with(jwtSubject("100")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.xp").value(150))
                .andExpect(jsonPath("$.level").value(2))
                .andExpect(jsonPath("$.currentStreak").value(3));
    }

    @Test
    void meRejectsMissingJwt() throws Exception {
        mvc.perform(get("/api/v1/gamification/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void badgesAndLeaderboardExposePublicReadModels() throws Exception {
        when(badgeService.findAll()).thenReturn(List.of(
                new BadgeResponse(
                        "FIRST_XP",
                        "First XP",
                        "Earn XP",
                        null,
                        BadgeConditionType.TOTAL_XP,
                        1,
                        null
                )
        ));
        when(leaderboardService.findLeaderboard(2)).thenReturn(List.of(
                new LeaderboardEntryResponse(1, 200L, "User 200", 300, 3)
        ));

        mvc.perform(get("/api/v1/gamification/badges"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].code").value("FIRST_XP"));

        mvc.perform(get("/api/v1/gamification/leaderboard?limit=2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].rank").value(1))
                .andExpect(jsonPath("$[0].userId").value(200));
    }

    @Test
    void addXpUsesAuthenticatedSubjectAndReturnsUpdatedProfile() throws Exception {
        // JWT subject("300")가 externalUserId로 전달되어야 한다(F10). 내부 PK는 Long(300L).
        when(gamificationService.addXp(eq(300L), eq("300"), eq("default"), any()))
                .thenReturn(new UserGamificationResponse(10, 1, 1, 1, List.of()));

        mvc.perform(post("/api/v1/gamification/xp/events")
                        .with(jwtSubject("300"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"eventType":"CARD_REVIEWED","xpAmount":10,"sourceId":"card-1","sourceType":"card","eventId":"event-1"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.xp").value(10))
                .andExpect(jsonPath("$.currentStreak").value(1));
    }

    @Test
    void historyUsesAuthenticatedSubject() throws Exception {
        when(gamificationService.getXpHistory(400L)).thenReturn(List.of(
                new XpEventResponse(EventType.CARD_REVIEWED, 10, "card-1", "card", "event-1", Instant.parse("2026-05-28T00:00:00Z"))
        ));

        mvc.perform(get("/api/v1/gamification/xp/history")
                        .with(jwtSubject("400")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].eventId").value("event-1"))
                .andExpect(jsonPath("$[0].xpAmount").value(10));
    }

    private static RequestPostProcessor jwtSubject(String subject) {
        return request -> {
            request.setAttribute("test.jwt.subject", subject);
            return request;
        };
    }

    @TestConfiguration
    static class AuthenticationPrincipalTestConfig implements WebMvcConfigurer {
        @Override
        public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
            resolvers.add(new TestJwtArgumentResolver());
        }
    }

    static class TestJwtArgumentResolver implements HandlerMethodArgumentResolver {
        @Override
        public boolean supportsParameter(MethodParameter parameter) {
            return parameter.hasParameterAnnotation(AuthenticationPrincipal.class)
                    && Jwt.class.isAssignableFrom(parameter.getParameterType());
        }

        @Override
        public Object resolveArgument(
                MethodParameter parameter,
                ModelAndViewContainer mavContainer,
                NativeWebRequest webRequest,
                WebDataBinderFactory binderFactory
        ) {
            String subject = (String) webRequest.getAttribute("test.jwt.subject", NativeWebRequest.SCOPE_REQUEST);
            if (subject == null) {
                return null;
            }
            return Jwt.withTokenValue("test-token")
                    .header("alg", "none")
                    .subject(subject)
                    .build();
        }
    }
}
