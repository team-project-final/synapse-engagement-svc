package io.synapse.community.group.repository;

import io.synapse.community.group.entity.Group;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface GroupRepository extends JpaRepository<Group, UUID> {

    long countByOwnerIdAndDeletedAtIsNull(UUID ownerId);

    List<Group> findByOwnerIdAndDeletedAtIsNull(UUID ownerId, Pageable pageable);

    Optional<Group> findByIdAndDeletedAtIsNull(UUID id);

    default long countActiveGroupsByOwnerId(UUID ownerId) {
        return countByOwnerIdAndDeletedAtIsNull(ownerId);
    }

    default Optional<Group> findActiveById(UUID id) {
        return findByIdAndDeletedAtIsNull(id);
    }

    @Query("""
            select g
            from #{#entityName} g
            where g.deletedAt is null
            order by g.createdAt desc, g.id desc
            """)
    // 첫 페이지는 최신 생성순으로 삭제되지 않은 그룹만 가져옵니다.
    List<Group> findFirstVisibleGroups(Pageable pageable);

    @Query("""
            select g
            from #{#entityName} g
            where g.deletedAt is null
              and (g.createdAt < :createdAt or (g.createdAt = :createdAt and g.id < :id))
            order by g.createdAt desc, g.id desc
            """)
    // 다음 페이지는 마지막으로 본 그룹보다 뒤에 오는 데이터만 가져옵니다.
    List<Group> findVisibleGroupsAfter(
            @Param("createdAt") LocalDateTime createdAt,
            @Param("id") UUID id,
            Pageable pageable);
}
