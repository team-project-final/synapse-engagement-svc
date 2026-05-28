package com.synapse.engagement.gamification.api.dto;

import com.synapse.engagement.gamification.domain.UserProfilesGamification;
import com.synapse.engagement.gamification.domain.UserStreak;

import java.util.List;

public record UserGamificationResponse(
        int xp,
        int level,
        int currentStreak,
        int longestStreak,
        List<BadgeResponse> badges
) {
    public static UserGamificationResponse from(
            UserProfilesGamification profile,
            UserStreak streak,
            List<BadgeResponse> badges
    ) {
        return new UserGamificationResponse(
                profile.getTotalXp(),
                profile.getLevel(),
                streak.getCurrentStreak(),
                streak.getLongestStreak(),
                badges
        );
    }
}
