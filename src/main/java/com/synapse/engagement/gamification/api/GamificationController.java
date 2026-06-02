package com.synapse.engagement.gamification.api;

import com.synapse.engagement.gamification.api.dto.AddXpRequest;
import com.synapse.engagement.gamification.api.dto.BadgeResponse;
import com.synapse.engagement.gamification.api.dto.LeaderboardEntryResponse;
import com.synapse.engagement.gamification.api.dto.UserGamificationResponse;
import com.synapse.engagement.gamification.api.dto.XpEventResponse;
import com.synapse.engagement.gamification.application.BadgeService;
import com.synapse.engagement.gamification.application.GamificationService;
import com.synapse.engagement.gamification.application.LeaderboardService;
import com.synapse.engagement.shared.CurrentTenant;
import com.synapse.engagement.shared.CurrentUser;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/gamification")
public class GamificationController {
    private final GamificationService gamificationService;
    private final BadgeService badgeService;
    private final LeaderboardService leaderboardService;

    public GamificationController(
            GamificationService gamificationService,
            BadgeService badgeService,
            LeaderboardService leaderboardService
    ) {
        this.gamificationService = gamificationService;
        this.badgeService = badgeService;
        this.leaderboardService = leaderboardService;
    }

    @GetMapping("/me")
    public UserGamificationResponse me(@AuthenticationPrincipal Jwt jwt) {
        // Controller는 인증 주체만 해석하고, XP/배지/스트릭 계산 규칙은 Service에 위임한다.
        return gamificationService.getProfile(CurrentUser.require(jwt));
    }

    @GetMapping("/xp/history")
    public List<XpEventResponse> history(@AuthenticationPrincipal Jwt jwt) {
        return gamificationService.getXpHistory(CurrentUser.require(jwt));
    }

    @GetMapping("/badges")
    public List<BadgeResponse> badges() {
        return badgeService.findAll();
    }

    @GetMapping("/leaderboard")
    public List<LeaderboardEntryResponse> leaderboard(
            @RequestParam(defaultValue = "all") String period,
            @RequestParam(defaultValue = "20") int limit
    ) {
        return leaderboardService.findLeaderboard(limit);
    }

    @PostMapping("/xp/events")
    @ResponseStatus(HttpStatus.CREATED)
    public UserGamificationResponse addXp(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody AddXpRequest request
    ) {
        // tenantId는 Kafka 이벤트 파티션 키와 Avro 메타 필드에 쓰이므로 XP 적립 흐름까지 함께 전달한다.
        return gamificationService.addXp(CurrentUser.require(jwt), CurrentTenant.resolve(jwt), request);
    }
}
