# WORKFLOW: @engagement-owner — Week 2

> **Task 문서**: [TASK_engagement.md](../task/TASK_engagement.md)  
> **기간**: 2026-05-19 ~ 2026-05-23  
> **PRD**: [PRD_W2.md](../prd/PRD_W2.md)

---

## Step 4: gamification XP 기초 — xp_events 기록 + XP 조회

### 4.1 TASK 시작
- [ ] Step Goal / Done When / Scope / Input 확인
- [ ] PRD_W2 해당 요구사항 확인 (gamification XP)
- [ ] Duration 산정 확인

### 4.2 요구사항 분석
- [ ] XP 이벤트 종류 정의 (card.reviewed, note.created 등)
- [ ] 이벤트별 XP 부여량 정의
- [ ] 사용자 총 XP 조회 요건 분석
- [ ] card.reviewed Kafka 컨슈머 요건 분석
- [ ] Instructions 초안 → TASK 문서 반영

### 4.3 Security 1차 검토
- [ ] 인증 필요 여부: Yes (JWT 인증 필요)
- [ ] 권한 종류: 로그인 사용자 (본인 XP만 조회)
- [ ] 공개 API 여부: No
- [ ] Kafka 메시지 위변조 방지 확인
- [ ] 결과 → TASK Constraints 반영

### 4.4 ERD 설계
- [ ] xp_events 테이블 설계 (id, userId, eventType, xpAmount, sourceId, createdAt)
- [ ] user_xp 테이블 설계 (id, userId UNIQUE, totalXp, level, updatedAt)
- [ ] 인덱스 설계 (xp_events.userId, xp_events.createdAt, user_xp.userId UNIQUE)
- [ ] 관계 정의 (xp_events.userId → user_xp.userId)
- [ ] Duration(final) 갱신

### 4.5 Security 2차 검토
- [ ] XP 적립 중복 방지 (idempotency key: eventType + sourceId)
- [ ] Soft Delete 정책: 물리삭제 없음 (이벤트 로그 누적 보관)
- [ ] 행 단위 접근 제어: 필요 (userId 기반)
- [ ] 결과 → TASK Constraints 반영

### 4.6 DTO / Entity 설계 (API First)
- [ ] XpEventResponse 정의 (eventType, xpAmount, createdAt)
- [ ] UserXpResponse 정의 (totalXp, level)
- [ ] XpEvent Entity 작성
- [ ] UserXp Entity 작성
- [ ] EventType Enum 작성 (CARD_REVIEWED, NOTE_CREATED 등)
- [ ] MapStruct 매퍼 작성
- [ ] Output Format → TASK 반영

### 4.7 Repository 구현
- [ ] XpEventRepository 인터페이스 작성
- [ ] UserXpRepository 인터페이스 작성
- [ ] findByUserId 커스텀 쿼리
- [ ] existsByUserIdAndEventTypeAndSourceId 중복 체크 쿼리
- [ ] Flyway 마이그레이션 스크립트 작성

### 4.8 Service + Test
- [ ] XpService 구현 (addXp, getUserXp, getXpHistory)
- [ ] XP 적립 서비스 구현 (이벤트 → xp_events 기록 → user_xp 갱신)
- [ ] card.reviewed Kafka Consumer 구현 (KafkaListener)
- [ ] 중복 적립 방지 로직 (idempotency)
- [ ] 레벨 계산 로직 (XP → Level 매핑)
- [ ] 단위 테스트 작성 (Mockito)
- [ ] Kafka Consumer 테스트 (@EmbeddedKafka)
- [ ] 테스트 통과 확인

### 4.9 Controller + Test
- [ ] GET /api/v1/gamification/xp 엔드포인트 구현 (사용자 총 XP/레벨 조회)
- [ ] GET /api/v1/gamification/xp/history 엔드포인트 구현 (XP 이력 조회)
- [ ] 슬라이스 테스트 (@WebMvcTest)
- [ ] 401/403 응답 테스트
- [ ] 통합 테스트
- [ ] 테스트 통과 확인

### 4.10 View + Test (해당 시)
- [ ] Flutter 화면 연동: 해당 없음 (프론트 별도)
- [ ] Swagger API 문서 확인
- [ ] RULE Reference → TASK 반영

**Step 4 Status**: [ ] Not Started / [ ] In Progress / [ ] Done

---

## Step 5: community 공유 — share_token + 공유 콘텐츠 검색/복사

### 5.1 TASK 시작
- [ ] Step Goal / Done When / Scope / Input 확인
- [ ] PRD_W2 해당 요구사항 확인 (커뮤니티 공유)
- [ ] Duration 산정 확인

### 5.2 요구사항 분석
- [ ] share_token 발행/검증 플로우 정의
- [ ] 공유 콘텐츠 유형 정의 (Deck, Note 등)
- [ ] 공유 콘텐츠 검색 요건 (키워드, 카테고리)
- [ ] 공유 콘텐츠 복사(fork) 요건 분석
- [ ] Instructions 초안 → TASK 문서 반영

### 5.3 Security 1차 검토
- [ ] 인증 필요 여부: Yes (공유 등록/복사 시 JWT 필요, 검색은 공개 가능)
- [ ] 권한 종류: 로그인 사용자 (본인 콘텐츠만 공유 등록)
- [ ] share_token 만료/무효화 정책 확인
- [ ] 결과 → TASK Constraints 반영

### 5.4 ERD 설계
- [ ] shared_contents 테이블 설계 (id, ownerId, contentType, contentId, shareToken, title, description, tags, downloadCount, createdAt, updatedAt, deletedAt)
- [ ] 인덱스 설계 (shared_contents.shareToken UNIQUE, shared_contents.ownerId, shared_contents.contentType)
- [ ] 전문 검색 인덱스 (title, description, tags)
- [ ] Duration(final) 갱신

### 5.5 Security 2차 검토
- [ ] share_token UUID v4 + URL-safe 인코딩 확인
- [ ] Soft Delete 정책: 논리삭제 (deletedAt)
- [ ] 행 단위 접근 제어: 필요 (ownerId 기반 수정/삭제)
- [ ] 공유 콘텐츠 부적절 내용 신고 기능 검토
- [ ] 결과 → TASK Constraints 반영

### 5.6 DTO / Entity 설계 (API First)
- [ ] ShareContentRequest 정의 (contentType, contentId, title, description, tags)
- [ ] SharedContentResponse 정의 (id, shareToken, title, description, tags, downloadCount, createdAt)
- [ ] ShareTokenResponse 정의 (shareToken, shareUrl)
- [ ] SharedContent Entity 작성
- [ ] ContentType Enum 작성 (DECK, NOTE)
- [ ] MapStruct 매퍼 작성
- [ ] Output Format → TASK 반영

### 5.7 Repository 구현
- [ ] SharedContentRepository 인터페이스 작성
- [ ] findByShareToken 커스텀 쿼리
- [ ] findByContentTypeAndTitleContaining 검색 쿼리
- [ ] Flyway 마이그레이션 스크립트 작성

### 5.8 Service + Test
- [ ] SharedContentService 구현 (share, findByToken, search, fork, delete)
- [ ] share_token 발행 로직 (UUID v4 생성)
- [ ] share_token 검증 로직 (존재 여부 + 만료 확인)
- [ ] 공유 콘텐츠 검색 서비스 구현 (키워드 + 카테고리 필터)
- [ ] 콘텐츠 복사(fork) 서비스 구현 (원본 → 사용자 소유 복사)
- [ ] Bean Validation 적용
- [ ] 단위 테스트 작성 (Mockito)
- [ ] 테스트 통과 확인

### 5.9 Controller + Test
- [ ] POST /api/v1/community/share 엔드포인트 구현 (공유 등록)
- [ ] GET /api/v1/community/share/{token} 엔드포인트 구현 (토큰 조회)
- [ ] GET /api/v1/community/search 엔드포인트 구현 (검색)
- [ ] POST /api/v1/community/share/{token}/fork 엔드포인트 구현 (복사)
- [ ] DELETE /api/v1/community/share/{id} 엔드포인트 구현
- [ ] 슬라이스 테스트 (@WebMvcTest)
- [ ] 401/403 응답 테스트
- [ ] 통합 테스트
- [ ] 테스트 통과 확인

### 5.10 View + Test (해당 시)
- [ ] Flutter 화면 연동: 해당 없음 (프론트 별도)
- [ ] Swagger API 문서 확인
- [ ] RULE Reference → TASK 반영

**Step 5 Status**: [ ] Not Started / [ ] In Progress / [ ] Done
