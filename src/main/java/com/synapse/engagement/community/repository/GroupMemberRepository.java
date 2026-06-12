package com.synapse.engagement.community.repository;

import com.synapse.engagement.community.domain.GroupMember;
import com.synapse.engagement.community.domain.MemberStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface GroupMemberRepository extends JpaRepository<GroupMember, Long> {
    Optional<GroupMember> findByGroupIdAndUserId(Long groupId, Long userId);

    Optional<GroupMember> findByGroupIdAndInviteToken(Long groupId, String inviteToken);

    Optional<GroupMember> findByIdAndGroupId(Long id, Long groupId);

    List<GroupMember> findByGroupId(Long groupId);

    List<GroupMember> findByGroupIdAndStatus(Long groupId, MemberStatus status);

    List<GroupMember> findByGroupIdAndStatusIn(Long groupId, List<MemberStatus> statuses);

    boolean existsByGroupIdAndUserId(Long groupId, Long userId);

    boolean existsByGroupIdAndUserIdAndStatusAndKickedAtAfter(
            Long groupId,
            Long userId,
            MemberStatus status,
            Instant kickedAfter
    );
}
