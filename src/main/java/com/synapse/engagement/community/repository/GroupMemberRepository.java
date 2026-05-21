package com.synapse.engagement.community.repository;

import com.synapse.engagement.community.entity.GroupMember;
import com.synapse.engagement.community.entity.GroupMemberStatus;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GroupMemberRepository extends JpaRepository<GroupMember, UUID> {

    Optional<GroupMember> findByGroupIdAndUserId(UUID groupId, UUID userId);

    Optional<GroupMember> findByIdAndGroupId(UUID id, UUID groupId);

    List<GroupMember> findByGroupIdAndStatusInOrderByJoinedAtAsc(UUID groupId, Collection<GroupMemberStatus> statuses);
}

