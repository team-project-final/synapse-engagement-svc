package com.synapse.engagement.gamification.dto.response;

import java.util.UUID;

public record LeaderboardEntryResponse(int rank, UUID userId, int xp) {
}
