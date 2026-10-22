# Progress

## Open Tasks

### Dependency Maintenance: Vite and Security Patch

- [x] Updated Vite from 8.3.4 to 8.3.5 and regenerated the npm lockfile.
- [x] Applied the audited `follow-redirects` 1.16.1 security patch; `npm audit` reports zero vulnerabilities.
- [x] Maven verification passed: production frontend build, TypeScript check, 22-file/97-test Vitest suite, and 333
  backend unit and integration tests.
- Blockers: none.

### Reproducible Frontend Build

- [x] Kept npm install for reliable local Maven builds after npm ci encountered a Windows file lock.
- [x] Added the existing frontend TypeScript check to Maven verification.
- [x] Full Maven verification passed with the TypeScript check, 333 backend tests, and 22 frontend files / 97 tests.
- [x] Restored the unintended QuizFinishedChecker expression exactly and reran verification successfully.
- Resolved: npm ci was not adopted because it requires replacing locally locked files; no user process was terminated.
- Blockers: none.

### Dependency Maintenance

- [x] Audited direct Maven and npm dependencies, their resolved versions, and npm vulnerabilities.
- [x] Updated the Maven-managed Node runtime from v24.21.0 to the current v24.22.0 LTS patch release.
- [x] Updated @playwright/test to 1.64.1, @types/node to the LTS-aligned 24.19.2, Vite to 8.3.4, and Vitest to 5.0.5;
  regenerated the npm lockfile with no audit vulnerabilities.
- [x] Updated the explicit Jackson Databind dependency from 2.21.5 to 2.22.3; controller integration tests cover JSON
  request and response handling.
- [x] Frontend type check, 22-file/97-test Vitest suite, and production build passed.
- [x] Maven verification passed: 321 backend unit and integration tests plus the configured 22-file/97-test frontend
  suite.
- [x] Post-update npm audit passed with zero reported vulnerabilities.
- Blockers: none.

### Punkte-Rangliste Mobile Columns

- [x] Confirmed the Team column receives all width released by smaller Punkte and Teiln. columns.
- [x] Reduced the mobile Punkte column from 64px to 56px and Teiln. from 64px to 48px; retained all `sm:` desktop
  widths.
- [x] Full verification passed: frontend tests (22 files, 97 tests), type check, production build, and Maven verify (321
  backend tests).
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
