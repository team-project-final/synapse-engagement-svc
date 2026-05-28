package com.synapse.engagement.community.application;

import com.synapse.engagement.community.api.dto.InviteDecisionResponse;
import com.synapse.engagement.community.api.dto.JoinRequestDecision;
import com.synapse.engagement.community.api.dto.JoinRequestDecisionRequest;
import com.synapse.engagement.community.api.dto.JoinRequestResponse;
import com.synapse.engagement.community.api.dto.MemberInviteRequest;
import com.synapse.engagement.community.api.dto.MemberResponse;
import com.synapse.engagement.community.domain.GroupMember;
import com.synapse.engagement.community.domain.MemberStatus;
import com.synapse.engagement.shared.BadRequestException;
import com.synapse.engagement.community.repository.GroupMemberRepository;
import com.synapse.engagement.shared.ConflictException;
import com.synapse.engagement.shared.ForbiddenException;
import com.synapse.engagement.shared.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class MemberService {
    private static final Duration KICKED_REJOIN_BLOCK = Duration.ofDays(7);
    private static final Duration INVITE_TTL = Duration.ofDays(7);

    private final GroupService groupService;
    private final GroupMemberRepository memberRepository;

    public MemberService(GroupService groupService, GroupMemberRepository memberRepository) {
        this.groupService = groupService;
        this.memberRepository = memberRepository;
    }

    @Transactional
    public InviteDecisionResponse invite(Long groupId, Long actorId, MemberInviteRequest request) {
        var group = groupService.findActiveGroup(groupId);
        requireModerator(groupId, actorId);
        var existing = reusableKickedMembership(groupId, request.userId());
        if (existing != null) {
            existing.reinvite(newInviteToken(), inviteExpiresAt());
            return InviteDecisionResponse.from(existing);
        }
        return memberRepository.findByGroupIdAndUserId(groupId, request.userId())
                .map(member -> reinviteExisting(member, request.userId()))
                .orElseGet(() -> InviteDecisionResponse.from(memberRepository.save(
                        GroupMember.invited(group, request.userId(), newInviteToken(), inviteExpiresAt())
                )));
    }

    @Transactional
    public MemberResponse join(Long groupId, Long userId) {
        var group = groupService.findActiveGroup(groupId);
        var existing = reusableKickedMembership(groupId, userId);
        if (existing != null) {
            existing.reactivate(group.isPublicGroup());
            return MemberResponse.from(existing);
        }
        var existingMembership = memberRepository.findByGroupIdAndUserId(groupId, userId)
                .orElse(null);
        if (existingMembership != null) {
            if (existingMembership.getStatus() == MemberStatus.DECLINED
                    || existingMembership.getStatus() == MemberStatus.REJECTED) {
                existingMembership.requestToJoin(group.isPublicGroup());
                return MemberResponse.from(existingMembership);
            }
            throw new ConflictException("User already has membership in this group");
        }
        return MemberResponse.from(memberRepository.save(GroupMember.joined(group, userId, group.isPublicGroup())));
    }

    @Transactional
    public MemberResponse approve(Long groupId, Long actorId, Long memberId) {
        requireModerator(groupId, actorId);
        var member = findMember(groupId, memberId);
        member.approve();
        return MemberResponse.from(member);
    }

    @Transactional
    public InviteDecisionResponse acceptInvite(Long groupId, Long userId, String token) {
        var member = findInvite(groupId, token);
        requireInviteTarget(member, userId);
        requireUsableInvite(member);
        member.acceptInvite();
        return InviteDecisionResponse.from(member);
    }

    @Transactional
    public InviteDecisionResponse declineInvite(Long groupId, Long userId, String token) {
        var member = findInvite(groupId, token);
        requireInviteTarget(member, userId);
        requireUsableInvite(member);
        member.declineInvite();
        return InviteDecisionResponse.from(member);
    }

    @Transactional(readOnly = true)
    public List<JoinRequestResponse> listJoinRequests(Long groupId, Long actorId) {
        groupService.findActiveGroup(groupId);
        requireModerator(groupId, actorId);
        return memberRepository.findByGroupIdAndStatusIn(groupId, List.of(MemberStatus.PENDING, MemberStatus.INVITED))
                .stream()
                .map(JoinRequestResponse::from)
                .toList();
    }

    @Transactional
    public MemberResponse decideJoinRequest(
            Long groupId,
            Long actorId,
            Long userId,
            JoinRequestDecisionRequest request
    ) {
        requireModerator(groupId, actorId);
        var member = memberRepository.findByGroupIdAndUserId(groupId, userId)
                .orElseThrow(() -> new NotFoundException("Join request not found: userId=" + userId));
        if (member.getStatus() != MemberStatus.PENDING) {
            throw new ConflictException("Only pending join requests can be decided");
        }
        if (request.decision() == JoinRequestDecision.APPROVE) {
            member.approve();
        } else if (request.decision() == JoinRequestDecision.REJECT) {
            member.reject();
        } else {
            throw new BadRequestException("Unsupported join request decision");
        }
        return MemberResponse.from(member);
    }

    @Transactional
    public void remove(Long groupId, Long actorId, Long memberId) {
        var member = findMember(groupId, memberId);
        if (member.isOwner()) {
            throw new ForbiddenException("Group owner cannot leave or be removed without ownership transfer");
        }
        if (!member.getUserId().equals(actorId)) {
            requireModerator(groupId, actorId);
        }
        member.kick();
    }

    @Transactional(readOnly = true)
    public List<MemberResponse> list(Long groupId) {
        groupService.findActiveGroup(groupId);
        return memberRepository.findByGroupId(groupId).stream()
                .map(MemberResponse::from)
                .toList();
    }

    private void ensureCanCreateMembership(Long groupId, Long userId) {
        if (memberRepository.existsByGroupIdAndUserId(groupId, userId)) {
            throw new ConflictException("User already has membership in this group");
        }
    }

    private InviteDecisionResponse reinviteExisting(GroupMember member, Long userId) {
        if (member.getStatus() == MemberStatus.DECLINED || member.getStatus() == MemberStatus.REJECTED) {
            member.reinvite(newInviteToken(), inviteExpiresAt());
            return InviteDecisionResponse.from(member);
        }
        throw new ConflictException("User already has membership in this group");
    }

    private GroupMember reusableKickedMembership(Long groupId, Long userId) {
        return memberRepository.findByGroupIdAndUserId(groupId, userId)
                .filter(member -> member.getStatus() == MemberStatus.KICKED)
                .map(member -> {
                    if (member.getKickedAt() != null
                            && member.getKickedAt().isAfter(Instant.now().minus(KICKED_REJOIN_BLOCK))) {
                        throw new ForbiddenException("Kicked users cannot rejoin for 7 days");
                    }
                    return member;
                })
                .orElse(null);
    }

    private void requireModerator(Long groupId, Long userId) {
        var actor = memberRepository.findByGroupIdAndUserId(groupId, userId)
                .orElseThrow(() -> new ForbiddenException("Group membership is required"));
        if (!actor.canModerate()) {
            throw new ForbiddenException("OWNER or ADMIN role is required");
        }
    }

    private GroupMember findMember(Long groupId, Long memberId) {
        return memberRepository.findByIdAndGroupId(memberId, groupId)
                .orElseThrow(() -> new NotFoundException("Group member not found: id=" + memberId));
    }

    private GroupMember findInvite(Long groupId, String token) {
        return memberRepository.findByGroupIdAndInviteToken(groupId, token)
                .orElseThrow(() -> new NotFoundException("Invite not found"));
    }

    private void requireInviteTarget(GroupMember member, Long userId) {
        if (!member.getUserId().equals(userId)) {
            throw new ForbiddenException("Only the invited user can decide this invite");
        }
    }

    private void requireUsableInvite(GroupMember member) {
        if (member.getStatus() != MemberStatus.INVITED) {
            throw new ConflictException("Invite has already been decided");
        }
        if (member.getInviteExpiresAt() == null || member.getInviteExpiresAt().isBefore(Instant.now())) {
            throw new ConflictException("Invite has expired");
        }
    }

    private String newInviteToken() {
        return UUID.randomUUID().toString();
    }

    private Instant inviteExpiresAt() {
        return Instant.now().plus(INVITE_TTL);
    }
}
