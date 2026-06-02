package com.synapse.engagement.community.api.dto;

import com.synapse.engagement.community.domain.GroupMember;
import com.synapse.engagement.community.domain.MemberStatus;

import java.time.Instant;

public record InviteDecisionResponse(
        Long groupId,
        Long userId,
        MemberStatus status,
        String inviteToken,
        Instant inviteExpiresAt
) {
    public static InviteDecisionResponse from(GroupMember member) {
        return new InviteDecisionResponse(
                member.getGroup().getId(),
                member.getUserId(),
                member.getStatus(),
                member.getInviteToken(),
                member.getInviteExpiresAt()
        );
    }
}
