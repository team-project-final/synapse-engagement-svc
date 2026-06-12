# Step 8 신고 모더레이션

## 작업일

- 2026-06-01

## 작업 내용

- workflow-guide Step 8 문서를 기준으로 community 신고 접수 및 관리자 모더레이션 기능을 구현했다.
- 신고 상태는 최신 guide 기준에 맞춰 `PENDING`, `APPROVED`, `REJECTED`로 정리했다.
- 신고 대상은 현재 engagement-svc가 실제로 소유하거나 식별할 수 있는 범위에 맞춰 `SHARED_DECK`, `SHARED_NOTE`, `STUDY_GROUP`, `USER`로 정의했다.
- `reports` 테이블을 추가하고 `reporter_id`, `target_type`, `target_id`, `reason`, `status`, `admin_note`, `created_at`, `resolved_at` 컬럼을 설계했다.
- 동일 사용자가 동일 대상에 여러 번 신고하지 못하도록 `reporter_id + target_type + target_id` 유니크 제약과 repository 중복 검사를 추가했다.
- `Report` Entity, `ReportRepository`, `ReportCreateRequest`, `ReportModerateRequest`, `ReportResponse`를 추가했다.
- 신고 응답에는 `reporterId`를 포함하지 않도록 설계해 신고 대상자에게 신고자가 노출되지 않게 했다.
- `ReportService`에서 신고 대상 존재 여부를 먼저 검증한 뒤 신고를 생성하도록 했다.
- `ModerationService`에서 관리자가 신고를 승인하거나 거부할 수 있게 했다.
- 승인 처리 시 `SHARED_DECK`/`SHARED_NOTE`는 `SharedContentService`의 soft delete 흐름을 사용하고, `STUDY_GROUP`은 `GroupService`의 soft delete 흐름을 사용하도록 연결했다.
- `USER` target은 engagement-svc가 사용자 계정 데이터를 소유하지 않기 때문에 신고 승인 상태까지만 기록하고 실제 계정 제재는 platform/auth 영역으로 남겼다.
- `CurrentUser.requireAdmin()`을 추가해 JWT `roles` 또는 `role` claim에 `ADMIN`이 없으면 관리자 API에서 403이 발생하도록 했다.
- API는 프로젝트의 기존 `/api/v1` 경로 규칙에 맞춰 `POST /api/v1/community/reports`, `GET /api/v1/admin/reports`, `PATCH /api/v1/admin/reports/{reportId}`로 구현했다.
- Flyway 위치에 `classpath:db/migration/community/report`를 추가하고 `V6__community_reports.sql` 마이그레이션을 등록했다.
- workflow/task 문서를 Step 8 구현 결과와 최신 guide 기준에 맞게 갱신했다.

## 검증

- `ReportServiceStep8Tests`를 추가해 중복 신고 409, 승인 시 대상 숨김, 거부 처리를 검증했다.
- `ReportControllerWebMvcTest`를 추가해 신고 생성, 중복 신고 409, 비관리자 관리자 API 접근 403, 관리자 목록 조회 및 승인 처리를 검증했다.
- `EngagementApiSmokeTests`에 Step 8 smoke flow를 추가해 신고 생성 → 중복 신고 차단 → 비관리자 403 → 관리자 승인 흐름을 검증했다.
- `/v3/api-docs` smoke test에 Step 8 신고/관리자 endpoint 노출 확인을 추가했다.
- `.\gradlew.bat test --tests "com.synapse.engagement.community.*" --tests "com.synapse.engagement.EngagementApiSmokeTests"` 통과.
- `.\gradlew.bat test` 전체 회귀 테스트 통과.

## 남은 이슈

- shared Avro 계약이 아직 없는 moderation audit Kafka 이벤트는 만들지 않았다.
- 실제 사용자 계정 제재는 engagement-svc가 처리하지 않고 platform/auth 서비스 설계가 확정된 뒤 연동해야 한다.
- 관리자 role claim 이름은 현재 `roles` 또는 `role`을 지원하며, 최종 platform JWT 계약이 고정되면 그 기준으로 한 번 더 정리해야 한다.
