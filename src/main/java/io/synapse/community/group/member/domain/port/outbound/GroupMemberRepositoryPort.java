package io.synapse.community.group.member.domain.port.outbound;

import io.synapse.community.group.member.domain.model.GroupMember;
import io.synapse.community.group.member.domain.model.MemberStatus;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

// application 계층이 멤버 저장소 구현을 모르고 사용할 수 있게 하는 포트입니다.
public interface GroupMemberRepositoryPort {

    Optional<GroupMember> findByGroupIdAndUserId(UUID groupId, UUID userId);

    Optional<GroupMember> findByIdAndGroupId(UUID id, UUID groupId);

    List<GroupMember> findByGroupIdAndStatusInOrderByJoinedAtAsc(UUID groupId, Collection<MemberStatus> statuses);

    GroupMember save(GroupMember member);
}
