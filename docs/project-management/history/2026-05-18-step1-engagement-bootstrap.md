# 2026-05-18 — Step 1 engagement-svc 골격 생성 작업 기록

## 제목

Step 1: Spring Boot 4 + Modulith 기반 engagement-svc 골격 생성

## 주제

engagement-svc의 W1 Step 1 작업으로, community/gamification 모듈 골격을 실제 코드로 구성하고 빌드 가능한 상태를 검증한다.

## 목적

- `PlaceholderComponent` 수준의 임시 골격을 실제 모듈별 Controller/Service 구조로 전환한다.
- Spring Modulith 모듈 경계 검증이 통과하는지 확인한다.
- Docker 이미지 생성을 위한 기본 Dockerfile과 빌드 컨텍스트 제외 규칙을 준비한다.
- `./gradlew build` 기준으로 서비스 골격이 컴파일 및 테스트 가능한지 확인한다.

## 작업 내용

- community 모듈에 빈 REST Controller와 Service 골격을 추가했다.
  - `CommunityController`
  - `CommunityService`
- gamification 모듈에 빈 REST Controller와 Service 골격을 추가했다.
  - `GamificationController`
  - `GamificationService`
- shared 모듈에는 컴포넌트 대신 모듈 마커 클래스를 추가했다.
  - `SharedModuleMarker`
- 기존 임시 컴포넌트를 제거했다.
  - `community/PlaceholderComponent`
  - `gamification/PlaceholderComponent`
  - `shared/PlaceholderComponent`
- 멀티스테이지 Dockerfile을 추가했다.
- Docker 빌드 컨텍스트 정리를 위해 `.dockerignore`를 추가했다.
- TASK/WORKFLOW 문서에는 실제 완료된 Step 1 체크 항목만 반영했다.

## 검증 결과

- `.\gradlew.bat test` 성공
- `.\gradlew.bat build` 성공
- `docker build -t synapse-engagement-svc:step1 .` 성공
- `docker images synapse-engagement-svc`로 `synapse-engagement-svc:step1` 이미지 생성 확인

## 남은 작업

- Step 2 community 그룹 CRUD 착수
- compose 파일이 필요해지는 시점에 docker compose 구성 추가

## 관련 문서

- `docs/project-management/task/TASK_engagement.md`
- `docs/project-management/workflow/WORKFLOW_engagement_W1.md`
- `docs/project-management/prd/PRD_W1.md`
