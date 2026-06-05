package com.synapse.engagement.global.config;

import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig;
import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import io.confluent.kafka.serializers.KafkaAvroDeserializerConfig;
import org.apache.avro.specific.SpecificRecord;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.util.backoff.FixedBackOff;

import java.util.HashMap;
import java.util.Map;

@EnableKafka
@Configuration
@ConditionalOnProperty(prefix = "synapse.kafka", name = "enabled", havingValue = "true")
public class KafkaConsumerConfig {
    private static final Logger log = LoggerFactory.getLogger(KafkaConsumerConfig.class);

    @Bean
    ConsumerFactory<String, SpecificRecord> specificRecordConsumerFactory(
            @Value("${spring.kafka.bootstrap-servers:localhost:9092}") String bootstrapServers,
            @Value("${spring.kafka.consumer.group-id:engagement-svc-group}") String groupId,
            @Value("${spring.kafka.producer.properties.schema.registry.url:http://localhost:8086}") String schemaRegistryUrl
    ) {
        Map<String, Object> config = new HashMap<>();
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        config.put(ErrorHandlingDeserializer.KEY_DESERIALIZER_CLASS, StringDeserializer.class);
        config.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, KafkaAvroDeserializer.class);
        config.put(AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, schemaRegistryUrl);
        config.put(KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG, true);
        return new DefaultKafkaConsumerFactory<>(config);
    }

    @Bean
    ConcurrentKafkaListenerContainerFactory<String, SpecificRecord> specificRecordKafkaListenerContainerFactory(
            ConsumerFactory<String, SpecificRecord> specificRecordConsumerFactory,
            KafkaTemplate<String, SpecificRecord> kafkaTemplate
    ) {
        var factory = new ConcurrentKafkaListenerContainerFactory<String, SpecificRecord>();
        factory.setConsumerFactory(specificRecordConsumerFactory);
        // 처리에 계속 실패한 inbound 이벤트는 유실하지 않고 원본 토픽의 DLQ로 옮긴다.
        // 예: learning.card.review-completed-v1 -> learning.card.review-completed-v1.dlq
        var recoverer = new DeadLetterPublishingRecoverer(
                kafkaTemplate,
                (record, ex) -> new TopicPartition(record.topic() + ".dlq", record.partition())
        );
        // Kafka는 at-least-once라 일시적인 DB/Kafka 오류가 생길 수 있다.
        // 1초 간격으로 3회 재시도한 뒤에도 실패하면 DLQ에 남겨 운영자가 재처리할 수 있게 한다.
        factory.setCommonErrorHandler(new DefaultErrorHandler((record, ex) -> {
            var topic = record == null ? "unknown" : record.topic();
            var offset = record == null ? -1 : record.offset();
            log.warn("Kafka consumer exhausted retries; publishing to DLQ. topic={}, offset={}", topic, offset, ex);
            if (record != null) {
                recoverer.accept(record, ex);
            }
        }, new FixedBackOff(1000L, 3L)));
        return factory;
    }
}
