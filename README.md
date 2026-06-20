# synapse-engagement-svc

Synapse engagement service는 커뮤니티 공유/그룹/신고와 gamification XP, 레벨, 배지, 리더보드를 담당하는 Spring Boot 4 서비스입니다.

## Service Surface

| 영역 | 엔드포인트 | 설명 |
|---|---|---|
| Gamification | `GET /api/v1/gamification/me` | 현재 사용자의 XP, 레벨, 스트릭, 배지 요약 |
| Gamification | `GET /api/v1/gamification/xp/history` | XP 이벤트 이력 |
| Gamification | `GET /api/v1/gamification/badges` | 보유/획득 가능한 배지 |
| Gamification | `GET /api/v1/gamification/leaderboard` | tenant 기준 리더보드 |
| Gamification | `POST /api/v1/gamification/xp/events` | 내부/관리자용 XP 이벤트 적재 |
| Community | `POST /api/v1/community/share` | 노트/덱 공유 토큰 생성 |
| Community | `GET /api/v1/community/share/{token}` | 공유 콘텐츠 조회 |
| Community | `GET /api/v1/community/search` | 공유 콘텐츠 검색 |
| Community | `POST /api/v1/community/share/{token}/fork` | 공유 콘텐츠 복사 |
| Groups | `/api/v1/community/groups/**` | 그룹 생성, 조회, 초대, 가입요청, 멤버 관리 |
| Reports | `POST /api/v1/community/reports` | 신고 접수 |
| Admin | `GET /api/v1/admin/reports`, `PATCH /api/v1/admin/reports/{reportId}` | 신고 목록/처리 |

OpenAPI UI는 기본 로컬 포트 기준 `http://localhost:8083/swagger-ui.html` 입니다.

## Runtime Dependencies

| 구성요소 | 기본값 | 용도 |
|---|---|---|
| Java | 21 | Spring Boot 4 실행 |
| PostgreSQL | `jdbc:postgresql://localhost:5432/synapse_engagement` | community/gamification 정본 저장소 |
| Kafka | `localhost:9092` | review/user 이벤트 소비, level/badge/notification 이벤트 발행 |
| Schema Registry | `http://localhost:8086` | Avro schema id 관리 |
| JWT issuer | `synapse-auth` | OAuth2 resource server 검증 |

공통 인프라는 `../synapse-shared/docker-compose.yml` 기준으로 올립니다.

```powershell
cd ..\synapse-shared
docker compose up -d
cd ..\synapse-engagement-svc
.\gradlew.bat bootRun --args='--spring.profiles.active=dev'
```

Kafka consumer/producer는 `KAFKA_ENABLED=true`일 때만 활성화됩니다.

## Kafka Contract

| 방향 | Topic | Payload | 처리 |
|---|---|---|---|
| 소비 | `platform.auth.user-registered-v1` | `UserRegistered` Avro | gamification profile bootstrap |
| 소비 | `learning.card.review-completed-v1` | `ReviewCompleted` Avro | review XP, streak, badge/level 평가 |
| 발행 | `engagement.gamification.level-up-v1` | `LevelUp` Avro | 레벨업 이벤트 |
| 발행 | `engagement.gamification.badge-earned-v1` | `BadgeEarned` Avro | 배지 획득 이벤트 |
| 발행 | `platform.notification.notification-send-v1` | `NotificationSend` Avro | 레벨업/커뮤니티 알림 |

MSK/EKS 환경에서는 `KAFKA_SECURITY_PROTOCOL=SSL`이 producer/consumer factory까지 전달되어야 합니다. `KafkaSecurityProtocolConfigTests`가 이 경로를 검증합니다.

## Verification

Phase C local verification, 2026-06-21 KST:

```powershell
.\gradlew.bat clean build
```

Result: PASS. 이 빌드는 community final E2E, gamification final E2E, embedded Kafka producer/consumer contract, Kafka ACL simulation, notification contract smoke를 포함합니다.

관련 테스트:

```powershell
.\gradlew.bat test --tests "*GamificationStep12FinalE2ETests"
.\gradlew.bat test --tests "*CommunityStep13FinalE2ETests"
.\gradlew.bat test --tests "*GamificationKafkaAclSimulationTests"
.\gradlew.bat test --tests "*GamificationKafkaProducerTests"
```

## Runbook

1. `review-completed`가 XP로 반영되지 않으면 `KAFKA_ENABLED`, `KAFKA_TOPIC_REVIEW_COMPLETED`, consumer group lag, `xp_events.event_id` idempotency를 순서대로 확인합니다.
2. level-up/badge-earned가 발행되지 않으면 `GamificationKafkaProducerTests`와 Schema Registry subject 등록 상태를 확인합니다.
3. platform notification이 누락되면 `platform.notification.notification-send-v1` topic ACL과 `NotificationSend` payload의 `tenantId`, `userId`, `notificationType`을 확인합니다.
4. community 신고/공유 흐름은 `CommunityStep13FinalE2ETests`를 우선 회귀로 사용합니다.
5. staging 배포 선결조건은 ECR semver image 도착, ArgoCD Image Updater `newTag` writeback, EKS pod의 Kafka/MSK 초기화 로그입니다.

## External Gates

로컬 `clean build`와 embedded Kafka 계약 검증은 통과했습니다. 최종 Phase C 완료 처리에는 실제 ECR/GitOps/EKS/MSK 환경에서 신규 semver image 배포와 live producer/consumer log 증거가 필요합니다.
