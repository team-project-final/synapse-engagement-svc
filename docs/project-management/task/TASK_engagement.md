# TASK: @engagement-owner

> **담당 서비스**: engagement-svc (community / gamification)
> **GitHub Repository**: [synapse-engagement-svc](https://github.com/team-project-final/synapse-engagement-svc)
> **주차**: W1 (2026-05-12 ~ 2026-05-15, 4 영업일)
> **관련 문서**: [SCOPE](../scope/SCOPE_engagement.md) | [PRD_W1](../prd/PRD_W1.md) | [WORKFLOW](../workflow/WORKFLOW_engagement_W1.md) | [HISTORY](../history/HISTORY_engagement.md)

---

## Step 1: engagement-svc 골격 생성

- **Step Goal**: engagement-owner가 Spring Boot 4 + Modulith 기반 engagement-svc를 생성하여 community/gamification 모듈 골격이 동작한다.
- **Done When**:
  - [x] Spring Boot 4 + Modulith 프로젝트 초기화 완료
  - [x] community / gamification 2개 모듈 패키지 구조 생성
  - [x] `./gradlew build` 성공
  - [x] Modulith 구조 검증 테스트 통과 (`ApplicationModulesTest`)
  - [x] Docker 이미지 빌드 성공
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
- **Input**: 03*아키텍처*정의서 §Modulith 구조, platform-svc 골격 참조
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
- **RULE Reference**: wiki 03*아키텍처*정의서 §Modulith, wiki 18*기술*스택\_정의서
- **Assignee**: @engagement-owner
- **Reviewer**: @team-lead

**Status**: [ ] Not Started / [ ] In Progress / [x] Done

---

## Step 2: community 그룹 CRUD

- **Step Goal**: 로그인 사용자가 학습 그룹을 생성/조회/수정/삭제할 수 있다.
- **Done When**:
  - [x] `POST /groups` → 그룹 생성 (이름, 설명, 공개여부)
  - [x] `GET /groups` → 그룹 목록 조회 (페이징)
  - [x] `GET /groups/{id}` → 그룹 상세 조회
  - [x] `PUT /groups/{id}` → 그룹 정보 수정 (소유자만)
  - [x] `DELETE /groups/{id}` → 그룹 삭제 (소유자만)
  - [x] groups 테이블 Flyway 마이그레이션 완료
  - [x] 통합 테스트 통과
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
  - W1에서는 platform-svc JWT 연동 전까지 `X-User-Id` 헤더로 인증 사용자 ID를 전달한다.
- **Duration**: 1.5일
- **RULE Reference**: wiki 03*아키텍처*정의서 §REST API 규칙, wiki 09*Git*규칙\_정의서 §커밋 컨벤션
- **Assignee**: @engagement-owner
- **Reviewer**: @team-lead

**Status**: [ ] Not Started / [ ] In Progress / [x] Done

---

## Step 3: community 멤버 관리

- **Step Goal**: 그룹 소유자가 멤버를 초대/가입승인/탈퇴시킬 수 있고, 멤버는 자발적으로 가입/탈퇴할 수 있다.
- **Done When**:
  - [x] `POST /groups/{id}/members/invite` → 멤버 초대 (소유자)
  - [x] `POST /groups/{id}/members/join` → 가입 신청 (사용자)
  - [x] `PUT /groups/{id}/members/{memberId}/approve` → 가입 승인 (소유자/관리자)
  - [x] `DELETE /groups/{id}/members/{memberId}` → 멤버 탈퇴/강퇴
  - [x] `GET /groups/{id}/members` → 멤버 목록 조회
  - [x] 멤버 역할 구분 (OWNER, ADMIN, MEMBER)
  - [x] group_members 테이블 마이그레이션 완료
  - [x] 통합 테스트 통과
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
  - W1에서는 platform-svc JWT 연동 전까지 `X-User-Id` 헤더로 인증 사용자 ID를 전달한다.
  - `group_members.group_id`는 `groups.id` FK를 사용하고, `user_id`는 platform-svc 사용자 ID를 논리 참조한다.
- **Duration**: 2일
- **RULE Reference**: wiki 03*아키텍처*정의서 §REST API 규칙, wiki 09*Git*규칙\_정의서 §커밋 컨벤션
- **Assignee**: @engagement-owner
- **Reviewer**: @team-lead

**Status**: [ ] Not Started / [ ] In Progress / [x] Done

---

## W2 (2026-05-18 ~ 2026-05-22, 5 영업일)

---

## Step 4: gamification XP 기초 및 수동 적립 API

- **Step Name**: XP 적립/조회 기반 구축
- **Step Goal**: 시스템이 내부 API/서비스 호출로 사용자에게 XP를 적립하고, 사용자가 누적 XP와 XP 이력을 조회할 수 있다.
- **Done When**:
  - [x] 내부 XP 적립 유스케이스가 사용자 XP를 적립
  - [x] xp_events 테이블에 XP 이벤트 이력 저장 + user_profiles_gamification 프로필 갱신
  - [x] `GET /gamification/profile` → 누적 XP 조회 (구 `/gamification/xp/me` → Wiki 기준 `/gamification/profile`)
  - [x] `GET /gamification/xp/history` → XP 적립 이력 조회
  - [x] 중복 이벤트 처리 방지 (멱등성)
  - [x] 통합 테스트 통과
- **Scope**:
  - In Scope:
    - xp_events 테이블 설계 + Flyway 마이그레이션 (XP 이벤트 로그)
    - user_profiles_gamification 테이블 (누적 XP, 레벨 프로필 저장)
    - XP 적립 로직 (내부 유스케이스 호출 → XP 계산 → 저장)
    - 누적 XP 조회 API
    - XP 이력 조회 API
    - 멱등성 처리 (event_id 또는 user_id + event_type + source_id 기반 중복 방지)
    - 통합 테스트 (Testcontainers)
  - Out of Scope:
    - 외부 이벤트 연동 및 토픽 연결 (Step 9로 이연)
    - 서비스 간 이벤트 기반 XP 자동 적립
    - XP 기반 레벨 시스템 (Step 6)
    - XP 차감/소모
    - XP 부스트/이벤트
- **Input**: 사용자 정보, XP 계산 규칙, source_id/source_type 기반 원본 식별자
- **Instructions**:
  1. xp_events 테이블 DDL 작성 (id, user_id, xp_amount, event_type, source_id, source_type, event_id, created_at)
     - `event_type`: 이벤트 종류 (예: `card_reviewed`), `source_id`/`source_type`: 이벤트 발생 원본 식별
     - 구 `source_event` 단일 컬럼 → `event_type` + `source_id` + `source_type` 분리
  2. user_profiles_gamification 테이블 DDL 작성 (누적 XP, 레벨, 뱃지 요약 등 프로필)
  3. Flyway 마이그레이션 파일 생성
  4. 내부 XP 적립 유스케이스 구현 (`AddXpCommand` 기반)
  5. XP 계산 로직 구현 (기본 학습 활동 1회 = 10 XP)
  6. 멱등성 처리 (event_id UNIQUE 제약)
  7. 누적 XP 조회 API 구현 (`GET /gamification/profile`)
  8. XP 이력 조회 API 구현 (`GET /gamification/xp/history`)
  9. 통합 테스트 작성 (Testcontainers)
- **Output Format**: gamification 모듈 XP 코드 + REST/유스케이스 테스트 코드
- **Constraints**:
  - 최종 인증 방식은 JWT가 필요하지만, platform-svc JWT 검증 연동 전까지는 `X-User-Id` 헤더를 임시 인증 수단으로 사용한다
  - 사용자는 본인 XP/이력만 조회 가능
  - JWT 직접 파싱/검증 구현은 W2 Step 4 범위에서 보류하고, platform-svc 연동 시 실제 인증 principal 추출 방식으로 교체한다
  - XP 적립 유스케이스는 후속 입력 경로/내부 호출 재사용을 위해 인증 구현에 직접 의존하지 않는다
  - 외부 이벤트 연동은 W2 Step 4 범위에서 제외하고 Step 9로 분리한다
  - 기본 학습 활동 1회 = 10 XP (고정)
  - 멱등성: event_id 및 user_id + event_type + source_id 기반 중복 방지
  - 외부 이벤트 연동은 W4 전까지 구현 범위에서 제외
- **Duration**: 1.5일
- **RULE Reference**: wiki 03*아키텍처*정의서 §REST API 규칙, wiki 03*아키텍처*정의서 §멱등성
- **Assignee**: @engagement-owner
- **Reviewer**: @team-lead

**Status**: [ ] Not Started / [ ] In Progress / [x] Done

---

## Step 5: 덱/노트 공유 및 검색/복사

- **Step Name**: 공유 토큰 기반 콘텐츠 공유
- **Step Goal**: 사용자가 덱/노트를 share_token으로 공유하고, 다른 사용자가 공유 콘텐츠를 검색하여 복사할 수 있다.
- **Done When**:
  - [x] `POST /api/v1/community/share` → 공유 토큰 생성
  - [x] `GET /api/v1/community/share/{token}` → 공유 콘텐츠 토큰 조회
  - [x] `POST /api/v1/community/share/{token}/fork` → 공유 콘텐츠 복사
  - [x] `GET /api/v1/community/search?q=...` → 공유 콘텐츠 검색
  - [x] `DELETE /api/v1/community/share/{id}` → 소유자 공유 삭제
  - [x] shared_contents 테이블 마이그레이션 완료
  - [x] 통합 테스트 통과
- **Scope**:
  - In Scope:
    - shared_contents 테이블 설계 + Flyway 마이그레이션 (DECK/NOTE 단일 공유 모델)
    - share_token 생성 API (UUID v4 URL-safe 인코딩 기반)
    - 공유 콘텐츠 조회 API (토큰 기반)
    - 공유 콘텐츠 복사 API
    - 공유 콘텐츠 검색 API (제목/설명/태그)
    - 공유 콘텐츠 소유자 삭제 API
    - 통합 테스트
  - Out of Scope:
    - 공유 콘텐츠 댓글/평점
    - 공유 만료 정책
    - OpenSearch 기반 검색 (DB LIKE 검색)
    - 외부 이벤트 발행/소비 및 알림 연동
- **Input**: 덱/노트 데이터, JWT 인증 토큰, community 모듈
- **Instructions**:
  1. shared_contents 테이블 DDL 작성 (id, owner_id, content_type, content_id, share_token, title, description, tags, download_count, created_at, updated_at, deleted_at)
  2. share_token UNIQUE, owner_id, content_type, created_at, title/description/tags 검색 인덱스 작성
  3. Flyway V4 마이그레이션 파일 생성
  4. 공유 토큰 생성 API 구현: `POST /api/v1/community/share`
     - share_token: UUID v4 기반 URL-safe 문자열
  5. 공유 콘텐츠 조회 API 구현 (토큰 기반, 인증 불필요): `GET /api/v1/community/share/{token}`
  6. 공유 콘텐츠 검색 API 구현: `GET /api/v1/community/search?q=...&contentType=...`
  7. 공유 콘텐츠 복사 API 구현: `POST /api/v1/community/share/{token}/fork` (인증 필요)
  8. 공유 콘텐츠 삭제 API 구현: `DELETE /api/v1/community/share/{id}` (소유자만)
  9. 단위/슬라이스/통합 테스트 작성
- **Output Format**: community 모듈 공유 코드 + Flyway 마이그레이션 + 테스트 코드
- **Constraints**:
  - share_token: UUID v4 기반 URL-safe 인코딩
  - 공유 등록/복사처럼 인증이 필요한 API는 platform-svc JWT 연동 전까지 `X-User-Id` 임시 헤더를 사용
  - 공유 콘텐츠 조회는 인증 불필요 (공개 접근)
  - W2에서는 실제 learning-card 복제 연동 없이 공유 메타데이터 복사본을 생성한다
  - W2 Step 5까지 외부 이벤트 Producer/Consumer를 추가하지 않는다
- **Duration**: 1.5일
- **RULE Reference**: wiki 03*아키텍처*정의서 §REST API 규칙, wiki 09*Git*규칙\_정의서 §커밋 컨벤션
- **Assignee**: @engagement-owner
- **Reviewer**: @team-lead

**Status**: [ ] Not Started / [ ] In Progress / [ ] Done

---

## W3 (2026-05-26 ~ 2026-05-29, 5/25 부처님오신날 제외 — 기능 고도화)

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
- **RULE Reference**: wiki 03*아키텍처*정의서 §게이미피케이션, wiki 18*기술*스택\_정의서 §Redis
- **Assignee**: @engagement-owner
- **Reviewer**: @team-lead

**Status**: [ ] Not Started / [ ] In Progress / [ ] Done

---

## Step 7: 그룹 초대 수락/거절 및 가입 신청 관리

- **Step Name**: 그룹 멤버십 워크플로우 고도화
- **Step Goal**: 초대받은 사용자가 초대를 수락/거절할 수 있고, 그룹 관리자가 가입 신청 목록을 조회해 승인/거절할 수 있다.
- **Done When**:
  - [ ] `POST /community/groups/{id}/invite/{token}/accept` → 초대 수락
  - [ ] `POST /community/groups/{id}/invite/{token}/decline` → 초대 거절
  - [ ] `GET /community/groups/{id}/join-requests` → 가입 신청 목록 조회
  - [ ] `PATCH /community/groups/{id}/join-requests/{uid}` → 가입 승인/거절
  - [ ] 통합 테스트 통과
- **Scope**:
  - In Scope:
    - 초대 토큰 상태 관리 (invited → active/declined)
    - 가입 신청 목록/승인/거절 API
    - OWNER/ADMIN 권한 검증
    - 중복 수락/거절 방지
    - 통합 테스트
  - Out of Scope:
    - 초대 이메일/푸시 알림
    - 외부 이벤트 발행 (Step 9로 이연)
    - 소유권 이전
- **Input**: group_members 데이터, 초대 token, JWT 인증 사용자, OWNER/ADMIN 권한
- **Instructions**:
  1. 초대 token 컬럼/상태 모델 확인 및 부족 시 마이그레이션 작성
  2. 초대 수락 API 구현: token 검증 → ACTIVE 전환
  3. 초대 거절 API 구현: token 검증 → DECLINED 또는 제거 정책 적용
  4. 가입 신청 목록 조회 API 구현 (PENDING/INVITED 필터)
  5. 가입 신청 승인/거절 API 구현 (OWNER/ADMIN 권한)
  6. 중복 처리 및 만료/무효 token 에러 응답 정의
  7. 통합 테스트 작성
- **Output Format**: community 멤버십 고도화 코드 + Flyway 마이그레이션 + 테스트 코드
- **Constraints**:
  - 초대 token은 추측 불가능해야 한다.
  - OWNER/ADMIN만 가입 신청 승인/거절 가능
  - 동일 사용자의 중복 가입 신청 방지
- **Duration**: 0.5일
- **RULE Reference**: wiki 03*아키텍처*정의서 §REST API 규칙, wiki 09*Git*규칙\_정의서 §커밋 컨벤션
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
- **RULE Reference**: wiki 03*아키텍처*정의서 §REST API 규칙, wiki 09*Git*규칙\_정의서 §커밋 컨벤션
- **Assignee**: @engagement-owner
- **Reviewer**: @team-lead

**Status**: [ ] Not Started / [ ] In Progress / [ ] Done

---

## W4 (2026-06-01 ~ 2026-06-05 — Kafka 연동 + 안정화)

---

## Step 9: Kafka 이벤트 연동 및 XP 자동 적립 전환

- **Step Name**: Kafka card.reviewed 소비 및 gamification 이벤트 발행
- **Step Goal**: W2에서 만든 XP 적립 유스케이스를 Kafka `card.reviewed` 소비와 연결하고, 레벨업/배지 수여 이벤트 발행 계약을 확정한다.
- **Done When**:
  - [ ] `card.reviewed` Kafka 이벤트 소비 시 XP 적립
  - [ ] 중복 이벤트 수신 시 XP 중복 적립 방지
  - [ ] 레벨 상승 시 `gamification.level.up` 이벤트 발행
  - [ ] 배지 수여 시 `gamification.badge.earned` 이벤트 발행
  - [ ] EmbeddedKafka 또는 Testcontainers Kafka 통합 테스트 통과
- **Scope**:
  - In Scope:
    - Kafka Consumer 구현 (`card.reviewed`)
    - Kafka Producer 구현 (`gamification.level.up`, `gamification.badge.earned`)
    - Avro/JSON Schema 및 토픽 계약 문서화
    - W2 XP 유스케이스와 consumer 연결
    - 멱등성/재시도/DLQ 기본 정책
  - Out of Scope:
    - platform notification 실제 발송 구현
    - 이벤트 소싱
    - 대규모 부하 테스트
- **Input**: learning-card `card.reviewed` 이벤트, gamification 레벨/배지 로직, Kafka 토픽/Schema Registry
- **Instructions**:
  1. `card.reviewed` 이벤트 스키마 확정 (eventId, userId, cardId, reviewedAt)
  2. `card.reviewed` Consumer 구현 및 W2 `AddXpCommand` 연결
  3. `gamification.level.up` / `gamification.badge.earned` 이벤트 스키마 작성
  4. Producer 구현 및 레벨/배지 서비스에 연결
  5. Kafka 역직렬화/직렬화 설정 및 listener enable 플래그 정리
  6. 멱등성 테스트와 실패 재시도 정책 검증
  7. 토픽/스키마/소비자 가이드 문서화
- **Output Format**: Kafka consumer/producer 코드 + 스키마 문서 + 통합 테스트 코드
- **Constraints**:
  - 이벤트 발행/소비는 at-least-once 기준
  - XP 멱등성은 event_id 및 user_id + event_type + source_id 기준으로 보장
  - Kafka listener는 로컬 기본 비활성화, 통합 환경에서 명시적으로 활성화
- **Duration**: 1일
- **RULE Reference**: wiki 03*아키텍처*정의서 §이벤트 설계, wiki 18*기술*스택\_정의서 §Kafka
- **Assignee**: @engagement-owner
- **Reviewer**: @team-lead

**Status**: [ ] Not Started / [ ] In Progress / [x] Done

---

## Step 10: 게이미피케이션 전체 플로우 E2E 테스트

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
- **RULE Reference**: wiki 03*아키텍처*정의서 §테스트 전략, wiki 09*Git*규칙\_정의서 §이슈 관리
- **Assignee**: @engagement-owner
- **Reviewer**: @team-lead

**Status**: [ ] Not Started / [ ] In Progress / [ ] Done

---

## Step 11: 커뮤니티 공유/신고 E2E 테스트 및 P0 버그 수정

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
- **RULE Reference**: wiki 09*Git*규칙*정의서 §이슈 관리, wiki 03*아키텍처\_정의서 §테스트 전략
- **Assignee**: @engagement-owner
- **Reviewer**: @team-lead

**Status**: [ ] Not Started / [ ] In Progress / [ ] Done
