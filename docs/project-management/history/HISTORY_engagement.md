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
| Step 4 | XP 시스템 구현 | Not Started | — | — | |
| Step 5 | 공유 기능 API | Not Started | — | — | |
| Step 6 | 피드 알고리즘 | Not Started | — | — | |

**W2 진행률**: 0/3 Steps 완료

### W3 (2026-05-26 ~ 05-30)

| Step | 내용 | 상태 | 시작일 | 완료일 | 비고 |
|------|------|------|--------|--------|------|
| Step 7 | 배지/레벨 시스템 | Not Started | — | — | |
| Step 8 | Kafka 이벤트 연동 | Not Started | — | — | |
| Step 9 | 신고/차단 기능 | Not Started | — | — | |

**W3 진행률**: 0/3 Steps 완료

### W4 (2026-06-02 ~ 06-06)

| Step | 내용 | 상태 | 시작일 | 완료일 | 비고 |
|------|------|------|--------|--------|------|
| Step 10 | E2E 테스트 | Not Started | — | — | |
| Step 11 | 버그 수정 | Not Started | — | — | |
| Step 12 | 안정화 | Not Started | — | — | |

**W4 진행률**: 0/3 Steps 완료

---

## 작업 로그

### W1 (2026-05-12 ~ 05-16)

#### 2026-05-12 (월)
- **완료**:
- **진행 중**:
- **이슈**:
- **다음**:

#### 2026-05-13 (화)
- **완료**:
- **진행 중**:
- **이슈**:
- **다음**:

#### 2026-05-14 (수)
- **완료**:
- **진행 중**:
- **이슈**:
- **다음**:

#### 2026-05-15 (목)
- **완료**:
- **진행 중**:
- **이슈**:
- **다음**:

#### 2026-05-16 (금)
- **완료**:
- **진행 중**:
- **이슈**:
- **주간 요약**:

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
  - W2 Step 4 Kafka `card.reviewed` 이벤트 소비 및 XP 적립 설계 확인.

#### 2026-05-19 (화)
- **완료**:
  - workflow-guide W1 Step 3 기준과 현재 구현을 비교했다.
  - 미사용 `MemberApproveRequest` DTO를 제거했다. 현재 approve API는 `PUT /api/v1/groups/{groupId}/members/{memberId}/approve` path variable을 API 계약으로 사용한다.
  - Step 3 역할별 테스트를 보강했다.
    - Mockito 단위 테스트: 일반 멤버 invite 차단, 소유자 approve 성공, OWNER 본인 탈퇴 차단, 소유자 강퇴 성공.
    - Testcontainers 통합 테스트: 소유자 invite 성공, 일반 멤버 invite 403, 소유자 강퇴 204, 강퇴 후 7일 이내 재가입 400.
  - Docker API 접근 확인: `docker info --format '{{.ServerVersion}}'` → `29.3.1`.
  - 테스트 실행: `.\gradlew.bat test --rerun-tasks`.
  - 테스트 결과: BUILD SUCCESSFUL.
    - `MemberControllerIntegrationTest`: 7 tests, skipped 0, failures 0, errors 0.
    - `MemberServiceTest`: 6 tests, skipped 0, failures 0, errors 0.
    - `GroupControllerIntegrationTest`: 7 tests, skipped 0, failures 0, errors 0.
    - `ModuleStructureTest`: 1 test, skipped 0, failures 0, errors 0.
- **진행 중**:
  - W2 Step 4 착수 준비.
- **이슈**:
  - ADMIN 승격/강등 API는 Step 3 Out of Scope라 통합 테스트에서 ADMIN 생성 흐름은 검증하지 않았다. 현재 도메인 권한 로직은 ADMIN을 관리자로 인정하지만, ADMIN 부여 API는 후속 작업 필요.
- **다음**:
  - Step 4 시작 시 TASK/WORKFLOW/HISTORY를 In Progress로 갱신하고 XP 이벤트/멱등성 ERD부터 확정한다.

### W2 (2026-05-19 ~ 05-23)

#### 2026-05-19 (월)
- **완료**:
- **진행 중**:
- **이슈**:
- **다음**:

#### 2026-05-20 (화)
- **완료**:
- **진행 중**:
- **이슈**:
- **다음**:

#### 2026-05-21 (수)
- **완료**:
- **진행 중**:
- **이슈**:
- **다음**:

#### 2026-05-22 (목)
- **완료**:
- **진행 중**:
- **이슈**:
- **다음**:

#### 2026-05-23 (금)
- **완료**:
- **진행 중**:
- **이슈**:
- **주간 요약**:

### W3 (2026-05-26 ~ 05-30)

#### 2026-05-26 (월)
- **완료**:
- **진행 중**:
- **이슈**:
- **다음**:

#### 2026-05-27 (화)
- **완료**:
- **진행 중**:
- **이슈**:
- **다음**:

#### 2026-05-28 (수)
- **완료**:
- **진행 중**:
- **이슈**:
- **다음**:

#### 2026-05-29 (목)
- **완료**:
- **진행 중**:
- **이슈**:
- **다음**:

#### 2026-05-30 (금)
- **완료**:
- **진행 중**:
- **이슈**:
- **주간 요약**:

### W4 (2026-06-02 ~ 06-06)

#### 2026-06-02 (월)
- **완료**:
- **진행 중**:
- **이슈**:
- **다음**:

#### 2026-06-03 (화)
- **완료**:
- **진행 중**:
- **이슈**:
- **다음**:

#### 2026-06-04 (수)
- **완료**:
- **진행 중**:
- **이슈**:
- **다음**:

#### 2026-06-05 (목)
- **완료**:
- **진행 중**:
- **이슈**:
- **다음**:

#### 2026-06-06 (금)
- **완료**:
- **진행 중**:
- **이슈**:
- **주간 요약**:

---

## 변경 이력

| 날짜 | 변경 사항 |
|------|-----------|
| 2026-05-11 | W2/W3/W4 대시보드 및 로그 템플릿 추가 |
| 2026-05-11 | 초기 템플릿 생성 |
