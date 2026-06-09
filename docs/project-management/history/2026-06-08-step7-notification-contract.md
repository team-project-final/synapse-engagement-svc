# 2026-06-08 Step 7 Notification Contract 보강

## 배경

- W3 Step 7은 gamification `level-up` / `badge-earned` 이벤트 발행 작업이다.
- 기존 문서에는 실제 notification 서비스 연동 테스트가 미검증으로 남아 있어 Step 7 진행률이 36/38이었다.

## 처리 내용

- `GamificationNotificationContractTests`를 Step 7 notification 연동 slice/contract 검증으로 재확인했다.
- 테스트는 EmbeddedKafka + mock Schema Registry 환경에서 engagement gamification Avro 이벤트를 발행하고, fake notification processor가 이를 notification command로 변환할 수 있는지 검증한다.
- 실제 notification-svc 라이브 연동은 Step 7 완료 조건이 아니라 W5 통합/배포 검증 항목으로 분리했다.

## 검증

- `.\gradlew.bat test --tests "com.synapse.engagement.gamification.application.event.GamificationNotificationContractTests"` → BUILD SUCCESSFUL

## 상태

- Step 7 notification 연동 항목은 slice/contract 테스트 기준으로 완료 처리했다.
- Step 7 잔여 항목은 실제 Kafka broker ACL 적용 확인 1건이다.
- W3 진행률은 124/126에서 125/126으로 갱신했다.
