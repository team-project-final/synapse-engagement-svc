# WORKFLOW: engagement — Week 3

> **Task 문서**: [TASK_engagement.md](../task/TASK_engagement.md)
> **기간**: 2026-05-26 ~ 2026-05-29
> **PRD**: [PRD_W3.md](../prd/PRD_W3.md)
> **Source**: Workflow Dashboard, updated at 2026-05-27T08:33:09.800Z

---

## Step 6: gamification 완성 — 배지 수여 + 레벨 시스템 + 스트릭 추적 + 리더보드

### 6.1 TASK 시작
- [x] Step Goal / Done When / Scope / Input 확인
- [x] PRD_W3 해당 요구사항 확인 (gamification 완성)
- [x] Duration 산정 확인

### 6.2 요구사항 분석
- [x] 배지 수여 조건 정의 (초기: 최초 XP, 레벨 2, 3일 스트릭)
- [x] 레벨 시스템 설계 (W2 XP 기반 레벨 계산 유지)
- [x] 스트릭 추적 로직 분석 (user_profiles_gamification.last_activity_date 기반 유지)
- [x] 리더보드 정렬 기준 (초기: 전체 XP 총합 기준)
- [x] Instructions 초안 → TASK 문서 반영

### 6.3 Security 1차 검토
- [x] 인증 필요 여부: Yes (profile/xp/badges는 로그인 사용자, leaderboard는 공개)
- [x] 권한 종류: 본인 데이터만 조회 (리더보드는 공개)
- [x] XP 조작 방지: 서버 사이드 검증만 허용
- [x] 결과 → TASK Constraints 반영

### 6.4 ERD 설계
- [x] badges 테이블 설계 (id, name, description, icon_url, condition_type, condition_value)
- [x] user_badges 테이블 설계 (id, user_id, badge_id, earned_at)
- [x] user_streaks 테이블 설계 (id, user_id, current_streak, longest_streak, last_activity_date)
- [x] 인덱스 설계 (user_id on user_badges, user_id on user_streaks)
- [x] Duration(final) 갱신

### 6.5 Security 2차 검토
- [x] 리더보드 사용자 정보 최소 노출 (초기: userId + nickname placeholder + xp/level)
- [x] XP/레벨 수정 API 관리자 전용 (W3 초기 구현에는 관리자 수정 API 미노출)
- [x] 스트릭 자동 리셋 로직 서버 사이드 전용
- [x] 결과 → TASK Constraints 반영

### 6.6 DTO / Entity 설계 (API First)
- [x] Badge Entity 작성
- [x] UserBadge Entity 작성
- [x] UserStreak Entity 작성
- [x] BadgeResponse DTO 정의
- [x] LeaderboardEntryResponse DTO 정의 (rank, userId, nickname, xp, level)
- [x] UserGamificationResponse DTO 정의 (xp, level, streak, badges)
- [x] Output Format → TASK 반영

### 6.7 Repository 구현
- [x] BadgeRepository 인터페이스 작성
- [x] UserBadgeRepository 인터페이스 작성 (findByUserId)
- [x] UserStreakRepository 인터페이스 작성 (findByUserId)
- [x] 리더보드 쿼리 (상위 N명, XP 기준 정렬) — user_profiles_gamification 기반

### 6.8 Service + Test
- [x] BadgeService 구현 (condition_type/condition_value 기반 배지 조건 평가 → 수여)
- [x] LevelService 구현 (XP → 레벨 계산, 레벨업 판정)
- [x] StreakService 구현 (일일 활동 기록, 연속 일수 계산)
- [x] LeaderboardService 구현 (user_profiles_gamification XP 기준 랭킹 조회)
- [x] 단위 테스트 작성 (각 서비스별 Mockito)
- [x] 통합 테스트 통과 확인

### 6.9 Controller + Test
- [x] GET /gamification/me 엔드포인트 구현 (내 XP, 레벨, 스트릭, 배지)
- [x] GET /gamification/leaderboard 엔드포인트 구현 (초기: 전체 XP 기준)
- [x] GET /gamification/badges 엔드포인트 구현 (전체 배지 목록)
- [x] 슬라이스 테스트 (@WebMvcTest)
- [x] 통합 테스트 통과 확인

### 6.10 View + Test (해당 시)
- [x] Flutter 화면 연동: 해당 없음 (프론트 별도)
- [x] Swagger API 문서 확인 (`/v3/api-docs` gamification endpoints 테스트)
- [x] RULE Reference → TASK 반영

**Step 6 Status**: [ ] Not Started / [ ] In Progress / [x] Done

---

## Step 7: Kafka 연동 — gamification.level_up / gamification.badge_earned 이벤트 발행

### 7.1 TASK 시작
- [x] Step Goal / Done When / Scope / Input 확인
- [x] PRD_W3 해당 요구사항 확인 (Kafka 이벤트 발행)
- [x] Duration 산정 확인

### 7.2 요구사항 분석
- [x] gamification.level_up 이벤트 스키마 정의 (userId, oldLevel, newLevel, xp)
- [x] gamification.badge_earned 이벤트 스키마 정의 (userId, badgeId, badgeName, earnedAt)
- [x] 이벤트 발행 트리거 시점 정의 (레벨업 시, 배지 수여 시)
- [x] Instructions 초안 → TASK 문서 반영

### 7.3 Security 1차 검토
- [ ] Kafka 토픽 ACL 설정 (engagement-svc만 발행 권한)
- [x] Kafka ACL 계약 시뮬레이션 테스트 (허용 토픽 + tenantId partition key 검증)
  - 주석: 실제 broker ACL 적용 검증은 아니며, mock KafkaTemplate 기반으로 engagement-svc가 허용된 gamification 토픽 2개에만 발행하는지 검증 완료.
- [x] 이벤트 페이로드 민감정보 미포함 확인
- [x] Schema Registry Avro 스키마 등록
- [x] 결과 → TASK Constraints 반영

### 7.4 Kafka 토픽 설계
- [x] gamification.level_up 토픽 설정 (파티션, 복제, 보존)
- [x] gamification.badge_earned 토픽 설정
- [x] 이벤트 키 전략 (tenantId 기반 파티셔닝)
- [x] Duration(final) 갱신

### 7.5 Security 2차 검토
- [x] 이벤트 중복 발행 방지 (멱등성 Producer 설정)
- [x] 이벤트 스키마 버전 관리 (호환성 모드: BACKWARD)
- [x] 소비자 측 실패 시 재시도 정책 가이드 제공
- [x] 결과 → TASK Constraints 반영

### 7.6 DTO / Entity 설계 (API First)
- [x] `com.synapse.engagement.LevelUp` Avro record 정의 (eventId, tenantId, userId, newLevel, previousLevel, totalXp, occurredAt)
- [x] `com.synapse.engagement.BadgeEarned` Avro record 정의 (eventId, tenantId, userId, badgeId, badgeCode, badgeName, occurredAt)
- [x] shared Avro 스키마 벤더링 (`src/main/avro/engagement`) 및 Schema Registry 발행 경로 반영
- [x] Output Format → TASK 반영

### 7.7 Producer 구현
- [x] GamificationKafkaProducer 구현 (KafkaTemplate)
- [x] publishLevelUp() 메서드 구현
- [x] publishBadgeEarned() 메서드 구현
- [x] 멱등성 Producer 설정 (enable.idempotence=true)

### 7.8 Service 연동 + Test
- [x] LevelService → 레벨업 시 GamificationKafkaProducer.publishLevelUp() 호출
- [x] BadgeService → 배지 수여 시 GamificationKafkaProducer.publishBadgeEarned() 호출
- [x] 통합 테스트 작성 (EmbeddedKafka)
- [x] 이벤트 발행 → 토픽 수신 검증
- [x] 테스트 통과 확인

### 7.9 E2E 검증
- [x] Docker Compose 환경에서 이벤트 발행 확인
- [x] kafka-console-consumer로 이벤트 수신 확인
- [x] mock notification processor 계약 테스트 (Avro consume → notification command 변환)
  - 주석: 실제 platform notification 프로세스 연동은 아니며, mock consumer가 level-up/badge-earned Avro 이벤트를 읽어 notification command로 변환할 수 있음을 검증 완료.
- [ ] notification 서비스 연동 테스트 (이벤트 → 알림 발송)

### 7.10 결과 정리
- [x] 이벤트 스키마 문서화
- [x] 소비자 가이드 작성 (토픽명, 스키마, 파티션 키)
- [x] RULE Reference → TASK 반영

**Step 7 Status**: [ ] Not Started / [x] In Progress / [ ] Done

---

## Step 8: community 신고 + Admin 모더레이션 — 신고 접수/처리 API

### 8.1 TASK 시작
- [x] Step Goal / Done When / Scope / Input 확인
- [x] PRD_W3 해당 요구사항 확인 (신고/모더레이션)
- [x] Duration 산정 확인

### 8.2 요구사항 분석
- [x] 신고 유형 정의 (초기: 자유 입력 reason, 1000자 제한)
- [x] 신고 대상 정의 (`SHARED_DECK`, `SHARED_NOTE`, `STUDY_GROUP`, `USER`)
- [x] 관리자 모더레이션 워크플로우 (접수 → 검토 → 승인/거부)
- [x] Instructions 초안 → TASK 문서 반영

### 8.3 Security 1차 검토
- [x] 인증 필요 여부: Yes (신고: 로그인 사용자, 처리: 관리자)
- [x] 권한 종류: USER(신고), ADMIN(처리)
- [x] 중복 신고 방지 (동일 사용자 → 동일 대상 1회 제한)
- [x] 결과 → TASK Constraints 반영

### 8.4 ERD 설계
- [x] reports 테이블 설계 (id, reporter_id, target_type, target_id, reason, status, admin_note, created_at, resolved_at)
- [x] target_type 값: `SHARED_DECK`|`SHARED_NOTE`|`STUDY_GROUP`|`USER`
- [x] status ENUM 정의 (`PENDING`, `APPROVED`, `REJECTED`)
- [x] 인덱스 설계 (status, reporter_id, target_type+target_id)
- [x] UNIQUE 제약: reporter_id + target_type + target_id
- [x] Duration(final) 갱신

### 8.5 Security 2차 검토
- [x] 신고자 익명성 보장 (ReportResponse에 reporterId 미포함)
- [x] 승인 시 콘텐츠 숨김/삭제 처리 + DB audit 기록 (`admin_note`, `resolved_at`)
- [x] 거부 시 신고 무효 처리 + 사유 기록
- [x] 결과 → TASK Constraints 반영
  - 주석: shared Avro 계약이 없는 moderation audit Kafka 이벤트는 생성하지 않았고, Step 8 범위에서는 DB audit record로 검증 완료.

### 8.6 DTO / Entity 설계 (API First)
- [x] Report Entity 작성
- [x] ReportCreateRequest DTO 정의 (targetType, targetId, reason)
- [x] ReportResponse DTO 정의
- [x] ReportModerateRequest DTO 정의 (status, adminNote)
- [x] Output Format → TASK 반영

### 8.7 Repository 구현
- [x] ReportRepository 인터페이스 작성
- [x] findByStatus 쿼리 (pending 목록)
- [x] existsByReporterIdAndTargetTypeAndTargetId 중복 검사 쿼리

### 8.8 Service + Test
- [x] ReportService 구현 (신고 접수 — 대상 존재 검증, 중복 검사, 생성)
- [x] ModerationService 구현 (관리자 처리 — 승인/거부, 콘텐츠 숨김)
- [x] 승인 시 community report DB audit 기반 후속 처리 실행 (별도 approved 토픽 없음 — shared Avro 계약 미정의)
- [x] 단위 테스트 작성 (Mockito)
- [x] 테스트 통과 확인

### 8.9 Controller + Test
- [x] POST `/api/v1/community/reports` 엔드포인트 구현 (신고 접수)
- [x] GET `/api/v1/admin/reports` 엔드포인트 구현 (관리자 신고 목록, status 필터)
- [x] PATCH `/api/v1/admin/reports/{id}` 엔드포인트 구현 (승인/거부 처리)
- [x] 슬라이스 테스트 (@WebMvcTest)
- [x] 403 Forbidden 테스트 (비관리자 처리 시도)
- [x] 409 Conflict 테스트 (중복 신고)
- [x] 테스트 통과 확인

### 8.10 View + Test (해당 시)
- [x] Flutter 화면 연동: 해당 없음 (프론트 별도)
- [x] Swagger API 문서 확인 (`/v3/api-docs` report/admin endpoints 테스트)
- [x] RULE Reference → TASK 반영

**Step 8 Status**: [ ] Not Started / [ ] In Progress / [x] Done
