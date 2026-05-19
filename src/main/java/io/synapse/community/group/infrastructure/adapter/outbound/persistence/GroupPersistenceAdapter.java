package io.synapse.community.group.infrastructure.adapter.outbound.persistence;

import io.synapse.community.group.domain.model.Group;
import io.synapse.community.group.domain.port.outbound.GroupRepositoryPort;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
// GroupRepositoryPort를 JPA로 구현하는 outbound adapter입니다.
class GroupPersistenceAdapter implements GroupRepositoryPort {

    private final JpaGroupRepository groupRepository;

    GroupPersistenceAdapter(JpaGroupRepository groupRepository) {
        this.groupRepository = groupRepository;
    }

    @Override
    public long countActiveGroupsByOwnerId(UUID ownerId) {
        return groupRepository.countByOwnerIdAndDeletedAtIsNull(ownerId);
    }

    @Override
    public Optional<Group> findActiveById(UUID id) {
        return groupRepository.findByIdAndDeletedAtIsNull(id);
    }

    @Override
    public List<Group> findFirstVisibleGroups(Pageable pageable) {
        return groupRepository.findFirstVisibleGroups(pageable);
    }

    @Override
    public List<Group> findVisibleGroupsAfter(LocalDateTime createdAt, UUID id, Pageable pageable) {
        return groupRepository.findVisibleGroupsAfter(createdAt, id, pageable);
    }

    @Override
    public Group save(Group group) {
        return groupRepository.save(group);
    }
}
