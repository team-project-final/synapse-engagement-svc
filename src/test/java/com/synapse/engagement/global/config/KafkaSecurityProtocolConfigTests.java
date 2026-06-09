package com.synapse.engagement.global.config;

import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;

import static org.assertj.core.api.Assertions.assertThat;

class KafkaSecurityProtocolConfigTests {
    @Test
    void producerFactory_sslSecurityProtocol_shouldPassProtocolToKafkaClient() {
        // Given
        var config = new KafkaProducerConfig();

        // When
        var factory = (DefaultKafkaProducerFactory<?, ?>) config.producerFactory(
                "b-1.msk.local:9094",
                "SSL",
                "http://schema-registry:8081"
        );

        // Then
        // #26 회귀 방지: Spring env에 SSL이 있어도 커스텀 props에 빠지면 MSK 연결은 실패한다.
        assertThat(factory.getConfigurationProperties())
                .containsEntry(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "b-1.msk.local:9094")
                .containsEntry(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SSL");
    }

    @Test
    void consumerFactory_sslSecurityProtocol_shouldPassProtocolToKafkaClient() {
        // Given
        var config = new KafkaConsumerConfig();

        // When
        var factory = (DefaultKafkaConsumerFactory<?, ?>) config.specificRecordConsumerFactory(
                "b-1.msk.local:9094",
                "engagement-svc-group",
                "SSL",
                "http://schema-registry:8081"
        );

        // Then
        // Consumer도 별도 Factory를 쓰므로 Producer와 독립적으로 security.protocol을 검증한다.
        assertThat(factory.getConfigurationProperties())
                .containsEntry(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "b-1.msk.local:9094")
                .containsEntry(ConsumerConfig.GROUP_ID_CONFIG, "engagement-svc-group")
                .containsEntry(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SSL");
    }
}
