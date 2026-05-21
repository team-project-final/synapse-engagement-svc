package com.synapse.engagement.community.service;

import com.synapse.engagement.community.entity.Group;
import com.synapse.engagement.community.repository.GroupRepository;
import com.synapse.engagement.community.entity.GroupMember;
import com.synapse.engagement.community.repository.GroupMemberRepository;
import com.synapse.engagement.community.dto.request.GroupMemberInviteRequest;
import com.synapse.engagement.community.dto.response.GroupMemberResponse;
import com.synapse.engagement.community.exception.GroupMemberPermissionDeniedException;
import com.synapse.engagement.community.exception.OwnerCannotLeaveException;
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
class GroupMemberServiceTest {

    @Mock
    private GroupRepository groupRepository;

    @Mock
    private GroupMemberRepository groupMemberRepository;

    @Mock
    private GroupMemberMapper groupMemberMapper;

    private GroupMemberService groupMemberService;

    @BeforeEach
    void setUp() {
        groupMemberService = new GroupMemberService(groupRepository, groupMemberRepository, groupMemberMapper);
    }

    @Test
    @DisplayName("invite_소유자요청_should멤버추가")
    void invite_소유자요청_should멤버추가() {
        UUID ownerId = UUID.randomUUID();
        UUID targetUserId = UUID.randomUUID();
        Group group = Group.create("Private Group", null, false, ownerId);
        GroupMember owner = GroupMember.owner(group.id(), ownerId);
        GroupMemberResponse expected = new GroupMemberResponse(null, group.id(), targetUserId, null, null, null);

        given(groupRepository.findActiveById(group.id())).willReturn(Optional.of(group));
        given(groupMemberRepository.findByGroupIdAndUserId(group.id(), ownerId)).willReturn(Optional.of(owner));
        given(groupMemberRepository.findByGroupIdAndUserId(group.id(), targetUserId)).willReturn(Optional.empty());
        given(groupMemberRepository.save(any(GroupMember.class))).willAnswer(invocation -> invocation.getArgument(0));
        given(groupMemberMapper.toResponse(any(GroupMember.class))).willReturn(expected);

        GroupMemberResponse response = groupMemberService.invite(
                ownerId,
                group.id(),
                new GroupMemberInviteRequest(targetUserId));

        assertThat(response).isEqualTo(expected);
        verify(groupMemberRepository).save(any(GroupMember.class));
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
        given(groupMemberRepository.findByGroupIdAndUserId(group.id(), userId)).willReturn(Optional.of(member));

        assertThatThrownBy(() -> groupMemberService.invite(
                        userId,
                        group.id(),
                        new GroupMemberInviteRequest(targetUserId)))
                .isInstanceOf(GroupMemberPermissionDeniedException.class);
    }

    @Test
    @DisplayName("approve_소유자요청_shouldACTIVE전환")
    void approve_소유자요청_shouldACTIVE전환() {
        UUID ownerId = UUID.randomUUID();
        UUID pendingUserId = UUID.randomUUID();
        Group group = Group.create("Private Group", null, false, ownerId);
        GroupMember owner = GroupMember.owner(group.id(), ownerId);
        GroupMember pendingMember = GroupMember.joined(group.id(), pendingUserId, false);
        GroupMemberResponse expected = new GroupMemberResponse(
                pendingMember.id(),
                group.id(),
                pendingUserId,
                pendingMember.role(),
                pendingMember.status(),
                pendingMember.joinedAt());

        given(groupRepository.findActiveById(group.id())).willReturn(Optional.of(group));
        given(groupMemberRepository.findByGroupIdAndUserId(group.id(), ownerId)).willReturn(Optional.of(owner));
        given(groupMemberRepository.findByIdAndGroupId(pendingMember.id(), group.id())).willReturn(Optional.of(pendingMember));
        given(groupMemberMapper.toResponse(pendingMember)).willReturn(expected);

        GroupMemberResponse response = groupMemberService.approve(ownerId, group.id(), pendingMember.id());

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
        given(groupMemberRepository.findByGroupIdAndUserId(group.id(), userId)).willReturn(Optional.of(member));

        assertThatThrownBy(() -> groupMemberService.approve(userId, group.id(), pendingMemberId))
                .isInstanceOf(GroupMemberPermissionDeniedException.class);
    }

    @Test
    @DisplayName("delete_소유자본인탈퇴_shouldOwnerCannotLeave")
    void delete_소유자본인탈퇴_shouldOwnerCannotLeave() {
        UUID ownerId = UUID.randomUUID();
        Group group = Group.create("Group", null, true, ownerId);
        GroupMember owner = GroupMember.owner(group.id(), ownerId);

        given(groupRepository.findActiveById(group.id())).willReturn(Optional.of(group));
        given(groupMemberRepository.findByIdAndGroupId(owner.id(), group.id())).willReturn(Optional.of(owner));
        given(groupMemberRepository.findByGroupIdAndUserId(group.id(), ownerId)).willReturn(Optional.of(owner));

        assertThatThrownBy(() -> groupMemberService.delete(ownerId, group.id(), owner.id()))
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
        given(groupMemberRepository.findByIdAndGroupId(targetMember.id(), group.id())).willReturn(Optional.of(targetMember));
        given(groupMemberRepository.findByGroupIdAndUserId(group.id(), ownerId)).willReturn(Optional.of(owner));

        groupMemberService.delete(ownerId, group.id(), targetMember.id());

        assertThat(targetMember.blocksRejoin(java.time.LocalDateTime.now())).isTrue();
    }
}

