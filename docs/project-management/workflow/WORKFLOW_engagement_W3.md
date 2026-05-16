# WORKFLOW: @engagement-owner — Week 3

> **Task 문서**: [TASK_engagement.md](../task/TASK_engagement.md)
> **기간**: 2026-05-26 ~ 2026-05-29, 4 영업일
> **PRD**: [PRD_W3.md](../prd/PRD_W3.md)

---

## Step 6: gamification 완성 — 배지 수여 + 레벨 시스템 + 스트릭 추적 + 리더보드

### 1.1 TASK 시작
- [ ] Step Goal / Done When / Scope / Input 확인
- [ ] PRD_W3 해당 요구사항 확인 (gamification 완성)
- [ ] Duration 산정 확인

### 1.2 요구사항 분석
- [ ] 배지 수여 조건 정의 (복습 횟수, 연속 스트릭, 레벨 달성 등)
- [ ] 레벨 시스템 설계 (XP 기반 레벨 구간 정의)
- [ ] 스트릭 추적 로직 분석 (연속 복습 일수 계산)
- [ ] 리더보드 정렬 기준 (XP 총합, 주간/월간 필터)
- [ ] Instructions 초안 → TASK 문서 반영

### 1.3 Security 1차 검토
- [ ] 인증 필요 여부: Yes (로그인 사용자)
- [ ] 권한 종류: 본인 데이터만 조회 (리더보드는 공개)
- [ ] XP 조작 방지: 서버 사이드 검증만 허용
- [ ] 결과 → TASK Constraints 반영

### 1.4 ERD 설계
- [ ] badges 테이블 설계 (id, code UK, name, description, icon_url, category, criteria_json, xp_reward)
- [ ] user_badges 테이블 설계 (id, user_id, badge_id, earned_at)
- [ ] 스트릭 데이터 — user_profiles_gamification 테이블 컬럼으로 관리 (current_streak, longest_streak, last_activity_date) — 별도 user_streaks 테이블 없음
- [ ] 인덱스 설계 (user_id on user_badges)
- [ ] Duration(final) 갱신

### 1.5 Security 2차 검토
- [ ] 리더보드 사용자 정보 최소 노출 (닉네임 + 아바타만)
- [ ] XP/레벨 수정 API 관리자 전용
- [ ] 스트릭 자동 리셋 로직 서버 사이드 전용
- [ ] 결과 → TASK Constraints 반영

### 1.6 DTO / Entity 설계 (API First)
- [ ] Badge Entity 작성
- [ ] UserBadge Entity 작성
- [ ] BadgeResponse DTO 정의
- [ ] LeaderboardEntryResponse DTO 정의 (rank, userId, displayName, xp, level)
- [ ] UserGamificationResponse DTO 정의 (xp, level, streak, badges)
- [ ] Output Format → TASK 반영

### 1.7 Repository 구현
- [ ] BadgeRepository 인터페이스 작성
- [ ] UserBadgeRepository 인터페이스 작성 (findByUserId)
- [ ] 리더보드 쿼리 (상위 N명, XP 기준 정렬) — user_profiles_gamification 기반

### 1.8 Service + Test
- [ ] BadgeService 구현 (badges.criteria_json 기반 배지 조건 평가 → 수여)
- [ ] LevelService 구현 (XP → 레벨 계산, 레벨업 판정)
- [ ] StreakService 구현 (user_profiles_gamification 스트릭 컬럼 갱신, 연속 일수 계산, 자정 리셋)
- [ ] LeaderboardService 구현 (user_profiles_gamification XP 기준 랭킹 조회)
- [ ] 단위 테스트 작성 (각 서비스별 Mockito)
- [ ] 테스트 통과 확인

### 1.9 Controller + Test
- [ ] GET /gamification/profile 엔드포인트 구현 (내 XP, 레벨, 스트릭, 배지 — level, totalXp, currentStreak, longestStreak, title, nextLevelXp, recentBadges)
- [ ] GET /gamification/leaderboard 엔드포인트 구현 (주간/월간/전체)
- [ ] GET /gamification/badges 엔드포인트 구현 (전체 배지 목록)
- [ ] 슬라이스 테스트 (@WebMvcTest)
- [ ] 테스트 통과 확인

### 1.10 View + Test (해당 시)
- [ ] Flutter 화면 연동: 해당 없음 (프론트 별도)
- [ ] Swagger API 문서 확인
- [ ] RULE Reference → TASK 반영

**Step 6 Status**: [ ] Not Started / [ ] In Progress / [ ] Done

---

## Step 7: Kafka 연동 — gamification.level.up / gamification.badge.earned 이벤트 발행

### 1.1 TASK 시작
- [ ] Step Goal / Done When / Scope / Input 확인
- [ ] PRD_W3 해당 요구사항 확인 (Kafka 이벤트 발행)
- [ ] Duration 산정 확인

### 1.2 요구사항 분석
- [ ] gamification.level.up 이벤트 스키마 정의 (userId, oldLevel, newLevel, xp)
- [ ] gamification.badge.earned 이벤트 스키마 정의 (userId, badgeId, badgeName, earnedAt)
- [ ] 이벤트 발행 트리거 시점 정의 (레벨업 시, 배지 수여 시)
- [ ] Instructions 초안 → TASK 문서 반영

### 1.3 Security 1차 검토
- [ ] Kafka 토픽 ACL 설정 (engagement-svc만 발행 권한)
- [ ] 이벤트 페이로드 민감정보 미포함 확인
- [ ] Schema Registry Avro/JSON Schema 등록
- [ ] 결과 → TASK Constraints 반영

### 1.4 Kafka 토픽 설계
- [ ] gamification.level.up 토픽 설정 (파티션, 복제, 보존)
- [ ] gamification.badge.earned 토픽 설정
- [ ] 이벤트 키 전략 (userId 기반 파티셔닝)
- [ ] Duration(final) 갱신

### 1.5 Security 2차 검토
- [ ] 이벤트 중복 발행 방지 (멱등성 Producer 설정)
- [ ] 이벤트 스키마 버전 관리 (호환성 모드: BACKWARD)
- [ ] 소비자 측 실패 시 재시도 정책 가이드 제공
- [ ] 결과 → TASK Constraints 반영

### 1.6 DTO / Entity 설계 (API First)
- [ ] LevelUpEvent DTO 정의 (userId, oldLevel, newLevel, totalXp, occurredAt)
- [ ] BadgeEarnedEvent DTO 정의 (userId, badgeId, badgeName, occurredAt)
- [ ] Avro/JSON Schema 작성 → Schema Registry 등록
- [ ] Output Format → TASK 반영

### 1.7 Producer 구현
- [ ] GamificationKafkaProducer 구현 (KafkaTemplate)
- [ ] publishLevelUp() 메서드 구현
- [ ] publishBadgeEarned() 메서드 구현
- [ ] 멱등성 Producer 설정 (enable.idempotence=true)

### 1.8 Service 연동 + Test
- [ ] LevelService → 레벨업 시 GamificationKafkaProducer.publishLevelUp() 호출
- [ ] BadgeService → 배지 수여 시 GamificationKafkaProducer.publishBadgeEarned() 호출
- [ ] 통합 테스트 작성 (EmbeddedKafka)
- [ ] 이벤트 발행 → 토픽 수신 검증
- [ ] 테스트 통과 확인

### 1.9 E2E 검증
- [ ] Docker Compose 환경에서 이벤트 발행 확인
- [ ] kafka-console-consumer로 이벤트 수신 확인
- [ ] notification 서비스 연동 테스트 (이벤트 → 알림 발송)

### 1.10 결과 정리
- [ ] 이벤트 스키마 문서화
- [ ] 소비자 가이드 작성 (토픽명, 스키마, 파티션 키)
- [ ] RULE Reference → TASK 반영

**Step 7 Status**: [ ] Not Started / [ ] In Progress / [ ] Done

---

## Step 8: community 신고 + Admin 모더레이션 — 신고 접수/처리 API

### 1.1 TASK 시작
- [ ] Step Goal / Done When / Scope / Input 확인
- [ ] PRD_W3 해당 요구사항 확인 (신고/모더레이션)
- [ ] Duration 산정 확인

### 1.2 요구사항 분석
- [ ] 신고 유형 정의 (스팸, 욕설, 부적절 콘텐츠 등)
- [ ] 신고 대상 정의 (게시글, 댓글, 사용자)
- [ ] 관리자 모더레이션 워크플로우 (접수 → 검토 → 승인/거부)
- [ ] Instructions 초안 → TASK 문서 반영

### 1.3 Security 1차 검토
- [ ] 인증 필요 여부: Yes (신고: 로그인 사용자, 처리: 관리자)
- [ ] 권한 종류: USER(신고), ADMIN(처리)
- [ ] 중복 신고 방지 (동일 사용자 → 동일 대상 1회 제한)
- [ ] 결과 → TASK Constraints 반영

### 1.4 ERD 설계
- [ ] reports 테이블 설계 (id, reporter_user_id, target_type, target_id, reason, status, action_taken, created_at, reviewed_at)
- [ ] target_type 값: shared_deck|shared_note|study_group|user
- [ ] status ENUM 정의 (pending, reviewed, resolved, dismissed)
- [ ] 인덱스 설계 (status, reporter_user_id, target_type+target_id)
- [ ] UNIQUE 제약: reporter_user_id + target_type + target_id
- [ ] Duration(final) 갱신

### 1.5 Security 2차 검토
- [ ] 신고자 익명성 보장 (신고 대상자에게 신고자 미노출)
- [ ] 승인 시 콘텐츠 숨김/삭제 처리 + audit 이벤트 발행
- [ ] 거부 시 신고 무효 처리 + 사유 기록
- [ ] 결과 → TASK Constraints 반영

### 1.6 DTO / Entity 설계 (API First)
- [ ] Report Entity 작성
- [ ] ReportCreateRequest DTO 정의 (target_type, target_id, reason)
- [ ] ReportResponse DTO 정의
- [ ] ReportModerateRequest DTO 정의 (status, action_taken)
- [ ] Output Format → TASK 반영

### 1.7 Repository 구현
- [ ] ReportRepository 인터페이스 작성
- [ ] findByStatus 커스텀 쿼리 (pending 목록)
- [ ] existsByReporterUserIdAndTargetTypeAndTargetId 중복 검사 쿼리

### 1.8 Service + Test
- [ ] ReportService 구현 (신고 접수 — 중복 검사, 생성)
- [ ] ModerationService 구현 (관리자 처리 — 승인/거부, 콘텐츠 숨김)
- [ ] 승인 시 community.report.created 기반 후속 처리 실행 (별도 approved 토픽 없음 — 아키텍처 미정의)
- [ ] 단위 테스트 작성 (Mockito)
- [ ] 테스트 통과 확인

### 1.9 Controller + Test
- [ ] POST /community/reports 엔드포인트 구현 (신고 접수)
- [ ] GET /admin/reports 엔드포인트 구현 (관리자 신고 목록, 페이징)
- [ ] PUT /admin/reports/{id}/resolve 엔드포인트 구현 (승인/거부 처리)
- [ ] 슬라이스 테스트 (@WebMvcTest)
- [ ] 403 Forbidden 테스트 (비관리자 처리 시도)
- [ ] 409 Conflict 테스트 (중복 신고)
- [ ] 테스트 통과 확인

### 1.10 View + Test (해당 시)
- [ ] Flutter 화면 연동: 해당 없음 (프론트 별도)
- [ ] Swagger API 문서 확인
- [ ] RULE Reference → TASK 반영

**Step 8 Status**: [ ] Not Started / [ ] In Progress / [ ] Done
