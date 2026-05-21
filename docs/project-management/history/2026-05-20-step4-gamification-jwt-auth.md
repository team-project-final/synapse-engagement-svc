# 2026-05-20 11:18 KST — Step 4 gamification 인증 방식 재검토

## 작업 시간

- 작업일: 2026-05-20
- 기록 시각: 11:18 KST
- 대상 범위: W2 Step 4 gamification XP 조회 API 인증 방식, W2 Step 4/5 문서 정리

## 작업 내용

- workflow guide의 JWT 항목은 인증 필요 여부 확인으로 해석하고, W2에서는 직접 JWT 파싱/검증 구현을 보류하기로 정리했다.
- platform-svc JWT 서명 검증/JWK 연동 전까지 gamification 조회 API는 기존 `X-User-Id` 임시 헤더를 유지한다.
- Step 5의 `share_token`은 공유 링크 식별자이며 로그인 인증 JWT와 별개임을 확인했다.
- Step 4/Step 5 문서를 임시 인증 헤더 + REST + DB 범위로 정리하고, 외부 이벤트 Producer/Consumer 및 알림 연동은 Step 9로 이연했다.
- Step 5까지는 외부 이벤트 연동 없이 REST/DB 기반으로 진행하도록 TASK/WORKFLOW에 명시했다.
- 이전에 추가한 Step 4 코드 주석을 유지해 XP 적립, 멱등성, 프로필/이벤트 테이블 역할을 초보자도 따라갈 수 있게 했다.

## 변경 파일

- `src/main/java/io/synapse/gamification/api/GamificationController.java`
- `src/main/java/io/synapse/gamification/api/GamificationExceptionHandler.java`
- `src/main/java/io/synapse/gamification/package-info.java`
- `src/test/java/io/synapse/gamification/api/GamificationControllerWebMvcTest.java`
- `src/test/java/io/synapse/gamification/api/GamificationControllerIntegrationTest.java`
- `docs/project-management/task/TASK_engagement.md`
- `docs/project-management/workflow/WORKFLOW_engagement_W2.md`
- `docs/project-management/history/HISTORY_engagement.md`

## 검증

- `.\gradlew.bat compileJava` 성공
- `.\gradlew.bat test` 성공
- `.\gradlew.bat clean test` 성공

## 남은 이슈

- platform-svc의 실제 JWT 서명 검증/JWK 연동이 확정되면 Spring Security Resource Server 방식으로 교체해야 한다.
- community Step 2/3과 gamification Step 4 API는 현재 동일하게 `X-User-Id` 임시 헤더를 사용한다.
