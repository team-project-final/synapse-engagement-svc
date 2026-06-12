# Step 7 Kafka 리팩토링

## 작업일

- 2026년 6월 1일

## 작업 내용

- `D-002_SCHEMA_FAMILY_DECISION.md`와 `EVENT_CONTRACT_STANDARD.md` 기준으로 Step 7 Kafka 이벤트 발행 방식을 다시 확인했다.
- 기존 CloudEvents JSON 문자열 발행 구조를 제거하고, `synapse-shared` 표준에 맞춰 `LevelUp`, `BadgeEarned` Avro record 기반 발행 구조로 리팩토링했다.
- `src/main/avro/engagement/LevelUp.avsc`, `src/main/avro/engagement/BadgeEarned.avsc`를 추가해 shared Avro 스키마를 벤더링했다.
- Gradle에 Avro codegen plugin, Avro 라이브러리, Confluent `kafka-avro-serializer`, Confluent Maven repository 설정을 추가했다.
- Kafka Producer 설정을 `StringSerializer` 기반 JSON 발행에서 `KafkaAvroSerializer` + Schema Registry 기반 발행으로 변경했다.
- `GamificationKafkaProducer`가 generated Avro class인 `com.synapse.engagement.LevelUp`, `com.synapse.engagement.BadgeEarned`를 직접 생성해 Kafka로 보내도록 수정했다.
- Kafka partition key를 `tenantId`로 맞추고, Avro record 내부에 `eventId`, `tenantId`, `occurredAt` 공통 메타 필드가 들어가도록 정리했다.
- 기존 `CloudEventEnvelope`, `LevelUpEvent`, `BadgeEarnedEvent` DTO와 `src/main/resources/avro/Gamification*.avsc` 임시 스키마를 제거했다.
- `GamificationKafkaProducerTests`를 JSON 문자열 검증에서 EmbeddedKafka + mock Schema Registry 기반 Avro produce/consume 검증으로 변경했다.
- 실제 notification 서비스와 Kafka ACL은 외부 프로세스/인프라가 필요하므로, mock 기반 계약 테스트를 추가했다.
- `GamificationNotificationContractTests`를 추가해 fake notification processor가 `level-up`, `badge-earned` Avro 이벤트를 읽고 notification command로 변환할 수 있음을 검증했다.
- `GamificationKafkaAclSimulationTests`를 추가해 Producer가 허용된 두 gamification 토픽에만 `tenantId` key로 발행하는지 검증했다.
- `GAMIFICATION_EVENTS.md`, `TASK_engagement.md`, `WORKFLOW_engagement_W3.md`를 Avro + Schema Registry 기준으로 갱신했다.
- Workflow에는 mock notification/ACL 검증이 완료됐지만 실제 broker ACL 및 실제 platform notification 연동은 아직 남은 항목임을 주석으로 구분해 적었다.
- 프로젝트 이해를 돕기 위해 컨트롤러, 서비스, Kafka 설정, JWT/tenant 처리, 예외 처리, community/gamification 주요 흐름에 설명 주석을 추가했다.
- 생성자나 단순 getter 같은 뻔한 코드에는 주석을 달지 않고, 권한 검증, 멱등성, 상태 전이, 이벤트 발행, mock 검증처럼 이해가 필요한 지점에만 주석을 달았다.

## 검증

- `.\gradlew.bat test --tests "com.synapse.engagement.gamification.application.event.GamificationNotificationContractTests" --tests "com.synapse.engagement.gamification.application.event.GamificationKafkaAclSimulationTests"` 성공
- `.\gradlew.bat clean test` 성공

## 남은 이슈

- 실제 Kafka broker ACL 적용 검증은 아직 필요하다.
- 실제 platform notification 서비스와의 E2E 연동 검증은 아직 필요하다.
