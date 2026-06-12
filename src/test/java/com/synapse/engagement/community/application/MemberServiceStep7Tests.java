package com.synapse.engagement.community.application;

import com.synapse.engagement.community.api.dto.JoinRequestDecision;
import com.synapse.engagement.community.api.dto.JoinRequestDecisionRequest;
import com.synapse.engagement.community.api.dto.MemberInviteRequest;
import com.synapse.engagement.community.domain.Group;
import com.synapse.engagement.community.domain.GroupMember;
import com.synapse.engagement.community.domain.MemberStatus;
import com.synapse.engagement.community.repository.GroupMemberRepository;
import com.synapse.engagement.shared.ForbiddenException;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MemberServiceStep7Tests {
    private final GroupService groupService = mock(GroupService.class);
    private final GroupMemberRepository memberRepository = mock(GroupMemberRepository.class);
    private final MemberService memberService = new MemberService(groupService, memberRepository);

    @Test
    void inviteCreatesTokenBackedInvitedMembership() {
        Group group = group(10L, 100L, false);
        GroupMember owner = GroupMember.owner(group);
        when(groupService.findActiveGroup(10L)).thenReturn(group);
        when(memberRepository.findByGroupIdAndUserId(10L, 100L)).thenReturn(Optional.of(owner));
        when(memberRepository.findByGroupIdAndUserId(10L, 200L)).thenReturn(Optional.empty());
        when(memberRepository.save(any(GroupMember.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = memberService.invite(10L, 100L, new MemberInviteRequest(200L));

        assertThat(response.status()).isEqualTo(MemberStatus.INVITED);
        assertThat(response.inviteToken()).isNotBlank();
        assertThat(response.inviteExpiresAt()).isAfter(Instant.now());
    }

    @Test
    void acceptInviteRejectsUsersOtherThanInviteTarget() {
        Group group = group(10L, 100L, false);
        GroupMember invite = GroupMember.invited(group, 200L, "token-1", Instant.now().plusSeconds(60));
        when(memberRepository.findByGroupIdAndInviteToken(10L, "token-1")).thenReturn(Optional.of(invite));

        assertThatThrownBy(() -> memberService.acceptInvite(10L, 300L, "token-1"))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void decideJoinRequestCanRejectPendingRequest() {
        Group group = group(10L, 100L, false);
        GroupMember owner = GroupMember.owner(group);
        GroupMember request = GroupMember.joined(group, 200L, false);
        when(memberRepository.findByGroupIdAndUserId(10L, 100L)).thenReturn(Optional.of(owner));
        when(memberRepository.findByGroupIdAndUserId(10L, 200L)).thenReturn(Optional.of(request));

        var response = memberService.decideJoinRequest(
                10L,
                100L,
                200L,
                new JoinRequestDecisionRequest(JoinRequestDecision.REJECT)
        );

        assertThat(response.status()).isEqualTo(MemberStatus.REJECTED);
    }

    private Group group(Long id, Long ownerId, boolean publicGroup) {
        Group group = Group.create("study", "desc", publicGroup, ownerId);
        ReflectionTestUtils.setField(group, "id", id);
        return group;
    }
}
