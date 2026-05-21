package com.synapse.engagement.community.service;

import com.synapse.engagement.community.entity.Group;
import com.synapse.engagement.community.exception.GroupNotFoundException;
import com.synapse.engagement.community.repository.GroupRepository;
import com.synapse.engagement.community.entity.GroupMember;
import com.synapse.engagement.community.entity.GroupMemberStatus;
import com.synapse.engagement.community.repository.GroupMemberRepository;
import com.synapse.engagement.community.dto.request.GroupMemberInviteRequest;
import com.synapse.engagement.community.dto.response.GroupMemberResponse;
import com.synapse.engagement.community.exception.GroupMemberAlreadyExistsException;
import com.synapse.engagement.community.exception.GroupMemberNotFoundException;
import com.synapse.engagement.community.exception.GroupMemberPermissionDeniedException;
import com.synapse.engagement.community.exception.GroupMemberRejoinBlockedException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
// 그룹 멤버의 초대, 가입, 승인, 탈퇴/강퇴 흐름을 담당하는 usecase입니다.
public class GroupMemberService {

    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final GroupMemberMapper groupMemberMapper;

    GroupMemberService(
            GroupRepository groupRepository,
            GroupMemberRepository groupMemberRepository,
            GroupMemberMapper groupMemberMapper) {
        this.groupRepository = groupRepository;
        this.groupMemberRepository = groupMemberRepository;
        this.groupMemberMapper = groupMemberMapper;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public GroupMemberResponse invite(UUID currentUserId, UUID groupId, GroupMemberInviteRequest request) {
        Group group = findGroup(groupId);
        // 초대는 그룹 OWNER만 할 수 있습니다.
        requireOwner(currentUserId, groupId);
        GroupMember member = joinOrReactivate(group, request.userId(), true);

        return groupMemberMapper.toResponse(member);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public GroupMemberResponse join(UUID currentUserId, UUID groupId) {
        Group group = findGroup(groupId);
        // 공개 그룹은 즉시 ACTIVE, 비공개 그룹은 PENDING 상태로 가입됩니다.
        GroupMember member = joinOrReactivate(group, currentUserId, group.isPublic());

        return groupMemberMapper.toResponse(member);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public GroupMemberResponse approve(UUID currentUserId, UUID groupId, UUID memberId) {
        findGroup(groupId);
        // 가입 승인은 OWNER 또는 ADMIN만 할 수 있습니다.
        requireManager(currentUserId, groupId);
        GroupMember member = findGroupMember(groupId, memberId);
        member.approve();

        return groupMemberMapper.toResponse(member);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void delete(UUID currentUserId, UUID groupId, UUID memberId) {
        findGroup(groupId);
        GroupMember member = findGroupMember(groupId, memberId);
        GroupMember actor = findGroupMembership(groupId, currentUserId);

        if (member.userId().equals(currentUserId)) {
            // 본인 요청이면 탈퇴 처리입니다.
            member.leave(currentUserId);
            return;
        }

        if (!actor.canManageMembers()) {
            throw new GroupMemberPermissionDeniedException(groupId);
        }

        // 다른 멤버를 삭제하는 요청이면 강퇴 처리입니다.
        member.kick();
    }

    @Transactional(readOnly = true)
    public List<GroupMemberResponse> list(UUID currentUserId, UUID groupId) {
        findGroup(groupId);
        findGroupMembership(groupId, currentUserId);

        return groupMemberRepository
                .findByGroupIdAndStatusInOrderByJoinedAtAsc(
                        groupId,
                        List.of(GroupMemberStatus.PENDING, GroupMemberStatus.ACTIVE))
                .stream()
                .map(groupMemberMapper::toResponse)
                .toList();
    }

    private GroupMember joinOrReactivate(Group group, UUID userId, boolean approveNow) {
        // 기존 멤버 기록이 있으면 재가입 가능 여부를 확인하고, 없으면 새 멤버를 만듭니다.
        return groupMemberRepository.findByGroupIdAndUserId(group.id(), userId)
                .map(existing -> reactivateIfAllowed(existing, userId, approveNow))
                .orElseGet(() -> createNewMember(group, userId, approveNow));
    }

    private GroupMember createNewMember(Group group, UUID userId, boolean approveNow) {
        return groupMemberRepository.save(GroupMember.joined(group.id(), userId, approveNow));
    }

    private GroupMember reactivateIfAllowed(GroupMember member, UUID userId, boolean approveNow) {
        if (member.isActive()) {
            throw new GroupMemberAlreadyExistsException(userId);
        }
        if (member.blocksRejoin(LocalDateTime.now())) {
            // 강퇴된 사용자는 일정 기간 동안 다시 가입할 수 없습니다.
            throw new GroupMemberRejoinBlockedException(userId);
        }

        member.reactivate(approveNow);
        return member;
    }

    private void requireOwner(UUID currentUserId, UUID groupId) {
        if (!findGroupMembership(groupId, currentUserId).isOwner()) {
            throw new GroupMemberPermissionDeniedException(groupId);
        }
    }

    private void requireManager(UUID currentUserId, UUID groupId) {
        if (!findGroupMembership(groupId, currentUserId).canManageMembers()) {
            throw new GroupMemberPermissionDeniedException(groupId);
        }
    }

    private GroupMember findGroupMembership(UUID groupId, UUID userId) {
        // 권한 검사는 ACTIVE 멤버만 통과합니다.
        return groupMemberRepository.findByGroupIdAndUserId(groupId, userId)
                .filter(GroupMember::isActive)
                .orElseThrow(() -> new GroupMemberPermissionDeniedException(groupId));
    }

    private GroupMember findGroupMember(UUID groupId, UUID memberId) {
        return groupMemberRepository.findByIdAndGroupId(memberId, groupId)
                .orElseThrow(() -> new GroupMemberNotFoundException(memberId));
    }

    private Group findGroup(UUID groupId) {
        return groupRepository.findActiveById(groupId)
                .orElseThrow(() -> new GroupNotFoundException(groupId));
    }
}

