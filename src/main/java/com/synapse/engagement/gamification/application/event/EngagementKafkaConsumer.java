package com.synapse.engagement.gamification.application.event;

import com.synapse.learning.ReviewCompleted;
import com.synapse.platform.UserRegistered;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "synapse.kafka", name = "enabled", havingValue = "true")
public class EngagementKafkaConsumer {
    private final EngagementKafkaEventHandler eventHandler;

    public EngagementKafkaConsumer(EngagementKafkaEventHandler eventHandler) {
        this.eventHandler = eventHandler;
    }

    @KafkaListener(
            topics = "${synapse.kafka.topics.user-registered}",
            groupId = "${spring.kafka.consumer.group-id:engagement-svc-group}",
            containerFactory = "specificRecordKafkaListenerContainerFactory"
    )
    public void onUserRegistered(UserRegistered event) {
        eventHandler.handleUserRegistered(event);
    }

    @KafkaListener(
            topics = "${synapse.kafka.topics.review-completed}",
            groupId = "${spring.kafka.consumer.group-id:engagement-svc-group}",
            containerFactory = "specificRecordKafkaListenerContainerFactory"
    )
    public void onReviewCompleted(ReviewCompleted event) {
        eventHandler.handleReviewCompleted(event);
    }
}
