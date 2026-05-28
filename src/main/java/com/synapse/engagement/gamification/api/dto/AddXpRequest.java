package com.synapse.engagement.gamification.api.dto;

import com.synapse.engagement.gamification.domain.EventType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record AddXpRequest(
        @NotNull EventType eventType,
        @Positive Integer xpAmount,
        @NotBlank String sourceId,
        @NotBlank String sourceType,
        @NotBlank String eventId
) {
}
