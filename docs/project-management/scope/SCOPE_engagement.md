# 작업 스코프: @engagement-owner

## 담당자 정보

| 항목 | 내용 |
|------|------|
| Handle | @engagement-owner |
| 역할 | 트랙 B (1명) |
| 담당 서비스 | synapse-engagement-svc |
| 담당 모듈 | community, gamification |
| GitHub Repository | [synapse-engagement-svc](https://github.com/team-project-final/synapse-engagement-svc) |

## 5주 전체 책임 범위

### 도메인 경계

- **In Scope**:
  - 학습 그룹 CRUD + 멤버 관리 (초대/가입/탈퇴/권한)
  - 덱/노트 공유 (share_token 발행 + 공유 콘텐츠 검색)
  - XP 시스템 (xp_events 기록, 레벨 계산)
  - 배지 시스템 (조건 달성 → 배지 수여)
  - 스트릭 (연속 학습일 추적)
  - 리더보드 (주간/월간/전체)
  - 신고 처리 + Admin 모더레이션 API
  - Kafka 이벤트 발행 (gamification.*, community.*)
  - **Community 세부 엔드포인트**: `POST /community/groups/{id}/invite/{token}/accept` (초대 수락), `POST /community/groups/{id}/invite/{token}/decline` (초대 거절), `GET /community/groups/{id}/join-requests` (가입 신청 목록), `PATCH /community/groups/{id}/join-requests/{uid}` (가입 승인/거절) *(Wiki API 명세서 동기화 — 추가)*
  - **공유 덱 평가**: `POST /community/shared-decks/{id}/rate` (공유 덱 평가), `GET /community/shared-decks/{id}/ratings` (평가/댓글 목록) *(Wiki API 명세서 동기화 — 추가)*
  - **공유 노트 관리**: `POST /community/shared-notes`, `GET /community/shared-notes`, `GET /community/shared-notes/{id}`, `DELETE /community/shared-notes/{id}` *(Wiki API 명세서 동기화 — 추가)*
  - **그룹 리더보드**: `GET /gamification/leaderboard/group/{id}` *(Wiki API 명세서 동기화 — 추가)*
  - **배지 상세**: `GET /gamification/badges/{code}` *(Wiki API 명세서 동기화 — 추가)*
  - **Admin 게이미피케이션 관리**: `/admin/gamification/stats`, `/admin/gamification/badges` CRUD, `/admin/gamification/levels`, `/admin/gamification/xp-config` *(Wiki API 명세서 동기화 — 추가)*
  - **Admin 커뮤니티 관리**: `/admin/study-groups`, `/admin/study-groups/{id}/status`, `/admin/shared-content`, `/admin/shared-content/{id}/status` *(Wiki API 명세서 동기화 — 추가)*
- **Out of Scope**:
  - 알림 발송 (platform notification 담당)
  - 카드/노트 자체 CRUD (knowledge/learning 담당)
  - 결제/플랜 관리 (platform billing 담당)

### 주차별 스코프 매트릭스

| 주차 | 기간 | 핵심 목표 | 산출물 | 의존성 |
|------|------|-----------|--------|--------|
| W1 | 05-12~15 | engagement-svc 골격 + community(그룹CRUD+멤버) | 서비스 골격, 그룹/멤버 API | 인프라 (team-lead) |
| W2 | 05-18~22 | gamification XP + community 공유 | XP API, share_token API | auth (platform W1) |
| W3 | 05-26~29 | 배지/레벨/스트릭/리더보드 + 신고/Admin + 그룹 초대 수락/거절 + 가입 신청 관리 + 공유 덱 평가 + 공유 노트 관리 | gamification 완성, 모더레이션 API, 초대/가입 API, shared-notes API | Kafka (team-lead W2) |
| W4 | 06-01~05 | Admin 게이미피케이션/커뮤니티 관리 API + 그룹 리더보드 + 배지 상세 + 버그 수정 + 통합 테스트 | admin gamification/community API, 그룹 리더보드, 안정화 | 전체 통합 (W3) |
| W5 | 06-08~12 | 게이미피케이션/커뮤니티 공유·신고 E2E + P0 버그 수정 | E2E 결과, P0 수정 PR, 데모 시나리오 | platform 알림, learning-card 이벤트 |

## 협업 인터페이스

| 상대 | 주고받는 것 | 방향 |
|------|------------|------|
| @platform-owner | gamification.level_up / community.shared 이벤트 | 발행 → |
| @learning-card-owner | card.reviewed 이벤트 → XP 적립 | ← 수신 |
| @knowledge-owner-1 | 노트 공유 시 note 정보 조회 (내부 API) | ← 요청 |
| Frontend | 게이미피케이션 UI 데이터 제공 | 제공 → |

## 성공 기준

- [ ] 그룹 CRUD + 멤버 관리 완전 동작
- [ ] 덱/노트 공유 → 복사 플로우 동작
- [ ] 복습 완료 → XP 적립 → 레벨업 플로우
- [ ] 배지 수여 + 리더보드 조회
- [ ] 신고 → 관리자 처리 동작
- [ ] 그룹 초대 수락/거절 API 동작 *(Wiki API 명세서 동기화 — 추가)*
- [ ] 가입 신청 목록 조회 및 승인/거절 API 동작 *(Wiki API 명세서 동기화 — 추가)*
- [ ] 공유 덱 평가 및 평가 목록 조회 API 동작 *(Wiki API 명세서 동기화 — 추가)*
- [ ] 공유 노트 CRUD API 동작 *(Wiki API 명세서 동기화 — 추가)*
- [ ] 그룹별 리더보드 (`GET /gamification/leaderboard/group/{id}`) 동작 *(Wiki API 명세서 동기화 — 추가)*
- [ ] 배지 상세 조회 (`GET /gamification/badges/{code}`) 동작 *(Wiki API 명세서 동기화 — 추가)*
- [ ] Admin 게이미피케이션 관리 API (stats/badges/levels/xp-config) 동작 *(Wiki API 명세서 동기화 — 추가)*
- [ ] Admin 커뮤니티 관리 API (study-groups/shared-content) 동작 *(Wiki API 명세서 동기화 — 추가)*
