package com.synapse.engagement.community.repository;

import com.synapse.engagement.community.domain.Group;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GroupRepository extends JpaRepository<Group, Long> {
    long countByOwnerIdAndDeletedAtIsNull(Long ownerId);

    Optional<Group> findByIdAndDeletedAtIsNull(Long id);

    List<Group> findByDeletedAtIsNullOrderByCreatedAtDesc(Pageable pageable);
}
