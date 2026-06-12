package com.synapse.engagement.gamification.api.dto;

import com.synapse.engagement.gamification.domain.UserProfilesGamification;

public record LeaderboardEntryResponse(
        int rank,
        Long userId,
        String nickname,
        int xp,
        int level
) {
    public static LeaderboardEntryResponse from(int rank, UserProfilesGamification profile) {
        return new LeaderboardEntryResponse(
                rank,
                profile.getUserId(),
                "User " + profile.getUserId(),
                profile.getTotalXp(),
                profile.getLevel()
        );
    }
}
