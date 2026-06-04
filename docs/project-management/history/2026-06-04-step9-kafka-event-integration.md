# Step 9 Kafka 이벤트 통합

## 작업일

- 2026-06-04

## 작업 내용

- W4 Step 9 기준으로 `learning.card.review-completed-v1` inbound 이벤트를 engagement-svc XP 적립 흐름에 연결된 상태로 검증했다.
- `ReviewCompleted` 수신 시 `GamificationService.addXp`가 호출되고, `CARD_REVIEWED` XP 이벤트가 저장되며, 중복 수신은 기존 XP 멱등성 검증으로 skip되도록 확인했다.
- 레벨 상승과 배지 수여 결과가 `engagement.gamification.level-up-v1`, `engagement.gamification.badge-earned-v1` Avro 이벤트로 발행되는지 EmbeddedKafka 통합 테스트를 추가했다.
- Consumer 실패 처리를 1초 간격 3회 재시도 후 `{원본토픽}.dlq`로 발행하는 정책으로 보강했다.
- W4 Step 9 workflow, TASK, HISTORY 대시보드를 현재 코드와 검증 결과 기준으로 갱신했다.

## 검증

- `.\gradlew.bat test --tests "com.synapse.engagement.gamification.application.event.*"` 통과.
- `EngagementKafkaStep9IntegrationTests`에서 `ReviewCompleted` publish → XP 적립 → level-up/badge-earned consume 흐름을 검증했다.
- `EngagementKafkaEventHandlerTests`에서 UserRegistered profile 생성, ReviewCompleted XP 적립, 중복 XP skip을 검증했다.
- `GamificationKafkaProducerTests`에서 Avro Producer publish/consume 경로를 검증했다.

## 남은 이슈

- 실제 Kafka broker ACL 적용 검증은 Step 7 잔여 항목으로 남아 있다.
- 실제 platform notification 서비스와의 E2E 연동 검증은 외부 서비스 준비 후 Step 10 또는 통합 검증 단계에서 수행해야 한다.
- inbound shared `ReviewCompleted` 스키마에 `eventId`가 없어서 현재 멱등성 키는 `cardId + reviewedAt` 조합을 사용한다. shared 계약에 `eventId`가 추가되면 해당 필드 기준으로 전환하는 것이 좋다.
