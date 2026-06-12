# Work History: @engagement

> **담당**: engagement-svc / 커뮤니티  
> **관련 문서**: [SCOPE](../scope/SCOPE_engagement.md) | [TASK](../task/TASK_engagement.md) | [WORKFLOW](../workflow/WORKFLOW_engagement_W1.md)

---

## 진행 상태 대시보드

### W1 (2026-05-12 ~ 05-16)

| Step | 내용 | 상태 | 시작일 | 완료일 | 비고 |
|------|------|------|--------|--------|------|
| Step 1 | engagement-svc 골격 생성 | Done | 2026-05-18 | 2026-05-18 | Spring Boot 4 + Modulith 골격, build/test, Docker 이미지 검증 |
| Step 2 | community 그룹 CRUD | Done | 2026-05-18 | 2026-05-18 | 그룹 CRUD, soft delete, 소유자 권한, Testcontainers 검증 |
| Step 3 | community 멤버 관리 | Done | 2026-05-18 | 2026-05-19 | 멤버 초대/가입/승인/탈퇴/강퇴, 역할별 테스트 보강 |

**W1 진행률**: 3/3 Steps 완료

### W2 (2026-05-19 ~ 05-23)

| Step | 내용 | 상태 | 시작일 | 완료일 | 비고 |
|------|------|------|--------|--------|------|
| Step 4 | XP 시스템 구현 | Done | 2026-05-19 | 2026-05-19 | XP 적립/조회 유스케이스, 멱등성, Testcontainers 검증 |
| Step 5 | 공유 기능 API | Done | 2026-05-20 | 2026-05-20 | share_token, 공유 검색/복사, shared_contents, Testcontainers 검증 |

**W2 진행률**: 2/2 Steps 완료

### W3 (2026-05-26 ~ 05-30) / 실 작업일: 2026-06-12

| Step | 내용 | 상태 | 시작일 | 완료일 | 비고 |
|------|------|------|--------|--------|------|
| Step 6 | 배지/레벨/스트릭/리더보드 시스템 | Done | 2026-06-12 | 2026-06-12 | LevelService·BadgeService·StreakService·LeaderboardService(Redis), Flyway V5-V8, 단위/통합 테스트 전체 통과 |
| Step 7 | 그룹 초대/가입 신청 고도화 | Not Started | — | — | |
| Step 8 | 신고/Admin 모더레이션 | Not Started | — | — | |

**W3 진행률**: 1/3 Steps 완료

### W4 (2026-06-02 ~ 06-06)

| Step | 내용 | 상태 | 시작일 | 완료일 | 비고 |
|------|------|------|--------|--------|------|
| Step 9 | Kafka 이벤트 연동 | Not Started | — | — | card.reviewed 소비, gamification 이벤트 발행 |
| Step 10 | 게이미피케이션 E2E 테스트 + 버그 수정 | Not Started | — | — | |
| Step 11 | 커뮤니티 공유/신고 E2E 테스트 + 안정화 | Not Started | — | — | |

**W4 진행률**: 0/3 Steps 완료

---

## 작업 로그

### W1 (2026-05-12 ~ 05-16)

#### 2026-05-18 (월)
- **완료**:
  - Step 1 engagement-svc 골격 생성 완료.
  - Step 2 community 그룹 CRUD 완료.
  - Step 3 community 멤버 관리 구현 완료.
  - `group_members` Flyway V2 마이그레이션 작성: `groups.id` FK, `group_id + user_id` UNIQUE, 상태 기반 인덱스.
  - `MemberService` invite/join/approve/delete/list 구현 및 OWNER/ADMIN/MEMBER 권한 분기 적용.
  - `MemberController` REST API 구현: invite, join, approve, delete, list.
  - `X-User-Id` 임시 인증 헤더 기반 401/403 ProblemDetail 응답 연결.
- **진행 중**:
  - W2 Step 4 XP 시스템 착수 전 문서/테스트 정합성 확인.
- **이슈**:
  - platform-svc JWT 연동 전까지는 `X-User-Id` 헤더를 임시 인증 수단으로 사용한다.
  - `group_members.user_id`는 platform-svc 사용자 ID 논리 참조로 두고, 서비스 간 DB FK는 설정하지 않는다.
- **다음**:
  - W2 Step 4 XP 적립/조회 유스케이스 설계 확인. 외부 이벤트 연동은 W4 이후로 이연.

#### 2026-05-19 (화)
- **완료**:
  - workflow-guide W1 Step 3 기준과 현재 구현을 비교했다.
  - 미사용 `MemberApproveRequest` DTO를 제거했다. 현재 approve API는 `PUT /api/v1/groups/{groupId}/members/{memberId}/approve` path variable을 API 계약으로 사용한다.
  - Step 3 역할별 테스트를 보강했다.
    - Mockito 단위 테스트: 일반 멤버 invite 차단, 소유자 approve 성공, OWNER 본인 탈퇴 차단, 소유자 강퇴 성공.
    - Testcontainers 통합 테스트: 소유자 invite 성공, 일반 멤버 invite 403, 소유자 강퇴 204, 강퇴 후 7일 이내 재가입 400.
  - 테스트 결과: BUILD SUCCESSFUL.
- **진행 중**:
  - W2 Step 4 착수 준비.
- **이슈**:
  - ADMIN 승격/강등 API는 Step 3 Out of Scope라 통합 테스트에서 ADMIN 생성 흐름은 검증하지 않았다.
- **다음**:
  - Step 4 시작 시 TASK/WORKFLOW/HISTORY를 In Progress로 갱신하고 XP 적립/멱등성 ERD부터 확정한다.

### W2 (2026-05-19 ~ 05-23)

#### 2026-05-19 (월)
- **완료**:
  - Step 4 gamification XP 기초 구현 완료.
  - `user_profiles_gamification` / `xp_events` Flyway V3 마이그레이션 추가.
  - `GamificationService.addXp/getProfile/getXpHistory` 구현.
  - XP 적립 유스케이스 구현: 학습 활동 1회 = 10 XP 적립 기준을 적용.
  - 멱등성 처리: `event_id` UNIQUE + `event_type/source_id` UNIQUE + 서비스 중복 체크.
  - `GET /api/v1/gamification/profile`, `GET /api/v1/gamification/xp/history` 구현.
  - 외부 이벤트 작업은 W4 이후로 이연하고, XP 이벤트 source 멱등성 UNIQUE 기준을 `user_id + event_type + source_id`로 정정했다.
  - W2 skeleton 방향에 맞춰 공통 모듈 패키지를 `shared/`에서 `global/`로 정리했다.
  - 테스트 결과: BUILD SUCCESSFUL.
- **진행 중**:
  - W2 Step 5 공유 기능 API 착수 준비.
- **이슈**:
  - 외부 이벤트 consumer/producer는 W4 이후 토픽/스키마가 확정된 뒤 연동한다.
- **다음**:
  - Step 5 공유 토큰/공유 덱 API 설계 및 마이그레이션 착수.

#### 2026-05-20 (화)
- **완료**:
  - W2 Step 4 workflow guide 기준으로 gamification XP 조회 API 인증 방식을 재정리했다.
  - Step 5 community 공유 기능 API 구현 완료.
  - `POST /api/v1/community/share`, `GET /api/v1/community/share/{token}`, `GET /api/v1/community/search`, `POST /api/v1/community/share/{token}/fork`, `DELETE /api/v1/community/share/{id}` 구현.
  - share_token은 UUID v4 기반 URL-safe 문자열로 생성하고, UNIQUE 제약과 soft delete 기반 무효화 정책을 적용했다.
  - `SharedContentServiceTest`, `SharedContentControllerWebMvcTest`, `SharedContentControllerIntegrationTest` 추가.
  - 테스트 결과: BUILD SUCCESSFUL.
- **진행 중**:
  - W2 잔여 작업 범위 확인.
- **이슈**:
  - platform-svc의 실제 JWT 서명 검증/JWK 연동은 보안 공통 설정이 확정되는 시점에 Resource Server 방식으로 진행한다.
- **다음**:
  - Step 5 API 응답 예시와 Swagger 노출을 확인한다.

### W3 (2026-05-26 ~ 05-30) / 실 작업일: 2026-06-12

#### 2026-06-12 (금) — Step 6 완료
- **완료**:
  - Step 6 레벨/배지/스트릭/리더보드 시스템 전체 구현 완료.
  - Flyway 마이그레이션 V5-V8 추가.
    - V5: `level_definitions` 테이블 + Lv1(0 XP, Novice)~Lv10(4500 XP, Grandmaster) 초기 데이터.
    - V6: `badges` 테이블 + 9개 배지 정의(FIRST_STEP, CENTURION, SCHOLAR, LEGEND, STREAK_3/7/30, LEVEL_5/10) + `criteria_json`.
    - V7: `user_badges` 테이블 (user_id + badge_code UNIQUE, FK badges(code)).
    - V8: `user_profiles_gamification.last_activity_date DATE` 컬럼 추가.
  - `LevelDefinition`, `Badge`, `UserBadge` 엔티티 추가.
  - `UserProfilesGamification`에 `applyLevel()` + `updateStreak()` 메서드 추가 (KST 기준 연속 일수 계산, 자정 리셋).
  - `LevelService` (XP → 레벨 자동 상승, `findCurrentLevel` JPQL 쿼리 활용).
  - `BadgeService` (badges.criteria_json Jackson 파싱 → 조건 평가 → 중복 수여 방지 → `user_badges` 저장).
  - `StreakService` (KST 기준 당일/익일/리셋 분기 처리).
  - `LeaderboardService` (Redis ZSet `engagement:leaderboard` → DB 폴백).
  - `GamificationService.addXp()` 재구성: 스트릭→레벨→배지→리더보드 순서로 갱신.
  - `GET /gamification/leaderboard?scope=global&limit=10` 엔드포인트 추가.
  - `BadgeResponse`, `LeaderboardEntryResponse` DTO 추가.
  - `JacksonConfig` 추가 (Spring Boot 4.0 + Modulith ObjectMapper bean 미노출 이슈 해소).
  - `RedisConfig`에 `RedisTemplate<String, String>` Bean 추가.
  - `LevelServiceTest`(3), `BadgeServiceTest`(4), `StreakServiceTest`(5), `GamificationServiceTest`(2) 작성 — 전체 통과.
  - `./gradlew test` BUILD SUCCESSFUL.
- **진행 중**: —
- **이슈**:
  - Spring Boot 4.0 + Spring Modulith 환경에서 `ObjectMapper`가 자동 노출되지 않아 `BadgeService` 주입 실패 → `JacksonConfig` 빈 명시 등록으로 해소.
  - Mockito strict stubbing 충돌 (lenient 처리), `UnfinishedStubbingException` (mock 생성 순서 보정)으로 해소.
- **다음**:
  - Step 7 그룹 초대 수락/거절 + 가입 신청 관리 구현.
- **주간 요약**:
  - Step 6 완료로 gamification 모듈 기능 고도화(레벨·배지·스트릭·리더보드) 완성.

### W4 (2026-06-02 ~ 06-06)

#### (미기록)
- **완료**:
- **진행 중**:
- **이슈**:
- **주간 요약**:

---

## 변경 이력

| 날짜 | 변경 사항 |
|------|-----------|
| 2026-06-12 | Step 6 완료 PM 문서 동기화 (HISTORY/TASK/WORKFLOW/PRD/SCOPE) |
| 2026-05-20 | Step 5 공유 기능 API 구현 완료 |
| 2026-05-19 | Step 4 XP 시스템 구현 완료, Step 3 역할별 테스트 보강 |
| 2026-05-18 | Step 1/2/3 구현 완료 |
| 2026-05-11 | 초기 템플릿 생성 |
