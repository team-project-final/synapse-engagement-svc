package com.synapse.engagement.gamification.application.event;

import com.synapse.engagement.BadgeEarned;
import com.synapse.engagement.LevelUp;
import com.synapse.engagement.gamification.api.dto.BadgeResponse;
import com.synapse.engagement.gamification.domain.BadgeConditionType;
import org.apache.avro.specific.SpecificRecord;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

class GamificationKafkaAclSimulationTests {
    @Test
    void producerUsesOnlyAllowedGamificationTopicsWithTenantPartitionKey() {
        @SuppressWarnings("unchecked")
        KafkaTemplate<String, SpecificRecord> kafkaTemplate = mock(KafkaTemplate.class);
        var producer = new GamificationKafkaProducer(
                kafkaTemplate,
                "engagement.gamification.level-up-v1",
                "engagement.gamification.badge-earned-v1"
        );

        producer.publishLevelUp(70L, "tenant-acl", 1, 2, 120);
        producer.publishBadgeEarned(
                70L,
                "tenant-acl",
                new BadgeResponse(
                        "LEVEL_2",
                        "Level 2",
                        "Reach level 2",
                        null,
                        BadgeConditionType.LEVEL,
                        2,
                        Instant.now()
                )
        );

        var levelUpCaptor = ArgumentCaptor.forClass(SpecificRecord.class);
        var badgeEarnedCaptor = ArgumentCaptor.forClass(SpecificRecord.class);
        verify(kafkaTemplate).send(
                eq("engagement.gamification.level-up-v1"),
                eq("tenant-acl"),
                levelUpCaptor.capture()
        );
        verify(kafkaTemplate).send(
                eq("engagement.gamification.badge-earned-v1"),
                eq("tenant-acl"),
                badgeEarnedCaptor.capture()
        );
        verifyNoMoreInteractions(kafkaTemplate);

        assertThat(levelUpCaptor.getValue()).isInstanceOf(LevelUp.class);
        assertThat(badgeEarnedCaptor.getValue()).isInstanceOf(BadgeEarned.class);
    }
}
