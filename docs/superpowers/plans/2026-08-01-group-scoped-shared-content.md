# 그룹 전용 공유 콘텐츠 (FE-05) Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 특정 그룹의 활성 멤버에게만 보이는 공유 콘텐츠를 만들고 조회할 수 있게 한다.

**Architecture:** 기존 `SharedContent` 애그리게이트에 nullable `groupId`를 속성으로 추가한다. 생성은 기존 `POST /api/v1/community/share`를 확장하고, 권한 검사가 필요한 조회는 그룹 서브리소스 `GET /api/v1/community/groups/{groupId}/shared-content`로 분리한다. 공개 검색은 `groupId IS NULL`만 반환하도록 좁힌다.

**Tech Stack:** Java 21, Spring Boot(Web/Data JPA/Security OAuth2 Resource Server), Flyway, JUnit 5 + AssertJ + Mockito, MockMvc, H2(테스트), Flutter/Riverpod/Dio(프론트).

**Spec:** `docs/superpowers/specs/2026-08-01-group-scoped-shared-content-design.md`

## Global Constraints

- 레포 절대경로: 백엔드 `D:/workspace/final-project-syn/synapse-engagement-svc`, 프론트 `D:/workspace/final-project-syn/synapse-frontend`. 모든 git 명령에 `-C <절대경로>`를 쓴다.
- 작업 브랜치는 이미 존재한다: 백엔드 `feat/group-scoped-shared-content`(main에서 분기, 설계 문서 커밋 `e6f21f0` 포함). 새로 만들지 않는다.
- **"멤버"는 `MemberStatus.ACTIVE`만 뜻한다.** `INVITED`·`PENDING`·`DECLINED`·`REJECTED`·`KICKED`는 모두 비멤버로 취급한다.
- 신규 Flyway 마이그레이션 파일명은 **14자리 타임스탬프** `V<yyyyMMddHHmmss>__<설명>.sql` (synapse-shared 규칙 12, 2026-06-05 cutover). CI `Flyway Guard`가 위반 시 fail한다. **기존 `V1`~`V6` 파일은 수정·이동·재번호 금지.**
- 토큰 경로(`/share/{token}`, `/share/{token}/fork`)에서 권한 없음은 **403이 아니라 404**로 응답한다(토큰 존재 노출 방지). 그룹 서브리소스 경로는 403을 쓴다.
- fork 사본은 `groupId`를 승계하지 않는다(항상 개인 공유).
- 테스트 프로파일(`src/test/resources/application-test.yml`)은 H2 + `ddl-auto: create-drop` + `flyway.enabled: false`다. 즉 **테스트는 마이그레이션 SQL을 실행하지 않고 엔티티로 스키마를 만든다.** 마이그레이션과 엔티티가 어긋나도 테스트는 통과하므로 Task 1에서 둘을 함께 바꾼다.
- 빌드/테스트 명령은 백엔드 레포 루트에서 `./gradlew test --no-daemon`(전체) 또는 `./gradlew test --tests '<FQCN>' --no-daemon`(단일).

## File Structure

**백엔드 (`synapse-engagement-svc`)**

| 파일 | 책임 | 변경 |
|---|---|---|
| `src/main/resources/db/migration/community/share/V20260801000000__add_group_id_to_shared_contents.sql` | `shared_contents.group_id` 컬럼·FK·인덱스 | 생성 |
| `src/main/java/com/synapse/engagement/community/domain/SharedContent.java` | 공유 콘텐츠 애그리게이트 | `groupId` 필드·팩토리·`isGroupScoped()`·fork 미승계 |
| `src/main/java/com/synapse/engagement/community/repository/SharedContentRepository.java` | 조회 쿼리 | 공개 검색에 `groupId is null`, `searchByGroupId` 추가 |
| `src/main/java/com/synapse/engagement/shared/CurrentUser.java` | JWT → userId 변환 | `optional(Jwt)` 추가(익명 허용 경로용) |
| `src/main/java/com/synapse/engagement/community/application/SharedContentService.java` | 공유 유스케이스 + 멤버십 검증 | 리포지토리 2개 주입, 검증 로직, 시그니처 변경 |
| `src/main/java/com/synapse/engagement/community/api/dto/ShareContentRequest.java` | 생성 요청 | `groupId` 필드 |
| `src/main/java/com/synapse/engagement/community/api/dto/SharedContentResponse.java` | 응답 | `groupId` 필드 |
| `src/main/java/com/synapse/engagement/community/api/SharedContentController.java` | HTTP 경계 | 그룹 목록 엔드포인트, 토큰·fork에 JWT 전달 |
| `src/test/java/com/synapse/engagement/community/domain/SharedContentGroupScopeTests.java` | 도메인 단위 테스트 | 생성 |
| `src/test/java/com/synapse/engagement/community/repository/SharedContentRepositoryGroupScopeTests.java` | 쿼리 통합 테스트 | 생성 |
| `src/test/java/com/synapse/engagement/community/application/SharedContentServiceGroupScopeTests.java` | 서비스 권한 테스트 | 생성 |
| `src/test/java/com/synapse/engagement/community/GroupSharedContentE2ETests.java` | HTTP E2E | 생성 |
| `src/test/java/com/synapse/engagement/community/application/SharedContentServiceOwnerTests.java` | 기존 테스트 | 생성자·팩토리 시그니처 변경 반영 |

**프론트 (`synapse-frontend`)**

| 파일 | 책임 | 변경 |
|---|---|---|
| `lib/services/engagement/data/engagement_api.dart` | HTTP 어댑터 | `groupId` 파싱·전달, `getGroupSharedContent` |
| `lib/services/engagement/providers/engagement_providers.dart` | Provider 등록 | `groupSharedContentProvider` family |
| `lib/services/engagement/features/community/presentation/screens/community_screens/group_detail_screen.dart` | 그룹 상세 화면 | 공유 콘텐츠 섹션 |
| `test/services/engagement/fake_engagement_api.dart` | 테스트 더블 | 신규 메서드 |
| `test/services/engagement/engagement_api_test.dart` | API 테스트 | 케이스 추가 |
| `test/services/engagement/community_screens_render_test.dart` | 렌더 테스트 | 케이스 추가 |

---

## Task 1: 도메인 모델 + 마이그레이션

**Files:**
- Create: `src/main/resources/db/migration/community/share/V20260801000000__add_group_id_to_shared_contents.sql`
- Modify: `src/main/java/com/synapse/engagement/community/domain/SharedContent.java`
- Modify: `src/test/java/com/synapse/engagement/community/application/SharedContentServiceOwnerTests.java` (팩토리 시그니처 변경 반영)
- Test: `src/test/java/com/synapse/engagement/community/domain/SharedContentGroupScopeTests.java`

**Interfaces:**
- Produces: `SharedContent.create(Long ownerId, ContentType contentType, Long contentId, String shareToken, String title, String description, String tags, Long groupId)` — 인자 8개(기존 7개 + `groupId`). `SharedContent.getGroupId() → Long`, `SharedContent.isGroupScoped() → boolean`. `fork(Long newOwnerId, String newToken)`은 시그니처 유지하되 사본의 `groupId`는 항상 `null`.

- [ ] **Step 1: 실패하는 테스트 작성**

`src/test/java/com/synapse/engagement/community/domain/SharedContentGroupScopeTests.java`:

```java
package com.synapse.engagement.community.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SharedContentGroupScopeTests {

    @Test
    void createWithoutGroupIsNotGroupScoped() {
        var content = SharedContent.create(1L, ContentType.NOTE, 10L, "tok", "title", null, "", null);

        assertThat(content.getGroupId()).isNull();
        assertThat(content.isGroupScoped()).isFalse();
    }

    @Test
    void createWithGroupIsGroupScoped() {
        var content = SharedContent.create(1L, ContentType.DECK, 10L, "tok", "title", null, "", 77L);

        assertThat(content.getGroupId()).isEqualTo(77L);
        assertThat(content.isGroupScoped()).isTrue();
    }

    @Test
    void forkDoesNotInheritGroupScope() {
        var source = SharedContent.create(1L, ContentType.DECK, 10L, "tok", "title", "desc", "a,b", 77L);

        var forked = source.fork(2L, "tok2");

        assertThat(forked.getGroupId()).isNull();
        assertThat(forked.isGroupScoped()).isFalse();
        assertThat(forked.getOwnerId()).isEqualTo(2L);
        assertThat(forked.getShareToken()).isEqualTo("tok2");
    }
}
```

- [ ] **Step 2: 테스트 실패 확인**

Run: `./gradlew test --tests 'com.synapse.engagement.community.domain.SharedContentGroupScopeTests' --no-daemon`
Expected: 컴파일 실패 — `create(...)` 인자 8개 오버로드 없음, `getGroupId()`/`isGroupScoped()` 미정의.

- [ ] **Step 3: 엔티티 수정**

`SharedContent.java`에서 필드 추가(기존 `sourceShareId` 필드 아래):

```java
    @Column(name = "group_id")
    private Long groupId;
```

private 생성자에 파라미터를 추가하고 대입:

```java
    private SharedContent(
            Long ownerId,
            ContentType contentType,
            Long contentId,
            String shareToken,
            String title,
            String description,
            String tags,
            Long sourceShareId,
            Long groupId
    ) {
        this.ownerId = ownerId;
        this.contentType = contentType;
        this.contentId = contentId;
        this.shareToken = shareToken;
        this.title = title;
        this.description = description;
        this.tags = tags;
        this.sourceShareId = sourceShareId;
        this.groupId = groupId;
    }
```

팩토리와 fork 교체:

```java
    public static SharedContent create(
            Long ownerId,
            ContentType contentType,
            Long contentId,
            String shareToken,
            String title,
            String description,
            String tags,
            Long groupId
    ) {
        return new SharedContent(ownerId, contentType, contentId, shareToken, title, description, tags, null, groupId);
    }

    // fork는 "내 것으로 가져오기"이므로 그룹 스코프를 승계하지 않는다(마지막 인자 null).
    // 승계하면 그룹을 나간 뒤 자기 사본에 접근하지 못하는 모순이 생긴다.
    public SharedContent fork(Long newOwnerId, String newToken) {
        return new SharedContent(newOwnerId, contentType, contentId, newToken, title, description, tags, id, null);
    }
```

getter 추가(`getSourceShareId()` 아래):

```java
    public Long getGroupId() {
        return groupId;
    }

    public boolean isGroupScoped() {
        return groupId != null;
    }
```

- [ ] **Step 4: 기존 테스트의 팩토리 호출 갱신**

`SharedContentServiceOwnerTests.java`의 `SharedContent.create(99L, ContentType.NOTE, 1L, "tok", "title", null, "")` 호출 2곳 중 실제로 존재하는 호출(첫 테스트 1곳)을 다음으로 바꾼다:

```java
        var content = SharedContent.create(99L, ContentType.NOTE, 1L, "tok", "title", null, "", null);
```

`grep -n "SharedContent.create(" src/test` 로 남은 호출이 없는지 확인한다.

- [ ] **Step 5: 테스트 통과 확인**

Run: `./gradlew test --tests 'com.synapse.engagement.community.domain.SharedContentGroupScopeTests' --tests 'com.synapse.engagement.community.application.SharedContentServiceOwnerTests' --no-daemon`
Expected: PASS (SharedContentServiceOwnerTests는 아직 서비스 생성자가 안 바뀌었으므로 그대로 통과)

- [ ] **Step 6: 마이그레이션 작성**

`src/main/resources/db/migration/community/share/V20260801000000__add_group_id_to_shared_contents.sql`:

```sql
-- 그룹 전용 공유 콘텐츠(FE-05): group_id가 NULL이면 기존과 동일한 공개 공유다.
ALTER TABLE shared_contents ADD COLUMN group_id BIGINT;

ALTER TABLE shared_contents ADD CONSTRAINT fk_shared_contents_group
    FOREIGN KEY (group_id) REFERENCES groups(id);

CREATE INDEX idx_shared_contents_group_id ON shared_contents(group_id);
```

- [ ] **Step 7: 마이그레이션 규칙 확인**

Run: `ls src/main/resources/db/migration/community/share/`
Expected: `V2__shared_contents.sql`, `V20260801000000__add_group_id_to_shared_contents.sql` 두 개. 기존 파일은 수정되지 않은 상태여야 한다(`git -C D:/workspace/final-project-syn/synapse-engagement-svc status --porcelain`에 `V2__` 파일이 나타나면 안 됨).

- [ ] **Step 8: 커밋**

```bash
git -C D:/workspace/final-project-syn/synapse-engagement-svc add src/main/java/com/synapse/engagement/community/domain/SharedContent.java src/main/resources/db/migration/community/share/V20260801000000__add_group_id_to_shared_contents.sql src/test/java/com/synapse/engagement/community/domain/SharedContentGroupScopeTests.java src/test/java/com/synapse/engagement/community/application/SharedContentServiceOwnerTests.java
git -C D:/workspace/final-project-syn/synapse-engagement-svc commit -m "feat(community): shared_contents에 group_id 추가"
```

---

## Task 2: 리포지토리 쿼리 — 공개 검색 제외 + 그룹별 조회

**Files:**
- Modify: `src/main/java/com/synapse/engagement/community/repository/SharedContentRepository.java`
- Test: `src/test/java/com/synapse/engagement/community/repository/SharedContentRepositoryGroupScopeTests.java`

**Interfaces:**
- Consumes: Task 1의 `SharedContent.create(..., Long groupId)`.
- Produces: `SharedContentRepository.searchByGroupId(Long groupId, String keyword, ContentType contentType) → List<SharedContent>`. 기존 `search(String keyword, ContentType contentType)`는 시그니처 유지, 동작만 "그룹 공유 제외"로 변경.

- [ ] **Step 1: 실패하는 테스트 작성**

`src/test/java/com/synapse/engagement/community/repository/SharedContentRepositoryGroupScopeTests.java`:

```java
package com.synapse.engagement.community.repository;

import com.synapse.engagement.community.domain.ContentType;
import com.synapse.engagement.community.domain.SharedContent;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class SharedContentRepositoryGroupScopeTests {

    @Autowired
    private SharedContentRepository repository;

    @Test
    void publicSearchExcludesGroupScopedContent() {
        repository.save(SharedContent.create(1L, ContentType.DECK, 100L, "tok-public-1", "Repo Alpha Public", null, "", null));
        repository.save(SharedContent.create(1L, ContentType.DECK, 101L, "tok-group-1", "Repo Alpha Group", null, "", 4001L));

        var found = repository.search("Repo Alpha", null);

        assertThat(found).extracting(SharedContent::getShareToken).containsExactly("tok-public-1");
    }

    @Test
    void searchByGroupIdReturnsOnlyThatGroup() {
        repository.save(SharedContent.create(1L, ContentType.NOTE, 200L, "tok-g-4002", "Repo Beta", null, "", 4002L));
        repository.save(SharedContent.create(1L, ContentType.NOTE, 201L, "tok-g-4003", "Repo Beta", null, "", 4003L));
        repository.save(SharedContent.create(1L, ContentType.NOTE, 202L, "tok-p-beta", "Repo Beta", null, "", null));

        var found = repository.searchByGroupId(4002L, null, null);

        assertThat(found).extracting(SharedContent::getShareToken).containsExactly("tok-g-4002");
    }

    @Test
    void searchByGroupIdAppliesKeywordAndContentType() {
        repository.save(SharedContent.create(1L, ContentType.DECK, 300L, "tok-g-match", "Repo Gamma Deck", null, "", 4004L));
        repository.save(SharedContent.create(1L, ContentType.NOTE, 301L, "tok-g-type", "Repo Gamma Note", null, "", 4004L));
        repository.save(SharedContent.create(1L, ContentType.DECK, 302L, "tok-g-word", "Repo Delta Deck", null, "", 4004L));

        var found = repository.searchByGroupId(4004L, "Gamma", ContentType.DECK);

        assertThat(found).extracting(SharedContent::getShareToken).containsExactly("tok-g-match");
    }
}
```

- [ ] **Step 2: 테스트 실패 확인**

Run: `./gradlew test --tests 'com.synapse.engagement.community.repository.SharedContentRepositoryGroupScopeTests' --no-daemon`
Expected: 컴파일 실패 — `searchByGroupId` 미정의. (`publicSearchExcludesGroupScopedContent`도 컴파일이 되면 실패했을 것이다.)

- [ ] **Step 3: 리포지토리 수정**

`SharedContentRepository.java`의 `search` 쿼리에 `and c.groupId is null`을 추가하고, `searchByGroupId`를 새로 정의한다. 파일 전체 내용:

```java
package com.synapse.engagement.community.repository;

import com.synapse.engagement.community.domain.ContentType;
import com.synapse.engagement.community.domain.SharedContent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SharedContentRepository extends JpaRepository<SharedContent, Long> {
    Optional<SharedContent> findByShareTokenAndDeletedAtIsNull(String shareToken);

    Optional<SharedContent> findByIdAndDeletedAtIsNull(Long id);

    Optional<SharedContent> findByIdAndContentTypeAndDeletedAtIsNull(Long id, ContentType contentType);

    // 공개 검색은 익명 접근이 가능하므로(SecurityConfig permitAll) 그룹 전용 공유를 절대 노출하지 않는다.
    @Query("""
            select c from SharedContent c
            where c.deletedAt is null
              and c.groupId is null
              and (:contentType is null or c.contentType = :contentType)
              and (
                :keyword is null
                or lower(c.title) like lower(concat('%', :keyword, '%'))
                or lower(c.description) like lower(concat('%', :keyword, '%'))
                or lower(c.tags) like lower(concat('%', :keyword, '%'))
              )
            order by c.createdAt desc
            """)
    List<SharedContent> search(@Param("keyword") String keyword, @Param("contentType") ContentType contentType);

    @Query("""
            select c from SharedContent c
            where c.deletedAt is null
              and c.groupId = :groupId
              and (:contentType is null or c.contentType = :contentType)
              and (
                :keyword is null
                or lower(c.title) like lower(concat('%', :keyword, '%'))
                or lower(c.description) like lower(concat('%', :keyword, '%'))
                or lower(c.tags) like lower(concat('%', :keyword, '%'))
              )
            order by c.createdAt desc
            """)
    List<SharedContent> searchByGroupId(
            @Param("groupId") Long groupId,
            @Param("keyword") String keyword,
            @Param("contentType") ContentType contentType
    );
}
```

- [ ] **Step 4: 테스트 통과 확인**

Run: `./gradlew test --tests 'com.synapse.engagement.community.repository.SharedContentRepositoryGroupScopeTests' --no-daemon`
Expected: PASS (3 tests)

- [ ] **Step 5: 커밋**

```bash
git -C D:/workspace/final-project-syn/synapse-engagement-svc add src/main/java/com/synapse/engagement/community/repository/SharedContentRepository.java src/test/java/com/synapse/engagement/community/repository/SharedContentRepositoryGroupScopeTests.java
git -C D:/workspace/final-project-syn/synapse-engagement-svc commit -m "feat(community): 공개 검색에서 그룹 공유 제외 + 그룹별 조회 쿼리"
```

---

## Task 3: 서비스 — 멤버십 검증

**Files:**
- Modify: `src/main/java/com/synapse/engagement/shared/CurrentUser.java`
- Modify: `src/main/java/com/synapse/engagement/community/application/SharedContentService.java`
- Modify: `src/main/java/com/synapse/engagement/community/api/dto/ShareContentRequest.java`
- Modify: `src/main/java/com/synapse/engagement/community/api/SharedContentController.java` (컴파일 유지용 최소 수정)
- Modify: `src/test/java/com/synapse/engagement/community/application/SharedContentServiceOwnerTests.java` (생성자 변경 반영)
- Test: `src/test/java/com/synapse/engagement/community/application/SharedContentServiceGroupScopeTests.java`

**Interfaces:**
- Consumes: Task 1의 `SharedContent.create(..., groupId)`·`isGroupScoped()`, Task 2의 `searchByGroupId(...)`.
- Produces:
  - `CurrentUser.optional(Jwt jwt) → Long` (jwt가 null이거나 subject가 비면 `null`)
  - `SharedContentService(SharedContentRepository, GroupRepository, GroupMemberRepository)` — 생성자 인자 3개
  - `SharedContentService.findByToken(String token, Long viewerId) → SharedContentResponse`
  - `SharedContentService.searchInGroup(Long groupId, Long viewerId, String keyword, ContentType contentType) → List<SharedContentResponse>`
  - `ShareContentRequest`에 `Long groupId` 컴포넌트 추가(6번째)

- [ ] **Step 1: 실패하는 테스트 작성**

`src/test/java/com/synapse/engagement/community/application/SharedContentServiceGroupScopeTests.java`:

```java
package com.synapse.engagement.community.application;

import com.synapse.engagement.community.api.dto.ShareContentRequest;
import com.synapse.engagement.community.domain.ContentType;
import com.synapse.engagement.community.domain.Group;
import com.synapse.engagement.community.domain.GroupMember;
import com.synapse.engagement.community.domain.MemberStatus;
import com.synapse.engagement.community.domain.SharedContent;
import com.synapse.engagement.community.repository.GroupMemberRepository;
import com.synapse.engagement.community.repository.GroupRepository;
import com.synapse.engagement.community.repository.SharedContentRepository;
import com.synapse.engagement.shared.ForbiddenException;
import com.synapse.engagement.shared.NotFoundException;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SharedContentServiceGroupScopeTests {

    private static final Long GROUP_ID = 500L;
    private static final Long MEMBER_ID = 11L;
    private static final Long OUTSIDER_ID = 12L;

    private final SharedContentRepository sharedContentRepository = mock(SharedContentRepository.class);
    private final GroupRepository groupRepository = mock(GroupRepository.class);
    private final GroupMemberRepository groupMemberRepository = mock(GroupMemberRepository.class);
    private final SharedContentService service =
            new SharedContentService(sharedContentRepository, groupRepository, groupMemberRepository);

    @Test
    void shareToGroupSucceedsForActiveMember() {
        givenGroupExists();
        givenMembership(MEMBER_ID, MemberStatus.ACTIVE);
        when(sharedContentRepository.save(any(SharedContent.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.share(MEMBER_ID, request(GROUP_ID));

        assertThat(response.shareToken()).isNotBlank();
    }

    @Test
    void shareToGroupRejectsPendingMember() {
        givenGroupExists();
        givenMembership(MEMBER_ID, MemberStatus.PENDING);

        assertThatThrownBy(() -> service.share(MEMBER_ID, request(GROUP_ID)))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void shareToGroupRejectsNonMember() {
        givenGroupExists();
        when(groupMemberRepository.findByGroupIdAndUserId(GROUP_ID, OUTSIDER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.share(OUTSIDER_ID, request(GROUP_ID)))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void shareToMissingGroupIsNotFound() {
        when(groupRepository.findByIdAndDeletedAtIsNull(GROUP_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.share(MEMBER_ID, request(GROUP_ID)))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void searchInGroupRejectsNonMember() {
        givenGroupExists();
        when(groupMemberRepository.findByGroupIdAndUserId(GROUP_ID, OUTSIDER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.searchInGroup(GROUP_ID, OUTSIDER_ID, null, null))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void searchInGroupReturnsContentForActiveMember() {
        givenGroupExists();
        givenMembership(MEMBER_ID, MemberStatus.ACTIVE);
        when(sharedContentRepository.searchByGroupId(GROUP_ID, null, null))
                .thenReturn(List.of(groupContent()));

        var found = service.searchInGroup(GROUP_ID, MEMBER_ID, null, null);

        assertThat(found).hasSize(1);
        assertThat(found.getFirst().groupId()).isEqualTo(GROUP_ID);
    }

    @Test
    void findByTokenHidesGroupContentFromAnonymous() {
        when(sharedContentRepository.findByShareTokenAndDeletedAtIsNull("tok"))
                .thenReturn(Optional.of(groupContent()));

        assertThatThrownBy(() -> service.findByToken("tok", null))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void findByTokenHidesGroupContentFromNonMember() {
        when(sharedContentRepository.findByShareTokenAndDeletedAtIsNull("tok"))
                .thenReturn(Optional.of(groupContent()));
        when(groupMemberRepository.findByGroupIdAndUserId(GROUP_ID, OUTSIDER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findByToken("tok", OUTSIDER_ID))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void findByTokenAllowsPublicContentForAnonymous() {
        when(sharedContentRepository.findByShareTokenAndDeletedAtIsNull("pub"))
                .thenReturn(Optional.of(SharedContent.create(1L, ContentType.NOTE, 9L, "pub", "Public", null, "", null)));

        var response = service.findByToken("pub", null);

        assertThat(response.groupId()).isNull();
    }

    @Test
    void forkOfGroupContentByNonMemberIsNotFound() {
        when(sharedContentRepository.findByShareTokenAndDeletedAtIsNull("tok"))
                .thenReturn(Optional.of(groupContent()));
        when(groupMemberRepository.findByGroupIdAndUserId(GROUP_ID, OUTSIDER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.fork(OUTSIDER_ID, "tok"))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void forkOfGroupContentByMemberProducesPersonalCopy() {
        when(sharedContentRepository.findByShareTokenAndDeletedAtIsNull("tok"))
                .thenReturn(Optional.of(groupContent()));
        givenMembership(MEMBER_ID, MemberStatus.ACTIVE);
        when(sharedContentRepository.save(any(SharedContent.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var forked = service.fork(MEMBER_ID, "tok");

        assertThat(forked.groupId()).isNull();
        assertThat(forked.ownerId()).isEqualTo(MEMBER_ID);
    }

    private ShareContentRequest request(Long groupId) {
        return new ShareContentRequest(ContentType.DECK, 1L, "title", null, List.of(), groupId);
    }

    private SharedContent groupContent() {
        return SharedContent.create(1L, ContentType.DECK, 9L, "tok", "Group content", null, "", GROUP_ID);
    }

    private void givenGroupExists() {
        when(groupRepository.findByIdAndDeletedAtIsNull(GROUP_ID)).thenReturn(Optional.of(mock(Group.class)));
    }

    private void givenMembership(Long userId, MemberStatus status) {
        var member = mock(GroupMember.class);
        when(member.getStatus()).thenReturn(status);
        when(groupMemberRepository.findByGroupIdAndUserId(GROUP_ID, userId)).thenReturn(Optional.of(member));
    }
}
```

- [ ] **Step 2: 테스트 실패 확인**

Run: `./gradlew test --tests 'com.synapse.engagement.community.application.SharedContentServiceGroupScopeTests' --no-daemon`
Expected: 컴파일 실패 — 생성자 인자 3개 없음, `searchInGroup`·`findByToken(String, Long)` 미정의, `ShareContentRequest` 컴포넌트 6개 없음.

- [ ] **Step 3: `CurrentUser.optional` 추가**

`CurrentUser.java`의 `require(Jwt)` 아래에 삽입:

```java
    /**
     * permitAll 경로(공개 공유 링크 등)는 익명 요청이 정상이므로 JWT가 없어도 예외를 던지지 않는다.
     * 호출자는 null을 "비로그인"으로 해석한다.
     */
    public static Long optional(Jwt jwt) {
        if (jwt == null || jwt.getSubject() == null || jwt.getSubject().isBlank()) {
            return null;
        }
        return resolveUserId(jwt.getSubject());
    }
```

- [ ] **Step 4: `ShareContentRequest`에 `groupId` 추가**

```java
public record ShareContentRequest(
        @NotNull ContentType contentType,
        @NotNull Long contentId,
        @NotBlank @Size(max = 200) String title,
        @Size(max = 1000) String description,
        @Size(max = 10) List<@Size(max = 30) String> tags,
        // null이면 공개 공유(기존 동작), 값이 있으면 해당 그룹의 ACTIVE 멤버에게만 보인다.
        Long groupId
) {
}
```

- [ ] **Step 5: 서비스 수정**

`SharedContentService.java`에 import 추가:

```java
import com.synapse.engagement.community.domain.MemberStatus;
import com.synapse.engagement.community.repository.GroupMemberRepository;
import com.synapse.engagement.community.repository.GroupRepository;
```

필드·생성자 교체:

```java
    private final SecureRandom secureRandom = new SecureRandom();
    private final SharedContentRepository sharedContentRepository;
    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;

    public SharedContentService(
            SharedContentRepository sharedContentRepository,
            GroupRepository groupRepository,
            GroupMemberRepository groupMemberRepository
    ) {
        this.sharedContentRepository = sharedContentRepository;
        this.groupRepository = groupRepository;
        this.groupMemberRepository = groupMemberRepository;
    }
```

`share`·`findByToken`·`search`·`fork` 교체 및 `searchInGroup` 추가:

```java
    @Transactional
    public ShareTokenResponse share(Long ownerId, ShareContentRequest request) {
        if (request.groupId() != null) {
            requireActiveMember(request.groupId(), ownerId);
        }
        // shareToken은 URL에 노출되므로 예측 가능한 시퀀스 대신 SecureRandom 기반 토큰을 쓴다.
        var token = newToken();
        var content = sharedContentRepository.save(SharedContent.create(
                ownerId,
                request.contentType(),
                request.contentId(),
                token,
                request.title(),
                request.description(),
                joinTags(request.tags()),
                request.groupId()
        ));
        return new ShareTokenResponse(content.getShareToken(), "/api/v1/community/share/" + content.getShareToken());
    }

    @Transactional(readOnly = true)
    public SharedContentResponse findByToken(String token, Long viewerId) {
        var content = findActiveByToken(token);
        requireVisibleToViewer(content, viewerId);
        return SharedContentResponse.from(content);
    }

    @Transactional(readOnly = true)
    public List<SharedContentResponse> search(String keyword, ContentType contentType) {
        // 빈 검색어는 null로 넘겨 repository가 "전체 검색 + 타입 필터"처럼 처리할 수 있게 한다.
        return sharedContentRepository.search(normalizeKeyword(keyword), contentType).stream()
                .map(SharedContentResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<SharedContentResponse> searchInGroup(Long groupId, Long viewerId, String keyword, ContentType contentType) {
        requireActiveMember(groupId, viewerId);
        return sharedContentRepository.searchByGroupId(groupId, normalizeKeyword(keyword), contentType).stream()
                .map(SharedContentResponse::from)
                .toList();
    }

    @Transactional
    public SharedContentResponse fork(Long userId, String token) {
        var source = findActiveByToken(token);
        requireVisibleToViewer(source, userId);
        // fork는 원본 조회 수를 올리고, 복사본에는 새 shareToken을 부여해 원본과 URL을 분리한다.
        source.incrementDownloadCount();
        var forked = sharedContentRepository.save(source.fork(userId, newToken()));
        return SharedContentResponse.from(forked);
    }
```

private 헬퍼를 `findActiveByToken` 위에 추가:

```java
    /**
     * 토큰 경로는 permitAll이라 익명 요청이 도달한다. 권한이 없을 때 403을 주면 "그 토큰은 실재한다"는
     * 사실이 새어나가므로, 존재 자체를 감추기 위해 404를 던진다.
     */
    private void requireVisibleToViewer(SharedContent content, Long viewerId) {
        if (content.isGroupScoped() && !isActiveMember(content.getGroupId(), viewerId)) {
            throw new NotFoundException("Shared content not found");
        }
    }

    private void requireActiveMember(Long groupId, Long userId) {
        groupRepository.findByIdAndDeletedAtIsNull(groupId)
                .orElseThrow(() -> new NotFoundException("Group not found: id=" + groupId));
        if (!isActiveMember(groupId, userId)) {
            throw new ForbiddenException("Only active group members can access group-scoped content");
        }
    }

    // 멤버십은 ACTIVE만 인정한다. INVITED/PENDING/DECLINED/REJECTED/KICKED는 모두 비멤버다.
    private boolean isActiveMember(Long groupId, Long userId) {
        if (userId == null) {
            return false;
        }
        return groupMemberRepository.findByGroupIdAndUserId(groupId, userId)
                .map(member -> member.getStatus() == MemberStatus.ACTIVE)
                .orElse(false);
    }

    private String normalizeKeyword(String keyword) {
        return keyword == null || keyword.isBlank() ? null : keyword.trim();
    }
```

- [ ] **Step 6: 기존 테스트의 생성자 호출 갱신**

`SharedContentServiceOwnerTests.java` 상단 필드 선언을 다음으로 바꾼다:

```java
    private final SharedContentRepository repo = mock(SharedContentRepository.class);
    private final GroupRepository groupRepository = mock(GroupRepository.class);
    private final GroupMemberRepository groupMemberRepository = mock(GroupMemberRepository.class);
    private final SharedContentService service =
            new SharedContentService(repo, groupRepository, groupMemberRepository);
```

import 2줄을 추가한다:

```java
import com.synapse.engagement.community.repository.GroupMemberRepository;
import com.synapse.engagement.community.repository.GroupRepository;
```

- [ ] **Step 7: 컨트롤러 컴파일 유지 수정**

`SharedContentController.java`의 `findByToken`을 서비스 새 시그니처에 맞춘다(엔드포인트 추가는 Task 4에서 한다):

```java
    @GetMapping("/share/{token}")
    public SharedContentResponse findByToken(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable String token
    ) {
        // 공개 공유 링크는 익명 열람이 정상 경로이므로 require가 아니라 optional을 쓴다.
        return sharedContentService.findByToken(token, CurrentUser.optional(jwt));
    }
```

- [ ] **Step 8: 테스트 통과 확인**

Run: `./gradlew test --tests 'com.synapse.engagement.community.application.*' --no-daemon`
Expected: PASS (`SharedContentServiceGroupScopeTests` 11개 + 기존 `SharedContentServiceOwnerTests` 2개 + `GroupServiceOwnerTests`)

- [ ] **Step 9: 커밋**

```bash
git -C D:/workspace/final-project-syn/synapse-engagement-svc add src/main/java/com/synapse/engagement/shared/CurrentUser.java src/main/java/com/synapse/engagement/community/application/SharedContentService.java src/main/java/com/synapse/engagement/community/api/dto/ShareContentRequest.java src/main/java/com/synapse/engagement/community/api/SharedContentController.java src/test/java/com/synapse/engagement/community/application/
git -C D:/workspace/final-project-syn/synapse-engagement-svc commit -m "feat(community): 그룹 공유 멤버십 검증 추가"
```

---

## Task 4: HTTP 경계 — 그룹 목록 엔드포인트 + 응답 DTO

**Files:**
- Modify: `src/main/java/com/synapse/engagement/community/api/dto/SharedContentResponse.java`
- Modify: `src/main/java/com/synapse/engagement/community/api/SharedContentController.java`
- Test: `src/test/java/com/synapse/engagement/community/GroupSharedContentE2ETests.java`

**Interfaces:**
- Consumes: Task 3의 `SharedContentService.searchInGroup(...)`, `findByToken(String, Long)`.
- Produces: `GET /api/v1/community/groups/{groupId}/shared-content?q=&contentType=` → `200 List<SharedContentResponse>` / `403` 비멤버 / `404` 없는 그룹. `SharedContentResponse`에 `groupId` 필드(마지막 컴포넌트).

- [ ] **Step 1: 실패하는 테스트 작성**

`src/test/java/com/synapse/engagement/community/GroupSharedContentE2ETests.java`:

```java
package com.synapse.engagement.community;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.synapse.engagement.support.TestJwt;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class GroupSharedContentE2ETests {

    @Autowired
    private MockMvc mvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void groupScopedShareIsVisibleToMembersOnlyAndHiddenFromPublicSearch() throws Exception {
        var ownerToken = bearer("52100");
        var outsiderToken = bearer("52101");

        var groupId = json(mvc.perform(post("/api/v1/community/groups")
                        .header("Authorization", ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"FE05 Study Group","description":"group scoped share","isPublic":true}
                                """))
                .andExpect(status().isCreated())
                .andReturn()).get("id").asLong();

        // 1) 그룹 멤버(= 소유자)는 그룹 공유를 만들 수 있다.
        var shareToken = json(mvc.perform(post("/api/v1/community/share")
                        .header("Authorization", ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"contentType":"DECK","contentId":52001,"title":"FE05 Group Deck","description":"members only","tags":["fe05"],"groupId":%d}
                                """.formatted(groupId)))
                .andExpect(status().isCreated())
                .andReturn()).get("shareToken").asText();

        // 2) 멤버는 그룹 목록에서 볼 수 있다.
        mvc.perform(get("/api/v1/community/groups/" + groupId + "/shared-content")
                        .header("Authorization", ownerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("FE05 Group Deck"))
                .andExpect(jsonPath("$[0].groupId").value(groupId));

        // 3) 비멤버는 그룹 목록에 접근할 수 없다(403).
        mvc.perform(get("/api/v1/community/groups/" + groupId + "/shared-content")
                        .header("Authorization", outsiderToken))
                .andExpect(status().isForbidden());

        // 4) 없는 그룹은 404다.
        mvc.perform(get("/api/v1/community/groups/99999999/shared-content")
                        .header("Authorization", ownerToken))
                .andExpect(status().isNotFound());

        // 5) 익명 토큰 조회는 존재를 감춘다(404).
        mvc.perform(get("/api/v1/community/share/" + shareToken))
                .andExpect(status().isNotFound());

        // 6) 비멤버 토큰 조회도 404다.
        mvc.perform(get("/api/v1/community/share/" + shareToken)
                        .header("Authorization", outsiderToken))
                .andExpect(status().isNotFound());

        // 7) 멤버는 토큰으로 조회할 수 있다.
        mvc.perform(get("/api/v1/community/share/" + shareToken)
                        .header("Authorization", ownerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.groupId").value(groupId));

        // 8) 공개 검색에는 그룹 공유가 노출되지 않는다.
        var publicSearch = mvc.perform(get("/api/v1/community/search").param("q", "FE05 Group Deck"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        assertThat(publicSearch).doesNotContain("FE05 Group Deck");
    }

    @Test
    void nonMemberCannotShareToGroup() throws Exception {
        var ownerToken = bearer("52200");
        var outsiderToken = bearer("52201");

        var groupId = json(mvc.perform(post("/api/v1/community/groups")
                        .header("Authorization", ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"FE05 Closed Group","description":"no outsiders","isPublic":true}
                                """))
                .andExpect(status().isCreated())
                .andReturn()).get("id").asLong();

        mvc.perform(post("/api/v1/community/share")
                        .header("Authorization", outsiderToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"contentType":"NOTE","contentId":52002,"title":"Outsider Note","tags":[],"groupId":%d}
                                """.formatted(groupId)))
                .andExpect(status().isForbidden());
    }

    private JsonNode json(org.springframework.test.web.servlet.MvcResult result) throws Exception {
        return objectMapper.readTree(result.getResponse().getContentAsString());
    }

    private String bearer(String subject) {
        return "Bearer " + TestJwt.accessToken(subject);
    }
}
```

**참고:** 그룹 생성 요청 본문 필드명은 `GroupCreateRequest`를 열어 확인하고, 다르면 그 레코드의 실제 컴포넌트명에 맞춘다. 응답의 그룹 식별자 필드도 `GroupResponse`에서 확인한다(`id`가 아니면 그 이름을 쓴다).

- [ ] **Step 2: 테스트 실패 확인**

Run: `./gradlew test --tests 'com.synapse.engagement.community.GroupSharedContentE2ETests' --no-daemon`
Expected: FAIL — `/groups/{id}/shared-content`가 없어 404(3번 단계는 403 기대인데 404), `$.groupId`도 응답에 없음.

- [ ] **Step 3: 응답 DTO에 `groupId` 추가**

`SharedContentResponse.java`:

```java
public record SharedContentResponse(
        Long id,
        String shareToken,
        ContentType contentType,
        Long contentId,
        Long ownerId,
        String title,
        String description,
        List<String> tags,
        long downloadCount,
        Long sourceShareId,
        Instant createdAt,
        Long groupId
) {
    public static SharedContentResponse from(SharedContent content) {
        return new SharedContentResponse(
                content.getId(),
                content.getShareToken(),
                content.getContentType(),
                content.getContentId(),
                content.getOwnerId(),
                content.getTitle(),
                content.getDescription(),
                splitTags(content.getTags()),
                content.getDownloadCount(),
                content.getSourceShareId(),
                content.getCreatedAt(),
                content.getGroupId()
        );
    }
```

(`splitTags`는 그대로 둔다.)

- [ ] **Step 4: 컨트롤러에 그룹 목록 엔드포인트 추가**

`SharedContentController.java`의 `search` 아래에 추가:

```java
    // 멤버십 검증이 필요한 조회이므로 공개 /search와 분리해 그룹 서브리소스로 노출한다.
    // (MemberController의 /groups/{groupId}/members 와 같은 라우트 패턴)
    @GetMapping("/groups/{groupId}/shared-content")
    public List<SharedContentResponse> searchInGroup(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long groupId,
            @RequestParam(required = false, name = "q") String keyword,
            @RequestParam(required = false) ContentType contentType
    ) {
        return sharedContentService.searchInGroup(groupId, CurrentUser.require(jwt), keyword, contentType);
    }
```

- [ ] **Step 5: 테스트 통과 확인**

Run: `./gradlew test --tests 'com.synapse.engagement.community.GroupSharedContentE2ETests' --no-daemon`
Expected: PASS (2 tests)

- [ ] **Step 6: 전체 회귀 확인**

Run: `./gradlew test --no-daemon`
Expected: 전체 PASS. 특히 `CommunityStep11E2ETests`·`CommunityStep13FinalE2ETests`가 통과해야 한다(공개 공유 흐름은 동작이 바뀌지 않았다).

- [ ] **Step 7: 커밋 및 푸시**

```bash
git -C D:/workspace/final-project-syn/synapse-engagement-svc add src/main/java/com/synapse/engagement/community/api/ src/test/java/com/synapse/engagement/community/GroupSharedContentE2ETests.java
git -C D:/workspace/final-project-syn/synapse-engagement-svc commit -m "feat(community): 그룹 공유 콘텐츠 조회 API 추가"
git -C D:/workspace/final-project-syn/synapse-engagement-svc push -u origin feat/group-scoped-shared-content
```

- [ ] **Step 8: PR 생성**

```bash
cd D:/workspace/final-project-syn/synapse-engagement-svc
gh pr create --base main --head feat/group-scoped-shared-content \
  --title "feat(community): 그룹 전용 공유 콘텐츠 (FE-05)" \
  --body "설계: docs/superpowers/specs/2026-08-01-group-scoped-shared-content-design.md

- shared_contents에 nullable group_id 추가(V20260801000000)
- 그룹 ACTIVE 멤버만 생성/조회/fork, 공개 검색에서 제외
- GET /api/v1/community/groups/{groupId}/shared-content 신규
- 토큰 경로는 권한 없음을 404로 응답(토큰 존재 노출 방지)"
```

CI(`build`, `guard / flyway-guard`)가 녹색인지 확인한다. `dev-smoke`는 org 시크릿 `DOCKERHUB_TOKEN` 만료 시 실패할 수 있으며 이 변경과 무관하다.

---

## Task 5: 프론트엔드 연동

**선행 조건:** Task 4의 PR이 머지되어 있어야 한다. 응답에 `groupId`가 실제로 존재해야 계약이 일치한다.

**Files:**
- Modify: `lib/services/engagement/data/engagement_api.dart`
- Modify: `lib/services/engagement/providers/engagement_providers.dart`
- Modify: `lib/services/engagement/features/community/presentation/screens/community_screens/group_detail_screen.dart`
- Test: `test/services/engagement/fake_engagement_api.dart`, `test/services/engagement/engagement_api_test.dart`, `test/services/engagement/community_screens_render_test.dart`

**Interfaces:**
- Consumes: `GET /api/v1/community/groups/{groupId}/shared-content` → `List<SharedContent>`(각 항목에 `groupId`).
- Produces: `EngagementApi.getGroupSharedContent(String groupId, {String? keyword, String? contentType}) → Future<List<SharedContent>>`, `groupSharedContentProvider(String groupId)` (FutureProvider family).

- [ ] **Step 1: 브랜치 생성**

```bash
git -C D:/workspace/final-project-syn/synapse-frontend checkout main
git -C D:/workspace/final-project-syn/synapse-frontend pull --ff-only
git -C D:/workspace/final-project-syn/synapse-frontend checkout -b feat/fe05-group-shared-content
```

- [ ] **Step 2: 실패하는 API 테스트 작성**

`test/services/engagement/engagement_api_test.dart`에 케이스를 추가한다. 같은 파일의 기존 테스트가 쓰는 Dio mock 방식(`DioAdapter`/`MockAdapter` 등 파일 상단에서 이미 사용 중인 것)을 그대로 따른다:

```dart
  test('getGroupSharedContent가 그룹 공유 목록을 반환한다', () async {
    // 기존 테스트와 동일한 방식으로 GET /api/v1/community/groups/7/shared-content 응답을 스텁한다.
    // 응답 예시:
    // [{"id":1,"shareToken":"tok","contentType":"DECK","contentId":9,"ownerId":3,
    //   "title":"Group Deck","description":null,"tags":[],"downloadCount":0,
    //   "sourceShareId":null,"createdAt":"2026-08-01T00:00:00Z","groupId":7}]
    final result = await api.getGroupSharedContent('7');

    expect(result, hasLength(1));
    expect(result.first.title, 'Group Deck');
    expect(result.first.groupId, '7');
  });
```

- [ ] **Step 3: 테스트 실패 확인**

Run: `cd D:/workspace/final-project-syn/synapse-frontend && flutter test test/services/engagement/engagement_api_test.dart`
Expected: 컴파일 실패 — `getGroupSharedContent` 미정의, `SharedContent.groupId` 미정의.

- [ ] **Step 4: API 어댑터 구현**

`engagement_api.dart`의 `SharedContent` 클래스에 `groupId` 필드를 추가한다(기존 필드 선언·생성자·`fromJson`에 각각 반영, id 계열은 파일에 이미 있는 `_stringId` 헬퍼로 문자열화):

```dart
  final String? groupId;
```

`fromJson`에 추가:

```dart
      groupId: json['groupId'] == null ? null : _stringId(json['groupId']),
```

`shareContent(...)` 요청 본문에 그룹을 선택적으로 싣는다:

```dart
      if (groupId != null) 'groupId': int.parse(groupId),
```

그룹 목록 메서드를 `searchSharedContent` 아래에 추가:

```dart
  Future<List<SharedContent>> getGroupSharedContent(
    String groupId, {
    String? keyword,
    String? contentType,
  }) async {
    final response = await _dio.get<dynamic>(
      '/api/v1/community/groups/$groupId/shared-content',
      queryParameters: {
        if (keyword != null && keyword.isNotEmpty) 'q': keyword,
        if (contentType != null) 'contentType': contentType,
      },
    );
    return _unwrapList(response.data)
        .map(SharedContent.fromJson)
        .toList();
  }
```

(`_unwrapList`·`_unwrapMap`은 파일에 이미 있는 헬퍼다. `searchSharedContent` 구현을 그대로 참고해 같은 헬퍼를 쓴다.)

- [ ] **Step 5: 테스트 통과 확인**

Run: `flutter test test/services/engagement/engagement_api_test.dart`
Expected: PASS

- [ ] **Step 6: Provider 추가**

`engagement_providers.dart`에 기존 family provider와 같은 형태로 추가한다:

```dart
final groupSharedContentProvider =
    FutureProvider.family<List<SharedContent>, String>((ref, groupId) {
  return ref.watch(engagementApiProvider).getGroupSharedContent(groupId);
});
```

- [ ] **Step 7: 화면 섹션 렌더 테스트 작성**

`test/services/engagement/fake_engagement_api.dart`에 `getGroupSharedContent`를 구현(기본값은 빈 리스트, 테스트가 주입할 수 있게 필드로 노출)하고, `community_screens_render_test.dart`에 3개 케이스를 추가한다:

1. 목록이 있으면 제목이 화면에 보인다
2. 목록이 비면 "아직 공유된 콘텐츠가 없습니다"가 보인다
3. API가 403(`DioException` with `response.statusCode == 403`)을 던지면 "가입 후 열람할 수 있습니다"가 보인다

- [ ] **Step 8: 그룹 상세 화면에 섹션 추가**

`group_detail_screen.dart`의 멤버 목록 아래에 섹션을 넣는다. 화면이 이미 쓰는 `AppAsyncValueWidget`·`AppErrorWidget` 패턴을 따르고, 403만 안내 문구로 분기한다:

```dart
        const SizedBox(height: AppSpacing.xl),
        Text('그룹 공유 콘텐츠', style: textTheme.titleSmall),
        const SizedBox(height: AppSpacing.sm),
        Consumer(
          builder: (context, ref, _) {
            final shared = ref.watch(groupSharedContentProvider(group.id));
            return AppAsyncValueWidget<List<SharedContent>>(
              value: shared,
              loading: const AppLoadingWidget(label: '공유 콘텐츠를 불러오는 중입니다.'),
              error: (error, _) {
                final isForbidden =
                    error is DioException && error.response?.statusCode == 403;
                if (isForbidden) {
                  return Text(
                    '가입 후 열람할 수 있습니다.',
                    style: textTheme.bodySmall?.copyWith(color: AppColors.muted),
                  );
                }
                return AppErrorWidget(
                  message: '공유 콘텐츠를 불러오지 못했습니다.',
                  onRetry: () => ref.invalidate(groupSharedContentProvider(group.id)),
                );
              },
              data: (items) {
                if (items.isEmpty) {
                  return Text(
                    '아직 공유된 콘텐츠가 없습니다.',
                    style: textTheme.bodySmall?.copyWith(color: AppColors.muted),
                  );
                }
                return Column(
                  children: [
                    for (final item in items)
                      ListTile(
                        contentPadding: EdgeInsets.zero,
                        title: Text(item.title),
                        subtitle: item.description == null
                            ? null
                            : Text(item.description!),
                      ),
                  ],
                );
              },
            );
          },
        ),
```

`DioException` import가 없으면 파일 상단 `part of` 대상인 `community_screens.dart`의 import 목록에 `package:dio/dio.dart`를 추가한다.

- [ ] **Step 9: 검증**

Run: `flutter analyze`
Expected: `No issues found!`

Run: `flutter test`
Expected: 전체 PASS (기존 212 + 신규 케이스)

- [ ] **Step 10: 커밋·푸시·PR**

```bash
git -C D:/workspace/final-project-syn/synapse-frontend add lib test
git -C D:/workspace/final-project-syn/synapse-frontend commit -m "feat(frontend): 그룹 공유 콘텐츠 섹션 연동 (FE-05)"
git -C D:/workspace/final-project-syn/synapse-frontend push -u origin feat/fe05-group-shared-content
cd D:/workspace/final-project-syn/synapse-frontend
gh pr create --base main --head feat/fe05-group-shared-content \
  --title "feat(frontend): 그룹 공유 콘텐츠 섹션 (FE-05)" \
  --body "engagement-svc의 GET /api/v1/community/groups/{groupId}/shared-content 연동.

- 그룹 상세에 '그룹 공유 콘텐츠' 섹션 추가(목록/빈 상태/403 안내)
- SharedContent에 groupId 파싱, shareContent에 groupId 전달

검증: flutter analyze 무경고, flutter test 전체 통과"
```

**주의:** frontend `main`은 보호 브랜치이며 CODEOWNERS가 PR 작성자 본인이라 정상 승인이 불가능하다. 머지는 `gh pr merge <번호> --squash --admin`을 써야 하고, 레포가 squash-only라 `--merge`/`--rebase`는 실패한다.

---

## Self-Review

**1. Spec coverage**

| Spec 요구 | 담당 |
|---|---|
| `shared_contents.group_id` 컬럼·FK·인덱스, 14자리 버전 | Task 1 Step 6-7 |
| 엔티티 `groupId`·`isGroupScoped()`·fork 미승계 | Task 1 Step 3 |
| 공개 검색에서 그룹 공유 제외 | Task 2 Step 3 |
| 그룹별 조회 쿼리 | Task 2 Step 3 |
| `ACTIVE`만 멤버 인정 | Task 3 Step 5 (`isActiveMember`) |
| 그룹 공유 생성 권한(404/403) | Task 3 Step 5 (`share` → `requireActiveMember`) |
| 토큰 조회·fork 404 은닉 | Task 3 Step 5 (`requireVisibleToViewer`) |
| `ShareContentRequest.groupId` | Task 3 Step 4 |
| `SharedContentResponse.groupId` | Task 4 Step 3 |
| `GET /groups/{groupId}/shared-content` | Task 4 Step 4 |
| 기존 `SharedContentServiceOwnerTests` 회귀 | Task 1 Step 4, Task 3 Step 6 |
| E2E 회귀(Step11/Step13) | Task 4 Step 6 |
| 프론트 API·Provider·화면·테스트 | Task 5 |
| 작업 순서(백엔드 → 프론트) | Task 5 선행 조건 |

누락 없음.

**2. Placeholder scan**

Task 5 Step 2·Step 7은 기존 테스트 파일의 mock 방식에 의존하므로 스텁 코드 대신 "파일 상단에서 이미 쓰는 방식을 따르라"고 지시했다. 이는 해당 파일을 열면 즉시 확인 가능한 구체 지시이며, 검증 명령과 기대 결과가 명시돼 있다. 그 외 TBD/TODO 없음.

**3. Type consistency**

- `SharedContent.create(...)` 8인자 — Task 1에서 정의, Task 2·3 테스트에서 동일하게 사용 ✓
- `SharedContentService` 생성자 3인자 — Task 3에서 정의, 같은 Task Step 6에서 기존 테스트 갱신 ✓
- `findByToken(String, Long)` — Task 3 정의, Task 3 Step 7 컨트롤러에서 사용 ✓
- `searchInGroup(Long, Long, String, ContentType)` — Task 3 정의, Task 4 Step 4 컨트롤러에서 동일 순서로 호출 ✓
- `searchByGroupId(Long, String, ContentType)` — Task 2 정의, Task 3 서비스에서 동일 순서로 호출 ✓
- `CurrentUser.optional(Jwt)` — Task 3 Step 3 정의, Step 7에서 사용 ✓
- 프론트 `getGroupSharedContent(String, {String? keyword, String? contentType})` — Task 5 Step 4 정의, Step 6 provider에서 사용 ✓

모든 시그니처가 정의처와 사용처에서 일치한다.
