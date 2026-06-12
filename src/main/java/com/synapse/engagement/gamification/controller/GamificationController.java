package com.synapse.engagement.gamification.controller;

import com.synapse.engagement.gamification.dto.response.LeaderboardEntryResponse;
import com.synapse.engagement.gamification.dto.response.UserXpResponse;
import com.synapse.engagement.gamification.dto.response.XpEventResponse;
import com.synapse.engagement.gamification.service.GamificationService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.List;
import java.util.UUID;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/gamification")
@Validated
public class GamificationController {

    private final GamificationService gamificationService;

    GamificationController(GamificationService gamificationService) {
        this.gamificationService = gamificationService;
    }

    @GetMapping("/profile")
    UserXpResponse getProfile(@RequestHeader("X-User-Id") UUID currentUserId) {
        return gamificationService.getProfile(currentUserId);
    }

    @GetMapping("/xp/history")
    List<XpEventResponse> getXpHistory(
            @RequestHeader("X-User-Id") UUID currentUserId,
            @RequestParam(name = "size", defaultValue = "20") @Min(1) @Max(100) int size) {
        return gamificationService.getXpHistory(currentUserId, size);
    }

    @GetMapping("/leaderboard")
    List<LeaderboardEntryResponse> getLeaderboard(
            @RequestParam(name = "scope", defaultValue = "global") String scope,
            @RequestParam(name = "limit", defaultValue = "10") @Min(1) @Max(100) int limit) {
        return gamificationService.getLeaderboard(limit);
    }
}
