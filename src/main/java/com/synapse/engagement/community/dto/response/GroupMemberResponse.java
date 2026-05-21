package com.synapse.engagement.community.dto.response;

import com.synapse.engagement.community.entity.GroupMemberRole;
import com.synapse.engagement.community.entity.GroupMemberStatus;
import java.time.LocalDateTime;
import java.util.UUID;

public record GroupMemberResponse(
        UUID id,
        UUID groupId,
        UUID userId,
        GroupMemberRole role,
        GroupMemberStatus status,
        LocalDateTime joinedAt) {
}

