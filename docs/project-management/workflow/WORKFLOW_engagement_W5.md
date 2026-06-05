# WORKFLOW: engagement — Week 5

> **Task 문서**: [TASK_engagement.md](../task/TASK_engagement.md)
> **기간**: 2026-06-08 ~ 2026-06-12
> **PRD**: [PRD_W5.md](../prd/PRD_W5.md)
> **Source**: Workflow Dashboard, updated at 2026-05-27T08:33:09.800Z

---

## Step 12: 게이미피케이션 E2E

> 관련 이슈: [#27](https://github.com/team-project-final/synapse-engagement-svc/issues/27)

### 12.1 시나리오 정의
- [ ] 복습 → XP 적립 → 레벨업 시나리오 작성
- [ ] 배지 조건 → 수여 → 알림 시나리오 작성
- [ ] 리더보드 조회 시나리오 작성

### 12.2 실행 및 수정
- [ ] 게이미피케이션 E2E 테스트 실행
- [ ] 이벤트 중복/멱등성 확인
- [ ] P0 버그 수정 및 회귀 테스트
- [ ] notification 서비스 연동 테스트 (gamification 이벤트 → 알림 발송)

### 12.3 배포 선결조건
- [x] MSK TLS-only 대비 `spring.kafka.security.protocol` 배선 반영 ([#26](https://github.com/team-project-final/synapse-engagement-svc/issues/26))
- [x] semver 이미지 릴리스 workflow 보정 ([#25](https://github.com/team-project-final/synapse-engagement-svc/issues/25))
- [x] Flyway migration guard 및 표준 runtime 옵션 반영 ([#28](https://github.com/team-project-final/synapse-engagement-svc/issues/28))
- [ ] ECR `synapse/engagement-svc:<신규 semver>` 도착 확인
- [ ] image-updater dev `newTag` 자동 bump 확인
- [ ] EKS engagement 파드 Kafka 초기화/MSK 연결 로그 확인

**Step 12 Status**: [x] Not Started / [ ] In Progress / [ ] Done

---

## Step 13: 커뮤니티 E2E

> 관련 이슈: [#27](https://github.com/team-project-final/synapse-engagement-svc/issues/27)

### 13.1 시나리오 정의
- [ ] 공유 덱 생성 → 검색 → 복사 시나리오 작성
- [ ] 공유 노트 조회 시나리오 작성
- [ ] 신고 접수 → 관리자 처리 시나리오 작성

### 13.2 실행 및 수정
- [ ] 커뮤니티 E2E 테스트 실행
- [ ] 권한/소유자 검증 실패 케이스 확인
- [ ] P0 버그 수정 및 회귀 테스트
- [ ] 커뮤니티 신고 → 모더레이션 → notification-send 발행 라이브 E2E 확인
- [ ] 게이미피케이션 E2E가 통과한다.
- [ ] 커뮤니티 공유/신고 E2E가 통과한다.
- [ ] engagement P0 버그가 0건이다.
- [ ] 발표용 데모 데이터가 준비된다.

**Step 13 Status**: [x] Not Started / [ ] In Progress / [ ] Done
