# 그룹 전용 공유 콘텐츠 (FE-05) Design

> 작성일: 2026-08-01 · 대상 레포: `synapse-engagement-svc`, `synapse-frontend`

## 배경

프론트엔드 그룹 상세 화면(`group_detail_screen.dart`)은 그룹 정보와 멤버 목록만 표시한다.
"그룹에 콘텐츠를 공유한다"는 개념이 백엔드에 없기 때문이다. `shared_contents` 테이블에는
그룹 참조 컬럼이 없고, `SharedContentController.search(keyword, contentType)`에도 그룹
파라미터가 없다. 이 때문에 FE-05는 2026-06-22 슬라이스에서 backend-blocked로 남았다.

이 설계는 그룹 스코프 공유를 도입해 해당 blocker를 해소한다.

## 목표와 비목표

**목표**
- 사용자가 특정 그룹에만 보이는 공유 콘텐츠를 만들 수 있다.
- 그룹의 활성 멤버만 그 콘텐츠를 조회·fork할 수 있다.
- 그룹 상세 화면에서 그룹 공유 콘텐츠 목록을 볼 수 있다.

**비목표**
- 하나의 공유를 여러 그룹에 동시 게시(다대다). 현재 화면이 요구하지 않는다.
- 그룹 공유 콘텐츠의 댓글·좋아요 등 상호작용.
- 공유 콘텐츠 페이지네이션. 기존 공개 검색도 전체 목록을 반환하며, 이 설계는 그 동작을 그대로 따른다.

## 아키텍처

`community` 패키지 내부에서 해결한다. 새 애그리게이트를 만들지 않고 기존 `SharedContent`에
그룹 참조를 속성으로 추가한다. 공유는 하나의 행위이고 그룹은 그 행위의 스코프이므로,
생성 경로를 나누면 서비스 로직이 중복된다.

- **생성**: 기존 `POST /api/v1/community/share`를 확장 (요청 바디에 `groupId` 추가)
- **조회**: 신규 `GET /api/v1/community/groups/{groupId}/shared-content` (그룹 서브리소스)

권한 검사가 필요한 조회 경로를 그룹 서브리소스로 분리하면, 이미 존재하는
`MemberController`의 `/groups/{groupId}/members` 패턴과 라우트가 일관되고 권한 규칙이
경로에 드러난다.

## 데이터 모델

마이그레이션: `src/main/resources/db/migration/community/share/V20260801000000__add_group_id_to_shared_contents.sql`

```sql
ALTER TABLE shared_contents ADD COLUMN group_id BIGINT;
ALTER TABLE shared_contents ADD CONSTRAINT fk_shared_contents_group
    FOREIGN KEY (group_id) REFERENCES groups(id);
CREATE INDEX idx_shared_contents_group_id ON shared_contents(group_id);
```

- 14자리 타임스탬프 버전은 `synapse-shared` 규칙 12(2026-06-05 cutover)를 따른다. CI `Flyway Guard`가 이를 강제한다.
- 컬럼이 nullable이므로 기존 행 백필이 필요 없다. `group_id IS NULL`이 곧 지금까지의 공개 공유다.
- FK 명명은 기존 `fk_shared_contents_source`와 같은 규칙을 따른다.

엔티티 `SharedContent` 변경:
- `private Long groupId` 필드 + getter
- `create(...)` 팩토리에 `groupId` 인자 추가
- `fork(newOwnerId, newToken)`은 **`groupId`를 승계하지 않는다**(사본은 항상 개인 공유)
- 판별 메서드 `boolean isGroupScoped()`

## 권한 규칙

`MemberStatus`는 `INVITED / PENDING / ACTIVE / DECLINED / REJECTED / KICKED` 6종이다.
이 설계에서 **"멤버"는 `ACTIVE`만** 뜻한다. 초대만 받은 상태(`INVITED`)나 가입 신청 중
(`PENDING`)은 열람할 수 없다.

| 행위 | 규칙 | 위반 시 |
|---|---|---|
| 그룹에 공유 생성 | 해당 그룹의 `ACTIVE` 멤버 | 그룹 없음 404 / 비멤버 403 |
| 그룹 공유 목록 조회 | 해당 그룹의 `ACTIVE` 멤버 | 그룹 없음 404 / 비멤버 403 |
| 토큰으로 조회 | 그룹 스코프면 `ACTIVE` 멤버 | 익명·비멤버 **404** |
| fork | 그룹 스코프면 `ACTIVE` 멤버 | 익명·비멤버 **404** |
| 공개 검색 | `group_id IS NULL`만 반환 | — |
| 삭제 | 기존과 동일하게 소유자만 | 403 |

**토큰 경로에서 403이 아니라 404를 쓰는 이유**: `GET /api/v1/community/share/*`는
`SecurityConfig`에서 `permitAll`이라 익명 요청이 도달한다. 403을 주면 "그 토큰은 실재한다"는
사실이 새어나가 토큰 추측 공격에 단서를 준다. 그룹 서브리소스 경로는 호출자가 이미 그룹
존재를 아는 상태이므로 403이 적절하다.

**fork가 그룹을 승계하지 않는 이유**: fork는 "내 것으로 가져오기"다. 사본까지 그룹에 묶이면
그룹을 나간 뒤 자기 사본에 접근하지 못하는 모순이 생긴다.

## API 계약

### 1. 공유 생성 (확장) — `POST /api/v1/community/share`

```java
public record ShareContentRequest(
        @NotNull ContentType contentType,
        @NotNull Long contentId,
        @NotBlank @Size(max = 200) String title,
        @Size(max = 1000) String description,
        @Size(max = 10) List<@Size(max = 30) String> tags,
        Long groupId                    // 신규 · nullable = 공개 공유(기존 동작)
) {}
```

응답 `ShareTokenResponse`는 변경 없음.

### 2. 그룹 공유 목록 (신규) — `GET /api/v1/community/groups/{groupId}/shared-content`

| 파라미터 | 타입 | 필수 | 설명 |
|---|---|---|---|
| `q` | string | 아니오 | 제목·설명·태그 부분 일치 |
| `contentType` | `ContentType` | 아니오 | 타입 필터 |

- 인증 필수. 응답 `List<SharedContentResponse>`, 정렬 `createdAt desc`(공개 검색과 동일).
- 컨트롤러는 `MemberController`와 같이 `/api/v1/community/groups/{groupId}`를 베이스로 두지 않고,
  `SharedContentController`에 `@GetMapping("/groups/{groupId}/shared-content")`로 추가한다.
  공유 콘텐츠 응답 조립 로직이 이미 그 컨트롤러에 있기 때문이다.

### 3. 토큰 조회 (권한 추가) — `GET /api/v1/community/share/{token}`

`permitAll` 유지. 조회된 콘텐츠가 그룹 스코프면 JWT 부재 또는 비-`ACTIVE` 멤버일 때 404.
컨트롤러 시그니처에 `@AuthenticationPrincipal Jwt jwt`를 추가하되 `CurrentUser.require`가
아니라 null 허용으로 받는다(익명 요청이 정상 경로다).

### 4. 공개 검색 (범위 축소) — `GET /api/v1/community/search`

`SharedContentRepository.search` JPQL에 `and c.groupId is null`을 추가한다.
그룹 공유물이 익명 검색에 노출되지 않게 하는 핵심 방어선이다.

### 5. fork (권한 추가) — `POST /api/v1/community/share/{token}/fork`

그룹 스코프면 `ACTIVE` 멤버만. 위반 시 404. 사본의 `groupId`는 `null`.

### 6. 응답 DTO

`SharedContentResponse`에 `Long groupId` 추가. 프론트가 배지·필터에 사용한다.

## 컴포넌트별 변경

**`synapse-engagement-svc`**

| 파일 | 변경 |
|---|---|
| `community/domain/SharedContent.java` | `groupId` 필드·getter, `create` 시그니처, `fork` 미승계, `isGroupScoped()` |
| `community/repository/SharedContentRepository.java` | `search`에 `groupId is null` 조건, `searchByGroupId(...)` 추가 |
| `community/application/SharedContentService.java` | 생성자에 `GroupRepository`·`GroupMemberRepository` 주입, 멤버십 검증 로직, `share`/`fork`/`findByToken` 시그니처에 호출자 전달 |
| `community/api/SharedContentController.java` | 그룹 목록 엔드포인트, 토큰·fork에 JWT 전달 |
| `community/api/dto/ShareContentRequest.java` | `groupId` 필드 |
| `community/api/dto/SharedContentResponse.java` | `groupId` 필드 |
| `db/migration/community/share/V20260801000000__...sql` | 신규 |

**`synapse-frontend`**

| 파일 | 변경 |
|---|---|
| `lib/services/engagement/data/engagement_api.dart` | `shareContent`에 `groupId`, `getGroupSharedContent(...)` 신규, `SharedContent.fromJson`에 `groupId` |
| `lib/services/engagement/providers/engagement_providers.dart` | `groupSharedContentProvider(groupId)` family |
| `.../community_screens/group_detail_screen.dart` | "그룹 공유 콘텐츠" 섹션 |
| `test/services/engagement/fake_engagement_api.dart` | 신규 메서드 |
| `test/services/engagement/engagement_api_test.dart`, `community_screens_render_test.dart` | 케이스 추가 |

## 데이터 흐름

그룹 공유 생성:
```
Client → POST /share {groupId}
       → SharedContentController.share(jwt, request)
       → SharedContentService.share(ownerId, request)
           ├ groupId != null → GroupRepository.findByIdAndDeletedAtIsNull (없으면 NotFoundException)
           │                 → GroupMemberRepository.findByGroupIdAndUserId
           │                   status != ACTIVE → ForbiddenException
           └ SharedContent.create(..., groupId) 저장
```

그룹 목록 조회:
```
Client → GET /groups/{id}/shared-content?q=&contentType=
       → 멤버십 검증(위와 동일, 비멤버 403)
       → SharedContentRepository.searchByGroupId(groupId, keyword, contentType)
```

## 에러 처리

기존 예외 계층을 그대로 쓴다: `NotFoundException`(404), `ForbiddenException`(403),
`BadRequestException`(400). 새 예외 타입을 만들지 않는다.

프론트는 그룹 상세에서 403을 받으면 목록 대신 "가입 후 열람할 수 있습니다" 안내를 표시한다.
그 외 오류는 화면이 이미 쓰는 `AppErrorWidget` + 재시도 패턴을 따른다.

## 테스트 전략

**백엔드** — 기존 `SharedContentServiceOwnerTests` 스타일(Mockito mock 리포지토리 + AssertJ).

1. 그룹 공유 생성: `ACTIVE` 멤버 성공 / `PENDING` 403 / 비멤버 403 / 없는 그룹 404
2. 그룹 목록: 비-`ACTIVE` 403 / 다른 그룹 콘텐츠 미포함
3. 공개 검색이 그룹 공유물을 제외 — 회귀 방지의 핵심 케이스
4. 토큰 조회: 익명 404 / 비멤버 404 / `ACTIVE` 멤버 성공 / 공개 공유는 익명 성공(기존 동작 유지)
5. fork: 비멤버 404 / 멤버 성공 + 사본 `groupId == null`

**회귀 대상**: `SharedContentServiceOwnerTests`는 `new SharedContentService(repo)`로 서비스를
직접 생성하므로 생성자 변경 시 컴파일이 깨진다. 해당 호출을 갱신한다.
`CommunityStep11E2ETests`·`CommunityStep13FinalE2ETests`도 통과를 확인한다.

**프론트엔드**: `flutter analyze` 무경고, `flutter test` 전체 통과, 그룹 공유 섹션 렌더 테스트
(목록 표시 / 빈 상태 / 403 안내).

## 작업 순서

1. `synapse-engagement-svc` PR — 마이그레이션·엔티티·서비스·컨트롤러·테스트
2. 머지 후 `synapse-frontend` PR — 응답에 `groupId`가 존재해야 프론트 테스트가 실제 계약과 일치한다

## 열린 위험

- **소프트 삭제된 그룹**: `groups.deleted_at`이 설정된 그룹의 공유 콘텐츠는 목록 조회 경로가
  그룹 조회 단계에서 404가 되어 접근 불가가 된다. 콘텐츠 자체를 정리하지는 않는다.
  현 단계에서는 의도된 동작으로 두고, 그룹 삭제 정책을 다룰 때 함께 재검토한다.
- **페이지네이션 부재**: 공개 검색과 동일하게 전체 목록을 반환한다. 그룹당 공유물이 수백 건을
  넘기면 응답이 커진다. 프론트 그룹 페이지네이션 UX와 함께 후속으로 다룬다.
