# TASK: engagement — synapse-engagement-svc

> **Repository**: [synapse-engagement-svc](https://github.com/team-project-final/synapse-engagement-svc)
> **Track Owner**: 한승완
> **Service Scope**: community / gamification
> **Source of Truth**: [Workflow Dashboard](https://team-project-final.github.io/workflow-dashboard/#/detail/synapse-engagement-svc)
> **Dashboard Updated At**: 2026-05-27T08:33:09.800Z
> **관련 문서**: [SCOPE](../scope/SCOPE_engagement.md) | [HISTORY](../history/HISTORY_engagement.md)

---

## 진행 요약

| Week | Period | Progress | Status |
|------|--------|----------|--------|
| W1 | 05-12~05-16 | 116/116 | Done |
| W2 | 05-19~05-23 | 106/106 | Done |
| W3 | 05-26~05-29 | 124/126 | In Progress |
| W4 | 06-01~06-05 | 43/43 | Done |
| W5 | 06-08~06-12 | 0/16 | Not Started |

---

## TASK-EG-001

- **Task ID**: TASK-EG-001
- **Title**: engagement-svc 골격 생성
- **Owner**: 한승완
- **Status**: DONE
- **Priority**: P0
- **Step Goal**: engagement-owner가 Spring Boot 4 + Modulith 기반 engagement-svc를 생성하여 community/gamification 모듈 골격이 동작한다.
- **Done When**:
  - [x] Spring Boot 4 + Modulith 프로젝트 초기화 완료
  - [x] community / gamification 모듈 패키지 구조 생성
  - [x] `./gradlew build` 성공
  - [x] Modulith 구조 검증 테스트 통과
  - [x] Docker 이미지 빌드 성공
- **Scope**:
  - In Scope:
    - Spring Boot 4 + Modulith 프로젝트 골격
    - community / gamification `package-info.java`
    - Dockerfile 및 빌드 검증
  - Out of Scope:
    - 비즈니스 로직
    - DB 마이그레이션
    - Kafka 이벤트 연동
- **Dependencies**: 없음
- **Due Date**: 2026-05-16

## TASK-EG-002

- **Task ID**: TASK-EG-002
- **Title**: community 그룹 CRUD
- **Owner**: 한승완
- **Status**: DONE
- **Priority**: P0
- **Step Goal**: 로그인 사용자가 학습 그룹을 생성/조회/수정/삭제할 수 있다.
- **Done When**:
  - [x] 그룹 생성/목록/상세/수정/삭제 API 구현
  - [x] 소유자 권한 검증 구현
  - [x] Soft delete 및 사용자별 그룹 생성 제한 구현
  - [x] Flyway 마이그레이션 완료
  - [x] WebMvcTest 및 통합 테스트 통과
- **Scope**:
  - In Scope:
    - `groups` 테이블, Entity, Repository, Service, Controller
    - JWT subject 기반 사용자 식별
    - 페이징/커서 기반 목록 조회
  - Out of Scope:
    - OpenSearch 기반 그룹 검색
    - 그룹 이미지 업로드
    - platform-svc JWT 완전 연동
- **Dependencies**: TASK-EG-001
- **Due Date**: 2026-05-16

## TASK-EG-003

- **Task ID**: TASK-EG-003
- **Title**: community 멤버 관리
- **Owner**: 한승완
- **Status**: DONE
- **Priority**: P0
- **Step Goal**: 그룹 소유자가 멤버를 초대/가입승인/탈퇴시킬 수 있고, 멤버는 자발적으로 가입/탈퇴할 수 있다.
- **Done When**:
  - [x] 멤버 초대/가입/승인/탈퇴/강퇴 API 구현
  - [x] OWNER/ADMIN/MEMBER 역할 검증 구현
  - [x] PENDING/ACTIVE/KICKED 상태 전이 구현
  - [x] `group_members` 마이그레이션 완료
  - [x] 역할별 테스트 통과
- **Scope**:
  - In Scope:
    - `group_members` 테이블
    - 역할 기반 멤버 관리
    - 공개/비공개 그룹 가입 정책
  - Out of Scope:
    - 초대 이메일/알림 발송
    - 멤버 활동 이력
    - 초대 수락/거절 고도화 (workflow-guide 기준 Step 7 범위 아님)
- **Dependencies**: TASK-EG-002
- **Due Date**: 2026-05-16

## TASK-EG-004

- **Task ID**: TASK-EG-004
- **Title**: gamification XP 기초
- **Owner**: 한승완
- **Status**: DONE
- **Priority**: P0
- **Step Goal**: 시스템이 내부 XP 적립 유스케이스를 통해 `xp_events`를 기록하고 사용자가 자신의 XP 프로필과 이력을 조회할 수 있다.
- **Done When**:
  - [x] `xp_events` 테이블 구현
  - [x] `user_profiles_gamification` 테이블 구현
  - [x] XP 적립 멱등성 구현
  - [x] `GET /gamification/profile` 구현
  - [x] `GET /gamification/xp/history` 구현
  - [x] 테스트 통과
- **Scope**:
  - In Scope:
    - XP 이벤트 로그와 사용자 프로필 집계
    - 기본 활동 10 XP 규칙
    - JWT subject 기반 조회
  - Out of Scope:
    - Kafka Producer 연동, TASK-EG-007에서 처리
    - Kafka Consumer 연동, TASK-EG-009에서 처리
    - 배지/리더보드 완성, TASK-EG-006에서 처리
- **Dependencies**: TASK-EG-001
- **Due Date**: 2026-05-23

## TASK-EG-005

- **Task ID**: TASK-EG-005
- **Title**: community 공유
- **Owner**: 한승완
- **Status**: DONE
- **Priority**: P0
- **Step Goal**: 사용자가 덱/노트를 `share_token`으로 공유하고, 다른 사용자가 공유 콘텐츠를 검색하여 복사할 수 있다.
- **Done When**:
  - [x] 공유 등록 API 구현
  - [x] 토큰 기반 공유 콘텐츠 조회 API 구현
  - [x] 공유 콘텐츠 검색 API 구현
  - [x] 공유 콘텐츠 fork API 구현
  - [x] 삭제 및 soft delete 구현
  - [x] 테스트 통과
- **Scope**:
  - In Scope:
    - `shared_contents` 테이블
    - UUID 기반 URL-safe share token
    - `DECK` / `NOTE` 콘텐츠 타입
    - DB 기반 키워드 검색
  - Out of Scope:
    - 공유 콘텐츠 신고, TASK-EG-008에서 처리
    - OpenSearch 기반 검색
    - 외부 알림/이벤트 연동
- **Dependencies**: TASK-EG-002
- **Due Date**: 2026-05-23

## TASK-EG-006

- **Task ID**: TASK-EG-006
- **Title**: gamification 완성
- **Owner**: 한승완
- **Status**: DONE
- **Current Progress**: Step 6 완료. badges/user_badges/user_streaks, condition-based badge award, XP level calculation, streak tracking, leaderboard, `/gamification/me`, `/leaderboard`, `/badges`, Swagger docs exposure, Mockito/WebMvc/integration tests verified.
- **Priority**: P0
- **Step Goal**: 시스템이 XP 누적에 따라 레벨을 계산하고, 조건 달성 시 배지를 수여하며, 스트릭과 리더보드를 제공한다.
- **Done When**:
  - [x] 배지 조건 평가 및 수여 구현
  - [x] XP 기반 레벨 계산 구현
  - [x] `user_profiles_gamification` 기반 스트릭 추적 구현
  - [x] 리더보드 조회 구현
  - [x] `GET /gamification/me`, `/leaderboard`, `/badges` 테스트 통과
- **Scope**:
  - In Scope:
    - `badges`, `user_badges`, `user_streaks`
    - 레벨/스트릭/리더보드 서비스
    - 사용자 정보 최소 노출
    - `/v3/api-docs` gamification endpoint 노출 확인
  - Out of Scope:
    - 배지 이미지 관리
    - 시즌 리셋 정책
    - 팀 리더보드
    - Flutter 화면 연동
  - Constraints:
    - JWT subject 기반 본인 데이터 조회만 허용
    - XP 적립은 서버 사이드 검증과 `eventId` 멱등성으로 중복 적립 방지
    - 리더보드는 초기 범위에서 userId/nickname placeholder/xp/level만 노출
  - Output Format:
    - `UserGamificationResponse`: xp, level, currentStreak, longestStreak, badges
    - `BadgeResponse`: code, name, description, iconUrl, conditionType, conditionValue, earnedAt
    - `LeaderboardEntryResponse`: rank, userId, nickname, xp, level
  - Verification:
    - `GamificationStep6ServiceTests`: Mockito service tests
    - `GamificationControllerWebMvcTest`: WebMvc slice tests
    - `EngagementApiSmokeTests`: JWT integration flow and `/v3/api-docs`
    - `./gradlew.bat test` 성공 (2026-05-28)
- **Dependencies**: TASK-EG-004
- **Due Date**: 2026-05-29

## TASK-EG-007

- **Task ID**: TASK-EG-007
- **Title**: Kafka 연동 — gamification.level_up / gamification.badge_earned 이벤트 발행
- **Owner**: 한승완
- **Status**: IN_PROGRESS
- **Current Progress**: Kafka Producer를 D-002/EVENT_CONTRACT_STANDARD 기준으로 리팩토링했다. `synapse-shared`의 `com.synapse.engagement.LevelUp` / `BadgeEarned` Avro 스키마를 `src/main/avro/engagement/`에 벤더링하고, Confluent `KafkaAvroSerializer` + Schema Registry 경로로 발행한다. EmbeddedKafka + mock Schema Registry publish/consume, mock notification processor 계약 테스트, Kafka ACL 계약 시뮬레이션은 통과했다. 실제 Kafka ACL 적용과 실제 notification 서비스 연동 테스트는 아직 남아 있다.
- **Priority**: P0
- **Step Goal**: engagement-svc가 레벨업과 배지 획득 시 `gamification.level_up`, `gamification.badge_earned` 이벤트를 발행하여 downstream 서비스가 알림/감사 처리를 할 수 있게 한다.
- **Done When**:
  - [x] `gamification.level_up` 이벤트 스키마 정의
  - [x] `gamification.badge_earned` 이벤트 스키마 정의
  - [x] `GamificationKafkaProducer` 구현
  - [x] 레벨업/배지 수여 시점에 Producer 호출
  - [x] EmbeddedKafka 또는 Testcontainers Kafka 테스트 통과
- **Scope**:
  - In Scope:
    - `gamification.level_up` Producer
    - `gamification.badge_earned` Producer
    - tenantId 기반 파티션 키
    - shared Avro 스키마 및 Schema Registry 문서화
    - Producer 멱등성 설정
  - Out of Scope:
    - `card.reviewed` Consumer
    - notification 서비스 내부 구현
    - 커뮤니티 멤버십 초대/가입신청 관리
  - Constraints:
    - Kafka 토픽 ACL은 engagement-svc 발행 권한만 허용한다
    - 이벤트 페이로드에는 민감 정보를 포함하지 않는다
    - 스키마 호환성은 BACKWARD 모드를 기준으로 한다
    - Kafka value는 CloudEvents JSON envelope이 아니라 topic별 bare Avro record다
    - Avro record에는 공통 메타 `eventId`, `tenantId`, `occurredAt`을 포함한다
    - Producer는 멱등성 설정을 사용한다
  - Output Format:
    - `com.synapse.engagement.LevelUp`: eventId, tenantId, userId, newLevel, previousLevel, totalXp, occurredAt
    - `com.synapse.engagement.BadgeEarned`: eventId, tenantId, userId, badgeId, badgeCode, badgeName, occurredAt
    - Consumer guide: topic, schema, partition key, retry note
  - Verification:
    - Producer publish/consume test: `GamificationKafkaProducerTests`
    - Mock notification contract test: `GamificationNotificationContractTests`
    - ACL contract simulation test: `GamificationKafkaAclSimulationTests`
    - Service trigger test: `GamificationStep7EventServiceTests`
    - Full regression: `./gradlew.bat test` 성공 (2026-06-01)
    - Docker Compose Kafka + kafka-console-consumer 수동 확인 성공 (2026-05-28)
    - Schema Registry subjects 등록 성공: `engagement.gamification.level-up-v1-value`, `engagement.gamification.badge-earned-v1-value`
    - Pending manual check: Kafka ACL + notification 서비스 연동
- **Dependencies**: TASK-EG-006
- **Due Date**: 2026-05-29

## TASK-EG-008

- **Task ID**: TASK-EG-008
- **Title**: community 신고 + Admin 모더레이션
- **Owner**: 한승완
- **Status**: DONE
- **Current Progress**: workflow-guide Step 8 기준으로 신고 접수/관리자 처리 API를 구현했다. `reports` 테이블, `PENDING/APPROVED/REJECTED` 상태, 중복 신고 방지, ADMIN role claim 검증, 신고자 미노출 응답, 승인 시 공유 콘텐츠/스터디 그룹 soft delete 처리, WebMvc/Service/Smoke 테스트를 추가했다. `USER` target은 engagement-svc가 계정 데이터를 소유하지 않으므로 신고 상태 승인까지만 처리하고 실제 계정 제재는 platform/auth 영역으로 남긴다.
- **Priority**: P0
- **Step Goal**: 사용자가 부적절한 콘텐츠를 신고하고, 관리자가 신고를 조회하여 승인/거부 처리할 수 있다.
- **Done When**:
  - [x] 신고 접수 API 구현
  - [x] 관리자 신고 목록 API 구현
  - [x] 관리자 신고 처리 API 구현
  - [x] 중복 신고 방지 구현
  - [x] 403/409 테스트 통과
- **Scope**:
  - In Scope:
    - `reports` 테이블
    - target type: `SHARED_DECK`, `SHARED_NOTE`, `STUDY_GROUP`, `USER`
    - 신고자 익명성 보장
    - 콘텐츠 숨김/처리 기록 (`adminNote`, `resolvedAt`)
    - ADMIN role claim 기반 관리자 API 403 처리
  - Out of Scope:
    - AI 자동 감지
    - 신고자 알림
    - 별도 `community.report.approved` 토픽
    - shared Avro 계약이 없는 moderation audit Kafka 이벤트
    - engagement-svc 외부 계정 제재
  - Constraints:
    - 신고자는 JWT subject에서만 결정하고 request body로 받지 않는다
    - 응답에는 `reporterId`를 포함하지 않아 신고 대상자에게 신고자를 노출하지 않는다
    - 동일 reporter + targetType + targetId 조합은 1회만 허용한다
    - 관리자 처리는 `APPROVED` 또는 `REJECTED`만 허용하고 이미 처리된 신고는 재처리하지 않는다
  - Output Format:
    - `ReportCreateRequest`: targetType, targetId, reason
    - `ReportModerateRequest`: status, adminNote
    - `ReportResponse`: id, targetType, targetId, reason, status, adminNote, createdAt, resolvedAt
  - Verification:
    - `ReportServiceStep8Tests`: 중복 신고 409, 승인 시 대상 숨김, 거부 처리
    - `ReportControllerWebMvcTest`: 신고 생성, 403 non-admin, 409 duplicate, 관리자 목록/처리
    - `EngagementApiSmokeTests`: 신고 생성→중복 방지→비관리자 403→관리자 승인 flow 및 `/v3/api-docs` 노출
- **Dependencies**: TASK-EG-005
- **Due Date**: 2026-05-29

## TASK-EG-009

- **Task ID**: TASK-EG-009
- **Title**: Kafka 이벤트 연동
- **Owner**: 한승완
- **Status**: DONE
- **Current Progress**: Step 9 기준으로 `learning.card.review-completed-v1` Avro Consumer를 XP 적립 유스케이스에 연결하고, 레벨업/배지 수여 시 `engagement.gamification.level-up-v1`, `engagement.gamification.badge-earned-v1` Avro 이벤트를 발행하도록 통합 검증했다. Consumer 실패 처리는 1초 간격 3회 재시도 후 `{원본토픽}.dlq` 발행으로 정리했다.
- **Priority**: P0
- **Step Goal**: engagement-svc가 `learning.card.review-completed-v1` 이벤트를 소비해 XP를 적립하고, 레벨업/배지 수여 이벤트를 발행한다.
- **Done When**:
  - [x] `learning.card.review-completed-v1` 소비 계약 확정
  - [x] XP 적립 Consumer 구현
  - [x] level up / badge earned Producer 구현
  - [x] 중복 수신 시 XP 중복 적립 방지
  - [x] Kafka 통합 테스트 통과
- **Scope**:
  - In Scope:
    - Consumer/Producer 설정
    - Avro + Schema Registry 규칙 반영
    - DLQ/재처리 정책 문서화
  - Out of Scope:
    - notification 서비스 알림 구현
    - 스키마 호환성 정책 변경
- **Constraints**:
  - Kafka listener는 `synapse.kafka.enabled=true`일 때만 활성화한다.
  - inbound `ReviewCompleted` 스키마에는 `eventId`가 없어 `cardId + reviewedAt` 조합을 XP 멱등성 키로 사용한다.
  - 이벤트 payload에는 민감 정보를 포함하지 않고 `tenantId`를 partition key로 사용한다.
  - Consumer 실패는 1초 간격 3회 재시도 후 `{원본토픽}.dlq`로 발행한다.
- **Output Format**:
  - Consumer input: `com.synapse.learning.ReviewCompleted`
  - Producer output: `com.synapse.engagement.LevelUp`, `com.synapse.engagement.BadgeEarned`
- **Verification**:
  - `EngagementKafkaEventHandlerTests`: UserRegistered/Profile 생성, ReviewCompleted XP 적립, 중복 XP skip
  - `EngagementKafkaStep9IntegrationTests`: EmbeddedKafka `ReviewCompleted` → XP 적립 → level-up/badge-earned 발행
  - `GamificationKafkaProducerTests`: Avro Producer publish/consume 검증
- **Dependencies**: TASK-EG-004, TASK-EG-006
- **Due Date**: 2026-06-05

## TASK-EG-010

- **Task ID**: TASK-EG-010
- **Title**: 게이미피케이션 E2E 테스트 + 버그 수정
- **Owner**: 한승완
- **Status**: DONE
- **Current Progress**: Step 10 기준으로 `GamificationStep10E2ETests`를 추가해 복습 XP 적립, 최초 XP 배지, 레벨업, LEVEL_2 배지, 중복 이벤트 409 방지, 내 프로필 조회, XP 이력 조회, 리더보드 1위 반영까지 REST E2E로 검증했다. 신규 P0/P1/P2 실패는 없었고 전체 회귀 테스트가 통과했다.
- **Priority**: P0
- **Step Goal**: 게이미피케이션 플로우가 E2E로 통과하고 발견된 P0 버그가 수정된다.
- **Done When**:
  - [x] 복습→XP→배지→레벨업→리더보드 시나리오 실행
  - [x] 실패 항목 기록
  - [x] P0/P1/P2 분류
  - [x] P0 버그 수정: 신규 P0 없음
  - [x] 회귀 테스트 통과
- **Scope**:
  - In Scope:
    - 게이미피케이션 E2E
    - P0 버그 수정
    - 커버리지 80% 이상 확인
  - Out of Scope:
    - 신규 기능 추가
    - 성능 튜닝
- **Constraints**:
  - Step 10은 engagement-svc REST E2E와 Step 9 Kafka 통합 테스트 결과를 기준으로 검증한다.
  - 실제 learning-svc/notification-svc를 포함한 MSA 전체 E2E는 별도 통합 환경 검증으로 남긴다.
  - 프로젝트에 정량 커버리지 도구가 없어 80% 수치는 산출하지 않고 전체 회귀 테스트 통과를 기록한다.
- **Verification**:
  - `GamificationStep10E2ETests`: 복습 XP → 배지 → 레벨업 → 중복 방지 → 이력 → 리더보드 REST E2E
  - `EngagementKafkaStep9IntegrationTests`: Kafka `ReviewCompleted` → XP 적립 → level-up/badge-earned 발행
  - `EngagementApiSmokeTests`: gamification API docs 및 smoke flow
  - Full regression: `.\gradlew.bat test` 성공 (2026-06-04)
- **Dependencies**: TASK-EG-006, TASK-EG-009
- **Due Date**: 2026-06-05

## TASK-EG-011

- **Task ID**: TASK-EG-011
- **Title**: 커뮤니티 공유/신고 E2E 테스트 + 안정화
- **Owner**: 한승완
- **Status**: DONE
- **Priority**: P0
- **Step Goal**: 커뮤니티 공유/검색/복사 및 신고/처리 플로우가 E2E로 통과하고 P0 버그가 수정된다.
- **Done When**:
  - [x] 공유→검색→복사 시나리오 실행
  - [x] 신고→관리자 처리 시나리오 실행
  - [x] 실패 항목 기록
  - [x] P0 버그 수정
  - [x] 회귀 테스트 통과
- **Current Progress**:
  - Step 11 E2E 테스트 `CommunityStep11E2ETests` 추가
  - 공유 생성, token 조회, 검색, fork, 원본 downloadCount 증가, sourceShareId 연결 검증
  - 신고 생성, 중복 신고 409, 비관리자 admin API 403, 관리자 승인, 신고 대상 숨김 검증
- **Constraints**:
  - 신규 P0/P1/P2 버그 없음
  - 정량 커버리지 리포트 태스크는 현재 Gradle 검증 흐름에 연결되어 있지 않아 E2E + 전체 회귀 테스트로 대체 확인
- **Verification**:
  - `.\gradlew.bat test --tests "com.synapse.engagement.community.CommunityStep11E2ETests"` → BUILD SUCCESSFUL
  - `.\gradlew.bat test` → BUILD SUCCESSFUL
- **Scope**:
  - In Scope:
    - 커뮤니티 전체 플로우 E2E
    - P0 버그 수정
    - API 문서 최신화
  - Out of Scope:
    - P1/P2 전체 수정
    - 새 기능 추가
- **Dependencies**: TASK-EG-005, TASK-EG-008
- **Due Date**: 2026-06-05

## TASK-EG-012

- **Task ID**: TASK-EG-012
- **Title**: 게이미피케이션 최종 E2E
- **Owner**: 한승완
- **Status**: TODO
- **Priority**: P0
- **Step Goal**: 발표 전 게이미피케이션 전체 플로우가 최종 E2E로 재검증된다.
- **Done When**:
  - [ ] 복습→XP→레벨업 시나리오 통과
  - [ ] 배지 조건→수여→알림 시나리오 통과
  - [ ] 리더보드 조회 시나리오 통과
  - [ ] 이벤트 중복/멱등성 확인
  - [ ] P0 버그 수정 및 회귀 테스트 완료
- **Scope**:
  - In Scope:
    - 최종 E2E 확인
    - 이벤트 멱등성 확인
    - 발표 전 P0 정리
  - Out of Scope:
    - 신규 게이미피케이션 기능
    - 장기 운영 모니터링
- **Dependencies**: TASK-EG-010
- **Due Date**: 2026-06-12

## TASK-EG-013

- **Task ID**: TASK-EG-013
- **Title**: 커뮤니티 최종 E2E
- **Owner**: 한승완
- **Status**: TODO
- **Priority**: P0
- **Step Goal**: 발표 전 커뮤니티 공유/신고 플로우가 최종 E2E로 재검증되고 데모 데이터가 준비된다.
- **Done When**:
  - [ ] 공유 덱 생성→검색→복사 시나리오 통과
  - [ ] 공유 노트 조회 시나리오 통과
  - [ ] 신고 접수→관리자 처리 시나리오 통과
  - [ ] engagement P0 버그 0건
  - [ ] 발표용 데모 데이터 준비
- **Scope**:
  - In Scope:
    - 커뮤니티 최종 E2E
    - 권한/소유자 검증 실패 케이스
    - 발표용 데모 데이터
  - Out of Scope:
    - 신규 커뮤니티 기능
    - 운영 자동화
- **Dependencies**: TASK-EG-011
- **Due Date**: 2026-06-12

---

## RULE Reference

| Area | Rule |
|------|------|
| Task structure | [14-task-structure.md](../../rules/14-task-structure.md) |
| Working log | [12-working-log.md](../../rules/12-working-log.md) |
| Security | [01-security.md](../../rules/01-security.md) |
| Spring Modulith | [07-platform-spring.md](../../rules/07-platform-spring.md) |
| Kafka / Avro | [08-kafka-event.md](../../rules/08-kafka-event.md) |
