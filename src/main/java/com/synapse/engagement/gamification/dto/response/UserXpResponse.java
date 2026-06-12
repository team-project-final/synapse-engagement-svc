package com.synapse.engagement.gamification.dto.response;

import java.util.List;
import java.util.UUID;

public record UserXpResponse(
        UUID userId,
        int level,
        int totalXp,
        int currentStreak,
        int longestStreak,
        String title,
        int nextLevelXp,
        List<BadgeResponse> recentBadges) {
}
