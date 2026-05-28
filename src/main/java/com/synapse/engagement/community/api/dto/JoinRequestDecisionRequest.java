package com.synapse.engagement.community.api.dto;

import jakarta.validation.constraints.NotNull;

public record JoinRequestDecisionRequest(
        @NotNull JoinRequestDecision decision
) {
}
