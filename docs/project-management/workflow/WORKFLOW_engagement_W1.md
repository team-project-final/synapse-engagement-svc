# WORKFLOW: @engagement-owner — Week 1

> **Task 문서**: [TASK_engagement.md](../task/TASK_engagement.md)  
> **기간**: 2026-05-12 ~ 2026-05-16  
> **기능개발 Workflow**: [README §7](../README.md)

---

## Step 1: engagement-svc 골격 생성

### 1.1 TASK 시작
- [ ] Step Goal / Done When / Scope / Input 확인
- [ ] PRD_W1 해당 요구사항 확인 (프로젝트 골격)
- [ ] Duration 산정 확인 (0.5일)

### 1.2 요구사항 분석
- [ ] Spring Boot 4 + Modulith 프로젝트 구조 분석
- [ ] community/gamification 2개 모듈 역할 정의
- [ ] platform-svc와 동일 빌드 구조 확인
- [ ] Instructions 초안 → TASK 문서 반영

### 1.3 Security 1차 검토
- [ ] 인증 필요 여부: No (골격만 생성)
- [ ] 권한 종류: 없음
- [ ] 공개 API 여부: No (Health endpoint만)
- [ ] 결과 → TASK Constraints 반영

### 1.4 ERD 설계
- [ ] 골격 단계 — ERD 해당 없음
- [ ] 모듈별 패키지 구조도 작성
- [ ] Duration(final) 갱신

### 1.5 Security 2차 검토
- [ ] 민감 정보 암호화: 비해당 (골격 단계)
- [ ] Soft Delete 정책: 비해당
- [ ] 행 단위 접근 제어: 불필요
- [ ] 결과 → TASK Constraints 반영

### 1.6 DTO / Entity 설계 (API First)
- [ ] 골격 단계 — 빈 Controller/Service 클래스만 생성
- [ ] 각 모듈 package-info.java 작성
- [ ] Output Format → TASK 반영

### 1.7 Repository 구현
- [ ] 골격 단계 — Repository 해당 없음
- [ ] ApplicationModulesTest 구조 검증 테스트 작성

### 1.8 Service + Test
- [ ] 빈 Service 클래스 생성 (2개 모듈)
- [ ] ApplicationModulesTest 통과 확인
- [ ] `./gradlew build` 성공 확인

### 1.9 Controller + Test
- [ ] 빈 Controller 클래스 생성 (2개 모듈)
- [ ] Dockerfile 작성 (multi-stage build)
- [ ] Docker 이미지 빌드 성공 확인

### 1.10 View + Test (해당 시)
- [ ] Flutter 화면 연동: 해당 없음
- [ ] docker compose에서 engagement-svc 실행 확인
- [ ] RULE Reference → TASK 반영

**Step 1 Status**: [ ] Not Started / [ ] In Progress / [ ] Done

---

## Step 2: community 그룹 CRUD

### 1.1 TASK 시작
- [ ] Step Goal / Done When / Scope / Input 확인
- [ ] PRD_W1 해당 요구사항 확인 (FR-CM-xxx 그룹 기능)
- [ ] Duration 산정 확인 (1.5일)

### 1.2 요구사항 분석
- [ ] 그룹 CRUD API 엔드포인트 5개 정의
- [ ] 소유자 권한 검증 로직 설계
- [ ] 페이징 처리 요건 (기본 20건)
- [ ] Instructions 초안 → TASK 문서 반영

### 1.3 Security 1차 검토
- [ ] 인증 필요 여부: Yes (JWT 인증 필요)
- [ ] 권한 종류: 로그인 사용자 (수정/삭제는 소유자만)
- [ ] 공개 API 여부: No
- [ ] 결과 → TASK Constraints 반영

### 1.4 ERD 설계
- [ ] groups 테이블 설계 (id, name, description, is_public, owner_id, created_at, updated_at, deleted_at)
- [ ] 인덱스 설계 (owner_id, is_public)
- [ ] 관계 정의 (groups.owner_id → users.id FK)
- [ ] Duration(final) 갱신

### 1.5 Security 2차 검토
- [ ] 민감 정보 암호화: 비해당
- [ ] Soft Delete 정책: 논리삭제 (deleted_at)
- [ ] 행 단위 접근 제어: 필요 (수정/삭제 시 owner_id 확인)
- [ ] 결과 → TASK Constraints 반영

### 1.6 DTO / Entity 설계 (API First)
- [ ] GroupCreateRequest 정의 (name, description, isPublic)
- [ ] GroupUpdateRequest 정의 (name, description, isPublic)
- [ ] GroupResponse 정의 (id, name, description, isPublic, ownerId, createdAt)
- [ ] Group Entity 작성
- [ ] MapStruct 매퍼 작성
- [ ] Output Format → TASK 반영

### 1.7 Repository 구현
- [ ] GroupRepository 인터페이스 작성
- [ ] findByOwnerId, countByOwnerId 커스텀 쿼리

### 1.8 Service + Test
- [ ] GroupService 구현 (create, findAll, findById, update, delete)
- [ ] 소유자 권한 검증 로직 구현
- [ ] 한 사용자 최대 10개 그룹 제한 로직 구현
- [ ] 단위 테스트 작성 (Mockito)
- [ ] 테스트 통과 확인

### 1.9 Controller + Test
- [ ] GroupController REST API 구현 (5개 엔드포인트)
- [ ] 슬라이스 테스트 (@WebMvcTest)
- [ ] 401/403 응답 테스트 (미인증, 비소유자)
- [ ] 통합 테스트 (@SpringBootTest + TestContainers)
- [ ] 테스트 통과 확인

### 1.10 View + Test (해당 시)
- [ ] Flutter 화면 연동: 해당 없음 (프론트 별도)
- [ ] Swagger API 문서 확인
- [ ] RULE Reference → TASK 반영

**Step 2 Status**: [ ] Not Started / [ ] In Progress / [ ] Done

---

## Step 3: community 멤버 관리

### 1.1 TASK 시작
- [ ] Step Goal / Done When / Scope / Input 확인
- [ ] PRD_W1 해당 요구사항 확인 (FR-CM-xxx 멤버 관리)
- [ ] Duration 산정 확인 (2일)

### 1.2 요구사항 분석
- [ ] 멤버 초대/가입/승인/탈퇴/강퇴 플로우 분석
- [ ] 역할 기반 권한 (OWNER, ADMIN, MEMBER) 설계
- [ ] 멤버 상태 (PENDING, ACTIVE, KICKED) 전이 다이어그램
- [ ] Instructions 초안 → TASK 문서 반영

### 1.3 Security 1차 검토
- [ ] 인증 필요 여부: Yes (JWT 인증 필요)
- [ ] 권한 종류: 역할 기반 (OWNER > ADMIN > MEMBER)
- [ ] 공개 API 여부: No
- [ ] 결과 → TASK Constraints 반영

### 1.4 ERD 설계
- [ ] group_members 테이블 설계 (id, group_id, user_id, role, status, joined_at)
- [ ] 인덱스 설계 (group_id+user_id UNIQUE, status)
- [ ] 관계 정의 (group_members → groups FK, group_members → users FK)
- [ ] Duration(final) 갱신

### 1.5 Security 2차 검토
- [ ] 민감 정보 암호화: 비해당
- [ ] Soft Delete 정책: 상태 변경 (KICKED) — 물리삭제 아님
- [ ] 행 단위 접근 제어: 필요 (역할별 작업 권한 분리)
- [ ] 강퇴 멤버 7일간 재가입 불가 정책
- [ ] 결과 → TASK Constraints 반영

### 1.6 DTO / Entity 설계 (API First)
- [ ] MemberInviteRequest 정의 (userId)
- [ ] MemberApproveRequest 정의 (memberId)
- [ ] MemberResponse 정의 (id, userId, role, status, joinedAt)
- [ ] GroupMember Entity 작성
- [ ] MemberRole Enum 작성 (OWNER, ADMIN, MEMBER)
- [ ] MemberStatus Enum 작성 (PENDING, ACTIVE, KICKED)
- [ ] MapStruct 매퍼 작성
- [ ] Output Format → TASK 반영

### 1.7 Repository 구현
- [ ] GroupMemberRepository 인터페이스 작성
- [ ] findByGroupIdAndUserId, findByGroupIdAndStatus 커스텀 쿼리

### 1.8 Service + Test
- [ ] MemberService 구현 (invite, join, approve, leave, kick)
- [ ] 역할 기반 권한 검증 로직 구현
- [ ] 공개/비공개 그룹 가입 정책 분기
- [ ] 강퇴 멤버 7일 재가입 차단 로직
- [ ] 단위 테스트 작성 (Mockito — 역할별 시나리오)
- [ ] 테스트 통과 확인

### 1.9 Controller + Test
- [ ] MemberController REST API 구현 (invite, join, approve, leave/kick, list)
- [ ] 슬라이스 테스트 (@WebMvcTest)
- [ ] 401/403 응답 테스트 (미인증, 권한 부족)
- [ ] 통합 테스트 (역할별 시나리오)
- [ ] 테스트 통과 확인

### 1.10 View + Test (해당 시)
- [ ] Flutter 화면 연동: 해당 없음 (프론트 별도)
- [ ] Swagger API 문서 확인
- [ ] RULE Reference → TASK 반영

**Step 3 Status**: [ ] Not Started / [ ] In Progress / [ ] Done
