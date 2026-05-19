package io.synapse.community.group.member.infrastructure.adapter.inbound.rest.dto;

import io.synapse.community.group.member.domain.model.MemberRole;
import io.synapse.community.group.member.domain.model.MemberStatus;
import java.time.LocalDateTime;
import java.util.UUID;

public record MemberResponse(
        UUID id,
        UUID groupId,
        UUID userId,
        MemberRole role,
        MemberStatus status,
        LocalDateTime joinedAt) {
}

