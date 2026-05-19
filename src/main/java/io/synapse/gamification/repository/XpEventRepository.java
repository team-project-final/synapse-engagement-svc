package io.synapse.gamification.repository;

import io.synapse.gamification.entity.EventType;
import io.synapse.gamification.entity.XpEvent;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface XpEventRepository extends JpaRepository<XpEvent, UUID> {

    boolean existsByEventId(String eventId);

    boolean existsByUserIdAndEventTypeAndSourceId(UUID userId, EventType eventType, String sourceId);

    List<XpEvent> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    default List<XpEvent> findByUserIdOrderByCreatedAtDesc(UUID userId, int size) {
        return findByUserIdOrderByCreatedAtDesc(userId, PageRequest.of(0, Math.min(size, 100)));
    }
}
