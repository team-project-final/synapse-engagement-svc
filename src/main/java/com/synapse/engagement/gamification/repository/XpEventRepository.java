package com.synapse.engagement.gamification.repository;

import com.synapse.engagement.gamification.entity.EventType;
import com.synapse.engagement.gamification.entity.XpEvent;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface XpEventRepository extends JpaRepository<XpEvent, UUID> {

    // eventId 기준 멱등성 확인: 같은 이벤트 메시지를 이미 처리했는지 검사합니다.
    boolean existsByEventId(String eventId);

    // 원본 활동 기준 멱등성 확인: 같은 사용자가 같은 활동으로 XP를 이미 받았는지 검사합니다.
    boolean existsByUserIdAndEventTypeAndSourceId(UUID userId, EventType eventType, String sourceId);

    // XP 이력 화면은 최신 적립 내역부터 보여주므로 createdAt 내림차순으로 조회합니다.
    List<XpEvent> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    default List<XpEvent> findByUserIdOrderByCreatedAtDesc(UUID userId, int size) {
        // 방어적으로 Repository에서도 최대 100개까지만 조회합니다.
        return findByUserIdOrderByCreatedAtDesc(userId, PageRequest.of(0, Math.min(size, 100)));
    }
}

