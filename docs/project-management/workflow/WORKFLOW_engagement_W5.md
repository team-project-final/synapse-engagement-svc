# WORKFLOW: engagement — Week 5

> **Task 문서**: [TASK_engagement.md](../task/TASK_engagement.md)
> **기간**: 2026-06-08 ~ 2026-06-12
> **PRD**: [PRD_W5.md](../prd/PRD_W5.md)
> **Source**: Workflow Dashboard, updated at 2026-05-27T08:33:09.800Z

---

## Step 12: 게이미피케이션 E2E

> 관련 이슈: [#27](https://github.com/team-project-final/synapse-engagement-svc/issues/27)

### 12.1 시나리오 정의
- [x] 복습 → XP 적립 → 레벨업 시나리오 작성
- [x] 배지 조건 → 수여 → 알림 시나리오 작성
- [x] 리더보드 조회 시나리오 작성

### 12.2 실행 및 수정
- [x] 게이미피케이션 E2E 테스트 실행
- [x] 이벤트 중복/멱등성 확인
- [x] P0 버그 수정 및 회귀 테스트: 신규 P0 없음
- [x] notification 연동 slice/contract 테스트 (gamification 이벤트 → fake notification command 변환)

### 12.3 배포 선결조건
- [x] MSK TLS-only 대비 `spring.kafka.security.protocol` 배선 반영 ([#26](https://github.com/team-project-final/synapse-engagement-svc/issues/26))
- [x] semver 이미지 릴리스 workflow 보정 ([#25](https://github.com/team-project-final/synapse-engagement-svc/issues/25))
- [x] Flyway migration guard 및 표준 runtime 옵션 반영 ([#28](https://github.com/team-project-final/synapse-engagement-svc/issues/28))
- [ ] ECR `synapse/engagement-svc:<신규 semver>` 도착 확인
- [ ] image-updater dev `newTag` 자동 bump 확인
- [ ] EKS engagement 파드 Kafka 초기화/MSK 연결 로그 확인

**검증 시나리오**
- learning `ReviewCompleted` 수신 → engagement XP 적립
- 누적 XP 100 도달 → level 1에서 level 2로 상승
- `FIRST_XP`, `LEVEL_2` 배지 수여 확인
- `engagement.gamification.level-up-v1`, `engagement.gamification.badge-earned-v1` Avro 이벤트 발행 확인
- fake notification processor가 gamification Avro 이벤트를 알림 command로 변환할 수 있는지 확인
- notification-svc 라이브 의존 없이 slice/contract test로 `LEVEL_UP`, `BADGE_EARNED` 알림 command 변환 확인
- `GET /api/v1/gamification/me`, `/xp/history`, `/leaderboard` 최종 상태 확인
- 동일 `ReviewCompleted` 재수신 시 XP 중복 적립 방지 확인

**검증 명령**
- `.\gradlew.bat test --tests "com.synapse.engagement.gamification.GamificationStep12FinalE2ETests"` → BUILD SUCCESSFUL

**외부 배포 검증 분리**
- 실제 notification-svc 라이브 연동은 Step 12 완료 조건이 아니라 W5 통합/배포 검증 항목으로 분리한다.
- ECR/image-updater/EKS/MSK 확인은 배포 환경 준비 후 진행한다.

**Step 12 Status**: [ ] Not Started / [ ] In Progress / [x] Done

---

## Step 13: 커뮤니티 E2E

> 관련 이슈: [#27](https://github.com/team-project-final/synapse-engagement-svc/issues/27)

### 13.1 시나리오 정의
- [x] 공유 덱 생성 → 검색 → 복사 시나리오 작성: [Step 13 Community Demo Scenario](../demo/2026-06-08-step13-community-demo-scenario.md)
- [x] 공유 노트 조회 시나리오 작성: [Step 13 Community Demo Scenario](../demo/2026-06-08-step13-community-demo-scenario.md)
- [x] 신고 접수 → 관리자 처리 시나리오 작성: [Step 13 Community Demo Scenario](../demo/2026-06-08-step13-community-demo-scenario.md)

### 13.2 실행 및 수정
- [x] 커뮤니티 E2E 테스트 실행
- [x] 권한/소유자 검증 실패 케이스 확인
- [x] P0 버그 수정 및 회귀 테스트: 신규 P0 없음
- [x] 커뮤니티 신고 → 모더레이션 → notification-send 발행 slice/contract 테스트
- [x] 게이미피케이션 E2E가 통과한다.
- [x] 커뮤니티 공유/신고 E2E가 통과한다.
- [x] engagement P0 버그가 0건이다.
- [x] 발표용 데모 데이터가 준비된다: [Step 13 Community Demo Scenario](../demo/2026-06-08-step13-community-demo-scenario.md)에 결정적 데모 actor/data/API 흐름 정리

**검증 시나리오**
- Step 11: 공유 덱 생성 → 검색 → 복사 → downloadCount/sourceShareId 검증
- Step 11: 공유 덱 신고 → 중복 신고 409 → 일반 사용자 관리자 API 403 → 관리자 승인 → 신고 대상 숨김
- Step 13: 공유 노트 생성 → token 조회 → NOTE 검색 → 신고 접수 → 관리자 승인
- Step 13: `platform.notification.notification-send-v1` Avro 이벤트 발행 확인
- Step 13: reporter `REPORT_RESOLVED`, owner `CONTENT_REMOVED` notification command 계약 확인

**검증 명령**
- `.\gradlew.bat test --tests "com.synapse.engagement.community.CommunityStep13FinalE2ETests"` → BUILD SUCCESSFUL
- `.\gradlew.bat test --tests "com.synapse.engagement.community.CommunityStep11E2ETests"` → 기존 Step 11 커뮤니티 E2E 기준

**외부 배포 검증 분리**
- 실제 notification-svc 라이브 소비/저장 검증은 Step 13 완료 조건이 아니라 W5 통합/배포 검증 항목으로 분리한다.

**Step 13 Status**: [ ] Not Started / [ ] In Progress / [x] Done
