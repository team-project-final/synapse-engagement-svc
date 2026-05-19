package io.synapse.community.member.repository;

import io.synapse.community.member.entity.GroupMember;
import io.synapse.community.member.entity.MemberStatus;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GroupMemberRepository extends JpaRepository<GroupMember, UUID> {

    Optional<GroupMember> findByGroupIdAndUserId(UUID groupId, UUID userId);

    Optional<GroupMember> findByIdAndGroupId(UUID id, UUID groupId);

    List<GroupMember> findByGroupIdAndStatus(UUID groupId, MemberStatus status);

    List<GroupMember> findByGroupIdAndStatusInOrderByJoinedAtAsc(UUID groupId, Collection<MemberStatus> statuses);
}
