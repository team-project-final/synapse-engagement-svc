# WORKFLOW: @engagement-owner — Week 2

> **Task 문서**: [TASK_engagement.md](../task/TASK_engagement.md)
> **기간**: 2026-05-18 ~ 2026-05-22, 5 영업일
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
- [ ] xp_events 테이블 설계 (id, user_id, event_type, xp_amount, source_id, source_type, created_at)
- [ ] user_profiles_gamification 테이블 설계 (user_id UNIQUE, total_xp, level, current_streak, longest_streak, updated_at) — 프로필 수준 XP/레벨/스트릭 데이터 관리 (별도 user_streaks 테이블 없음)
- [ ] 인덱스 설계 (xp_events.user_id, xp_events.created_at, user_profiles_gamification.user_id UNIQUE)
- [ ] 관계 정의 (xp_events.user_id → user_profiles_gamification.user_id)
- [ ] Duration(final) 갱신

### 4.5 Security 2차 검토
- [ ] XP 적립 중복 방지 (idempotency key: event_type + source_id)
- [ ] Soft Delete 정책: 물리삭제 없음 (이벤트 로그 누적 보관)
- [ ] 행 단위 접근 제어: 필요 (userId 기반)
- [ ] 결과 → TASK Constraints 반영

### 4.6 DTO / Entity 설계 (API First)
- [ ] XpEventResponse 정의 (eventType, xpAmount, createdAt)
- [ ] UserXpResponse 정의 — `/gamification/profile` 응답 기준 (level, totalXp, currentStreak, longestStreak, title, nextLevelXp, recentBadges)
- [ ] XpEvent Entity 작성
- [ ] UserProfilesGamification Entity 작성
- [ ] EventType Enum 작성 (CARD_REVIEWED, NOTE_CREATED 등)
- [ ] MapStruct 매퍼 작성
- [ ] Output Format → TASK 반영

### 4.7 Repository 구현
- [ ] XpEventRepository 인터페이스 작성
- [ ] UserProfilesGamificationRepository 인터페이스 작성
- [ ] findByUserId 커스텀 쿼리
- [ ] existsByUserIdAndEventTypeAndSourceId 중복 체크 쿼리 (event_type + source_id 기준)
- [ ] Flyway 마이그레이션 스크립트 작성

### 4.8 Service + Test
- [ ] XpService 구현 (addXp, getUserXp, getXpHistory)
- [ ] XP 적립 서비스 구현 (이벤트 → xp_events 기록 → user_profiles_gamification 갱신)
- [ ] card.reviewed Kafka Consumer 구현 (KafkaListener)
- [ ] 중복 적립 방지 로직 (idempotency)
- [ ] 레벨 계산 로직 (XP → Level 매핑)
- [ ] 단위 테스트 작성 (Mockito)
- [ ] Kafka Consumer 테스트 (@EmbeddedKafka)
- [ ] 테스트 통과 확인

### 4.9 Controller + Test
- [ ] GET /gamification/profile 엔드포인트 구현 (사용자 XP/레벨/스트릭/배지 프로필 조회)
- [ ] GET /gamification/xp/history 엔드포인트 구현 (XP 이력 조회)
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
- [ ] 공유 콘텐츠 복사(copy) 요건 분석
- [ ] Instructions 초안 → TASK 문서 반영

### 5.3 Security 1차 검토
- [ ] 인증 필요 여부: Yes (공유 등록/복사 시 JWT 필요, 검색은 공개 가능)
- [ ] 권한 종류: 로그인 사용자 (본인 콘텐츠만 공유 등록)
- [ ] share_token 만료/무효화 정책 확인
- [ ] 결과 → TASK Constraints 반영

### 5.4 ERD 설계
- [ ] shared_decks 테이블 설계 (id, deck_id, shared_by_user_id, share_type, target_group_id, share_token, allow_copy, download_count, rating_avg, rating_count, status, created_at, updated_at, deleted_at)
- [ ] shared_notes 테이블 설계 (id, note_id, shared_by_user_id, share_type, target_group_id, share_token, allow_copy, download_count, status, created_at, updated_at, deleted_at)
- [ ] 인덱스 설계 (shared_decks.share_token UNIQUE, shared_decks.shared_by_user_id; shared_notes.share_token UNIQUE, shared_notes.shared_by_user_id)
- [ ] 전문 검색 인덱스 (덱/노트 원본 테이블의 title, description, tags 활용)
- [ ] Duration(final) 갱신

### 5.5 Security 2차 검토
- [ ] share_token UUID v4 + URL-safe 인코딩 확인
- [ ] Soft Delete 정책: 논리삭제 (deleted_at)
- [ ] 행 단위 접근 제어: 필요 (shared_by_user_id 기반 수정/삭제)
- [ ] 공유 콘텐츠 부적절 내용 신고 기능 검토
- [ ] 결과 → TASK Constraints 반영

### 5.6 DTO / Entity 설계 (API First)
- [ ] ShareDeckRequest 정의 (deck_id, share_type, target_group_id, allow_copy)
- [ ] SharedDeckResponse 정의 (id, share_token, deck_id, shared_by_user_id, download_count, rating_avg, status, created_at)
- [ ] ShareTokenResponse 정의 (share_token, shareUrl)
- [ ] SharedDeck Entity 작성
- [ ] SharedNote Entity 작성
- [ ] MapStruct 매퍼 작성
- [ ] Output Format → TASK 반영

### 5.7 Repository 구현
- [ ] SharedDeckRepository 인터페이스 작성
- [ ] SharedNoteRepository 인터페이스 작성
- [ ] findByShareToken 커스텀 쿼리
- [ ] 검색 쿼리 (원본 덱/노트 조인, 키워드 필터)
- [ ] Flyway 마이그레이션 스크립트 작성

### 5.8 Service + Test
- [ ] SharedDeckService 구현 (share, findById, search, copy, delete)
- [ ] share_token 발행 로직 (UUID v4 생성)
- [ ] share_token 검증 로직 (존재 여부 + 만료 확인)
- [ ] 공유 덱 검색 서비스 구현 (키워드 + 카테고리 필터, `GET /community/shared-decks?q=...`)
- [ ] 덱 복사(copy) 서비스 구현 — `fork` 대신 `copy` 용어 사용, deck_copies 테이블 활용 (원본 → 사용자 소유 복사); 실제 덱 복사는 learning-card 내부 API `POST /internal/decks/copy` 호출로 위임
- [ ] Bean Validation 적용
- [ ] 단위 테스트 작성 (Mockito)
- [ ] 테스트 통과 확인

### 5.9 Controller + Test
- [ ] POST /community/shared-decks 또는 POST /community/shared-notes 엔드포인트 구현 (공유 등록)
- [ ] GET /community/shared-decks/{id} 또는 GET /community/shared-notes/{id} 엔드포인트 구현 (ID 조회)
- [ ] GET /community/shared-decks?q=... 엔드포인트 구현 (검색)
- [ ] POST /community/shared-decks/{id}/copy 엔드포인트 구현 (복사)
- [ ] DELETE /community/shared-decks/{id} 엔드포인트 구현
- [ ] 슬라이스 테스트 (@WebMvcTest)
- [ ] 401/403 응답 테스트
- [ ] 통합 테스트
- [ ] 테스트 통과 확인

### 5.10 View + Test (해당 시)
- [ ] Flutter 화면 연동: 해당 없음 (프론트 별도)
- [ ] Swagger API 문서 확인
- [ ] RULE Reference → TASK 반영

**Step 5 Status**: [ ] Not Started / [ ] In Progress / [ ] Done
