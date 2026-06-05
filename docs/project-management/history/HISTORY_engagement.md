# Work History: engagement — synapse-engagement-svc

> **담당**: engagement-svc / community / gamification
> **Track Owner**: 한승완
> **Source of Truth**: [Workflow Dashboard](https://team-project-final.github.io/workflow-dashboard/#/detail/synapse-engagement-svc)
> **Dashboard Updated At**: 2026-05-27T08:33:09.800Z
> **관련 문서**: [SCOPE](../scope/SCOPE_engagement.md) | [TASK](../task/TASK_engagement.md) | [WORKFLOW W1](../workflow/WORKFLOW_engagement_W1.md) | [WORKFLOW W2](../workflow/WORKFLOW_engagement_W2.md)

---

## 진행 상태 대시보드

### W1 (2026-05-12 ~ 05-16)

| Step | 내용 | 상태 | 진행률 | 비고 |
|------|------|------|--------|------|
| Step 1 | engagement-svc 골격 생성 | Done | 32/32 | Spring Boot 4 + Modulith 골격, Dockerfile |
| Step 2 | community 그룹 CRUD | Done | 40/40 | groups API, owner 권한, soft delete |
| Step 3 | community 멤버 관리 | Done | 44/44 | invite/join/approve/remove/list |

**W1 진행률**: 116/116 Checks 완료

### W2 (2026-05-19 ~ 05-23)

| Step | 내용 | 상태 | 진행률 | 비고 |
|------|------|------|--------|------|
| Step 4 | gamification XP 기초 — xp_events 기록 + XP 조회 | Done | 52/52 | REST/DB 기반, Kafka는 W4로 이연 |
| Step 5 | community 공유 — share_token + 공유 콘텐츠 검색/복사 | Done | 54/54 | share/search/fork/delete |

**W2 진행률**: 106/106 Checks 완료

### W3 (2026-05-26 ~ 05-29)

| Step | 내용 | 상태 | 진행률 | 비고 |
|------|------|------|--------|------|
| Step 6 | gamification 완성 — 배지/레벨/스트릭/리더보드 | Done | 44/44 | badges/user_badges/user_streaks, WebMvc/통합 테스트 |
| Step 7 | Kafka 연동 — gamification.level_up / gamification.badge_earned 이벤트 발행 | In Progress | 36/38 | Producer/Schema/EmbeddedKafka/Docker Kafka 확인, ACL/notification 미검증 |
| Step 8 | community 신고 + Admin 모더레이션 | Done | 44/44 | reports API, ADMIN role claim, soft delete moderation |

**W3 진행률**: 124/126 Checks 완료

### W4 (2026-06-01 ~ 06-05)

| Step | 내용 | 상태 | 진행률 | 비고 |
|------|------|------|--------|------|
| Step 9 | Kafka 이벤트 연동 — learning.card.review-completed 소비 + gamification 이벤트 발행 | Done | 17/17 | Avro Consumer, XP 적립, DLQ, EmbeddedKafka 통합 테스트 |
| Step 10 | 게이미피케이션 E2E 테스트 + 버그 수정 | Done | 13/13 | REST E2E, 중복 방지, 이력/리더보드, 회귀 테스트 |
| Step 11 | 커뮤니티 공유/신고 E2E 테스트 + 안정화 | Done | 13/13 | 공유/검색/fork/신고/관리자 처리 E2E, 회귀 테스트 |

**W4 진행률**: 43/43 Checks 완료

### W5 (2026-06-08 ~ 06-12)

| Step | 내용 | 상태 | 진행률 | 비고 |
|------|------|------|--------|------|
| Step 12 | 게이미피케이션 E2E | Not Started | 0/6 | |
| Step 13 | 커뮤니티 E2E | Not Started | 0/10 | |

**W5 진행률**: 0/16 Checks 완료

---

## 작업 로그

### 2026-05-28 (목) — Codex

**한 일**
- docs: Workflow Dashboard 기준으로 `TASK_engagement.md`, `WORKFLOW_engagement_W1~W5.md` 최신화
- feat(community): 그룹 CRUD API 구현
- feat(community): 그룹 멤버 invite/join/approve/remove/list API 구현
- feat(gamification): XP 적립, 프로필 조회, XP 이력 조회 구현
- feat(community): share_token 기반 공유 등록/조회/검색/fork/delete 구현
- feat(gamification): Step 6 배지 수여, 레벨 계산, 스트릭 추적, 리더보드 구현
- test(gamification): Mockito 서비스 테스트, WebMvc 슬라이스 테스트, Swagger `/v3/api-docs` 노출 테스트 추가
- docs: workflow-guide 기준으로 W3 Step 7을 Kafka level_up/badge_earned 이벤트 발행 작업으로 재구성
- feat(gamification): Step 7 Kafka Producer, CloudEvents JSON envelope, Avro schema draft, JWT tenant claim 반영
- test(gamification): Step 7 서비스 트리거 테스트 및 EmbeddedKafka publish/consume 테스트 추가
- test(gamification): Docker Compose Kafka에서 XP 이벤트 API 호출 후 level-up/badge-earned topic 수신 확인
- chore(kafka): Schema Registry subject 등록 및 topic retention 설정 확인
- feat(community): 그룹 초대 token 수락/거절, 가입 신청 목록/승인/거절 구현 (guide 기준 Step 7 범위 밖 작업)
- test(community): 그룹 초대/가입 신청 Mockito 서비스 테스트, WebMvc 슬라이스 테스트, 통합 시나리오 테스트 추가
- chore(db): Flyway migration V1~V4 정리
- chore(db): group_members invite_token/invite_expires_at 마이그레이션 V5 추가
- chore(build): JPA/Flyway/H2/PostgreSQL 의존성 및 Dockerfile 추가

**이슈**
- 기존 `PlaceholderComponent` 3개가 동일 bean name `placeholderComponent`로 충돌해 Spring context가 실패함
- 각 placeholder bean name을 모듈별로 분리하여 context 로딩 문제 해결
- W1/W2는 대시보드상 Done이지만, 로컬 코드는 골격 수준이라 실제 구현을 추가해 상태를 맞춤
- Flyway 하위 경로별 `V1` 중복으로 bootRun 실패 가능성이 있어 migration version을 전역 순서로 정리
- WebMvc 슬라이스 테스트에서 `@AuthenticationPrincipal Jwt` 주입을 테스트 전용 resolver로 검증
- 기존 invite 흐름이 `PENDING`만 생성하던 상태라 멤버십 고도화 코드에는 `INVITED/DECLINED/REJECTED` 상태를 명시적으로 추가
- 초대 token은 수락/거절 시 제거하여 재사용을 차단하고, 가입 거절 사용자는 즉시 재신청 가능하도록 정책화
- 로컬 W3 Step 7 문서가 workflow-guide와 불일치했다. guide 기준 Step 7은 그룹 초대가 아니라 Kafka 이벤트 발행이므로 Done 상태를 해제하고 Kafka 작업으로 재구성함
- Step 7은 코드/EmbeddedKafka/Docker Kafka 검증까지 완료했지만, Kafka ACL과 notification 서비스 연동 테스트는 아직 남아 있어 Done 처리하지 않음

**검증**
- `./gradlew.bat test` 성공
- `./gradlew.bat build` 성공
- `./gradlew.bat test` 성공 (Step 6 서비스/컨트롤러/Swagger 문서 확인 포함)
- `./gradlew.bat test` 성공 (멤버십 고도화 서비스/WebMvc/통합/Swagger 문서 확인 포함)
- `./gradlew.bat test` 성공 (Step 7 Kafka Producer/EmbeddedKafka 포함)
- Docker Compose Kafka E2E 성공: `level-up` 1건, `badge-earned` 2건 console consumer 수신 확인

**내일 계획**
- W3 Step 7 Kafka ACL 적용 가능 여부 및 notification 연동 가능 여부 확인
- Step 7 완료 후 W3 Step 8 community 신고 + Admin 모더레이션 구현 착수

---

## 변경 이력

| 날짜 | 변경 사항 |
|------|-----------|
| 2026-06-05 | #25/#26/#28 배포 선결조건 보강: semver 이미지 릴리스 workflow, Kafka security.protocol, Flyway guard/runtime 표준 반영 |
| 2026-06-04 | W4 Step 11 커뮤니티 공유/신고 E2E 테스트 및 회귀 검증 완료 |
| 2026-06-04 | W4 Step 10 게이미피케이션 E2E 테스트 및 회귀 검증 완료 |
| 2026-06-04 | W4 Step 9 Kafka Consumer/Producer 통합 검증 및 DLQ 재처리 정책 반영 |
| 2026-06-01 | W3 Step 8 community 신고 + Admin 모더레이션 Done 처리 |
| 2026-05-28 | W3 Step 7 Kafka Producer/Schema/EmbeddedKafka 검증 구현 |
| 2026-05-28 | W3 Step 7 Docker Compose Kafka E2E 및 Schema Registry 등록 확인 |
| 2026-05-28 | workflow-guide 기준 W3 Step 7을 Kafka 이벤트 발행 작업으로 재구성 |
| 2026-05-28 | W3 Step 6 gamification 완성 Done 처리 및 검증 기록 반영 |
| 2026-05-28 | 대시보드 기준 W1/W2 완료 상태와 실제 구현 동기화 |
| 2026-05-28 | W3/W4/W5 대시보드 계획 반영 |
| 2026-05-11 | 초기 템플릿 생성 |
