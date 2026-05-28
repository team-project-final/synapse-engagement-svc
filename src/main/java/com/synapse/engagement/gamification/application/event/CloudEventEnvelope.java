package com.synapse.engagement.gamification.application.event;

import java.time.Instant;

public record CloudEventEnvelope<T>(
        String specversion,
        String id,
        String source,
        String type,
        Instant time,
        String tenantid,
        String datacontenttype,
        String traceparent,
        T data
) {
    public static <T> CloudEventEnvelope<T> create(String id, String type, String tenantId, T data) {
        return new CloudEventEnvelope<>(
                "1.0",
                id,
                "engagement-svc",
                type,
                Instant.now(),
                tenantId,
                "application/json",
                null,
                data
        );
    }
}
