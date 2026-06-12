package com.synapse.engagement.gamification.dto.response;

import java.time.LocalDateTime;

public record BadgeResponse(String code, String name, LocalDateTime earnedAt) {
}
