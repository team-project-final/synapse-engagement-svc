package io.synapse.community.group.member.infrastructure.adapter.outbound.persistence;

import io.synapse.community.group.member.domain.model.GroupMember;
import io.synapse.community.group.member.domain.model.MemberStatus;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

// Spring Data JPA 전용 repository입니다. application 계층에서는 직접 사용하지 않습니다.
interface JpaGroupMemberRepository extends JpaRepository<GroupMember, UUID> {

    Optional<GroupMember> findByGroupIdAndUserId(UUID groupId, UUID userId);

    Optional<GroupMember> findByIdAndGroupId(UUID id, UUID groupId);

    List<GroupMember> findByGroupIdAndStatus(UUID groupId, MemberStatus status);

    List<GroupMember> findByGroupIdAndStatusInOrderByJoinedAtAsc(UUID groupId, Collection<MemberStatus> statuses);
}

