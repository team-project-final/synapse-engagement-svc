# TASK: @engagement-owner

> **담당 서비스**: engagement-svc (community / gamification)
> **GitHub Repository**: [synapse-engagement-svc](https://github.com/team-project-final/synapse-engagement-svc)
> **주차**: W1 (2026-05-12 ~ 2026-05-15, 4 영업일)
> **관련 문서**: [SCOPE](../scope/SCOPE_engagement.md) | [PRD_W1](../prd/PRD_W1.md) | [WORKFLOW](../workflow/WORKFLOW_engagement_W1.md) | [HISTORY](../history/HISTORY_engagement.md)

---

## Step 1: engagement-svc 골격 생성

- **Step Goal**: engagement-owner가 Spring Boot 4 + Modulith 기반 engagement-svc를 생성하여 community/gamification 모듈 골격이 동작한다.
- **Done When**:
  - [ ] Spring Boot 4 + Modulith 프로젝트 초기화 완료
  - [ ] community / gamification 2개 모듈 패키지 구조 생성
  - [ ] `./gradlew build` 성공
  - [ ] Modulith 구조 검증 테스트 통과 (`ApplicationModulesTest`)
  - [ ] Docker 이미지 빌드 성공
- **Scope**:
  - In Scope:
    - Spring Boot 4 + Modulith 프로젝트 생성
    - community / gamification 모듈 패키지 구조
    - build.gradle 의존성 설정
    - ApplicationModulesTest 작성
    - Dockerfile 작성
  - Out of Scope:
    - 비즈니스 로직 구현
    - DB 마이그레이션
    - Kafka 이벤트 연동
- **Input**: 03_아키텍처_정의서 §Modulith 구조, platform-svc 골격 참조
- **Instructions**:
  1. Spring Initializr로 프로젝트 생성 (Spring Boot 4, Java 21, Gradle)
  2. Modulith 의존성 추가 (spring-modulith-starter, spring-modulith-test)
  3. community / gamification 패키지 생성 + package-info.java
  4. 각 모듈에 빈 Controller + Service 클래스 생성
  5. ApplicationModulesTest 작성 및 통과 확인
  6. Dockerfile 작성 (multi-stage build)
  7. docker compose에서 engagement-svc 실행 확인
- **Output Format**: `engagement-svc/` 프로젝트 디렉토리 + Dockerfile + 테스트 통과 스크린샷
- **Constraints**:
  - Java 21 + Spring Boot 4 + Modulith
  - 모듈 간 순환 의존 금지
  - platform-svc와 동일한 빌드 구조 유지
- **Duration**: 0.5일
- **RULE Reference**: wiki 03_아키텍처_정의서 §Modulith, wiki 18_기술_스택_정의서
- **Assignee**: @engagement-owner
- **Reviewer**: @team-lead

**Status**: [ ] Not Started / [ ] In Progress / [ ] Done

---

## Step 2: community 그룹 CRUD

- **Step Goal**: 로그인 사용자가 학습 그룹을 생성/조회/수정/삭제할 수 있다.
- **Done When**:
  - [ ] `POST /community/groups` → 그룹 생성 (이름, 설명, join_type, maxMembers, avatarUrl)
  - [ ] `GET /community/groups` → 그룹 목록 조회 (페이징)
  - [ ] `GET /community/groups/{id}` → 그룹 상세 조회
  - [ ] `PATCH /community/groups/{id}` → 그룹 정보 수정 (소유자만)
  - [ ] `DELETE /community/groups/{id}` → 그룹 삭제 (소유자만)
  - [ ] study_groups 테이블 Flyway 마이그레이션 완료
  - [ ] 통합 테스트 통과
- **Scope**:
  - In Scope:
    - study_groups 테이블 설계 + Flyway 마이그레이션
    - Group 엔티티 + Repository
    - GroupService CRUD 로직
    - GroupController REST API
    - 소유자 권한 검증
    - 페이징 조회
    - 통합 테스트
  - Out of Scope:
    - 그룹 검색 (OpenSearch — W3)
    - 그룹 카테고리/태그
    - 그룹 이미지 업로드
- **Input**: PRD_W1 그룹 기능 요구사항, JWT 인증 토큰 (platform-svc)
- **Instructions**:
  1. study_groups 테이블 DDL 작성 (id, tenant_id, name, description, join_type, maxMembers, avatarUrl, owner_user_id, created_at, updated_at)
     - `tenant_id`: 멀티테넌트 식별 컬럼 (필수)
     - `join_type`: `open` (즉시 가입), `approval` (승인 필요), `invite` (초대 전용) — `is_public` 대신 사용
  2. Flyway V1 마이그레이션 파일 생성
  3. Group 엔티티 + JPA Repository 작성
  4. GroupService 구현 (create, findAll, findById, update, delete)
  5. 소유자 권한 검증 로직 (수정/삭제 시 owner_user_id 확인)
  6. GroupController REST API 구현 (5개 엔드포인트: POST/GET/GET/{id}/PATCH/{id}/DELETE/{id})
  7. 페이징 처리 (Pageable, 기본 20건)
  8. 통합 테스트 작성 (@SpringBootTest + TestContainers)
- **Output Format**: community 모듈 코드 + Flyway 마이그레이션 + API 문서 (Swagger)
- **Constraints**:
  - 그룹명 최대 100자, 설명 최대 500자
  - 한 사용자 최대 10개 그룹 생성 가능
  - Soft delete (deleted_at 컬럼)
- **Duration**: 1.5일
- **RULE Reference**: wiki 03_아키텍처_정의서 §REST API 규칙, wiki 09_Git_규칙_정의서 §커밋 컨벤션
- **Assignee**: @engagement-owner
- **Reviewer**: @team-lead

**Status**: [ ] Not Started / [ ] In Progress / [ ] Done

---

## Step 3: community 멤버 관리

- **Step Goal**: 그룹 소유자가 멤버를 초대/가입승인/탈퇴시킬 수 있고, 멤버는 자발적으로 가입/탈퇴할 수 있다.
- **Done When**:
  - [ ] `POST /community/groups/{id}/invite` → 멤버 초대 (소유자)
  - [ ] `POST /community/groups/{id}/join` → 가입 신청 (사용자)
  - [ ] `PATCH /community/groups/{id}/join-requests/{uid}` → 가입 승인 (소유자, body: `{ "action": "approve" }`)
  - [ ] `DELETE /community/groups/{id}/members/{uid}` → 멤버 탈퇴/강퇴
  - [ ] `GET /community/groups/{id}/members` → 멤버 목록 조회
  - [ ] 멤버 역할 구분 (`owner`, `admin`, `member` — 소문자)
  - [ ] study_group_members 테이블 마이그레이션 완료
  - [ ] 통합 테스트 통과
- **Scope**:
  - In Scope:
    - study_group_members 테이블 설계 + Flyway 마이그레이션
    - GroupMember 엔티티 + Repository
    - 멤버 초대/가입/승인/탈퇴/강퇴 로직
    - 멤버 역할 관리 (`owner`, `admin`, `member`)
    - 멤버 목록 조회 API
    - 통합 테스트
  - Out of Scope:
    - 초대 이메일/알림 발송 (W2)
    - 멤버 활동 이력
    - 멤버 수 제한 정책
- **Input**: study_groups 테이블, JWT 인증 토큰, PRD_W1 멤버 관리 요구사항
- **Instructions**:
  1. study_group_members 테이블 DDL 작성 (group_id, user_id, tenant_id, role, status, joined_at)
     - `tenant_id`: 멀티테넌트 식별 컬럼 (필수)
     - status 값: `invited` (초대됨), `active` (활성), `banned` (강퇴/차단) — 소문자
  2. Flyway V2 마이그레이션 파일 생성
  3. GroupMember 엔티티 + Repository 작성
  4. MemberService 구현 (invite, join, approve, leave, kick)
  5. 역할 기반 권한 검증 (`owner`: 모든 작업, `admin`: 승인/강퇴, `member`: 탈퇴만)
  6. MemberController REST API 구현
  7. 멤버 상태 관리 (`invited`, `active`, `banned`)
  8. 통합 테스트 작성 (역할별 시나리오)
- **Output Format**: community 모듈 멤버 관리 코드 + Flyway 마이그레이션 + 테스트 코드
- **Constraints**:
  - 공개 그룹: 즉시 가입, 비공개 그룹: 승인 필요
  - `owner`는 탈퇴 불가 (소유권 이전 후 탈퇴)
  - 강퇴된 멤버는 7일간 재가입 불가
- **Duration**: 2일
- **RULE Reference**: wiki 03_아키텍처_정의서 §REST API 규칙, wiki 09_Git_규칙_정의서 §커밋 컨벤션
- **Assignee**: @engagement-owner
- **Reviewer**: @team-lead

**Status**: [ ] Not Started / [ ] In Progress / [ ] Done

---

## W2 (2026-05-18 ~ 2026-05-22, 5 영업일)

---

## Step 4: Kafka card.reviewed 이벤트 소비 및 XP 적립

- **Step Name**: card.reviewed XP 적립
- **Step Goal**: 시스템이 card.reviewed Kafka 이벤트를 소비하여 사용자에게 XP를 적립하고, 사용자가 누적 XP를 조회할 수 있다.
- **Done When**:
  - [ ] Kafka Consumer가 card.reviewed 이벤트 수신
  - [ ] 이벤트 수신 시 사용자 XP 자동 적립
  - [ ] xp_events 테이블에 XP 이벤트 이력 저장 + user_profiles_gamification 프로필 갱신
  - [ ] `GET /gamification/profile` → 누적 XP 조회 (구 `/gamification/xp/me` → Wiki 기준 `/gamification/profile`)
  - [ ] 중복 이벤트 처리 방지 (멱등성)
  - [ ] 통합 테스트 통과
- **Scope**:
  - In Scope:
    - Kafka Consumer 구현 (card.reviewed 토픽)
    - xp_events 테이블 설계 + Flyway 마이그레이션 (XP 이벤트 로그)
    - user_profiles_gamification 테이블 (누적 XP, 레벨 프로필 저장)
    - XP 적립 로직 (이벤트 → XP 계산 → 저장)
    - 누적 XP 조회 API
    - 멱등성 처리 (event_id 기반 중복 방지)
    - 통합 테스트 (Embedded Kafka)
  - Out of Scope:
    - XP 기반 레벨 시스템 (Step 6)
    - XP 차감/소모
    - XP 부스트/이벤트
- **Input**: card.reviewed Kafka 이벤트, 사용자 정보, XP 계산 규칙
- **Instructions**:
  1. xp_events 테이블 DDL 작성 (id, user_id, xp_amount, event_type, source_id, source_type, event_id, created_at)
     - `event_type`: 이벤트 종류 (예: `card_reviewed`), `source_id`/`source_type`: 이벤트 발생 원본 식별
     - 구 `source_event` 단일 컬럼 → `event_type` + `source_id` + `source_type` 분리
  2. user_profiles_gamification 테이블 DDL 작성 (누적 XP, 레벨, 뱃지 요약 등 프로필)
  3. Flyway 마이그레이션 파일 생성
  4. Kafka Consumer 구현 (@KafkaListener, card.reviewed 토픽)
  5. XP 계산 로직 구현 (카드 복습 1회 = 10 XP)
  6. 멱등성 처리 (event_id UNIQUE 제약)
  7. 누적 XP 조회 API 구현 (`GET /gamification/profile`)
  8. 통합 테스트 작성 (Embedded Kafka + TestContainers)
- **Output Format**: gamification 모듈 XP 코드 + Kafka Consumer + 테스트 코드
- **Constraints**:
  - 카드 복습 1회 = 10 XP (고정)
  - 멱등성: event_id 기반 중복 방지
  - XP 적립 지연: 이벤트 수신 후 3초 이내
- **Duration**: 1.5일
- **RULE Reference**: wiki 03_아키텍처_정의서 §이벤트 설계, wiki 18_기술_스택_정의서 §Kafka
- **Assignee**: @engagement-owner
- **Reviewer**: @team-lead

**Status**: [ ] Not Started / [ ] In Progress / [ ] Done

---

## Step 5: 덱/노트 공유 및 검색/복사

- **Step Name**: 공유 토큰 기반 콘텐츠 공유
- **Step Goal**: 사용자가 덱/노트를 share_token으로 공유하고, 다른 사용자가 공유 콘텐츠를 검색하여 복사할 수 있다.
- **Done When**:
  - [ ] `POST /community/shared-decks` → 덱 공유 토큰 생성 / `POST /community/shared-notes` → 노트 공유 토큰 생성
  - [ ] `GET /community/shared-decks/{id}` → 공유 덱 조회 / `GET /community/shared-notes/{id}` → 공유 노트 조회
  - [ ] `POST /community/shared-decks/{id}/copy` → 공유 덱 복사
  - [ ] `GET /community/shared-decks?q=...` → 공유 덱 검색
  - [ ] shared_decks / shared_notes 테이블 마이그레이션 완료
  - [ ] 통합 테스트 통과
- **Scope**:
  - In Scope:
    - shared_decks 테이블 설계 + Flyway 마이그레이션 (덱 공유)
    - shared_notes 테이블 설계 + Flyway 마이그레이션 (노트 공유)
    - share_token 생성 API (UUID 기반)
    - 공유 콘텐츠 조회 API (토큰 기반)
    - 공유 콘텐츠 복사 API
    - 공유 콘텐츠 검색 API (제목/태그)
    - 통합 테스트
  - Out of Scope:
    - 공유 콘텐츠 댓글/평점
    - 공유 만료 정책
    - OpenSearch 기반 검색 (DB LIKE 검색)
- **Input**: 덱/노트 데이터, JWT 인증 토큰, community 모듈
- **Instructions**:
  1. shared_decks 테이블 DDL 작성 (id, share_token, shared_by_user_id, title, description, share_type, allow_copy, download_count, rating_avg, rating_count, status, created_at)
     - `shared_by_user_id`: 공유자 식별 (구 `owner_id` → ERD 기준 `shared_by_user_id`)
     - share_token 컬럼: 12자리 base62 문자열 생성기 사용
  2. shared_notes 테이블 DDL 작성 (id, share_token, shared_by_user_id, title, description, share_type, allow_copy, download_count, rating_avg, rating_count, status, created_at)
  3. Flyway 마이그레이션 파일 생성
  4. 공유 토큰 생성 API 구현 (`POST /community/shared-decks`, `POST /community/shared-notes`)
     - share_token: 12자리 base62 문자열 (UUID v4 아님)
  5. 공유 콘텐츠 조회 API 구현 (ID 기반, 인증 불필요): `GET /community/shared-decks/{id}`, `GET /community/shared-notes/{id}`
  6. 공유 덱 복사 API 구현: `POST /community/shared-decks/{id}/copy` (인증 필요, 원본 복제)
  7. 공유 덱 검색 API 구현: `GET /community/shared-decks?q=...` (DB LIKE 검색, 페이징)
  8. 통합 테스트 작성
- **Output Format**: community 모듈 공유 코드 + Flyway 마이그레이션 + 테스트 코드
- **Constraints**:
  - share_token: 12자리 base62 문자열 (UUID v4 아님 — Wiki 기준)
  - 공유 콘텐츠 조회는 인증 불필요 (공개 접근)
  - 복사 시 원본과의 연결 유지 (source_share_id)
- **Duration**: 1.5일
- **RULE Reference**: wiki 03_아키텍처_정의서 §REST API 규칙, wiki 09_Git_규칙_정의서 §커밋 컨벤션
- **Assignee**: @engagement-owner
- **Reviewer**: @team-lead

**Status**: [ ] Not Started / [ ] In Progress / [ ] Done

---

## W3 (2026-05-26 ~ 2026-05-29, 5/25 부처님오신날 제외 — 이벤트 발행자)

---

## Step 6: 레벨/배지/스트릭/리더보드 시스템

- **Step Name**: 게이미피케이션 레벨/배지/스트릭/리더보드
- **Step Goal**: 시스템이 XP 누적에 따라 자동으로 레벨을 상승시키고, 조건 달성 시 배지를 수여하며, 연속 학습 스트릭을 추적하고, 리더보드를 제공한다.
- **Done When**:
  - [ ] XP 누적 → 레벨 자동 상승 (레벨 테이블 기준)
  - [ ] 조건 달성 시 배지 자동 수여 (e.g., 7일 연속 학습)
  - [ ] 일별 학습 스트릭 추적 (연속 일수)
  - [ ] `GET /gamification/leaderboard` → 주간 XP 리더보드 (`scope` 파라미터로 전체/그룹 리더보드 전환 지원)
  - [ ] `GET /gamification/profile` → 내 레벨/배지/스트릭 조회 (구 `/gamification/me` → Wiki 기준 `/gamification/profile`)
  - [ ] 통합 테스트 통과
- **Scope**:
  - In Scope:
    - level_definitions 테이블 (레벨별 필요 XP 정의, 구 `levels` → ERD 기준 `level_definitions`)
    - user_badges 테이블 + 배지 조건 엔진
    - 스트릭 추적 (user_profiles_gamification의 current_streak, longest_streak 컬럼 — 별도 테이블 아님)
    - 리더보드 조회 API (주간 XP 기준, Redis 캐시)
    - 내 게이미피케이션 현황 조회 API
    - 통합 테스트
  - Out of Scope:
    - 배지 이미지 관리
    - 시즌/리셋 정책
    - 팀 리더보드
- **Input**: xp_events 데이터, 레벨/배지 조건 정의, Redis, user_profiles_gamification
- **Instructions**:
  1. level_definitions 테이블 DDL + 초기 데이터 (Lv1=0, Lv2=100, Lv3=300, ...)
  2. user_badges 테이블 DDL + Flyway 마이그레이션
  3. user_profiles_gamification 테이블에 current_streak, longest_streak 컬럼 추가 (별도 user_streaks 테이블 불필요)
  4. 레벨 상승 로직 구현 (XP 적립 시 레벨 체크)
  5. 배지 조건 엔진 구현 (스트릭 7일, XP 1000 등)
  6. 스트릭 추적 로직 구현 (일별 학습 여부 체크 → user_profiles_gamification.current_streak 갱신)
  7. 리더보드 API 구현 (Redis Sorted Set 활용)
  8. 내 현황 조회 API 구현 (`GET /gamification/profile`)
  9. 통합 테스트 작성
- **Output Format**: gamification 모듈 레벨/배지/스트릭 코드 + Flyway 마이그레이션 + 테스트 코드
- **Constraints**:
  - 레벨 상승은 실시간 (XP 적립 즉시 체크)
  - 리더보드 캐시: Redis, TTL 5분
  - 스트릭 리셋: 자정(KST) 기준, 1일 미학습 시 리셋
- **Duration**: 2일
- **RULE Reference**: wiki 03_아키텍처_정의서 §게이미피케이션, wiki 18_기술_스택_정의서 §Redis
- **Assignee**: @engagement-owner
- **Reviewer**: @team-lead

**Status**: [ ] Not Started / [ ] In Progress / [ ] Done

---

## Step 7: 게이미피케이션 Kafka 이벤트 발행

- **Step Name**: gamification 이벤트 발행
- **Step Goal**: engagement-svc가 gamification.level.up/badge.earned Kafka 이벤트를 발행하여 알림을 트리거한다.
- **Done When**:
  - [ ] 레벨 상승 시 gamification.level.up 이벤트 발행
  - [ ] 배지 수여 시 gamification.badge.earned 이벤트 발행
  - [ ] 이벤트 스키마 Schema Registry 등록
  - [ ] platform-svc notification 모듈에서 이벤트 수신 확인
  - [ ] 통합 테스트 통과
- **Scope**:
  - In Scope:
    - Kafka Producer 구현 (gamification.level.up, gamification.badge.earned)
    - Avro 스키마 정의 + Schema Registry 등록
    - 레벨 상승/배지 수여 시 이벤트 발행 로직
    - 이벤트 발행 실패 시 재시도
    - 통합 테스트 (Embedded Kafka)
  - Out of Scope:
    - 알림 발송 로직 (platform-svc 담당)
    - 이벤트 소싱
    - 이벤트 버전 관리
- **Input**: 레벨/배지 로직, Kafka 토픽, Schema Registry
- **Instructions**:
  1. gamification.level.up Avro 스키마 정의 (userId, newLevel, xp, timestamp)
  2. gamification.badge.earned Avro 스키마 정의 (userId, badgeId, badgeName, timestamp)
  3. Schema Registry에 스키마 등록
  4. Kafka Producer 구현 (KafkaTemplate)
  5. 레벨 상승 로직에 이벤트 발행 추가 (토픽: gamification.level.up)
  6. 배지 수여 로직에 이벤트 발행 추가 (토픽: gamification.badge.earned)
  7. 통합 테스트 작성 (이벤트 발행 검증)
- **Output Format**: gamification 모듈 Kafka Producer 코드 + Avro 스키마 + 테스트 코드
- **Constraints**:
  - 이벤트 발행: at-least-once
  - 스키마 호환성: BACKWARD
  - 이벤트 발행 실패 시 로그 기록 (비즈니스 로직 실패 X)
- **Duration**: 0.5일
- **RULE Reference**: wiki 03_아키텍처_정의서 §이벤트 설계, wiki 18_기술_스택_정의서 §Kafka
- **Assignee**: @engagement-owner
- **Reviewer**: @team-lead

**Status**: [ ] Not Started / [ ] In Progress / [ ] Done

---

## Step 8: 부적절 콘텐츠 신고 및 관리자 처리

- **Step Name**: 콘텐츠 신고/관리자 처리
- **Step Goal**: 사용자가 부적절한 콘텐츠를 신고하고, 관리자가 신고를 처리(승인/거부/숨김)할 수 있다.
- **Done When**:
  - [ ] `POST /community/reports` → 콘텐츠 신고
  - [ ] `GET /admin/reports` → 신고 목록 조회 (관리자)
  - [ ] `PUT /admin/reports/{id}/resolve` → 신고 처리 (body: `{ "status": "resolved", "actionTaken": "..." }`)
     - 기존 `/approve` 및 `/reject` 별도 엔드포인트 → `/resolve` 단일 엔드포인트로 통합 (Wiki 기준)
  - [ ] reports 테이블 마이그레이션 완료
  - [ ] 통합 테스트 통과
- **Scope**:
  - In Scope:
    - reports 테이블 설계 + Flyway 마이그레이션
    - 신고 접수 API (사용자)
    - 신고 목록 조회 API (관리자)
    - 신고 처리 API (승인/거부/숨김)
    - 승인 시 콘텐츠 숨김 처리
    - 통합 테스트
  - Out of Scope:
    - 자동 신고 감지 (AI 기반)
    - 신고자 알림
    - 누적 신고 자동 차단
- **Input**: 콘텐츠 데이터, JWT 인증 토큰, 관리자 권한
- **Instructions**:
  1. reports 테이블 DDL 작성 (id, reporter_user_id, target_type, target_id, reason, status, resolved_by, resolved_at, created_at)
     - `reporter_user_id`: 신고자 식별 (구 `reporter_id` → ERD 기준 `reporter_user_id`)
     - `target_type`/`target_id`: 신고 대상 식별 (구 `content_type`/`content_id` → ERD 기준 `target_type`/`target_id`)
     - reason 값: `spam`, `abuse`, `inappropriate`, `copyright`, `other` (소문자)
     - status 값: `pending`, `reviewed`, `resolved`, `dismissed` (소문자)
  2. Flyway 마이그레이션 파일 생성
  3. 신고 접수 API 구현 (`POST /community/reports`)
  4. 신고 목록 조회 API 구현 (`GET /admin/reports` + 상태 필터)
  5. 신고 처리 API 구현: `PUT /admin/reports/{id}/resolve` (body: `{ "status": "resolved", "actionTaken": "..." }`)
     - 콘텐츠 숨김 처리(is_hidden = true) 또는 기각(dismiss)은 `actionTaken` 필드로 구분
  6. 신고 상태: `pending` → `reviewed` → `resolved` 또는 `dismissed` (소문자, Wiki 기준)
  7. 통합 테스트 작성 (사용자 신고 + 관리자 처리)
- **Output Format**: community 모듈 신고 코드 + Flyway 마이그레이션 + 테스트 코드
- **Constraints**:
  - 동일 사용자가 같은 콘텐츠 중복 신고 불가
  - 신고 사유: `spam`, `abuse`, `inappropriate`, `copyright`, `other` (소문자, ERD 기준)
  - 관리자 권한(ADMIN role) 필수 (처리 API)
- **Duration**: 1.5일
- **RULE Reference**: wiki 03_아키텍처_정의서 §REST API 규칙, wiki 09_Git_규칙_정의서 §커밋 컨벤션
- **Assignee**: @engagement-owner
- **Reviewer**: @team-lead

**Status**: [ ] Not Started / [ ] In Progress / [ ] Done

---

## W5 (2026-06-08 ~ 2026-06-12 — E2E + 발표 준비)

---

## Step 9: 게이미피케이션 전체 플로우 E2E 테스트

- **Step Name**: 게이미피케이션 E2E 테스트
- **Step Goal**: 게이미피케이션 전체 플로우(복습→XP→배지→레벨업→리더보드→알림) E2E가 통과한다.
- **Done When**:
  - [ ] card.reviewed → XP 적립 → 레벨 상승 시나리오 통과
  - [ ] 배지 조건 달성 → 배지 수여 → 알림 발송 시나리오 통과
  - [ ] 스트릭 추적 → 7일 연속 → 배지 수여 시나리오 통과
  - [ ] 리더보드 조회 시나리오 통과
  - [ ] 전체 플로우 연속 실행 통과
  - [ ] 실패 케이스 식별 및 이슈 등록
- **Scope**:
  - In Scope:
    - card.reviewed → XP → 레벨 E2E
    - 배지 조건 → 수여 → 알림 E2E
    - 스트릭 → 배지 E2E
    - 리더보드 조회 E2E
    - 서비스 간 이벤트 흐름 검증
    - 실패 케이스 이슈 등록
  - Out of Scope:
    - 부하/성능 테스트
    - 프론트엔드 연동 테스트
    - 데이터 마이그레이션 테스트
- **Input**: staging 환경, Kafka 토픽, 테스트 사용자 계정
- **Instructions**:
  1. E2E 테스트 환경 설정 (staging, 테스트 데이터 초기화)
  2. card.reviewed 이벤트 발행 → XP 적립 확인
  3. XP 누적 → 레벨 상승 확인
  4. 배지 조건 시뮬레이션 → 배지 수여 확인
  5. gamification.level.up/badge.earned 이벤트 → 알림 발송 확인
  6. 리더보드 데이터 정합성 확인
  7. 실패 케이스 식별 및 이슈 등록
- **Output Format**: E2E 테스트 코드 + 테스트 결과 리포트
- **Constraints**:
  - Happy Path 100% 통과 필수
  - 이벤트 전달 지연 < 5초
  - 테스트 데이터 자동 정리 (teardown)
- **Duration**: 1.5일
- **RULE Reference**: wiki 03_아키텍처_정의서 §테스트 전략, wiki 09_Git_규칙_정의서 §이슈 관리
- **Assignee**: @engagement-owner
- **Reviewer**: @team-lead

**Status**: [ ] Not Started / [ ] In Progress / [ ] Done

---

## Step 10: 커뮤니티 공유/신고 E2E 테스트 및 P0 버그 수정

- **Step Name**: 커뮤니티 E2E/P0 버그 수정
- **Step Goal**: 커뮤니티 공유/신고 플로우 E2E가 통과하고 P0 버그가 모두 수정된다.
- **Done When**:
  - [ ] 콘텐츠 공유 → 토큰 생성 → 조회 → 복사 시나리오 통과
  - [ ] 콘텐츠 신고 → 관리자 처리 시나리오 통과
  - [ ] P0 버그 전수 수정 완료
  - [ ] 수정된 버그 재현 테스트 통과
  - [ ] 회귀 테스트 전체 통과
- **Scope**:
  - In Scope:
    - 공유 토큰 생성 → 조회 → 복사 E2E
    - 신고 접수 → 관리자 처리 E2E
    - P0 버그 전수 수정
    - 버그 수정 후 재현 테스트
    - 회귀 테스트 실행
  - Out of Scope:
    - P1/P2 버그 수정
    - 새 기능 추가
    - 성능 최적화
- **Input**: staging 환경, P0 버그 목록 (GitHub Issues), 테스트 데이터
- **Instructions**:
  1. 공유 플로우 E2E 테스트 실행
  2. 신고 플로우 E2E 테스트 실행
  3. P0 버그 목록 확인 (GitHub Issues 필터)
  4. 각 버그 재현 → 원인 분석 → 수정
  5. 수정 후 재현 테스트 작성 및 통과 확인
  6. 전체 회귀 테스트 실행
  7. E2E + 회귀 테스트 결과 리포트 작성
- **Output Format**: E2E 테스트 코드 + 버그 수정 PR 목록 + 테스트 결과 리포트
- **Constraints**:
  - P0 버그 0건 달성 필수
  - 수정 시 회귀 방지 (테스트 추가 필수)
  - E2E Happy Path 100% 통과
- **Duration**: 1.5일
- **RULE Reference**: wiki 09_Git_규칙_정의서 §이슈 관리, wiki 03_아키텍처_정의서 §테스트 전략
- **Assignee**: @engagement-owner
- **Reviewer**: @team-lead

**Status**: [ ] Not Started / [ ] In Progress / [ ] Done
