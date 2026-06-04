# WORKFLOW: engagement — Week 4

> **Task 문서**: [TASK_engagement.md](../task/TASK_engagement.md)
> **기간**: 2026-06-01 ~ 2026-06-05
> **PRD**: [PRD_W4.md](../prd/PRD_W4.md)
> **Source**: Workflow Dashboard, updated at 2026-05-27T08:33:09.800Z

---

## Step 9: Kafka 이벤트 연동 — learning.card.review-completed 소비 + gamification 이벤트 발행

### 9.1 이벤트 계약 확정
- [x] `learning.card.review-completed-v1` 소비 스키마 확정 (`cardId`, `userId`, `tenantId`, `rating`, `nextReviewAt`, `reviewedAt`)
- [x] `engagement.gamification.level-up-v1` 발행 스키마 확정
- [x] `engagement.gamification.badge-earned-v1` 발행 스키마 확정

### 9.2 Consumer 구현
- [x] W2 XP 적립 유스케이스와 `ReviewCompleted` consumer 연결
- [x] Kafka Avro + Schema Registry 역직렬화 설정
- [x] listener enable 플래그 정리 (`synapse.kafka.enabled`)

### 9.3 Producer 구현
- [x] 레벨 상승 시 `engagement.gamification.level-up-v1` 발행
- [x] 배지 수여 시 `engagement.gamification.badge-earned-v1` 발행
- [x] producer 멱등성 설정 정리 (`enable.idempotence=true`, `acks=all`)

### 9.4 멱등성/보안 검토
- [x] 중복 `ReviewCompleted` 수신 시 XP 중복 적립 방지
- [x] 이벤트 payload 민감정보 미포함 확인
- [x] DLQ/재처리 정책 문서화: 1초 간격 3회 재시도 후 `{원본토픽}.dlq`로 발행

### 9.5 통합 테스트
- [x] EmbeddedKafka 통합 테스트 작성
- [x] `ReviewCompleted` → XP 적립 검증
- [x] `level-up`/`badge-earned` 발행 검증

### 9.6 문서 업데이트
- [x] 토픽/스키마/소비자 가이드 작성
- [x] HISTORY 완료 기록

**Step 9 Status**: [ ] Not Started / [ ] In Progress / [x] Done

---

## Step 10: 게이미피케이션 E2E 테스트 + 버그 수정

### 10.1 E2E 시나리오 정의
- [x] 게이미피케이션 플로우 시나리오 작성 (복습→XP→배지→레벨업→리더보드)
- [x] 테스트 데이터 준비 (`userId=10100`, `step10-review-*`)

### 10.2 E2E 테스트 실행
- [x] 게이미피케이션 전체 플로우 E2E 테스트 실행 (`GamificationStep10E2ETests`)
- [x] 실패 항목 기록: P0/P1/P2 신규 실패 없음

### 10.3 버그 트리아지
- [x] P0/P1/P2 분류
- [x] P0 즉시 수정 대상 확정: 없음

### 10.4 버그 수정
- [x] P0 버그 수정: 해당 없음
- [x] 게이미피케이션 관련 버그 수정: 해당 없음
- [x] 수정 코드 리뷰 + 테스트: Step 10 E2E 추가 및 전체 회귀 테스트 통과

### 10.5 회귀 테스트
- [x] 수정 후 전체 테스트 재실행 (`.\gradlew.bat test`)
- [x] 커버리지 80% 이상 확인: 정량 커버리지 도구 미구성 확인, 전체 회귀 테스트 통과로 대체 기록

### 10.6 문서 업데이트
- [x] API 문서 최신화: 기존 `/v3/api-docs` gamification endpoint smoke 유지
- [x] HISTORY 완료 기록

**Step 10 Status**: [ ] Not Started / [ ] In Progress / [x] Done

---

## Step 11: 커뮤니티 공유/신고 E2E 테스트 + 안정화

### 11.1 E2E 시나리오 정의
- [ ] 커뮤니티 플로우 시나리오 작성 (공유→검색→복사 + 신고→처리)
- [ ] 테스트 데이터 준비

### 11.2 E2E 테스트 실행
- [ ] 커뮤니티 전체 플로우 E2E 테스트 실행
- [ ] 실패 항목 기록

### 11.3 버그 트리아지
- [ ] P0/P1/P2 분류
- [ ] P0 즉시 수정 대상 확정

### 11.4 버그 수정
- [ ] P0 버그 수정
- [ ] 커뮤니티 관련 버그 수정
- [ ] 수정 코드 리뷰 + 테스트

### 11.5 회귀 테스트
- [ ] 수정 후 전체 테스트 재실행
- [ ] 커버리지 80% 이상 확인

### 11.6 문서 업데이트
- [ ] API 문서 최신화
- [ ] HISTORY 완료 기록

**Step 11 Status**: [x] Not Started / [ ] In Progress / [ ] Done
