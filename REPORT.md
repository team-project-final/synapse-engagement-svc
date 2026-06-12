# 작업 보고서: synapse-engagement-svc

---

## 2026-06-12 — Step 6: 레벨/배지/스트릭/리더보드 시스템 구현

### 변경 요약

| 항목 | 이전 상태 | 변경 후 |
|------|-----------|---------|
| 레벨 시스템 | 없음 | `level_definitions` 테이블(V5) + `LevelService` (XP → 자동 레벨업) |
| 배지 시스템 | 없음 | `badges`·`user_badges` 테이블(V6·V7) + `BadgeService` (criteria_json 평가) |
| 스트릭 추적 | 없음 | `last_activity_date` 컬럼(V8) + `StreakService` (KST 기준 연속 일수) |
| 리더보드 | 없음 | `LeaderboardService` (Redis ZSet → DB 폴백) |
| `GamificationService.addXp()` | XP 저장만 | 스트릭→레벨→배지→리더보드 순서로 갱신 |
| `GET /gamification/leaderboard` | 없음 | 신규 엔드포인트 (scope, limit 파라미터) |
| `UserXpResponse.recentBadges` | `List<String>` | `List<BadgeResponse>` (code, name, earnedAt) |
| ObjectMapper Bean | 자동 노출 실패 | `JacksonConfig`에 `@Bean` 명시 등록 |
| Redis 설정 | 없음 | `RedisTemplate<String, String>` Bean 추가 |

### 기술적 근거

- **criteria_json 방식**: 배지 조건을 코드가 아닌 DB 데이터(`criteria_json`)로 관리해 배지 추가 시 마이그레이션만 필요, 코드 변경 불필요.
- **Redis ZSet 폴백 구조**: 리더보드 조회는 Redis 우선, 예외 시 DB(`findAllByOrderByTotalXpDesc`) 폴백. Redis가 없는 로컬 환경에서도 동작 보장.
- **JacksonConfig 분리**: Spring Boot 4.0 + Spring Modulith에서 `ObjectMapper`가 모듈 경계 밖으로 자동 노출되지 않는 문제를 `global/config/JacksonConfig`로 해소.
- **updateStreak KST 기준**: `ZoneId.of("Asia/Seoul")` — `LocalDate.now(KST)`로 자정 기준 계산, UTC 혼용 방지.

### 테스트 현황

| 테스트 클래스 | 건수 | 결과 |
|--------------|------|------|
| LevelServiceTest | 3 | PASS |
| BadgeServiceTest | 4 | PASS |
| StreakServiceTest | 5 | PASS |
| GamificationServiceTest | 2 | PASS |
| GamificationControllerWebMvcTest | 4 | PASS |
| GamificationControllerIntegrationTest | 3 | PASS |
| **전체** | **21** | **BUILD SUCCESSFUL** |

### PM 문서 갱신

- `TASK_engagement.md`: Steps 1-5 Status `[x] Done`, Step 6 Done When 전체 `[x]`, Step 6 Status `[x] Done`
- `HISTORY_engagement.md`: 대시보드 전체 갱신, 2026-06-12 Step 6 작업 로그 기록, 변경 이력 추가
- `WORKFLOW_engagement_W3.md`: Step 6 모든 항목 `[x]`, Step 6 Status `[x] Done`
- `PRD_W3.md`: FR-EG-201~204 ✅, 성공 기준 gamification 완성 `[x]`
- `SCOPE_engagement.md`: 그룹 CRUD, 공유 플로우, XP 레벨업, 배지·리더보드 성공 기준 `[x]`
