package io.synapse.community.member.service;

import io.synapse.community.group.entity.Group;
import io.synapse.community.group.repository.GroupRepository;
import io.synapse.community.member.entity.GroupMember;
import io.synapse.community.member.repository.GroupMemberRepository;
import io.synapse.community.member.dto.MemberInviteRequest;
import io.synapse.community.member.dto.MemberResponse;
import io.synapse.community.member.exception.MemberPermissionDeniedException;
import io.synapse.community.member.exception.OwnerCannotLeaveException;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @Mock
    private GroupRepository groupRepository;

    @Mock
    private GroupMemberRepository memberRepository;

    @Mock
    private MemberMapper memberMapper;

    private MemberService memberService;

    @BeforeEach
    void setUp() {
        memberService = new MemberService(groupRepository, memberRepository, memberMapper);
    }

    @Test
    @DisplayName("invite_소유자요청_should멤버추가")
    void invite_소유자요청_should멤버추가() {
        UUID ownerId = UUID.randomUUID();
        UUID targetUserId = UUID.randomUUID();
        Group group = Group.create("Private Group", null, false, ownerId);
        GroupMember owner = GroupMember.owner(group.id(), ownerId);
        MemberResponse expected = new MemberResponse(null, group.id(), targetUserId, null, null, null);

        given(groupRepository.findActiveById(group.id())).willReturn(Optional.of(group));
        given(memberRepository.findByGroupIdAndUserId(group.id(), ownerId)).willReturn(Optional.of(owner));
        given(memberRepository.findByGroupIdAndUserId(group.id(), targetUserId)).willReturn(Optional.empty());
        given(memberRepository.save(any(GroupMember.class))).willAnswer(invocation -> invocation.getArgument(0));
        given(memberMapper.toResponse(any(GroupMember.class))).willReturn(expected);

        MemberResponse response = memberService.invite(
                ownerId,
                group.id(),
                new MemberInviteRequest(targetUserId));

        assertThat(response).isEqualTo(expected);
        verify(memberRepository).save(any(GroupMember.class));
    }

    @Test
    @DisplayName("invite_일반멤버요청_should403")
    void invite_일반멤버요청_should403() {
        UUID ownerId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID targetUserId = UUID.randomUUID();
        Group group = Group.create("Private Group", null, false, ownerId);
        GroupMember member = GroupMember.joined(group.id(), userId, true);

        given(groupRepository.findActiveById(group.id())).willReturn(Optional.of(group));
        given(memberRepository.findByGroupIdAndUserId(group.id(), userId)).willReturn(Optional.of(member));

        assertThatThrownBy(() -> memberService.invite(
                        userId,
                        group.id(),
                        new MemberInviteRequest(targetUserId)))
                .isInstanceOf(MemberPermissionDeniedException.class);
    }

    @Test
    @DisplayName("approve_소유자요청_shouldACTIVE전환")
    void approve_소유자요청_shouldACTIVE전환() {
        UUID ownerId = UUID.randomUUID();
        UUID pendingUserId = UUID.randomUUID();
        Group group = Group.create("Private Group", null, false, ownerId);
        GroupMember owner = GroupMember.owner(group.id(), ownerId);
        GroupMember pendingMember = GroupMember.joined(group.id(), pendingUserId, false);
        MemberResponse expected = new MemberResponse(
                pendingMember.id(),
                group.id(),
                pendingUserId,
                pendingMember.role(),
                pendingMember.status(),
                pendingMember.joinedAt());

        given(groupRepository.findActiveById(group.id())).willReturn(Optional.of(group));
        given(memberRepository.findByGroupIdAndUserId(group.id(), ownerId)).willReturn(Optional.of(owner));
        given(memberRepository.findByIdAndGroupId(pendingMember.id(), group.id())).willReturn(Optional.of(pendingMember));
        given(memberMapper.toResponse(pendingMember)).willReturn(expected);

        MemberResponse response = memberService.approve(ownerId, group.id(), pendingMember.id());

        assertThat(response).isEqualTo(expected);
        assertThat(pendingMember.isActive()).isTrue();
    }

    @Test
    @DisplayName("approve_일반멤버요청_should403")
    void approve_일반멤버요청_should403() {
        UUID ownerId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID pendingMemberId = UUID.randomUUID();
        Group group = Group.create("Group", null, true, ownerId);
        GroupMember member = GroupMember.joined(group.id(), userId, true);

        given(groupRepository.findActiveById(group.id())).willReturn(Optional.of(group));
        given(memberRepository.findByGroupIdAndUserId(group.id(), userId)).willReturn(Optional.of(member));

        assertThatThrownBy(() -> memberService.approve(userId, group.id(), pendingMemberId))
                .isInstanceOf(MemberPermissionDeniedException.class);
    }

    @Test
    @DisplayName("delete_소유자본인탈퇴_shouldOwnerCannotLeave")
    void delete_소유자본인탈퇴_shouldOwnerCannotLeave() {
        UUID ownerId = UUID.randomUUID();
        Group group = Group.create("Group", null, true, ownerId);
        GroupMember owner = GroupMember.owner(group.id(), ownerId);

        given(groupRepository.findActiveById(group.id())).willReturn(Optional.of(group));
        given(memberRepository.findByIdAndGroupId(owner.id(), group.id())).willReturn(Optional.of(owner));
        given(memberRepository.findByGroupIdAndUserId(group.id(), ownerId)).willReturn(Optional.of(owner));

        assertThatThrownBy(() -> memberService.delete(ownerId, group.id(), owner.id()))
                .isInstanceOf(OwnerCannotLeaveException.class);
    }

    @Test
    @DisplayName("delete_소유자강퇴요청_should대상멤버KICKED")
    void delete_소유자강퇴요청_should대상멤버KICKED() {
        UUID ownerId = UUID.randomUUID();
        UUID targetUserId = UUID.randomUUID();
        Group group = Group.create("Group", null, true, ownerId);
        GroupMember owner = GroupMember.owner(group.id(), ownerId);
        GroupMember targetMember = GroupMember.joined(group.id(), targetUserId, true);

        given(groupRepository.findActiveById(group.id())).willReturn(Optional.of(group));
        given(memberRepository.findByIdAndGroupId(targetMember.id(), group.id())).willReturn(Optional.of(targetMember));
        given(memberRepository.findByGroupIdAndUserId(group.id(), ownerId)).willReturn(Optional.of(owner));

        memberService.delete(ownerId, group.id(), targetMember.id());

        assertThat(targetMember.blocksRejoin(java.time.LocalDateTime.now())).isTrue();
    }
}
