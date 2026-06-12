# 2026-05-18 — Step 3 community 멤버 관리 작업 기록

## 제목

Step 3: community 멤버 관리 API 구현

## 주제

engagement-svc의 W1 Step 3 작업으로, 학습 그룹의 멤버 초대/가입/승인/탈퇴/강퇴/목록 조회 흐름을 구현한다.

## 목적

- PRD_W1의 FR-EG-004~005 요구사항을 충족한다.
- OWNER, ADMIN, MEMBER 역할과 PENDING, ACTIVE, KICKED 상태 전이를 명확히 둔다.
- 공개 그룹은 즉시 가입, 비공개 그룹은 승인 대기 정책을 구현한다.
- 강퇴된 멤버의 7일 재가입 제한과 OWNER 탈퇴 차단 규칙을 반영한다.
- Entity에 public setter를 열지 않고 정적 팩토리와 도메인 메서드로 상태를 변경한다.

## 작업 내용

- `group_members` 테이블 Flyway V2 마이그레이션을 추가했다.
  - `id`, `group_id`, `user_id`, `role`, `status`, `joined_at`, `created_at`, `updated_at`, `kicked_at`
  - `group_id + user_id` UNIQUE 제약
  - `group_id + status`, `user_id + status` 인덱스
  - `group_id`는 `groups.id` FK, `user_id`는 platform-svc 사용자 ID 논리 참조
- `GroupMember` 엔티티를 추가했다.
  - `owner(...)`, `invited(...)`, `joined(...)` 정적 팩토리 사용
  - `approve()`, `leave(...)`, `kick()`, `reactivate(...)` 도메인 메서드 사용
  - public setter 없음
- `MemberRole` enum을 추가했다.
  - `OWNER`, `ADMIN`, `MEMBER`
- `MemberStatus` enum을 추가했다.
  - `PENDING`, `ACTIVE`, `KICKED`
- `GroupMemberRepository`를 추가했다.
  - 그룹+사용자 단건 조회
  - 그룹+상태 조회
  - 그룹 멤버 목록 조회
- DTO를 Java record로 추가했다.
  - `MemberInviteRequest`
  - `MemberResponse`
- approve API는 `PUT /api/v1/groups/{groupId}/members/{memberId}/approve` path variable을 계약으로 사용한다.
- `MemberMapper` MapStruct 매퍼를 추가했다.
- `MemberService`를 구현했다.
  - invite, join, approve, delete, list
  - OWNER/ADMIN/MEMBER 역할 기반 권한 검증
  - 공개/비공개 그룹 가입 정책
  - 강퇴 7일 재가입 제한
- `MemberController`를 추가했다.
  - `POST /api/v1/groups/{groupId}/members/invite`
  - `POST /api/v1/groups/{groupId}/members/join`
  - `PUT /api/v1/groups/{groupId}/members/{memberId}/approve`
  - `DELETE /api/v1/groups/{groupId}/members/{memberId}`
  - `GET /api/v1/groups/{groupId}/members`
- 그룹 생성 시 OWNER 멤버 row가 자동 생성되도록 `GroupService`를 확장했다.
- 멤버 관리 예외를 Problem Details 응답에 연결했다.
- Mockito 단위 테스트, WebMvc slice 테스트, TestContainers PostgreSQL 통합 테스트를 추가했다.
- TASK/WORKFLOW 문서를 Step 3 완료 상태로 갱신했다.

## 검증 결과

- `gradle compileJava` 성공
- `gradle compileTestJava` 성공
- `gradle test` 성공
- 2026-05-19 재검증: Docker/Testcontainers 활성 상태에서 `.\gradlew.bat test --rerun-tasks` 성공
  - `MemberControllerIntegrationTest`: 7 tests, skipped 0, failures 0, errors 0
  - `MemberServiceTest`: 6 tests, skipped 0, failures 0, errors 0
  - `GroupControllerIntegrationTest`: 7 tests, skipped 0, failures 0, errors 0

## 남은 작업

- platform JWT 연동 시 `X-User-Id` 임시 헤더를 실제 인증 principal 추출 방식으로 교체
- 소유권 이전 기능이 생기면 OWNER 탈퇴 정책과 연결
- ADMIN 승격/강등 API가 필요하면 후속 Step 또는 별도 이슈로 분리

## 관련 문서

- `docs/project-management/task/TASK_engagement.md`
- `docs/project-management/workflow/WORKFLOW_engagement_W1.md`
- `docs/project-management/prd/PRD_W1.md`
