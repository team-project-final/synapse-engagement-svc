# 2026-05-18 — Step 2 community 그룹 CRUD 작업 기록

## 제목

Step 2: community 그룹 CRUD API 구현

## 주제

engagement-svc의 W1 Step 2 작업으로, 로그인 사용자가 학습 그룹을 생성/조회/수정/삭제할 수 있는 REST API와 영속성 계층을 구현한다.

## 목적

- PRD_W1의 FR-EG-001~003 요구사항을 충족한다.
- rules의 API prefix, soft delete, 소유자 검증, RFC 7807 에러 응답 규칙을 반영한다.
- Entity에 public setter를 열지 않고 정적 팩토리와 도메인 메서드로 상태를 변경한다.
- Flyway 기반 groups 테이블 마이그레이션과 통합 테스트를 통해 실제 API 흐름을 검증한다.

## 작업 내용

- JPA/Flyway/PostgreSQL/H2 의존성을 추가했다.
- `groups` 테이블 Flyway 마이그레이션을 추가했다.
  - `id`, `name`, `description`, `is_public`, `owner_id`, `created_at`, `updated_at`, `deleted_at`
  - `owner_id`, `is_public + created_at + id` 인덱스
- `Group` 엔티티를 추가했다.
  - `Group.create(...)` 정적 팩토리 사용
  - `updateDetails(...)`, `softDelete()`, `requireOwner(...)` 도메인 메서드 사용
  - public setter 없음
- `GroupRepository`를 추가했다.
  - 소유자별 활성 그룹 수 조회
  - 활성 그룹 단건 조회
  - 커서 기반 목록 조회
- DTO를 Java record로 추가했다.
  - `GroupCreateRequest`
  - `GroupUpdateRequest`
  - `GroupResponse`
  - `GroupCursorResponse`
- `CommunityService`에 그룹 생성/목록/상세/수정/삭제 로직을 구현했다.
- `CommunityController`에 `/api/v1/groups` REST API 5개를 구현했다.
- W1 인증 연동 전 임시 인증 입력으로 `X-User-Id` 헤더를 사용했다.
  - 헤더 없음: 401
  - 소유자 불일치: 403
- community 전용 예외 계층과 Problem Details 응답 핸들러를 추가했다.
- 랜덤 포트 기반 통합 테스트를 추가했다.
  - 생성 201
  - 커서 목록 조회
  - 소유자 수정
  - 비소유자 403
  - soft delete 후 404
  - 미인증 401
  - 사용자당 그룹 10개 제한

## 검증 결과

- `.\gradlew.bat compileJava` 성공
- `.\gradlew.bat test` 성공
- `.\gradlew.bat build` 성공

## 남은 작업

- Step 3 community 멤버 관리 착수
- platform JWT 연동 시 `X-User-Id` 임시 헤더를 실제 인증 principal 추출 방식으로 교체
- Swagger/OpenAPI 의존성 도입 시 그룹 API 문서 자동화

## 관련 문서

- `docs/project-management/task/TASK_engagement.md`
- `docs/project-management/workflow/WORKFLOW_engagement_W1.md`
- `docs/project-management/prd/PRD_W1.md`
