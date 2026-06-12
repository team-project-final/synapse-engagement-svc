package com.synapse.engagement.shared;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class KafkaTopicResolverTests {
    @Test
    void keepsBaseTopicsWhenPrefixIsBlank() {
        var resolver = resolver("");

        assertThat(resolver.userRegistered()).isEqualTo("platform.auth.user-registered-v1");
        assertThat(resolver.reviewCompleted()).isEqualTo("learning.card.review-completed-v1");
        assertThat(resolver.levelUp()).isEqualTo("engagement.gamification.level-up-v1");
        assertThat(resolver.badgeEarned()).isEqualTo("engagement.gamification.badge-earned-v1");
        assertThat(resolver.notificationSend()).isEqualTo("platform.notification.notification-send-v1");
        assertThat(resolver.dlq(resolver.reviewCompleted())).isEqualTo("learning.card.review-completed-v1.dlq");
    }

    @Test
    void prefixesAllKafkaTopics() {
        var resolver = resolver("dev.");

        assertThat(resolver.userRegistered()).isEqualTo("dev.platform.auth.user-registered-v1");
        assertThat(resolver.reviewCompleted()).isEqualTo("dev.learning.card.review-completed-v1");
        assertThat(resolver.levelUp()).isEqualTo("dev.engagement.gamification.level-up-v1");
        assertThat(resolver.badgeEarned()).isEqualTo("dev.engagement.gamification.badge-earned-v1");
        assertThat(resolver.notificationSend()).isEqualTo("dev.platform.notification.notification-send-v1");
        assertThat(resolver.dlq(resolver.reviewCompleted())).isEqualTo("dev.learning.card.review-completed-v1.dlq");
    }

    private KafkaTopicResolver resolver(String prefix) {
        return new KafkaTopicResolver(
                prefix,
                "engagement.gamification.level-up-v1",
                "engagement.gamification.badge-earned-v1",
                "platform.auth.user-registered-v1",
                "learning.card.review-completed-v1",
                "platform.notification.notification-send-v1"
        );
    }
}
