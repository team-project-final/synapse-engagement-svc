package io.synapse.community.member.service;

import io.synapse.community.group.entity.Group;
import io.synapse.community.group.exception.GroupNotFoundException;
import io.synapse.community.group.repository.GroupRepository;
import io.synapse.community.member.entity.GroupMember;
import io.synapse.community.member.entity.MemberStatus;
import io.synapse.community.member.repository.GroupMemberRepository;
import io.synapse.community.member.dto.MemberInviteRequest;
import io.synapse.community.member.dto.MemberResponse;
import io.synapse.community.member.exception.MemberAlreadyExistsException;
import io.synapse.community.member.exception.MemberNotFoundException;
import io.synapse.community.member.exception.MemberPermissionDeniedException;
import io.synapse.community.member.exception.MemberRejoinBlockedException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
// 그룹 멤버의 초대, 가입, 승인, 탈퇴/강퇴 흐름을 담당하는 usecase입니다.
public class MemberService {

    private final GroupRepository groupRepository;
    private final GroupMemberRepository memberRepository;
    private final MemberMapper memberMapper;

    MemberService(
            GroupRepository groupRepository,
            GroupMemberRepository memberRepository,
            MemberMapper memberMapper) {
        this.groupRepository = groupRepository;
        this.memberRepository = memberRepository;
        this.memberMapper = memberMapper;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public MemberResponse invite(UUID currentUserId, UUID groupId, MemberInviteRequest request) {
        Group group = findGroup(groupId);
        // 초대는 그룹 OWNER만 할 수 있습니다.
        requireOwner(currentUserId, groupId);
        GroupMember member = joinOrReactivate(group, request.userId(), true);

        return memberMapper.toResponse(member);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public MemberResponse join(UUID currentUserId, UUID groupId) {
        Group group = findGroup(groupId);
        // 공개 그룹은 즉시 ACTIVE, 비공개 그룹은 PENDING 상태로 가입됩니다.
        GroupMember member = joinOrReactivate(group, currentUserId, group.isPublic());

        return memberMapper.toResponse(member);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public MemberResponse approve(UUID currentUserId, UUID groupId, UUID memberId) {
        findGroup(groupId);
        // 가입 승인은 OWNER 또는 ADMIN만 할 수 있습니다.
        requireManager(currentUserId, groupId);
        GroupMember member = findMember(groupId, memberId);
        member.approve();

        return memberMapper.toResponse(member);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void delete(UUID currentUserId, UUID groupId, UUID memberId) {
        findGroup(groupId);
        GroupMember member = findMember(groupId, memberId);
        GroupMember actor = findMembership(groupId, currentUserId);

        if (member.userId().equals(currentUserId)) {
            // 본인 요청이면 탈퇴 처리입니다.
            member.leave(currentUserId);
            return;
        }

        if (!actor.canManageMembers()) {
            throw new MemberPermissionDeniedException(groupId);
        }

        // 다른 멤버를 삭제하는 요청이면 강퇴 처리입니다.
        member.kick();
    }

    @Transactional(readOnly = true)
    public List<MemberResponse> list(UUID currentUserId, UUID groupId) {
        findGroup(groupId);
        findMembership(groupId, currentUserId);

        return memberRepository
                .findByGroupIdAndStatusInOrderByJoinedAtAsc(
                        groupId,
                        List.of(MemberStatus.PENDING, MemberStatus.ACTIVE))
                .stream()
                .map(memberMapper::toResponse)
                .toList();
    }

    private GroupMember joinOrReactivate(Group group, UUID userId, boolean approveNow) {
        // 기존 멤버 기록이 있으면 재가입 가능 여부를 확인하고, 없으면 새 멤버를 만듭니다.
        return memberRepository.findByGroupIdAndUserId(group.id(), userId)
                .map(existing -> reactivateIfAllowed(existing, userId, approveNow))
                .orElseGet(() -> createNewMember(group, userId, approveNow));
    }

    private GroupMember createNewMember(Group group, UUID userId, boolean approveNow) {
        return memberRepository.save(GroupMember.joined(group.id(), userId, approveNow));
    }

    private GroupMember reactivateIfAllowed(GroupMember member, UUID userId, boolean approveNow) {
        if (member.isActive()) {
            throw new MemberAlreadyExistsException(userId);
        }
        if (member.blocksRejoin(LocalDateTime.now())) {
            // 강퇴된 사용자는 일정 기간 동안 다시 가입할 수 없습니다.
            throw new MemberRejoinBlockedException(userId);
        }

        member.reactivate(approveNow);
        return member;
    }

    private void requireOwner(UUID currentUserId, UUID groupId) {
        if (!findMembership(groupId, currentUserId).isOwner()) {
            throw new MemberPermissionDeniedException(groupId);
        }
    }

    private void requireManager(UUID currentUserId, UUID groupId) {
        if (!findMembership(groupId, currentUserId).canManageMembers()) {
            throw new MemberPermissionDeniedException(groupId);
        }
    }

    private GroupMember findMembership(UUID groupId, UUID userId) {
        // 권한 검사는 ACTIVE 멤버만 통과합니다.
        return memberRepository.findByGroupIdAndUserId(groupId, userId)
                .filter(GroupMember::isActive)
                .orElseThrow(() -> new MemberPermissionDeniedException(groupId));
    }

    private GroupMember findMember(UUID groupId, UUID memberId) {
        return memberRepository.findByIdAndGroupId(memberId, groupId)
                .orElseThrow(() -> new MemberNotFoundException(memberId));
    }

    private Group findGroup(UUID groupId) {
        return groupRepository.findActiveById(groupId)
                .orElseThrow(() -> new GroupNotFoundException(groupId));
    }
}
