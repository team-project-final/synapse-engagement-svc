package com.synapse.engagement.community.api.dto;

import jakarta.validation.constraints.NotNull;

public record MemberInviteRequest(
        @NotNull Long userId
) {
}
