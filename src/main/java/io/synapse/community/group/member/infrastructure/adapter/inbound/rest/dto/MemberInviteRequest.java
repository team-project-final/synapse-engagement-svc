package io.synapse.community.group.member.infrastructure.adapter.inbound.rest.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record MemberInviteRequest(@NotNull UUID userId) {
}

