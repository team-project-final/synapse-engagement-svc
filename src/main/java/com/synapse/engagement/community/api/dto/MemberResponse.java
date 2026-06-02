package com.synapse.engagement.community.api.dto;

import com.synapse.engagement.community.domain.GroupMember;
import com.synapse.engagement.community.domain.MemberRole;
import com.synapse.engagement.community.domain.MemberStatus;

import java.time.Instant;

public record MemberResponse(
        Long id,
        Long groupId,
        Long userId,
        MemberRole role,
        MemberStatus status,
        Instant joinedAt
) {
    public static MemberResponse from(GroupMember member) {
        return new MemberResponse(
                member.getId(),
                member.getGroup().getId(),
                member.getUserId(),
                member.getRole(),
                member.getStatus(),
                member.getJoinedAt()
        );
    }
}
