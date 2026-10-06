# Progress

## Open Tasks

### Team Results Mobile Table

- [x] Traced the mobile overflow to Team results using a 320px minimum-width table inside an overflow container.
- [x] Added regression coverage for the fixed mobile table and blank action header; it failed as expected before the
  markup change.
- [x] Removed the Team results overflow fallback and minimum width; uses fixed columns for points and rank only.
- [x] Full verification passed: frontend tests (24 files, 120 tests), type check, production build, and Maven verify
  (321 backend tests).
- Blockers: none.

### Quiz Title Link Weight

- [x] Identified the only relevant regular-weight content links: quiz titles in the Top 10 leaderboard and team detail
  results.
- [x] Added regression coverage requiring both quiz title links to use `font-medium`; it failed as expected before the
  markup change.
- [x] Applied `font-medium` to both quiz title links and confirmed the focused frontend tests pass.
- [x] Full verification passed: frontend tests (24 files, 119 tests), type check, production build, and Maven verify
  (321 backend tests).
- Blockers: none.

### Top 10 Desktop Width Alignment

- [x] Changed the Top 10 results desktop container from `sm:max-w-4xl` to `sm:max-w-3xl`, matching the other
  leaderboard pages.
- [x] Changed the Top 10 results mobile table minimum width from 720px to 420px.
- [x] Made all four leaderboard tables fixed-width within mobile viewports, removed horizontal-scroll/minimum-width
  fallbacks, compacted mobile padding, and wrapped long team names. Simplified Top 10 to four columns and renders its
  quiz rank beneath the quiz title.
- [x] Widened the points and medal numeric columns on mobile so the team column does not dominate the table.
- [x] Widened desktop numeric leaderboard columns so their German headers remain centered without changing mobile
  widths.
- [x] Rebalanced the desktop Top 10 columns: widened team and points while constraining quiz titles.
- [x] Rebalanced mobile leaderboard columns to favor team names while retaining centered numeric headings.
- [x] Made the Quiz Ergebnisse table fit mobile viewports while reserving more space for winner names.
- [x] Renamed the quiz-result points heading and sized its column for centered mobile and desktop display.
- [x] Replaced shared result-detail text toggles with accessible chevrons and fit the quiz summary table on mobile.
- [x] Reduced the shared result-detail chevron to a subtle inline control.
- [x] Removed tooltip and accessible-label attributes from the compact result-detail toggle.
- [x] Applied the existing icon-button reset to the shared result-detail chevron.
- [x] Removed the shared detail-toggle hover background and used equal-size state glyphs.
- [x] Centered the shared detail-toggle glyph and suppressed the global hover background on touch devices.
- [x] Replaced the shared detail-toggle triangles with stable plus/minus symbols.
- [x] Aligned mobile team-name text sizing on quiz result pages with the leaderboards.
- [x] Reduced the mobile quiz-list Quiz column and aligned its title text size with team names.
- [x] Aligned the team-results quiz-title mobile text size with the other public pages.
- [x] Widened the mobile quiz-list Quiz column so month titles remain on one line.
- [x] Removed the unused quiz rank from the Top 10 UI, API, calculation, and tests.
- [x] Ran the full frontend and Maven verification suite after removing the Top 10 quiz rank.
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
