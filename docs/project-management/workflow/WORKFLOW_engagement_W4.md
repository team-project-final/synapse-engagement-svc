# WORKFLOW: engagement — Week 4

> **Task 문서**: [TASK_engagement.md](../task/TASK_engagement.md)
> **기간**: 2026-06-01 ~ 2026-06-05
> **PRD**: [PRD_W4.md](../prd/PRD_W4.md)
> **Source**: Workflow Dashboard, updated at 2026-05-27T08:33:09.800Z

---

## Step 9: Kafka 이벤트 연동 — card.reviewed 소비 + gamification 이벤트 발행

### 9.1 이벤트 계약 확정
- [ ] card.reviewed 소비 스키마 확정 (eventId, userId, cardId, reviewedAt)
- [ ] gamification.level.up 발행 스키마 확정
- [ ] gamification.badge.earned 발행 스키마 확정

### 9.2 Consumer 구현
- [ ] W2 XP 적립 유스케이스와 card.reviewed consumer 연결
- [ ] Kafka JSON/Avro 역직렬화 설정
- [ ] listener enable 플래그 정리

### 9.3 Producer 구현
- [ ] 레벨 상승 시 gamification.level.up 발행
- [ ] 배지 수여 시 gamification.badge.earned 발행
- [ ] 발행 실패 로깅/재시도 정책 정리

### 9.4 멱등성/보안 검토
- [ ] 중복 card.reviewed 수신 시 XP 중복 적립 방지
- [ ] 이벤트 payload 민감정보 미포함 확인
- [ ] DLQ/재처리 정책 문서화

### 9.5 통합 테스트
- [ ] EmbeddedKafka 또는 Testcontainers Kafka 테스트 작성
- [ ] card.reviewed → XP 적립 검증
- [ ] level.up/badge.earned 발행 검증

### 9.6 문서 업데이트
- [ ] 토픽/스키마/소비자 가이드 작성
- [ ] HISTORY 완료 기록

**Step 9 Status**: [x] Not Started / [ ] In Progress / [ ] Done

---

## Step 10: 게이미피케이션 E2E 테스트 + 버그 수정

### 10.1 E2E 시나리오 정의
- [ ] 게이미피케이션 플로우 시나리오 작성 (복습→XP→배지→레벨업→리더보드)
- [ ] 테스트 데이터 준비

### 10.2 E2E 테스트 실행
- [ ] 게이미피케이션 전체 플로우 E2E 테스트 실행
- [ ] 실패 항목 기록

### 10.3 버그 트리아지
- [ ] P0/P1/P2 분류
- [ ] P0 즉시 수정 대상 확정

### 10.4 버그 수정
- [ ] P0 버그 수정
- [ ] 게이미피케이션 관련 버그 수정
- [ ] 수정 코드 리뷰 + 테스트

### 10.5 회귀 테스트
- [ ] 수정 후 전체 테스트 재실행
- [ ] 커버리지 80% 이상 확인

### 10.6 문서 업데이트
- [ ] API 문서 최신화
- [ ] HISTORY 완료 기록

**Step 10 Status**: [x] Not Started / [ ] In Progress / [ ] Done

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
