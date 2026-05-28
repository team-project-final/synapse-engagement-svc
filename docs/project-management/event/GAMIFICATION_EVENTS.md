# Gamification Events

## Topics

| Event | Topic | Partition key | Local E2E config | Producer |
|------|-------|---------------|------------------|----------|
| `gamification.level_up` | `engagement.gamification.level-up-v1` | `userId` | partitions=1, replication=1, retention.ms=604800000 | `synapse-engagement-svc` |
| `gamification.badge_earned` | `engagement.gamification.badge-earned-v1` | `userId` | partitions=1, replication=1, retention.ms=604800000 | `synapse-engagement-svc` |

## CloudEvents Envelope

All messages are JSON encoded CloudEvents 1.0 envelopes.

- `source`: `engagement-svc`
- `type`: `com.synapse.event.engagement.GamificationLevelUp` or `com.synapse.event.engagement.GamificationBadgeEarned`
- `tenantid`: required
- `datacontenttype`: `application/json`
- `data`: event payload

## Payloads

Schema drafts are stored under `src/main/resources/avro/`.

- `GamificationLevelUp.avsc`
- `GamificationBadgeEarned.avsc`

Schema Registry subjects:

- `engagement.gamification.level-up-v1-value`
- `engagement.gamification.badge-earned-v1-value`

Compatibility mode:

- `BACKWARD`

## Consumer Notes

- Consumers should treat delivery as at-least-once.
- Use the CloudEvents `id` as the idempotency key.
- Keep processed event IDs for at least 7 days.
- Payloads do not include private profile or authentication data.
