package io.synapse.community.group.member.infrastructure.adapter.outbound.persistence;

import io.synapse.community.group.member.domain.model.GroupMember;
import io.synapse.community.group.member.domain.model.MemberStatus;
import io.synapse.community.group.member.domain.port.outbound.GroupMemberRepositoryPort;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
// GroupMemberRepositoryPort를 JPA로 구현하는 outbound adapter입니다.
class GroupMemberPersistenceAdapter implements GroupMemberRepositoryPort {

    private final JpaGroupMemberRepository memberRepository;

    GroupMemberPersistenceAdapter(JpaGroupMemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @Override
    public Optional<GroupMember> findByGroupIdAndUserId(UUID groupId, UUID userId) {
        return memberRepository.findByGroupIdAndUserId(groupId, userId);
    }

    @Override
    public Optional<GroupMember> findByIdAndGroupId(UUID id, UUID groupId) {
        return memberRepository.findByIdAndGroupId(id, groupId);
    }

    @Override
    public List<GroupMember> findByGroupIdAndStatusInOrderByJoinedAtAsc(
            UUID groupId,
            Collection<MemberStatus> statuses) {
        return memberRepository.findByGroupIdAndStatusInOrderByJoinedAtAsc(groupId, statuses);
    }

    @Override
    public GroupMember save(GroupMember member) {
        return memberRepository.save(member);
    }
}
