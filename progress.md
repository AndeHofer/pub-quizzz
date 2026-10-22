# Progress

## Open Tasks

### Remove Full-Page Transitions

- [x] Located all 19 public and admin page-script references and the shared opacity rules.
- [x] Removed the transition module, all 19 page-script references, and the obsolete body-opacity rules.
- [x] Confirmed no transition references remain in the frontend source.
- [x] Full Maven verification passed: frontend production build, TypeScript check, 22 Vitest files / 97 tests, and 333
  backend tests.
- [x] Confirmed the generated static frontend output contains no transition code or transition classes.
- [x] No backend business logic or APIs changed, so no additional unit or integration tests were needed.
- Blockers: none.

## Finished Phases

### Phase 135: Leaderboard Table 20px Gap Fix ✅ COMPLETE

- Scoped `mt-0` override for leaderboard tables to neutralize global table top margin.
- Added regression coverage in `leaderboard-pages-markup.test.ts`.
- Full verification passed (`npm run test`, `npm run type-check`, `npm run build`, `./mvnw.cmd verify`).

### Phase 134: ResultService Leaderboard Split ✅ COMPLETE

- Extracted shared ranking and title formatting logic into `RankingUtils` and `QuizTitleFormatter`.
- Moved leaderboard responsibilities to `LeaderboardService` and rewired controller/tests.
- Full verification passed (`npm run test`, `npm run type-check`, `npm run build`, `./mvnw.cmd verify`).

### Phase 133: Leaderboard Tab Visibility Bugfix ✅ COMPLETE

- Added missing `leaderboardYearTabs` containers and hide rule for 0-1 years.
- Added focused frontend tests and retained full verification coverage.
- Full verification passed (`npm run test`, `npm run type-check`, `npm run build`, `./mvnw.cmd verify`).

Older finished phases are tracked in `progress_archive.md`.
