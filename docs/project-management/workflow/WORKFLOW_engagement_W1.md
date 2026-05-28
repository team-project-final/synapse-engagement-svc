# WORKFLOW: engagement — Week 1

> **Task 문서**: [TASK_engagement.md](../task/TASK_engagement.md)
> **기간**: 2026-05-12 ~ 2026-05-16
> **PRD**: [PRD_W1.md](../prd/PRD_W1.md)
> **Source**: Workflow Dashboard, updated at 2026-05-27T08:33:09.800Z

---

## Step 1: engagement-svc 골격 생성

### 1.1 TASK 시작
- [x] Step Goal / Done When / Scope / Input 확인
- [x] PRD_W1 해당 요구사항 확인 (프로젝트 골격)
- [x] Duration 산정 확인 (0.5일)

### 1.2 요구사항 분석
- [x] Spring Boot 4 + Modulith 프로젝트 구조 분석
- [x] community/gamification 2개 모듈 역할 정의
- [x] platform-svc와 동일 빌드 구조 확인
- [x] Instructions 초안 → TASK 문서 반영

### 1.3 Security 1차 검토
- [x] 인증 필요 여부: No (골격만 생성)
- [x] 권한 종류: 없음
- [x] 공개 API 여부: No (Health endpoint만)
- [x] 결과 → TASK Constraints 반영

### 1.4 ERD 설계
- [x] 골격 단계 — ERD 해당 없음
- [x] 모듈별 패키지 구조도 작성
- [x] Duration(final) 갱신

### 1.5 Security 2차 검토
- [x] 민감 정보 암호화: 비해당 (골격 단계)
- [x] Soft Delete 정책: 비해당
- [x] 행 단위 접근 제어: 불필요
- [x] 결과 → TASK Constraints 반영

### 1.6 DTO / Entity 설계 (API First)
- [x] 골격 단계 — 빈 Controller/Service 클래스만 생성
- [x] 각 모듈 package-info.java 작성
- [x] Output Format → TASK 반영

### 1.7 Repository 구현
- [x] 골격 단계 — Repository 해당 없음
- [x] ApplicationModulesTest 구조 검증 테스트 작성

### 1.8 Service + Test
- [x] 빈 Service 클래스 생성 (2개 모듈)
- [x] ApplicationModulesTest 통과 확인
- [x] `./gradlew build` 성공 확인

### 1.9 Controller + Test
- [x] 빈 Controller 클래스 생성 (2개 모듈)
- [x] Dockerfile 작성 (multi-stage build)
- [x] Docker 이미지 빌드 성공 확인

### 1.10 View + Test (해당 시)
- [x] Flutter 화면 연동: 해당 없음
- [x] docker compose에서 engagement-svc 실행 확인 (compose 파일 없음, Docker 이미지 빌드로 대체 확인)
- [x] RULE Reference → TASK 반영

**Step 1 Status**: [ ] Not Started / [ ] In Progress / [x] Done

---

## Step 2: community 그룹 CRUD

### 2.1 TASK 시작
- [x] Step Goal / Done When / Scope / Input 확인
- [x] PRD_W1 해당 요구사항 확인 (FR-EG-001~003 그룹 기능)
- [x] Duration 산정 확인 (1.5일)

### 2.2 요구사항 분석
- [x] 그룹 CRUD API 엔드포인트 5개 정의
- [x] 소유자 권한 검증 로직 설계
- [x] 페이징 처리 요건 (기본 20건)
- [x] Instructions 초안 → TASK 문서 반영

### 2.3 Security 1차 검토
- [x] 인증 필요 여부: Yes (현재 JWT subject 기반 사용자 식별)
- [x] 권한 종류: 로그인 사용자 (수정/삭제는 소유자만)
- [x] 공개 API 여부: No
- [x] 결과 → TASK Constraints 반영

### 2.4 ERD 설계
- [x] groups 테이블 설계 (id, name, description, is_public, owner_id, created_at, updated_at, deleted_at)
- [x] 인덱스 설계 (owner_id, is_public)
- [x] 관계 정의 (groups.owner_id → users.id 논리 참조, platform auth 연동 전 FK 미설정)
- [x] Duration(final) 갱신

### 2.5 Security 2차 검토
- [x] 민감 정보 암호화: 비해당
- [x] Soft Delete 정책: 논리삭제 (deleted_at)
- [x] 행 단위 접근 제어: 필요 (수정/삭제 시 owner_id 확인)
- [x] 결과 → TASK Constraints 반영

### 2.6 DTO / Entity 설계 (API First)
- [x] GroupCreateRequest 정의 (name, description, isPublic)
- [x] GroupUpdateRequest 정의 (name, description, isPublic)
- [x] GroupResponse 정의 (id, name, description, isPublic, ownerId, createdAt)
- [x] Group Entity 작성
- [x] MapStruct 매퍼 작성
- [x] Output Format → TASK 반영

### 2.7 Repository 구현
- [x] GroupRepository 인터페이스 작성
- [x] owner_id 기준 count 쿼리와 커서 목록 조회 쿼리

### 2.8 Service + Test
- [x] GroupService 구현 (create, findAll, findById, update, delete)
- [x] 소유자 권한 검증 로직 구현
- [x] 한 사용자 최대 10개 그룹 제한 로직 구현
- [x] 단위 테스트 작성 (Mockito)
- [x] 테스트 통과 확인

### 2.9 Controller + Test
- [x] GroupController REST API 구현 (5개 엔드포인트)
- [x] 슬라이스 테스트 (@WebMvcTest)
- [x] 401/403 응답 테스트 (미인증, 비소유자)
- [x] 통합 테스트 (@SpringBootTest + TestContainers PostgreSQL + Flyway)
- [x] 테스트 통과 확인

### 2.10 View + Test (해당 시)
- [x] Flutter 화면 연동: 해당 없음 (프론트 별도)
- [x] Swagger API 문서 확인 (springdoc-openapi)
- [x] RULE Reference → TASK 반영

**Step 2 Status**: [ ] Not Started / [ ] In Progress / [x] Done

---

## Step 3: community 멤버 관리

### 3.1 TASK 시작
- [x] Step Goal / Done When / Scope / Input 확인
- [x] PRD_W1 해당 요구사항 확인 (FR-EG-004~005 멤버 관리)
- [x] Duration 산정 확인 (2일)

### 3.2 요구사항 분석
- [x] 멤버 초대/가입/승인/탈퇴/강퇴 플로우 분석
- [x] 역할 기반 권한 (OWNER, ADMIN, MEMBER) 설계
- [x] 멤버 상태 (PENDING, ACTIVE, KICKED) 전이 다이어그램
- [x] Instructions 초안 → TASK 문서 반영

### 3.3 Security 1차 검토
- [x] 인증 필요 여부: Yes (현재 JWT subject 기반 사용자 식별)
- [x] 권한 종류: 역할 기반 (OWNER > ADMIN > MEMBER)
- [x] 공개 API 여부: No
- [x] 결과 → TASK Constraints 반영

### 3.4 ERD 설계
- [x] group_members 테이블 설계 (id, group_id, user_id, role, status, joined_at)
- [x] 인덱스 설계 (group_id+user_id UNIQUE, status)
- [x] 관계 정의 (group_members → groups FK, group_members.user_id → users.id 논리 참조)
- [x] Duration(final) 갱신

### 3.5 Security 2차 검토
- [x] 민감 정보 암호화: 비해당
- [x] Soft Delete 정책: 상태 변경 (KICKED) — 물리삭제 아님
- [x] 행 단위 접근 제어: 필요 (역할별 작업 권한 분리)
- [x] 강퇴 멤버 7일간 재가입 불가 정책
- [x] 결과 → TASK Constraints 반영

### 3.6 DTO / Entity 설계 (API First)
- [x] MemberInviteRequest 정의 (userId)
- [x] approve API는 `{memberId}` path variable로 계약 확정 (`MemberApproveRequest` DTO 미사용으로 제거)
- [x] MemberResponse 정의 (id, userId, role, status, joinedAt)
- [x] GroupMember Entity 작성
- [x] MemberRole Enum 작성 (OWNER, ADMIN, MEMBER)
- [x] MemberStatus Enum 작성 (PENDING, ACTIVE, KICKED)
- [x] MapStruct 매퍼 작성
- [x] Output Format → TASK 반영

### 3.7 Repository 구현
- [x] GroupMemberRepository 인터페이스 작성
- [x] findByGroupIdAndUserId, findByGroupIdAndStatus 커스텀 쿼리

### 3.8 Service + Test
- [x] MemberService 구현 (invite, join, approve, leave, kick)
- [x] 역할 기반 권한 검증 로직 구현
- [x] 공개/비공개 그룹 가입 정책 분기
- [x] 강퇴 멤버 7일 재가입 차단 로직
- [x] 단위 테스트 작성 (Mockito — 역할별 시나리오)
- [x] 테스트 통과 확인

### 3.9 Controller + Test
- [x] MemberController REST API 구현 (invite, join, approve, leave/kick, list)
- [x] 슬라이스 테스트 (@WebMvcTest)
- [x] 401/403 응답 테스트 (미인증, 권한 부족)
- [x] 통합 테스트 (역할별 시나리오)
- [x] 테스트 통과 확인

### 3.10 View + Test (해당 시)
- [x] Flutter 화면 연동: 해당 없음 (프론트 별도)
- [x] Swagger API 문서 확인
- [x] RULE Reference → TASK 반영

**Step 3 Status**: [ ] Not Started / [ ] In Progress / [x] Done
