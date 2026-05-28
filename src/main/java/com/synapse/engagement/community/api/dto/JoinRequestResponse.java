package com.synapse.engagement.community.api.dto;

import com.synapse.engagement.community.domain.GroupMember;
import com.synapse.engagement.community.domain.MemberRole;
import com.synapse.engagement.community.domain.MemberStatus;

import java.time.Instant;

public record JoinRequestResponse(
        Long memberId,
        Long groupId,
        Long userId,
        MemberRole role,
        MemberStatus status,
        Instant inviteExpiresAt
) {
    public static JoinRequestResponse from(GroupMember member) {
        return new JoinRequestResponse(
                member.getId(),
                member.getGroup().getId(),
                member.getUserId(),
                member.getRole(),
                member.getStatus(),
                member.getInviteExpiresAt()
        );
    }
}
