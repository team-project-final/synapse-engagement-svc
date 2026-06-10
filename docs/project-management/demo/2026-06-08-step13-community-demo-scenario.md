# 2026-06-08 Step 13 커뮤니티 발표용 데모 시나리오

## 목적

Step 13은 발표 전 커뮤니티 최종 플로우를 검증한다.

- 덱 또는 노트 공유
- 공유 콘텐츠 검색
- 공유 덱 복사
- 부적절한 공유 콘텐츠 신고
- 관리자 신고 처리
- 모더레이션 알림 명령 발행

이 문서는 Step 13 발표/데모 산출물이다. 13.1에서 체크한 시나리오와 데모 데이터를 테스트 코드 안에만 남기지 않고, 발표자가 그대로 따라갈 수 있도록 명시한다.

## 데모 사용자

| 사용자 | JWT subject | 역할 | 용도 |
|---|---:|---|---|
| 덱 소유자 | `11100` | USER | 원본 공유 덱 생성 |
| 덱 복사 사용자 | `11101` | USER | 공유 덱 복사 |
| 덱 신고자 | `11102` | USER | 복사된 덱 신고 |
| 노트 소유자 | `13100` | USER | 공유 노트 생성 |
| 노트 신고자 | `13101` | USER | 공유 노트 신고 |
| 관리자 | `11900` / `13900` | ADMIN | 신고 조회 및 승인 처리 |

## 데모 데이터

| 흐름 | 필드 | 값 |
|---|---|---|
| 덱 공유 | `contentType` | `DECK` |
| 덱 공유 | `contentId` | `91001` |
| 덱 공유 | `title` | `Step11 Deck Alpha` |
| 덱 공유 | `description` | `Step11 searchable deck` |
| 덱 공유 | `tags` | `["step11", "deck"]` |
| 노트 공유 | `contentType` | `NOTE` |
| 노트 공유 | `contentId` | `93001` |
| 노트 공유 | `title` | `Step13 Note Alpha` |
| 노트 공유 | `description` | `Step13 searchable note` |
| 노트 공유 | `tags` | `["step13", "note"]` |
| 알림 토픽 | Kafka topic | `platform.notification.notification-send-v1` |

## 시나리오 A: 공유 덱 검색 및 복사

1. 덱 소유자가 공유 덱을 생성한다.

```http
POST /api/v1/community/share
Authorization: Bearer <subject=11100>
Content-Type: application/json

{
  "contentType": "DECK",
  "contentId": 91001,
  "title": "Step11 Deck Alpha",
  "description": "Step11 searchable deck",
  "tags": ["step11", "deck"]
}
```

기대 결과:

- 응답 상태는 `201 Created`
- 응답에 비어 있지 않은 `shareToken` 포함

2. 발표자가 공유 토큰으로 상세를 조회한다.

```http
GET /api/v1/community/share/{shareToken}
```

기대 결과:

- `ownerId = 11100`
- `title = Step11 Deck Alpha`
- `tags`에 `step11` 포함

3. 발표자가 공유 덱을 검색한다.

```http
GET /api/v1/community/search?q=Step11&contentType=DECK
```

기대 결과:

- 결과에 `Step11 Deck Alpha` 포함

4. 덱 복사 사용자가 공유 덱을 복사한다.

```http
POST /api/v1/community/share/{shareToken}/fork
Authorization: Bearer <subject=11101>
```

기대 결과:

- 응답 상태는 `201 Created`
- 복사본의 `ownerId = 11101`
- 복사본의 `sourceShareId`가 원본 공유 콘텐츠를 가리킴
- 원본 공유 콘텐츠의 `downloadCount`가 `1`로 증가

## 시나리오 B: 공유 덱 신고 및 관리자 처리

1. 신고자가 복사된 덱을 신고한다.

```http
POST /api/v1/community/reports
Authorization: Bearer <subject=11102>
Content-Type: application/json

{
  "targetType": "SHARED_DECK",
  "targetId": <forkedShareId>,
  "reason": "Step11 moderation target"
}
```

기대 결과:

- 응답 상태는 `201 Created`
- 응답의 `status = PENDING`
- 응답에 `reporterId`가 노출되지 않음

2. 같은 신고자가 동일 대상을 다시 신고한다.

기대 결과:

- 응답 상태는 `409 Conflict`

3. 관리자가 아닌 사용자가 신고 목록을 조회한다.

```http
GET /api/v1/admin/reports
Authorization: Bearer <subject=11102>
```

기대 결과:

- 응답 상태는 `403 Forbidden`

4. 관리자가 신고를 승인한다.

```http
PATCH /api/v1/admin/reports/{reportId}
Authorization: Bearer <subject=11900, role=ADMIN>
Content-Type: application/json

{
  "status": "APPROVED",
  "adminNote": "Step11 hidden by moderation"
}
```

기대 결과:

- 응답 상태는 `200 OK`
- 신고 상태가 `APPROVED`로 변경
- 복사된 덱 토큰 조회는 `404 Not Found`
- 원본 덱 토큰 조회는 계속 `200 OK`

## 시나리오 C: 공유 노트 신고 및 알림 계약

1. 노트 소유자가 공유 노트를 생성한다.

```http
POST /api/v1/community/share
Authorization: Bearer <subject=13100>
Content-Type: application/json

{
  "contentType": "NOTE",
  "contentId": 93001,
  "title": "Step13 Note Alpha",
  "description": "Step13 searchable note",
  "tags": ["step13", "note"]
}
```

기대 결과:

- 응답 상태는 `201 Created`
- 응답에 비어 있지 않은 `shareToken` 포함

2. 발표자가 노트를 조회하고 검색한다.

```http
GET /api/v1/community/share/{noteShareToken}
GET /api/v1/community/search?q=Step13&contentType=NOTE
```

기대 결과:

- 상세 응답의 `ownerId = 13100`
- 검색 결과에 `Step13 Note Alpha` 포함

3. 노트 신고자가 노트를 신고하고, 관리자가 승인한다.

```http
POST /api/v1/community/reports
Authorization: Bearer <subject=13101>

{
  "targetType": "SHARED_NOTE",
  "targetId": <noteShareId>,
  "reason": "Step13 note moderation target"
}
```

```http
PATCH /api/v1/admin/reports/{reportId}
Authorization: Bearer <subject=13900, role=ADMIN>

{
  "status": "APPROVED",
  "adminNote": "Step13 note hidden by moderation"
}
```

기대 결과:

- 승인 후 노트 토큰 조회는 `404 Not Found`
- `platform.notification.notification-send-v1`에 알림 명령 발행
  - 신고자 `13101`: `REPORT_RESOLVED`
  - 소유자 `13100`: `CONTENT_REMOVED`

## 검증

아래 명령으로 검증한다.

```powershell
.\gradlew.bat test --tests "com.synapse.engagement.community.CommunityStep11E2ETests"
.\gradlew.bat test --tests "com.synapse.engagement.community.CommunityStep13FinalE2ETests"
```

현재 검증 결과:

- 공유 덱 생성/검색/복사/신고/관리자 처리: `CommunityStep11E2ETests`에서 검증
- 공유 노트 생성/검색/신고/관리자 처리/notification-send 계약: `CommunityStep13FinalE2ETests`에서 검증

## 알려진 제한 사항

로컬 Step 13 데모는 engagement-svc 동작과 Avro 알림 명령 형태를 검증한다. 실제 notification-svc 라이브 소비는 W5 통합/배포 검증 항목으로 분리한다. 현재 platform notification은 UUID 형태의 userId를 기대하므로, Long 기반 userId를 그대로 보내는 라이브 연동은 별도 식별자 모델 정리가 필요하다.
