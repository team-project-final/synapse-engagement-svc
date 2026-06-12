package com.synapse.engagement.shared;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class KafkaTopicResolver {
    private final String topicPrefix;
    private final String levelUp;
    private final String badgeEarned;
    private final String userRegistered;
    private final String reviewCompleted;
    private final String notificationSend;

    public KafkaTopicResolver(
            @Value("${synapse.kafka.topic-prefix:}") String topicPrefix,
            @Value("${synapse.kafka.topics.level-up}") String levelUp,
            @Value("${synapse.kafka.topics.badge-earned}") String badgeEarned,
            @Value("${synapse.kafka.topics.user-registered}") String userRegistered,
            @Value("${synapse.kafka.topics.review-completed}") String reviewCompleted,
            @Value("${synapse.kafka.topics.notification-send}") String notificationSend
    ) {
        this.topicPrefix = topicPrefix == null ? "" : topicPrefix;
        this.levelUp = resolve(levelUp);
        this.badgeEarned = resolve(badgeEarned);
        this.userRegistered = resolve(userRegistered);
        this.reviewCompleted = resolve(reviewCompleted);
        this.notificationSend = resolve(notificationSend);
    }

    public String levelUp() {
        return levelUp;
    }

    public String badgeEarned() {
        return badgeEarned;
    }

    public String userRegistered() {
        return userRegistered;
    }

    public String reviewCompleted() {
        return reviewCompleted;
    }

    public String notificationSend() {
        return notificationSend;
    }

    public String dlq(String sourceTopic) {
        return sourceTopic + ".dlq";
    }

    private String resolve(String baseTopic) {
        if (topicPrefix.isBlank()) {
            return baseTopic;
        }
        return topicPrefix + baseTopic;
    }
}
