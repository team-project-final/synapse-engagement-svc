# WORKFLOW: engagement — Week 2

> **Task 문서**: [TASK_engagement.md](../task/TASK_engagement.md)
> **기간**: 2026-05-19 ~ 2026-05-23
> **PRD**: [PRD_W2.md](../prd/PRD_W2.md)
> **Source**: Workflow Dashboard, updated at 2026-05-27T08:33:09.800Z

---

## Step 4: gamification XP 기초 — xp_events 기록 + XP 조회

### 4.1 TASK 시작
- [x] Step Goal / Done When / Scope / Input 확인
- [x] PRD_W2 해당 요구사항 확인 (gamification XP)
- [x] Duration 산정 확인

### 4.2 요구사항 분석
- [x] XP 이벤트 종류 정의 (CARD_REVIEWED, NOTE_CREATED)
- [x] 학습 활동별 XP 부여량 정의 (기본 활동 = 10 XP)
- [x] 사용자 총 XP 조회 요건 분석
- [x] 내부 XP 적립 유스케이스 요건 분석
- [x] Instructions 초안 → TASK 문서 반영

### 4.3 Security 1차 검토
- [x] 인증 필요 여부: Yes (JWT subject 기반 사용자 식별)
- [x] 권한 종류: 로그인 사용자 (본인 XP만 조회)
- [x] 공개 API 여부: No
- [x] XP 적립 요청 위변조 방지 확인 (서버 사이드 적립 유스케이스 + 멱등성 저장)
- [x] W2 Step 4에서는 외부 이벤트 Consumer/Producer를 구현하지 않고 Step 9로 분리
- [x] 결과 → TASK Constraints 반영

### 4.4 ERD 설계
- [x] xp_events 테이블 설계 (id, user_id, event_type, xp_amount, source_id, source_type, event_id, created_at)
- [x] user_profiles_gamification 테이블 설계 (user_id UNIQUE, total_xp, level, current_streak, longest_streak, updated_at) — 프로필 수준 XP/레벨/스트릭 데이터 관리 (별도 user_streaks 테이블 없음)
- [x] 인덱스 설계 (xp_events.user_id, xp_events.created_at, user_profiles_gamification.user_id UNIQUE)
- [x] 관계 정의 (xp_events.user_id → user_profiles_gamification.user_id)
- [x] Duration(final) 갱신

### 4.5 Security 2차 검토
- [x] XP 적립 중복 방지 (idempotency key: event_id + user_id/event_type/source_id)
- [x] Soft Delete 정책: 물리삭제 없음 (이벤트 로그 누적 보관)
- [x] 행 단위 접근 제어: 필요 (userId 기반)
- [x] 결과 → TASK Constraints 반영

### 4.6 DTO / Entity 설계 (API First)
- [x] XpEventResponse 정의 (eventType, xpAmount, createdAt)
- [x] UserXpResponse 정의 — `/gamification/profile` 응답 기준 (level, totalXp, currentStreak, longestStreak, title, nextLevelXp, recentBadges)
- [x] XpEvent Entity 작성
- [x] UserProfilesGamification Entity 작성
- [x] EventType Enum 작성 (CARD_REVIEWED, NOTE_CREATED 등)
- [x] MapStruct 매퍼 작성
- [x] Output Format → TASK 반영

### 4.7 Repository 구현
- [x] XpEventRepository 인터페이스 작성
- [x] UserProfilesGamificationRepository 인터페이스 작성
- [x] findByUserId 커스텀 쿼리
- [x] existsByUserIdAndEventTypeAndSourceId 중복 체크 쿼리 (user_id + event_type + source_id 기준)
- [x] Flyway 마이그레이션 스크립트 작성

### 4.8 Service + Test
- [x] GamificationService 구현 (addXp, getProfile, getXpHistory)
- [x] XP 적립 서비스 구현 (내부 요청 → xp_events 기록 → user_profiles_gamification 갱신)
- [x] 외부 이벤트 연동은 Step 9로 이연하고 W2는 임시 인증 헤더 + REST + DB 기반으로 제한
- [x] 중복 적립 방지 로직 (idempotency)
- [x] 레벨 계산 로직 (XP → Level 매핑)
- [x] 단위 테스트 작성 (Mockito)
- [x] Service/Controller 통합 테스트 작성 (Testcontainers)
- [x] 테스트 통과 확인

### 4.9 Controller + Test
- [x] GET /gamification/profile 엔드포인트 구현 (JWT subject 기준 사용자 XP/레벨/스트릭/배지 프로필 조회)
- [x] GET /gamification/xp/history 엔드포인트 구현 (JWT subject 기준 사용자 XP 이력 조회)
- [x] 슬라이스 테스트 (@WebMvcTest)
- [x] 401/403 응답 테스트
- [x] 통합 테스트
- [x] 테스트 통과 확인

### 4.10 View + Test (해당 시)
- [x] Flutter 화면 연동: 해당 없음 (프론트 별도)
- [x] Swagger API 문서 확인
- [x] RULE Reference → TASK 반영

**Step 4 Status**: [ ] Not Started / [ ] In Progress / [x] Done

---

## Step 5: community 공유 — share_token + 공유 콘텐츠 검색/복사

### 5.1 TASK 시작
- [x] Step Goal / Done When / Scope / Input 확인
- [x] PRD_W2 해당 요구사항 확인 (커뮤니티 공유)
- [x] Duration 산정 확인

### 5.2 요구사항 분석
- [x] share_token 발행/검증 플로우 정의
- [x] 공유 콘텐츠 유형 정의 (Deck, Note 등)
- [x] 공유 콘텐츠 검색 요건 (키워드, 카테고리)
- [x] 공유 콘텐츠 복사(fork) 요건 분석
- [x] Instructions 초안 → TASK 문서 반영

### 5.3 Security 1차 검토
- [x] 인증 필요 여부: Yes (공유 등록/복사/삭제는 JWT subject 필요, 검색은 공개 가능)
- [x] 권한 종류: 로그인 사용자 (본인 콘텐츠만 공유 등록)
- [x] share_token 만료/무효화 정책 확인 (W2는 만료 없음, soft delete로 무효화)
- [x] W2 Step 5에서는 외부 이벤트 발행/소비 및 알림 연동 제외
- [x] 결과 → TASK Constraints 반영

### 5.4 ERD 설계
- [x] shared_contents 테이블 설계 (id, owner_id, content_type, content_id, share_token, title, description, tags, download_count, created_at, updated_at, deleted_at)
- [x] 인덱스 설계 (shared_contents.share_token UNIQUE, owner_id, content_type, created_at)
- [x] 검색 인덱스 (title, description, tags)
- [x] Duration(final) 갱신

### 5.5 Security 2차 검토
- [x] share_token UUID v4 + URL-safe 인코딩 확인
- [x] Soft Delete 정책: 논리삭제 (deleted_at)
- [x] 행 단위 접근 제어: 필요 (owner_id 기반 삭제)
- [x] 공유 콘텐츠 부적절 내용 신고 기능 검토 (Step 8로 이연)
- [x] 결과 → TASK Constraints 반영

### 5.6 DTO / Entity 설계 (API First)
- [x] ShareContentRequest 정의 (contentType, contentId, title, description, tags)
- [x] SharedContentResponse 정의 (id, shareToken, contentType, contentId, ownerId, title, description, tags, downloadCount, createdAt)
- [x] ShareTokenResponse 정의 (shareToken, shareUrl)
- [x] SharedContent Entity 작성
- [x] ContentType Enum 작성 (DECK, NOTE)
- [x] Mapper 작성
- [x] Output Format → TASK 반영

### 5.7 Repository 구현
- [x] SharedContentRepository 인터페이스 작성
- [x] findByShareToken 커스텀 쿼리
- [x] 검색 쿼리 (키워드 + contentType 필터)
- [x] Flyway 마이그레이션 스크립트 작성

### 5.8 Service + Test
- [x] SharedContentService 구현 (share, findByToken, search, fork, delete)
- [x] share_token 발행 로직 (UUID v4 URL-safe 인코딩)
- [x] share_token 검증 로직 (존재 여부 + soft delete 확인)
- [x] 공유 콘텐츠 검색 서비스 구현 (키워드 + contentType 필터, `GET /api/v1/community/search?q=...`)
- [x] 콘텐츠 복사(fork) 서비스 구현 (공유 메타데이터를 현재 사용자 소유 복사본으로 생성)
- [x] 외부 이벤트 Producer/Consumer 없이 REST/DB 기반으로만 구현
- [x] Bean Validation 적용
- [x] 단위 테스트 작성 (Mockito)
- [x] 테스트 통과 확인

### 5.9 Controller + Test
- [x] POST /api/v1/community/share 엔드포인트 구현 (공유 등록)
- [x] GET /api/v1/community/share/{token} 엔드포인트 구현 (토큰 조회)
- [x] GET /api/v1/community/search 엔드포인트 구현 (검색)
- [x] POST /api/v1/community/share/{token}/fork 엔드포인트 구현 (복사)
- [x] DELETE /api/v1/community/share/{id} 엔드포인트 구현
- [x] 슬라이스 테스트 (@WebMvcTest)
- [x] 401/403 응답 테스트
- [x] 통합 테스트
- [x] 테스트 통과 확인

### 5.10 View + Test (해당 시)
- [x] Flutter 화면 연동: 해당 없음 (프론트 별도)
- [x] Swagger API 문서 확인
- [x] RULE Reference → TASK 반영

**Step 5 Status**: [ ] Not Started / [ ] In Progress / [x] Done
