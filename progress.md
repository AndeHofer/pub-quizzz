# Progress

## Open Tasks

None currently open.

## Finished Phases

### Phase 134: ResultService Leaderboard Split ✅ COMPLETE

- Step 1 done: Confirmed clean baseline (`./mvnw.cmd verify` green) before refactor.
- Step 2 done: Added `RankingUtils` (shared tie-break comparator: total points DESC, fives DESC, threes DESC)
  with dedicated unit tests.
- Step 3 done: Added `QuizTitleFormatter` (shared "YYYY Month" title formatting) with dedicated unit tests.
- Step 4 done: Rewired `ResultService` to use `RankingUtils`/`QuizTitleFormatter`, removed the 4 leaderboard
  methods + `getLeaderboardYears()` (moved to new `LeaderboardService`).
- Step 5 done: Created `LeaderboardService` (points/average/medal/top-results leaderboards + years), moved
  `ResultServiceLeaderboardTest` content into `LeaderboardServiceTest`.
- Step 6 done: Rewired `UserLeaderboardController` to `LeaderboardService`; updated its test's `@MockitoBean`.
- Step 7 done: Split `CacheAnnotationsTest` so leaderboard `@Cacheable` methods are asserted on
  `LeaderboardService`, CRUD/quiz/team methods remain asserted on `ResultService`.
- Step 8 done: Full verification passed.
  - `npm --prefix src/main/webapp run test`
  - `npm --prefix src/main/webapp run type-check`
  - `npm --prefix src/main/webapp run build`
  - `./mvnw.cmd verify`

### Phase 133: Leaderboard Tab Visibility Bugfix ✅ COMPLETE

- Step 1 done: Investigated why no tabs were visible on leaderboard pages.
  - Shared JS year-tab logic is present and built.
  - Root cause confirmed: leaderboard HTML pages are currently missing the `leaderboardYearTabs` container element.
- Step 2 done: Added focused frontend tests for container-backed rendering and the new rule to hide tabs unless at least
  2 years exist.
- Step 3 done: Added the missing tab container to the four leaderboard pages and suppressed rendering for 0-1 years.
- Step 4 done: Focused frontend tests and full project verification passed.
  - `npm --prefix src/main/webapp run test -- leaderboard-page.test.ts leaderboard-pages-markup.test.ts`
  - `npm --prefix src/main/webapp run test`
  - `npm --prefix src/main/webapp run type-check`
  - `npm --prefix src/main/webapp run build`
  - `./mvnw.cmd verify`

### Phase 132: Leaderboard Year Tabs ✅ COMPLETE

- Step 1 done: Added backend controller/service tests first for optional `year` leaderboard queries and
  `/api/leaderboard/years`.
- Step 2 done: Implemented year-aware repository queries, service caching keys, and controller endpoints.
- Step 3 done: Added integration tests for all four leaderboard endpoints with year-specific recalculation.
- Step 4 done: Added frontend tests for dynamic year tabs shared by the four leaderboard pages.
- Step 5 done: Implemented shared year tabs UI and year-aware refetch logic for the four leaderboard pages only.
- Step 6 done: Full verification passed.
  - `npm --prefix src/main/webapp run test`
  - `npm --prefix src/main/webapp run type-check`
  - `npm --prefix src/main/webapp run build`
  - `./mvnw.cmd verify`

Older finished phases have been moved to `progress_archive.md` to keep this file short.
