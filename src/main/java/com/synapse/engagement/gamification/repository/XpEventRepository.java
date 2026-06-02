package com.synapse.engagement.gamification.repository;

import com.synapse.engagement.gamification.domain.EventType;
import com.synapse.engagement.gamification.domain.XpEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface XpEventRepository extends JpaRepository<XpEvent, Long> {
    boolean existsByEventId(String eventId);

    boolean existsByUserIdAndEventTypeAndSourceId(Long userId, EventType eventType, String sourceId);

    List<XpEvent> findByUserIdOrderByCreatedAtDesc(Long userId);
}
