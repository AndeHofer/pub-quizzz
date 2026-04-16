# Progress

## Open Tasks

- [x] Phase 40: Redo Phase 35 width choice from `sm:max-w-4xl` to `sm:max-w-3xl` on public pages
- [x] Phase 40: Keep public page container widths consistent after switching to `sm:max-w-3xl`
- [x] Phase 40: Run full verification (`npm run type-check`, `npm run build`, `mvn.cmd test`)
- [x] Phase 36: Audit public pages and TS renderers for blue non-link text
- [x] Phase 36: Keep blue styling only on links; switch non-link blue text to neutral black/gray
- [x] Phase 36: Run full verification (`npm run type-check`, `npm run build`, `mvn.cmd test`)
- [x] Phase 37: Keep progress naming aligned so each new task is a phase (no follow-up labels)
- [x] Phase 37: Keep `progress.md` at or below 50 lines and move older finished entries to `progress_archive.md`
- [x] Phase 38: Archive removed detailed history from `progress.md` into `progress_archive.md`
- [x] Phase 39: Rework `progress_archive.md` to match `progress.md` style with concise phase sections
- [x] Blockers: none

## Finished Phases

### Phase 36: Public Blue Text Cleanup ✅ COMPLETE

- Audited listed public pages and dynamic renderers for `text-blue-*` usage.
- Kept blue only on actual links and removed misleading blue from non-link numeric values.
- Updated non-link score/average cells to neutral text in:
  - `src/main/webapp/src/js/quiz.ts`
  - `src/main/webapp/src/js/team.ts`
  - `src/main/webapp/src/js/points-leaderboard.ts`
  - `src/main/webapp/src/js/average-leaderboard.ts`
- Verification passed: `npm run type-check`, `npm run build`, `mvn.cmd test`.

### Phase 35: Public Desktop Width Consistency ✅ COMPLETE

- Standardized public page desktop container widths to `sm:max-w-4xl`.
- Updated pages:
  - `src/main/webapp/src/index.html`
  - `src/main/webapp/src/quizzes.html`
  - `src/main/webapp/src/quiz.html`
  - `src/main/webapp/src/team.html`
  - `src/main/webapp/src/points-leaderboard.html`
  - `src/main/webapp/src/medal-leaderboard.html`
  - `src/main/webapp/src/average-leaderboard.html`
  - `src/main/webapp/src/rules.html`
- `src/main/webapp/src/quiz-details.html` already used `sm:max-w-4xl`.
- Verification passed: `npm run type-check`, `npm run build`, `mvn.cmd test`.

### Phase 34: Quiz-Details Enhancements ✅ COMPLETE

- Added public quiz-details page and endpoint `GET /api/quizzes/{quizId}/detail`.
- Implemented reveal UX updates, visibility rules, and per-question `Punkte pro Team` modal.
- Refined quiz title handling (derived from `pubDate`) and related frontend/admin/API flows.
- Applied multiple UI/UX consistency refinements and full verification after each increment.
