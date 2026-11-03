# Progress

## Open Tasks

### Average Leaderboard Empty State

- [x] Show a separate German message card instead of the average leaderboard table when no teams qualify; restore the
  table when results are available. Reset the card and loading/error state on period changes.
- [x] Add frontend behavior coverage for switching between empty and populated periods and loading errors, plus coverage
  for the average page's empty-state wiring and message.
- [x] Run full Maven verification, including frontend type checking: 340 backend tests and 123 frontend tests passed.

### Remove Average Leaderboard Yearly Fallback

- [x] Restore the two-participation minimum for every year and the overall ranking, and remove the exception from the
  explanation.
- [x] Update unit, API integration, team statistics, and explanatory text tests to cover the restored rule, including
  years with no qualifying teams.
- [x] Run full Maven verification, including frontend type checking: 340 backend tests and 121 frontend tests passed.

### Average Leaderboard Yearly Fallback

- [x] Update yearly qualification to allow one participation only when no team reaches two that year; preserve the
  overall minimum. Updated the leaderboard explanation as well.
- [x] Update unit, API integration, team statistics, and explanatory text coverage for fallback and normal cases.
- [x] Run full Maven verification, including frontend type checking: 340 backend tests and 121 frontend tests passed.

### Hide Missing Average Team Placement

- [x] Update team statistics rendering to hide the average card when no placement exists, independently per period, and
  adapt the grid.
- [x] Update frontend behavior tests for missing and qualifying yearly and overall placements.
- [x] Run full Maven verification, including frontend type checking: 339 backend tests and 121 frontend tests passed.

### Average Leaderboard Qualification

- [x] Inspect average leaderboard calculation, API coverage, and CI verification commands: the shared repository counts
  distinct quizzes per selected period; Maven verify includes backend tests, frontend tests, and type checking.
- [x] Require at least two participations within the selected year or across all years; add unit and integration
  coverage. Added a German explanation to the leaderboard and markup coverage.
- [x] Run the complete CI verification suite, including frontend type checking: `mvnw.cmd verify` passed with 339
  backend tests, 120 frontend tests, and TypeScript checking.

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

### Average Leaderboard Empty State ✅ COMPLETE

- Show a separate explanatory card without table headings when no teams meet the participation minimum.
- Covered period changes and loading errors; full Maven verification passed with 340 backend tests, 123 frontend tests,
  and TypeScript checking.

### Remove Average Leaderboard Yearly Fallback ✅ COMPLETE

- Restored the two-participation minimum for every year and the overall ranking, including when no team qualifies.
- Full Maven verification passed with 340 backend tests, 121 frontend tests, and TypeScript checking.

### Average Leaderboard Yearly Fallback ✅ COMPLETE

- Previously allowed one participation in a year when no team had two; this exception has now been removed.
- Updated leaderboard explanation, unit tests, API integration tests, and team statistics expectations.
- Full Maven verification passed with 340 backend tests, 121 frontend tests, and TypeScript checking.

Older finished phases are tracked in `progress_archive.md`.
