# Step 8 재검증

작업일: 2026-06-01

## 작업 내용

- 이전 테스트 결과를 재사용하지 않고 현재 프로젝트 파일 상태를 기준으로 Step1부터 Step8까지 문서, 규칙, 코드, 마이그레이션, 테스트를 다시 확인했다.
- `TASK_engagement.md`, `WORKFLOW_engagement_W3.md`, `GAMIFICATION_EVENTS.md`, Kafka/Avro rule 문서를 다시 대조해 Step7은 shared 결정 문서 기준의 bare Avro record, Step8은 신고자 비노출/중복 신고/관리자 처리 금지가 핵심임을 재확인했다.
- Step8 검증 중 `reports` 마이그레이션에 문서상 기대했던 `target_type`, `status` CHECK 제약이 빠져 있는 것을 발견해 `chk_reports_target_type`, `chk_reports_status`를 추가했다.
- Step8 관리자 권한 검증에서 `scope.contains("admin")` 방식은 부분 문자열 오탐 가능성이 있어, scope를 공백 단위 토큰으로 분리해 정확히 `admin`과 일치할 때만 통과하도록 보강했다.
- 신고 응답에서 `reporterId`가 노출되지 않는지 `ReportControllerWebMvcTest`에 명시 검증을 추가했다.
- Step1~Step8 Flyway 마이그레이션이 실제로 처음부터 실행되는지 확인하기 위해 `MigrationSmokeTests`를 추가했다.
- Flyway 검증 과정에서 Step7의 `V5__group_member_invites.sql`이 H2 검증 DB에서 실행되지 않는 문제를 발견했다. 여러 컬럼을 한 번에 추가하던 ALTER TABLE 문장을 분리하고, partial unique index를 일반 unique index로 바꿔 PostgreSQL/H2 양쪽에서 실행 가능한 형태로 정리했다.
- `CurrentUserTests`를 추가해 ADMIN role claim, 정확한 admin scope, `notadmin` 같은 부분 문자열 거부를 검증했다.

## 검증 내용

- `.\gradlew.bat test --tests com.synapse.engagement.MigrationSmokeTests --tests com.synapse.engagement.shared.CurrentUserTests --tests com.synapse.engagement.community.api.ReportControllerWebMvcTest`를 실행해 보강한 Step8 권한/응답/마이그레이션 검증을 통과시켰다.
- `.\gradlew.bat test`를 실행해 Step1~Step8 전체 회귀 테스트가 통과하는지 확인했다.
- `.\gradlew.bat build`를 실행해 Avro code generation, Java compile, resource 처리, bootJar/jar 조립, test/check까지 전체 빌드가 통과하는지 확인했다.

## 결과

- Step1~Step8 현재 코드 기준 전체 테스트와 빌드가 통과했다.
- Step8 신고/관리자 모더레이션은 기존 기능 테스트에 더해 DB 제약, 신고자 비노출, 관리자 scope 오탐 방지까지 검증 범위가 넓어졌다.
- 남은 수동 검증 항목은 실제 외부 Kafka ACL 적용과 실제 notification 서비스 연동이다. 현재 프로젝트 내부에서는 mock/embedded 기반 계약 검증까지만 완료된 상태다.
