# Progress

## Open Tasks

None currently open.

## Finished Phases

### Phase 135: Leaderboard Table 20px Gap Fix ✅ COMPLETE

- Root cause: global `table { mt-5 }` rule in `src/main/webapp/src/css/styles.css:122-124` adds a
  20px top margin to every `<table>` site-wide, creating an oversized/inconsistent gap above the
  leaderboard tables once loaded (they already sit below the year-tabs wrapper's own spacing).
- Added a markup regression test asserting all four leaderboard tables carry an `mt-0` override
  (`leaderboard-pages-markup.test.ts`), verified RED first.
- Added `mt-0` to `<table id="leaderboardTable">` on all four leaderboard pages (points/average/medal/
  top-results), scoped fix — other pages using the global `table` style keep their existing spacing.
- Verification passed:
  - `npm --prefix src/main/webapp run test`
  - `npm --prefix src/main/webapp run type-check`
  - `npm --prefix src/main/webapp run build`
  - `./mvnw.cmd verify`

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

Older finished phases have been moved to `progress_archive.md` to keep this file short.
