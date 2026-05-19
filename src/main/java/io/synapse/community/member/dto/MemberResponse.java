package io.synapse.community.member.dto;

import io.synapse.community.member.entity.MemberRole;
import io.synapse.community.member.entity.MemberStatus;
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
