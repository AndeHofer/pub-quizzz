# Progress

## Open Tasks

### Previous Quiz Navigation

- [x] Add the right-side navigation link to the public quiz-results page.
- [x] Select the chronologically previous finished quiz and wrap from the oldest to the newest quiz.
- [x] Add unit and markup tests, then run the full CI verification suite.

### Preserve Leaderboard Year in Navigation

- [x] Inspect the failing frontend tests caused by the new next-leaderboard link.
- [x] Update affected test expectations and add coverage for retaining the selected year.
- [x] Run the complete CI verification suite, including frontend type checking.

### Docker Build Verification

- [ ] Run `docker build --tag pub-quizzz:toolchain-update .` in an environment with Docker installed. Blocked here:
  Docker is not installed or available on `PATH`.

## Finished Phases

### Team Leaderboard Statistics ✅ COMPLETE

- Added a team statistics API reusing existing rankings; participation years appear newest first and `Alle` follows when
  applicable.
- Displayed points, average, medal, and all qualifying Top 10 placements above participations, with separate error
  handling.
- Added unit, API integration, and frontend rendering coverage; browser checks passed at mobile and desktop widths.
- Full Maven verification passed with 333 backend tests, 113 frontend tests, and TypeScript checking.

### Participating Teams Directory ✅ COMPLETE

- Added a home-page link and alphabetically sorted team directory for teams with recorded participations.
- Added service unit, API integration, and frontend behavior tests; verified navigation and states on mobile and
  desktop.
- Full Maven verification passed with 337 backend tests, 118 frontend tests, and TypeScript checking.

### Hide Missing Team Statistics ✅ COMPLETE

- Hide medal and Top 10 sections independently for each year or overall period when no qualifying placement exists.
- Updated behavior coverage and verified mobile and desktop layouts in Microsoft Edge.
- Full Maven verification passed with 337 backend tests, 119 frontend tests, and TypeScript checking.

Older finished phases are tracked in `progress_archive.md`.
