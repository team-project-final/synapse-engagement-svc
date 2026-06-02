# Kafka Consumer 보완

## 작업일

- 2026-06-02

## 작업 내용

- GitHub issue #9, #15에서 지적된 engagement-svc의 Kafka Consumer 미구현 문제를 확인했다.
- `synapse-shared`의 inbound Avro 계약을 engagement-svc에 벤더링했다.
  - `src/main/avro/platform/UserRegistered.avsc`
  - `src/main/avro/learning/ReviewCompleted.avsc`
- `KafkaConsumerConfig`를 추가해 Avro Consumer 설정을 구성했다.
  - Consumer group: `engagement-svc-group`
  - key deserializer: `StringDeserializer`
  - value deserializer: `KafkaAvroDeserializer`
  - `specific.avro.reader=true`
  - 역직렬화/처리 실패 시 로그 후 skip하는 `DefaultErrorHandler` 설정
- `EngagementKafkaConsumer`를 추가해 inbound Kafka topic을 수신하게 했다.
  - `platform.auth.user-registered-v1`
  - `learning.card.review-completed-v1`
- `EngagementKafkaEventHandler`를 추가해 Consumer가 받은 이벤트를 기존 도메인 로직으로 연결했다.
  - `UserRegistered` 수신 시 gamification profile 자동 생성
  - `ReviewCompleted` 수신 시 기존 `GamificationService.addXp`를 호출해 XP 적립
  - 중복 review 이벤트는 `ConflictException`을 잡아 Consumer가 죽지 않고 skip하도록 처리
- `application.yml`에 inbound topic 이름과 consumer group 설정을 추가했다.

## 검증

- 신규 Consumer handler 테스트를 추가했다.
  - 신규 가입 이벤트 수신 시 프로필 생성
  - 이미 존재하는 프로필이면 중복 생성하지 않음
  - 복습 완료 이벤트 수신 시 `CARD_REVIEWED` XP 적립 요청 생성
  - 중복 XP 이벤트가 들어와도 Consumer가 예외로 중단되지 않음
- 전체 테스트를 clean부터 재실행했다.
  - 명령: `.\gradlew.bat clean test`
  - 결과: `suites=17 tests=47 failures=0 errors=0 skipped=0`

## 남은 이슈

- `ReviewCompleted` shared schema에는 `eventId` 또는 `reviewId`가 없어서, 현재는 `cardId + reviewedAt` 조합으로 멱등성 키를 만든다.
- 실제 Kafka E2E 검증은 shared `kafka-e2e-test.sh --scenarios` 또는 로컬 docker-compose로 별도 수행해야 한다.
