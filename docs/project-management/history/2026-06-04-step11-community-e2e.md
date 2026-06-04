# 2026-06-04 Step 11 커뮤니티 공유/신고 E2E 테스트

## 범위
- 커뮤니티 공유 생성, token 조회, 검색, fork 플로우를 E2E로 검증했다.
- 신고 생성, 중복 신고 차단, 관리자 권한, 관리자 처리 후 신고 대상 숨김 플로우를 E2E로 검증했다.
- Step 11 완료 상태를 W4 workflow, task, history 문서에 반영했다.

## 구현
- `CommunityStep11E2ETests` 추가
- 테스트 사용자
  - 공유 원본 소유자: `11100`
  - fork 소유자: `11101`
  - 신고자: `11102`
  - 관리자: `11900`, role `ADMIN`
- 검증 흐름
  - `POST /api/v1/community/share` → `201 Created`, shareToken 반환
  - `GET /api/v1/community/share/{token}` → ownerId/title/tags 확인
  - `GET /api/v1/community/search?q=Step11&contentType=DECK` → 검색 결과 포함 확인
  - `POST /api/v1/community/share/{token}/fork` → fork 생성, sourceShareId 연결 확인
  - 원본 공유 재조회 → downloadCount `1` 확인
  - `POST /api/v1/community/reports` → `201 Created`, reporterId 미노출 확인
  - 동일 신고 재요청 → `409 Conflict`
  - 일반 사용자 admin 신고 목록 조회 → `403 Forbidden`
  - ADMIN 신고 목록 조회 및 승인 → `APPROVED`, adminNote 저장 확인
  - 승인된 신고 대상 공유 조회 → `404 Not Found`
  - 원본 공유 조회 → `200 OK`

## 트리아지
- 신규 P0/P1/P2 버그 없음
- 테스트 컨텍스트의 `ObjectMapper` 빈 주입 실패는 테스트 내부 인스턴스 생성으로 정리했다.

## 검증
- `.\gradlew.bat test --tests "com.synapse.engagement.community.CommunityStep11E2ETests"` → BUILD SUCCESSFUL
- `.\gradlew.bat test` → BUILD SUCCESSFUL

## 비고
- 현재 Gradle 검증 흐름에는 정량 커버리지 리포트 태스크가 연결되어 있지 않다.
- Step 11 범위는 신규 E2E 테스트와 전체 회귀 테스트 통과로 완료 판정했다.
