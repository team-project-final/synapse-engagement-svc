package io.synapse.community.group.domain.port.outbound;

import io.synapse.community.group.domain.model.Group;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Pageable;

// application 계층이 DB 구현체를 직접 모르고 그룹 저장소를 사용할 수 있게 하는 포트입니다.
public interface GroupRepositoryPort {

    long countActiveGroupsByOwnerId(UUID ownerId);

    Optional<Group> findActiveById(UUID id);

    List<Group> findFirstVisibleGroups(Pageable pageable);

    List<Group> findVisibleGroupsAfter(LocalDateTime createdAt, UUID id, Pageable pageable);

    Group save(Group group);
}
