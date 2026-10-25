# Progress

## Open Tasks

None.

## Finished Phases

### Public Page Mobile Padding ✅ COMPLETE

- Changed all 10 public outer containers from `px-3` to `px-2` on mobile while preserving `sm:px-4`.
- Left admin-page spacing unchanged.
- Full Maven verification passed: production build, TypeScript check, 22 frontend files / 97 tests, and 333 backend
  tests.

### Phase 135: Leaderboard Table 20px Gap Fix ✅ COMPLETE

- Scoped `mt-0` override for leaderboard tables to neutralize global table top margin.
- Added regression coverage in `leaderboard-pages-markup.test.ts`.
- Full verification passed (`npm run test`, `npm run type-check`, `npm run build`, `./mvnw.cmd verify`).

### Phase 134: ResultService Leaderboard Split ✅ COMPLETE

- Extracted shared ranking and title formatting logic into `RankingUtils` and `QuizTitleFormatter`.
- Moved leaderboard responsibilities to `LeaderboardService` and rewired controller/tests.
- Full verification passed (`npm run test`, `npm run type-check`, `npm run build`, `./mvnw.cmd verify`).

Older finished phases are tracked in `progress_archive.md`.
