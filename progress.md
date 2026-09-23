# Progress

## Open Tasks

### Theme Preview Design

- [x] Explored the shared frontend style system, multi-page Vite entries, public pages, and neutral admin layout.
- [x] Approved scope: an isolated preview system for public pages only; existing and new admin pages remain neutral.
- [x] Approved selection model: all administrators select `Standard`, `November`, `Dezember`, `Jänner`, or `Halloween`
  from a dedicated admin page; the choice remains in that browser tab's `sessionStorage` only.
- [x] Approved behavior: selection does not navigate; the administrator manually opens public pages in the same tab.
- [x] Deferred calendar activation. The future resolver will use Halloween from 24–31 October and preserve the manual
  session preview as the highest-priority override.
- [x] Wrote and self-reviewed `docs/superpowers/specs/2026-09-23-public-theme-preview-design.md`: no placeholders,
  conflicting behavior, or out-of-scope calendar implementation remains.
- [ ] Await user review of the design specification before writing the implementation plan.
- Blockers: none.

### Two-Week Session Timeout

- [x] Confirmed scope: set the global servlet session idle timeout to two weeks for every user; application restarts
  continue to invalidate all in-memory sessions.
- [x] Changed `server.servlet.session.timeout` from `4h` to `14d` in the main application configuration. Spring Boot
  does not support the `w` duration unit, so `14d` is the valid two-week equivalent.
- [x] Initial `./mvnw.cmd verify` correctly exposed the invalid `2w` unit during application-context startup; replaced
  it with the supported `14d` unit before rerunning verification.
- [x] `./mvnw.cmd verify` passed in 35.409 s: 321 Java tests plus the Maven-managed Vitest suite (15 files / 54 tests),
  and application JAR packaging. The Maven build includes the frontend production build; standalone frontend type
  checking is not configured as a Maven phase.
- Blockers: none.

### Default Security Error Handling

- [x] Removed the custom exception-handling block, disconnected 401/403 handlers, their logging helper/request matcher,
  and their unit tests.
- [x] Updated security integration coverage for Spring Security default login redirects and invalid-CSRF 403 responses;
  removed the deleted 403 page markup assertion.
- [x] Removed the custom 403 page, its relogin script/test, Vite build input, and the custom-forwarding integration
  test.
- [x] Updated eight remaining user-controller integration tests from the removed JSON 401 contract to the default
  `/login` redirect contract.
- [x] No removed source references remain. Frontend checks passed: Vitest 5 ran 15 files / 54 tests,
  `npm run type-check` had no diagnostics, and Vite 8.3.0 built 110 modules in 355 ms.
- [x] Final `./mvnw.cmd verify` passed in 40.073 s after unused-import cleanup: 321 Java tests, Maven-managed Vitest 5
  suite (15 files / 54 tests), and application JAR packaging.
- Blockers: none.

### Frontend Unused Dependency Cleanup

- [x] Removed only the confirmed redundant direct dependencies: `autoprefixer`, `playwright`, and `postcss`.
  `@playwright/test` remains available for future browser-test/CLI use.
- [x] npm regenerated the lockfile and reduced resolved dependencies from 167 to 157; `npm audit --json` reports 0
  vulnerabilities.
- [x] Frontend checks passed: Vitest 5 ran 16 files / 56 tests, `npm run type-check` had no diagnostics, and Vite 8.3.0
  built 112 modules in 482 ms.
- [x] `./mvnw.cmd verify` passed in 37.851 s: 339 Java tests, Maven-managed Vitest 5 suite (16 files / 56 tests), and
  application JAR packaging.
- Blockers: none.

### Frontend Package Metadata Cleanup

- [x] Removed unused frontend package metadata: `version`, `description`, `main`, `keywords`, `author`, and `license`.
  Maven remains the sole maintained application-version source.
- [x] Regenerated the lockfile root package metadata with no package `version` or `license`; npm retains its independent
  top-level lockfile format version `1.0.0`.
- [x] `npm audit --json` reports 0 vulnerabilities. Frontend checks passed: Vitest 5 ran 16 files / 56 tests,
  `npm run type-check` had no diagnostics, and Vite 8.3.0 built 112 modules in 387 ms.
- [x] `./mvnw.cmd verify` passed in 32.880 s: 339 Java tests, Maven-managed Vitest 5 suite (16 files / 56 tests), and
  application JAR packaging.
- Blockers: none.

### Maven And Node Patch Migration

- [x] Updated only the Maven Wrapper distribution from 3.9.12 to 3.9.16 and Maven-managed Node from 24.20.0 to 24.21.0.
- [x] Confirmed `./mvnw.cmd --version` uses Apache Maven 3.9.16 and `target/node/node.exe --version` uses Node 24.21.0.
- [x] Frontend checks passed: Vitest 5 ran 16 files / 56 tests; `npm run type-check` had no diagnostics; Vite 8.3.0
  built 112 modules in 507 ms.
- [x] `./mvnw.cmd verify` passed in 56.963 s: 339 Java tests, Maven-managed Vitest 5 suite (16 files / 56 tests), and
  application JAR packaging.
- Blockers: none.

### Vitest 5 Migration

- [x] Updated only the frontend Vitest dependency from 4.1.11 to 5.0.0 and its npm lockfile resolution.
- [x] Vitest 5 suite passed without test changes: 16 test files / 56 tests. The configuration-free setup remains
  unchanged.
- [x] Frontend `npm run type-check` passed with no diagnostics; `npm run build` passed with Vite 8.3.0 (112 modules, 292
  ms).
- [x] `./mvnw.cmd verify` passed in 32.572 s: 339 Java tests plus Maven-managed Vitest 5 suite (16 files / 56 tests);
  the application JAR was built.
- Blockers: none.

### Vite ESM Configuration Cleanup

- [x] Set the frontend package to ESM so Vite's future native config loader can load `vite.config.ts` without warning.
- [x] Frontend tests passed: 16 files / 56 tests; type-check and production build passed without the Vite warning.
- [x] Maven verification passed: 339 backend tests, 16 frontend test files / 56 tests, and the application JAR build.

### TypeScript 7 Migration Planning

- [x] Inspected the clean TypeScript 6.0.3 baseline, compiler configuration, and TypeScript 7 migration requirements.
- [x] Agreed scope: TypeScript 7 only; remove obsolete `ignoreDeprecations` and `baseUrl`, make only confirmed
  type-error fixes.
- [x] Wrote and reviewed the implementation plan before migration work begins.
- [x] Task 1: Updated only the TypeScript manifest and lockfile entry to 7.0.2 with
  `npm install --save-dev typescript@7.0.2 --package-lock-only`.
- [x] Task 1: Removed only `ignoreDeprecations` and `baseUrl` from the TypeScript configuration; retained the relative
  `@/*` path alias.
- [x] Task 1: Installed the locked TypeScript 7.0.2 compiler with `npm install --ignore-scripts`. Root cause:
  `--package-lock-only` deliberately leaves `node_modules` at 6.0.3, so the first type-check did not validate TypeScript
  7.
- [x] Task 1: Ran `npm run type-check` with installed TypeScript 7.0.2; exact result: `> pub-quizzz@1.0.0 type-check` /
  `> tsc --noEmit`, exit code 0 with no diagnostics. No tests or production build run by scope.
- [x] Task 1 self-review: `npm ls typescript --depth=0` reports `typescript@7.0.2`; scope is limited to the frontend
  package/lockfile, the two requested tsconfig entries, and progress tracking.
- [x] Task 3: Frontend `npm run test` passed: 16 test files / 56 tests, exit code 0. `npm run type-check` passed with
  exit code 0 and no TypeScript diagnostics. `npm run build` passed: Vite 8.3.0 transformed 112 modules and completed in
  278 ms, with no warnings.
- [x] Task 3: Root `.\mvnw.cmd verify` passed with `BUILD SUCCESS` in 42.965 s: 339 backend tests (0 failures, 0 errors,
  0 skipped) and its embedded frontend test run (16 test files / 56 tests) passed; the application JAR was built.
- [x] Task 3 warnings: Maven emitted the JDK warning that Byte Buddy dynamically loaded a Java agent and this will be
  disallowed by default in a future release. The Maven log also contains expected WARN/ERROR application logging from
  security, validation, and backup/restore negative-path tests; these test scenarios passed and produced no test
  failures.
- Blockers: none.

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
