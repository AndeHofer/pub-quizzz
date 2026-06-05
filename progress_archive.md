# Progress Archive

Archived phases moved out of `progress.md` to keep active progress short and focused.

## Archived Phases

### Phase 89: Neuigkeiten Frontend Quality Hardening + Shared Admin UI Helpers ✅ COMPLETE

- Extracted shared admin UI primitives into `src/main/webapp/src/js/admin_ui.ts` (`showModal`, `showError`,
  `showLoading`, `trustedHtml`, `renderTable`) and reused them from both `admin_functions.ts` and `admin_news.ts` to
  reduce duplicated modal/table helper logic.
- Fixed trusted action markup in `src/main/webapp/src/js/admin_functions.ts` to use escaped `safeQuizId` in quiz action
  button `data-id` attributes.
- Hardened admin list error handling in `src/main/webapp/src/js/admin_functions.ts` by checking `response.ok` before
  JSON parsing for quizzes/teams/users and surfacing meaningful German status/body errors.
- Expanded `src/main/webapp/src/js/admin_news.test.ts` with unhappy-path coverage for create/update/delete plus
  prompt/confirm helper behavior.
- Expanded `src/main/webapp/src/js/news.test.ts` with invalid-date fallback, newline-to-`<br>` rendering, and
  same-timestamp tie-break sorting assertions.
- Final frontend alignment fixes: neutral news-load fallback class in `src/main/webapp/src/js/news.ts`, non-duplicated
  admin load error messaging in `src/main/webapp/src/js/admin_news.ts`, and German `Admin-Bereich` label in
  `src/main/webapp/src/index.html`.
- Verification passed: `npm run test -- src/js/news.test.ts src/js/admin_news.test.ts`, `npm run type-check`,
  `npm run test`, `npm run build`, and `./mvnw.cmd test`.

### Phase 88: Neuigkeiten Frontend Spec-Compliance Refactor (Button-Driven Admin Flow) ✅ COMPLETE

- Refactored public news rendering in `src/main/webapp/src/js/news.ts` to enforce newest-first ordering and max 3 items
  via `sortAndLimitNews(...)` before markup generation.
- Reworked `src/main/webapp/src/admin/admin_main.html` Neuigkeiten block to explicit admin actions (`createNewsBtn`,
  `viewNewsBtn`) matching approved button-driven flow.
- Reworked `src/main/webapp/src/js/admin_news.ts` to modal/table management style (load, create, edit, delete via
  `/admin/news`) aligned with existing admin UX patterns.
- Updated `src/main/webapp/src/js/admin_functions.ts` to initialize Neuigkeiten action wiring through
  `initAdminNewsActions()`.
- Added/updated focused frontend tests in `src/main/webapp/src/js/news.test.ts` and
  `src/main/webapp/src/js/admin_news.test.ts` for sort/limit, escaping, and helper/action request behavior.
- Verification passed: `npm run test -- src/js/news.test.ts src/js/admin_news.test.ts` and `npm run type-check`.

### Phase 84: Neuigkeiten End-to-End (Backend + Frontend + Tests) ✅ COMPLETE

- Added backend Neuigkeiten vertical slice: `src/main/java/com/ande/pubquizzz/database/entities/News.java`,
  `src/main/java/com/ande/pubquizzz/database/repositories/NewsRepository.java`,
  `src/main/java/com/ande/pubquizzz/service/NewsService.java`,
  `src/main/java/com/ande/pubquizzz/controller/UserNewsController.java`,
  `src/main/java/com/ande/pubquizzz/controller/AdminNewsController.java`, and DTOs
  `src/main/java/com/ande/pubquizzz/dto/NewsDTO.java`, `src/main/java/com/ande/pubquizzz/dto/CreateNewsRequest.java`,
  `src/main/java/com/ande/pubquizzz/dto/UpdateNewsRequest.java`.
- Implemented authenticated user read endpoint (`GET /api/news`) with newest-first and max/default 3 behavior;
  implemented admin CRUD endpoints (`GET/POST/PUT/DELETE /admin/news`) with validation and admin-only authorization.
- Added/expanded backend tests: `src/test/java/com/ande/pubquizzz/service/NewsServiceTest.java`,
  `src/test/java/com/ande/pubquizzz/controller/AdminNewsControllerTest.java`,
  `src/test/java/com/ande/pubquizzz/controller/UserNewsControllerTest.java`.
- Added homepage Neuigkeiten section in `src/main/webapp/src/index.html` and loading/render integration in
  `src/main/webapp/src/js/index.ts` via `/api/news?limit=3`.
- Added frontend news/admin modules and tests: `src/main/webapp/src/js/news.ts`, `src/main/webapp/src/js/admin_news.ts`,
  `src/main/webapp/src/js/news.test.ts`, `src/main/webapp/src/js/admin_news.test.ts`, plus `NewsDTO` in
  `src/main/webapp/src/js/types.ts`.
- Verification passed: `npm run test`, `npm run type-check`, `npm run build` (in `src/main/webapp`) and
  `./mvnw.cmd test`.

### Phase 86: Neuigkeiten Backend Quality Fixes (Repository List + Safe Delete + 201 Create) ✅ COMPLETE

- Updated `src/main/java/com/ande/pubquizzz/database/repositories/NewsRepository.java` with a dedicated full-list method
  `findAllByOrderByCreatedAtDescNewsIdDesc()` and removed the now-unneeded custom `@Query` import/annotation.
- Updated `src/main/java/com/ande/pubquizzz/service/NewsService.java` so admin listing uses the new repository method
  instead of `PageRequest.of(0, Integer.MAX_VALUE)`.
- Updated `src/main/java/com/ande/pubquizzz/service/NewsService.java` delete flow to load via `findById` (or throw) and
  then `delete(entity)`, removing the `existsById` + `deleteById` race window.
- Updated `src/main/java/com/ande/pubquizzz/controller/AdminNewsController.java` create endpoint to return HTTP
  `201 Created` (`ResponseEntity.status(HttpStatus.CREATED)`), not `200 OK`.
- Extended tests in `src/test/java/com/ande/pubquizzz/service/NewsServiceTest.java` for admin full-list path and safe
  delete behavior, and in `src/test/java/com/ande/pubquizzz/controller/AdminNewsControllerTest.java` for create `201`
  status.
- Verification passed: `./mvnw.cmd "-Dtest=NewsServiceTest,AdminNewsControllerTest,UserNewsControllerTest" test`.

### Phase 85: Neuigkeiten Spec-Compliance Test Expansion ✅ COMPLETE

- Expanded `src/test/java/com/ande/pubquizzz/controller/AdminNewsControllerTest.java` with admin GET/PUT/DELETE success
  coverage, non-admin `403`, and service-thrown not-found `404` coverage for update/delete.
- Expanded `src/test/java/com/ande/pubquizzz/service/NewsServiceTest.java` with default/clamp limit behavior tests (`0`,
  negative, and `>3`) plus `updateNews` createdAt-preservation assertion.
- Expanded `src/test/java/com/ande/pubquizzz/controller/UserNewsControllerTest.java` with `GET /api/news?limit=99`
  asserting `200` and verifying service receives `99` (service clamps).
- Verification passed: `./mvnw.cmd "-Dtest=NewsServiceTest,AdminNewsControllerTest,UserNewsControllerTest" test`.

### Phase 83: Fail-Safe Restore with Automatic Rollback Snapshot ✅ COMPLETE

- Hardened `src/main/java/com/ande/pubquizzz/listener/BackupRestoreListener.java` to create a rollback snapshot before
  applying pending restore, then automatically roll back to previous DB/uploads state when restore fails.
- Added rollback orchestration helpers (`prepareRollbackSnapshot`, `applyRollbackSnapshot`, `applyDatabaseRestore`,
  `replaceUploads`) and kept startup behavior non-fatal (app continues running).
- Extended `src/main/java/com/ande/pubquizzz/service/BackupService.java` with reusable `stageRestoreToDirectory(...)` so
  rollback ZIP extraction reuses the same validation/guardrail path as normal restore staging.
- Added rollback-focused backend tests in `src/test/java/com/ande/pubquizzz/service/BackupRestoreListenerTest.java` for
  DB restore failure rollback and upload-swap failure rollback.
- Updated listener test wiring to inject `BackupService` and use new restore APIs.
- Verification passed: `npm run test`, `npm run type-check`, `npm run build` (webapp), and `./mvnw.cmd test`.

### Phase 80: XSS Hardening for `admin_functions.ts` Rendering Paths ✅ COMPLETE

- Hardened `src/main/webapp/src/js/admin_functions.ts` by escaping untrusted modal titles/messages/table content and
  switching backup/cleanup status rendering to safe `textContent` updates instead of interpolated `innerHTML`.
- Added trusted-html boundary helper for action-button markup (`trustedHtml(...)`) so button rendering still works while
  default table cells are escaped.
- Added frontend unit tests in `src/main/webapp/src/js/admin_functions.test.ts` covering escaped headers/cells,
  trusted-action markup passthrough, and escaped error message content.
- Verification passed: `npm run test`, `npm run type-check`, `npm run build` (webapp), and `./mvnw.cmd test`.

### Phase 79: CSRF Request-Handler Compatibility + Accurate 403 User Logging ✅ COMPLETE

- Updated `src/main/java/com/ande/pubquizzz/security/SecurityConfig.java` to use a stable
  `CsrfTokenRequestAttributeHandler` with `CookieCsrfTokenRepository.withHttpOnlyFalse()` and token materialization in
  `CsrfCookieFilter`, resolving the live `InvalidCsrfTokenException` mismatch for SPA header/cookie submissions.
- Updated `src/main/java/com/ande/pubquizzz/security/LoggingAccessDeniedHandler.java` to resolve username from Spring
  Security context first (fallback to request principal), avoiding false `user=anonymous` when authentication exists.
- Added/extended backend test coverage in
  `src/test/java/com/ande/pubquizzz/security/LoggingAccessDeniedHandlerTest.java` for security-context username
  resolution.
- Verification passed: `npm run test`, `npm run type-check`, `npm run build` (webapp), and `./mvnw.cmd test`.

### Phase 78: WARN Logging for All HTTP 403 Access Denials ✅ COMPLETE

- Added new `src/main/java/com/ande/pubquizzz/security/LoggingAccessDeniedHandler.java` to emit `WARN` log entries for
  every security-layer 403 with method/path/user/exception details while preserving HTTP 403 response behavior.
- Wired the handler in `src/main/java/com/ande/pubquizzz/security/SecurityConfig.java` via
  `.exceptionHandling(...).accessDeniedHandler(new LoggingAccessDeniedHandler())` so CSRF and role-based denials are
  logged consistently.
- Added backend unit test coverage in
  `src/test/java/com/ande/pubquizzz/security/LoggingAccessDeniedHandlerTest.java` asserting warn log content and status.
- Verification passed: `npm run test`, `npm run type-check`, `npm run build` (webapp), and `./mvnw.cmd test`.

### Phase 77C: Emit Browser-Readable CSRF Cookie on Authenticated Requests ✅ COMPLETE

- Updated `src/main/java/com/ande/pubquizzz/security/SecurityConfig.java` to use
  `CookieCsrfTokenRepository.withHttpOnlyFalse()` so browser clients can read/send CSRF tokens for fetch-based admin
  mutations.
- Added a post-CSRF filter (`CsrfCookieFilter`) in `SecurityConfig` that generates/saves an `XSRF-TOKEN` cookie when
  missing, ensuring token availability on authenticated requests before mutation calls.
- Added backend controller test coverage in `src/test/java/com/ande/pubquizzz/controller/AdminQuizControllerTest.java`
  asserting that authenticated admin GET responses expose `XSRF-TOKEN`.
- Verification passed: `npm run test`, `npm run type-check`, `npm run build` (webapp), and `./mvnw.cmd test`.

### Phase 77B: Frontend CSRF Header Wiring for Admin Mutation Fetch Calls ✅ COMPLETE

- Added shared frontend CSRF helper `src/main/webapp/src/js/csrf.ts` that resolves the token from cookie
  (`XSRF-TOKEN` -> `X-XSRF-TOKEN`) with fallback to Spring meta tags (`_csrf`, `_csrf_header`).
- Extended `csrf.ts` with `withEnsuredCsrfHeaders(...)` to proactively bootstrap token availability via
  `GET /api/bootstrap` when no CSRF token source exists yet, then retry cookie/meta extraction before mutation calls.
- Added frontend unit coverage in `src/main/webapp/src/js/csrf.test.ts` for cookie extraction, meta fallback, and
  preserving existing headers when no token source exists, plus bootstrap fallback behavior.
- Wired CSRF headers into admin mutation requests in:
  `src/main/webapp/src/js/admin_functions.ts`,
  `src/main/webapp/src/js/admin_results.ts`,
  `src/main/webapp/src/js/create_result.ts`,
  `src/main/webapp/src/js/create_quiz.ts`, and
  `src/main/webapp/src/js/register_user.ts`.
- Verification passed: `npm run test`, `npm run type-check`, `npm run build` (webapp), and `./mvnw.cmd test`.

### Phase 77: Re-enable CSRF Protection for Session-Based Admin Mutations ✅ COMPLETE

- Enabled Spring Security CSRF protection in `src/main/java/com/ande/pubquizzz/security/SecurityConfig.java` and kept
  `h2-console` excluded from CSRF checks to preserve local admin tooling behavior.
- Added explicit missing-CSRF regression coverage for admin mutation endpoints in
  `src/test/java/com/ande/pubquizzz/security/SecurityAccessTest.java`,
  `src/test/java/com/ande/pubquizzz/controller/AdminUserControllerTest.java`, and
  `src/test/java/com/ande/pubquizzz/controller/AdminResultControllerTest.java`.
- Updated legacy controller tests in `src/test/java/com/ande/pubquizzz/controller/AdminQuizControllerTest.java` and
  `src/test/java/com/ande/pubquizzz/controller/AdminBackupControllerTest.java` to include `.with(csrf())` on mutating
  requests now that CSRF is enforced globally.
- Verification passed: `npm run test`, `npm run type-check`, `npm run build` (webapp), and `./mvnw.cmd test`.

### Phase 76A: Multiline Log Event Grouping for Stacktraces ✅ COMPLETE

- Updated `AdminLogService` to group log lines into full events using header-line detection, so stacktrace continuation
  lines stay attached to their originating ERROR event.
- Changed filtering behavior to operate on full event text (`rawLine` now contains full multiline event), enabling
  stacktrace search while keeping level/time filtering based on parsed event header.
- Added backend TDD coverage for multiline grouping and stacktrace search in
  `src/test/java/com/ande/pubquizzz/service/AdminLogServiceTest.java` and expanded controller JSON assertions in
  `src/test/java/com/ande/pubquizzz/controller/AdminUserControllerTest.java`.
- Added frontend unit coverage in `src/main/webapp/src/js/admin_logs.test.ts` to assert multiline raw rendering in
  log-stream details.
- Verification passed: `npm run test`, `npm run type-check`, `npm run build` (webapp), and `./mvnw.cmd test`.

### Phase 76: Admin Log-Stream Page from Logback File with Search/Filter/Line Amount ✅ COMPLETE

- Added new admin-only backend endpoint `GET /admin/logs` in `AdminUserController` returning explicit DTO response
  (`AdminLogResponseDTO`) with log entries (`AdminLogEntryDTO`), applied limit, and returned count.
- Implemented `AdminLogService` to read only active Logback file (`/logs/pub-quizzz.log`), parse log lines, and apply
  server-side filters (`q`, `level`, `from`, `to`) plus line amount (`limit`, default 200, max 1000).
- Added backend tests:
  `AdminLogServiceTest` (business logic unit coverage) and `AdminUserControllerTest` endpoint/security/validation
  coverage for `/admin/logs`.
- Added new admin page `src/main/webapp/src/admin/logs.html` with log-stream UI (not table), German filter controls,
  and script `src/main/webapp/src/js/admin_logs.ts` including URL query sync and safe rendering.
- Wired admin navigation under `Wartung` (`viewLogsBtn`) and added Vite entry `logs`; added frontend unit tests in
  `src/main/webapp/src/js/admin_logs.test.ts`.
- Verification passed: `npm run test`, `npm run type-check`, `npm run build` (webapp), and `./mvnw.cmd test`.

### Phase 75: Admin Monthly Login Statistics by Role (`USER`/`ADMIN`) ✅ COMPLETE

- Added monthly login stats API for admins at `GET /admin/login-stats/monthly`, grouped by month and current user role,
  based on persisted `AUTH_SUCCESS` usage events.
- Added explicit backend DTO + service mapping and native repository aggregation query joining usage events to users by
  username.
- Added backend tests:
  `MonthlyLoginStatsPersistenceTest`, `UsageEventServiceTest` extension, and `AdminUserControllerTest` endpoint/security
  coverage.
- Added new admin page `src/main/webapp/src/admin/login_stats.html` + script
  `src/main/webapp/src/js/admin_login_stats.ts`, plus navigation wiring from admin main page and Vite entry.
- Added frontend unit tests for login stats rendering helpers in
  `src/main/webapp/src/js/admin_login_stats.test.ts`.
- Verification passed: `npm run test`, `npm run type-check`, `npm run build` (webapp), and `./mvnw.cmd test`.

### Phase 74: Integrate Frontend Vitest into Maven `test` Lifecycle ✅ COMPLETE

- Replaced placeholder frontend test script with real Vitest execution by setting
  `"test": "vitest run"` in `src/main/webapp/package.json`.
- Added frontend Maven execution `npm run test` bound to `test` phase in `pom.xml` so
  `./mvnw.cmd test` now runs backend JUnit tests and frontend Vitest tests together.
- Verified TDD red/green flow for wiring: Maven `test` failed before script fix, then passed
  after script update.
- Verification passed: `npm run test`, `npm run type-check`, `npm run build` (webapp), and
  `./mvnw.cmd test` (including frontend plugin test execution).

### Phase 73: Clickable GitHub Version Badge on Homepage ✅ COMPLETE

- Updated homepage badge rendering so version is displayed as `v<version>` and links to
  `https://github.com/AndeHofer/pub-quizzz`.
- Added dedicated frontend unit test (`src/main/webapp/src/js/version-badge.test.ts`) covering generated link markup
  (`href`, `target`, `rel`, and label).
- Extracted badge markup creation into `src/main/webapp/src/js/version-badge.ts` and reused it from
  `src/main/webapp/src/js/index.ts`.
- Verification passed: `npm exec vitest run src/js/version-badge.test.ts`, `npm run type-check` (webapp),
  `npm run build` (webapp), and `./mvnw.cmd test`.

### Phase 72: Instruction File Cleanup and Clarification ✅ COMPLETE

- Cleaned up wording in `AGENTS.md` for clarity and consistency (progress retention, Maven/NUL wording, folder-ignore
  wording).
- Explicitly documented instruction-source precedence: `README.md` is informational; executable agent instructions are
  in `AGENTS.md`.
- Kept all existing behavioral rules unchanged (`no push/commit`, no worktrees/branches, test requirements,
  German UI text, business rules).
- Verification passed: `npm run type-check` (webapp), `npm run build` (webapp), and `./mvnw.cmd test`.

### Phase 71: Persist Authentication Usage Events in Extensible Statistics Table ✅ COMPLETE

- Added new generic usage-events persistence model with table `app_usage_event` via JPA entity
  `src/main/java/com/ande/pubquizzz/database/entities/UsageEvent.java`.
- Stored authentication success events as `AUTH_SUCCESS` with requested username string + timestamp via
  `UsageEventService` and listener integration.
- Wired login success path in `AuthenticationEventListener` to persist usage rows without changing login behavior.
- Added tests for persistence and event flow:
  - `UsageEventPersistenceTest`
  - `UsageEventServiceTest`
  - `AuthenticationEventListenerTest`
  - `AuthenticationUsageEventPersistenceTest`
- Verification passed: `npm run type-check` (webapp), `npm run build` (webapp), and `./mvnw.cmd test`.

### Phase 69: Directional Top Navigation on Leaderboards ✅ COMPLETE

- Added a second top navigation row under `&larr; Startseite` on leaderboard pages with directional neighboring links.
- Implemented exact flow requested:
  - `points-leaderboard.html`: right link `Medaillenspiegel &rarr;`
  - `medal-leaderboard.html`: left link `&larr; Punkterangliste` and right link `Durchschnittsrangliste &rarr;`
  - `average-leaderboard.html`: left link `&larr; Medaillenspiegel`
- Kept existing visual style/classes and responsive layout behavior unchanged.
- Verification passed: `npm run type-check` (webapp), `npm run build` (webapp), and `./mvnw.cmd test`.

### Phase 68: `register_user` Switched to Public-Template Layout Style ✅ COMPLETE

- Reworked `src/main/webapp/src/admin/register_user.html` to use the same modern card/container layout style as the
  non-admin/public pages (`bg-gray-50`, centered responsive container, card section blocks).
- Added top contextual link `&larr; Admin Bereich`, favicon, and standardized page title styling consistent with recent
  admin pages using the same template language.
- Kept existing register workflow contract unchanged by preserving all relevant element IDs and actions
  (`username`, `password`, `role`, `registerUserBtn`, `backBtn`, `message`) used by `register_user.ts`.
- Kept UI text in German and aligned input placeholders/buttons with German wording.
- Verification passed: `npm run type-check` (webapp), `npm run build` (webapp), and `./mvnw.cmd test` (`BUILD
  SUCCESS`, 206 tests, 0 failures, 0 errors, 0 skipped).

### Phase 67A: Remove `changed` from UI/API Contract (DB Column Kept for Safe Rollout) ✅ COMPLETE

- Removed visual changed-marker usage from `src/main/webapp/src/js/admin_results.ts` so result points render without
  appended `*` markers.
- Removed `changed` from shared frontend and backend answer DTO contracts:
  `src/main/webapp/src/js/types.ts` and `src/main/java/com/ande/pubquizzz/dto/AnswerScoreDTO.java`.
- Updated backend mapping/serialization to stop exposing `changed` in API responses:
  `src/main/java/com/ande/pubquizzz/mapper/ResultMapper.java` and
  `src/main/java/com/ande/pubquizzz/service/ResultService.java`.
- Updated impacted controller/service tests to remove `changed` setter/assertion expectations while preserving behavior
  coverage (`UserQuizControllerTest`, `UserTeamControllerTest`, `ResultServiceDeleteUpdateTest`,
  `ResultServiceTeamResultsTest`).
- Kept persistence field/DB column unchanged intentionally for safe phased rollout (Phase B will remove entity field +
  DB
  column/migration).
- Verification passed: `npm run type-check` (webapp), `npm run build` (webapp), and `./mvnw.cmd test` (`BUILD
  SUCCESS`, 206 tests, 0 failures, 0 errors, 0 skipped).

### Phase 66: Same-Date Results Sorted by Higher Total Points First ✅ COMPLETE

- Updated `compareResultsNewestFirst` in `src/main/webapp/src/js/admin_results.ts` so sorting now applies tie-breakers
  in this order: `quizDate` DESC, then `totalPoints` DESC, then `resultsId` DESC.
- Added robust total-points fallback (`undefined`/non-number -> `0`) so ordering remains deterministic.
- Verification passed: `npm run type-check` (webapp), `npm run build` (webapp), and `./mvnw.cmd test` (`BUILD
  SUCCESS`, 206 tests, 0 failures, 0 errors, 0 skipped).

### Phase 65: Full-Width Per-Result Points Mini-Table (No Clipped Q/Gesamt Columns) ✅ COMPLETE

- Refactored `src/main/webapp/src/js/admin_results.ts` result block rendering to keep top-level rows at 3 columns
  (`Team`, `Quiz Datum`, `Aktionen`) and render Q1-Q8/Gesamt in a nested full-width points table below each block.
- Removed mixed 3+9-column row layout pressure so points headers/values no longer compete with top-level column widths.
- Updated `src/main/webapp/src/css/styles.css` with dedicated nested points-table styles (`result-points-*`) including
  full-width layout, compact centered numeric columns, and horizontal overflow handling only inside the points section.
- Updated `src/main/webapp/src/admin/results.html` to remove the forced top-level table min width so desktop layout can
  fit naturally while preserving mobile resilience.
- Verification passed: `npm run type-check` (webapp), `npm run build` (webapp), and `./mvnw.cmd test` (`BUILD
  SUCCESS`, 206 tests, 0 failures, 0 errors, 0 skipped).

### Phase 64: Per-Result Repeated Subheader Row in Admin Results Table ✅ COMPLETE

- Changed `src/main/webapp/src/admin/results.html` to keep only the first header row (`Team`, `Quiz Datum`, `Aktionen`)
  in `thead`.
- Updated `src/main/webapp/src/js/admin_results.ts` rendering so each result block now repeats a local second header row
  (`Q1`-`Q8`, `Gesamt`) directly in `tbody` before the corresponding points row.
- Updated empty-state colspan handling for the new first-level header-only table structure.
- Added subheader row styling in `src/main/webapp/src/css/styles.css` to keep grouped blocks readable and stable.
- Verification passed: `npm run type-check` (webapp), `npm run build` (webapp), and `./mvnw.cmd test` (`BUILD
  SUCCESS`, 206 tests, 0 failures, 0 errors, 0 skipped).

### Phase 63: Fix Grouped Results Table Column Sizing so Q1-Q8 Values Render ✅ COMPLETE

- Fixed missing Q1-Q8 value rendering on `src/main/webapp/src/admin/results.html` by changing
  `.results-table-grouped` table layout from fixed to automatic sizing in `src/main/webapp/src/css/styles.css`.
- Kept grouped two-row entry UX and existing filters/actions unchanged while restoring visible points columns.
- Verification passed: `npm run type-check` (webapp), `npm run build` (webapp), and `./mvnw.cmd test` (`BUILD
  SUCCESS`, 206 tests, 0 failures, 0 errors, 0 skipped).

### Phase 62: Admin Results Two-Row Entry Layout (`Gesamt` with Q1-Q8) + Clear Group Borders ✅ COMPLETE

- Reworked `src/main/webapp/src/admin/results.html` table header and structure so each result renders as two rows:
  summary row (`Team`, `Quiz Datum`, `Aktionen`) plus detail row (`Q1`-`Q8` and `Gesamt`).
- Updated `src/main/webapp/src/js/admin_results.ts` row rendering to output grouped two-row entries while preserving
  existing newest-first sorting, cascading filter behavior, and edit/delete actions.
- Replaced packed compact styling with clearer grouped-row styling in `src/main/webapp/src/css/styles.css` using
  `results-table-grouped` classes and visible top/bottom borders to show both rows belong together.
- Kept mobile horizontal scrolling behavior and desktop readability expectations aligned with requested UX.
- Verification passed: `npm run type-check` (webapp), `npm run build` (webapp), and `./mvnw.cmd test` (`BUILD
  SUCCESS`, 206 tests, 0 failures, 0 errors, 0 skipped).

### Phase 61: Contextual Back-to-Results Navigation + Locked Select Styling on Create/Edit Result ✅ COMPLETE

- Added a second conditional button `Zurück zur Ergebnisliste` on `src/main/webapp/src/admin/create_result.html` that is
  shown only when the page was opened from the results list context.
- Updated `src/main/webapp/src/js/admin_results.ts` edit navigation to include context/query parameters (`from=results`,
  `quizId`, `team`) and updated `src/main/webapp/src/js/create_result.ts` to route back to `results.html` with preserved
  filters.
- Kept the existing `Zurück zum Admin Bereich` button unchanged to preserve direct admin navigation behavior.
- Styled locked quiz/team dropdowns in edit mode with a light-gray disabled appearance via
  `select.locked-select:disabled`
  in `src/main/webapp/src/css/styles.css` and applied the class in create_result edit mode.
- Verification passed: `npm run type-check` (webapp), `npm run build` (webapp), and `./mvnw.cmd test` (`BUILD
  SUCCESS`, 206 tests, 0 failures, 0 errors, 0 skipped).

### Phase 59: Admin Results as Dedicated Page (No Modal) + Visible Quiz Filter ✅ COMPLETE

- Replaced modal-based results listing with dedicated page `src/main/webapp/src/admin/results.html` in the same card
  layout style as the non-admin/public templates.
- Added page script `src/main/webapp/src/js/admin_results.ts` to load, sort (newest first by `quizDate` and tie-break by
  `resultsId`), filter by quiz, and handle edit/delete actions.
- Added visible quiz filter dropdown (`Alle Quizze` + newest-first quiz options), persisted selection in URL query
  parameter (`quizId`), and reloaded results on filter changes.
- Rewired `viewResultsBtn` in `src/main/webapp/src/js/admin_functions.ts` to navigate to `results.html` and removed
  obsolete modal-only results code/state (`viewResults`, result delete/edit modal handlers, stale filter cache field).
- Added new Vite entry `results` in `src/main/webapp/vite.config.ts` for the new admin results page.
- Verification passed: `npm run type-check` (webapp), `npm run build` (webapp), and `./mvnw.cmd test` (`BUILD
  SUCCESS`, 206 tests, 0 failures, 0 errors, 0 skipped).

### Phase 58: Complexity/Duplication Cleanup (Shared HTML + Table Toggle Helpers, Dead Code Removal, Service/Test Simplification) ✅ COMPLETE

- Added shared frontend helper modules `src/main/webapp/src/js/html-utils.ts` and
  `src/main/webapp/src/js/results-table-common.ts`; adopted shared HTML escaping in `quizzes.ts`, `quiz-details.ts`,
  and leaderboard utilities.
- Refactored `quiz.ts` and `team.ts` to reuse shared medal/toggle/badge helpers and switched row detail expansion from
  global inline handlers to delegated event handling (removed global `toggleDetail` exposure).
- Removed unused frontend code and globals: dead team cache + stale add-result function in `admin_functions.ts`, and
  unnecessary `window` exports in `register_user.ts` and `create_quiz.ts`.
- Simplified `ResultService` internals with shared score-comparison helpers, map-based answer updates in
  `updateResult`, removal of unused average-stat field, and inlined one-use result loading/log suffix helpers.
- Reduced test duplication via new `ResultServiceTestData` helper and parameterized duplicate points-leaderboard tests.
- Verification passed: `npm run type-check` (webapp), `npm run build` (webapp), and `./mvnw.cmd test` (`BUILD
  SUCCESS`, 206 tests, 0 failures, 0 errors, 0 skipped).

### Phase 57: Maintainability Refactor (Shared Helpers + Dead Code Cleanup + Validation Dedup) ✅ COMPLETE

- Added shared frontend quiz helper module `src/main/webapp/src/js/quiz-utils.ts` and adopted it in
  `create_result.ts`, `quiz-details.ts`, and `quizzes.ts` for consistent newest-first sorting, finished filtering,
  and display-title behavior.
- Simplified `create_result.ts` question input rendering and create->edit success path without changing UX behavior
  (first save remains on page, switches to edit mode, and locks quiz/team selection).
- Removed unused admin result modal remnants from `admin_functions.ts` (`buildAddResultForm`, `onSaveAddResult`, and
  stale caches) and cleaned the `editResult` signature.
- Consolidated duplicated result answer validation in `ResultService` into a shared internal validator while
  preserving validation rules and exception messages.
- Removed unused repository method `findAverageLeaderboardRaw` from `ResultRepository`.
- Verification passed: `npm run type-check` (webapp), `npm run build` (webapp), and `./mvnw.cmd test` (`BUILD
  SUCCESS`, 206 tests, 0 failures, 0 errors, 0 skipped).

### Phase 55: Remove Deprecated User Endpoints (`/api/is-admin`, `/api/version`) ✅ COMPLETE

- Removed unused mapped endpoints `/api/is-admin` and `/api/version` from `UserController`; kept only
  `/api/bootstrap` with unchanged payload and cache behavior.
- Kept admin-role evaluation as an internal helper method without exposing a dedicated endpoint.
- Updated `UserControllerTest` to remove old endpoint tests and keep bootstrap-focused coverage.
- Verified there are no remaining source references to `/api/is-admin` or `/api/version`.
- Verification passed: `npm run type-check`, `npm run build`, `./mvnw.cmd -Dtest=UserControllerTest test`,
  `./mvnw.cmd test` (`BUILD SUCCESS`, 201 tests, 0 failures).

### Phase 54: Bootstrap Endpoint with 1-Hour HTTP Cache (No Browser Storage) ✅ COMPLETE

- Added `GET /api/bootstrap` in `UserController` returning both `isAdmin` and `version` via new DTO
  `BootstrapResponse`.
- Configured response caching with `Cache-Control: max-age=3600, must-revalidate, private` to use HTTP caching for one
  hour without `sessionStorage`.
- Reworked index bootstrap logic in `index.ts` to remove `sessionStorage` keys and fetch only `/api/bootstrap` for admin
  card visibility and version badge.
- Added controller tests for bootstrap endpoint payload, authentication behavior, and cache header.
- Verification passed: `npm run type-check`, `npm run build`, `./mvnw.cmd -Dtest=UserControllerTest test`,
  `./mvnw.cmd test` (`BUILD SUCCESS`, 205 tests, 0 failures).

### Phase 53: Tighten Unauthenticated Access to Login + Favicon Only ✅ COMPLETE

- Updated `SecurityConfig` to keep only `/favicon.ico` as `permitAll`; all other paths now require authentication unless
  separately constrained by role.
- Updated `SecurityAccessTest` to assert unauthenticated requests to `/assets/**`, `/static/**`, and `/uploads/**`
  redirect to `/login`.
- Kept existing checks that protected API/admin paths redirect unauthenticated users and remain forbidden for non-admin
  authenticated users.
- Verification passed: `./mvnw.cmd test` -> `BUILD SUCCESS` (202 tests, 0 failures, 0 errors, 0 skipped).

### Phase 52: Remove Spring Security Ignore Warnings for Static Paths ✅ COMPLETE

- Removed `WebSecurityCustomizer` ignore configuration from `SecurityConfig` to follow Spring Security guidance and
  eliminate startup warnings from `WebSecurity.performBuild`.
- Kept static/public paths in `HttpSecurity.authorizeHttpRequests(...).permitAll(...)` and added `/static/**` there so
  all previously ignored startup-warning paths remain explicitly permitted.
- Extended `SecurityAccessTest` with a `/static/**` unauthenticated access check (no redirect to `/login`).
- Verification passed after setting `JAVA_HOME` in the shell session:
  - `./mvnw.cmd test` -> `BUILD SUCCESS` (202 tests, 0 failures, 0 errors, 0 skipped)
  - no `WebSecurity.performBuild` warnings found in the Maven test output.

### Phase 50: Create-Quiz Save Flow Switches to Update + No Redirect on Edit Save ✅ COMPLETE

- Changed `POST /admin/create-quiz` to return created `QuizDTO` so frontend can capture `quizId` after first save.
- Updated `create_quiz.ts` save handling so first save in create mode switches page into edit mode, later saves use
  update endpoint, and edit save no longer navigates away.
- Updated `AdminQuizControllerTest` create endpoint tests to validate JSON response payload (`quizId`).
- Verification: `npm run type-check`, `npm run build` passed; `.\mvnw.cmd test` blocked because `JAVA_HOME` is not
  defined correctly in this environment.

### Phase 49: Stable Quiz-Details Header Height (No Creator Jump) ✅ COMPLETE

- Reserved fixed visual space for title + creator subtitle in quiz-details header to prevent layout jump when creator
  text appears.
- Switched creator subtitle toggling from `display` changes to `visibility` changes so layout height remains stable.
- Verification passed: `npm run type-check`, `npm run build`, `mvn.cmd test`.

### Phase 48: Dynamic Quiz-Details Header with Creator ✅ COMPLETE

- Replaced static quiz-details heading with dynamic title handling: default `Quiz ansehen`, selected quiz title when a
  quiz is chosen.
- Added optional smaller subtitle `erstellt von <creator>` when detail payload contains a creator.
- Reset heading and subtitle to default when selection is cleared or detail loading fails.
- Verification passed: `npm run type-check`, `npm run build`, `mvn.cmd test`.

### Phase 47: Optional Quiz Creator (`Urheber`) in Backend + Admin Form ✅ COMPLETE

- Added optional `creator` to quiz domain/API flow (`Quiz`, `CreateQuizRequest`, `QuizDTO`, `QuizDetailDTO`) and
  persisted it in create/full-update service paths.
- Implemented admin create/edit form field labeled `Urheber (optional)` and wired payload + edit prefill in
  `create_quiz.ts`.
- Applied requested behavior where clearing `Urheber` in edit mode stores it as empty (`null`) by sending trimmed empty
  values as `null`.
- Updated unit/controller coverage for creator in service and quiz endpoints.
- Verification passed: `npm run type-check`, `npm run build`, `mvn.cmd test`.

### Phase 46: Informative Admin Controller Call Logging ✅ COMPLETE

- Archived rollout of per-endpoint admin controller call logging with `@Slf4j` alignment and successful verification.

### Phase 45: Session-Cached Admin Visibility on Index ✅ COMPLETE

- Archived index admin-card session-cache behavior for `/api/is-admin` with safe hidden fallback and successful
  verification.

### Phase 42: Session-Cached Version Badge ✅ COMPLETE

- Archived index version badge session-cache behavior with fallback fetch (`cache: 'no-store'`) and successful
  verification.

### Phase 41: Smoother Page-to-Page Transitions ✅ COMPLETE

- Archived global scrollbar-gutter/body fade transitions and index vertical spacing normalization with shared page
  transition module rollout and verification.

### Phase 40: Public Desktop Width Consistency (`sm:max-w-3xl`) ✅ COMPLETE

- Archived desktop container width standardization to `sm:max-w-3xl` with verification.

### Phase 39: Archive Format Alignment ✅ COMPLETE

- Reworked archive structure to match `progress.md` style with concise, phase-based sections.

### Phase 38: Detailed History Migration ✅ COMPLETE

- Archived removed detailed history from `progress.md` into this file.

### Phase 34: Quiz-Details Iterative Enhancements (Archived Follow-up Stream) ✅ COMPLETE

- Consolidated the prior Phase 34 follow-up stream (UI reveal behavior, title derivation, modal/actions, and repeated
  verification) into one archived phase summary.

### Phase 33: Team Ranking Table Refinements ✅ COMPLETE

- Archived Team table split, medal rank display, service/DTO fixture fixes, and successful verification.

### Phase 32: Additional Overall Leaderboards + Follow-up Refinements ✅ COMPLETE

- Archived medals/average leaderboard implementation plus endpoint cleanup, tie-rule/ranking refinements, UI updates,
  and repeated full verification.

### Phase 31: Quizzes Sorted by `pubDate` DESC ✅ COMPLETE

- Archived frontend sorting update for quiz listing (newest first) with verification.

### Phase 30: Legacy Backup Schema Compatibility ✅ COMPLETE

- Archived compatibility fixes for legacy restore (`question.answer_image_url`, `quiz_document`) with regression
  coverage.

### Phase 29: Backup/Restore FK Fix for `quiz_document` ✅ COMPLETE

- Archived foreign-key restore order and backup table coverage fix.

### Phase 28: Image Answers for Questions 5-8 ✅ COMPLETE

- Archived support for text/image answers in questions 5-8 across backend, frontend, and tests.

### Phase 24: Winner Column on Quiz Archive Page ✅ COMPLETE

- Archived winner column addition for the quiz archive page.

### Phase 22: Orphaned Image Cleanup ✅ COMPLETE

- Archived fix for orphaned image file handling.

### Phase 21: `hint_text` NOT NULL / Flyway Removal Fix ✅ COMPLETE

- Archived schema/data fix tied to `hint_text` nullability and Flyway removal.

### Phase 20: Admin Mobile Responsiveness ✅ COMPLETE

- Archived admin section mobile-friendly improvements.

### Phase 19: Quiz Archive Page ✅ COMPLETE

- Archived implementation of the quiz archive page.

### Phase 18: Team Detail Page ✅ COMPLETE

- Archived implementation of team detail view.

### Phase 17: Quiz Draft Saving + Finished Indicator ✅ COMPLETE

- Archived quiz draft persistence and finished-state indicator.

### Phase 16: Backup/Restore ✅ COMPLETE

- Archived initial backup/restore implementation.

### Phase 15: Two Hint Images (`imageUrlAtStart` + `imageUrlAsHint`) ✅ COMPLETE

- Archived dual hint-image support for quiz questions.

### Phase 14: Optional Quiz Title Field ✅ COMPLETE

- Archived optional title field support for quizzes.

### Phase 13: Quiz Delete FK Violation + Confirmation ✅ COMPLETE

- Archived quiz delete FK violation fix and danger confirmation flow.

### Phase 12: Team Delete FK Violation + Confirmation ✅ COMPLETE

- Archived team delete FK violation fix and danger confirmation flow.

### Phase 11: Rename Team + Edit/Delete Result ✅ COMPLETE

- Archived team renaming and result edit/delete improvements in admin.

### Phase 10: Max 5 Points Per Question ✅ COMPLETE

- Archived enforcement of per-question 5-point maximum.

### Phase 9: BigIntegrationTest DB Seeder ✅ COMPLETE

- Archived manual DB seeder setup for integration testing.

### Phase 8: In-Memory H2 for Tests ✅ COMPLETE

- Archived migration of tests to in-memory H2 setup.

### Phase 7: Remove Export/Leaderboard from Admin Panel ✅ COMPLETE

- Archived admin panel cleanup removing export/leaderboard entries.

### Phase 6: Style Unification (Less Color) ✅ COMPLETE

- Archived UI style harmonization toward a less colorful theme.

### Phase 5: User All-Time Leaderboard ✅ COMPLETE

- Archived user-facing all-time leaderboard implementation.

### Phase 4: Refactoring (7-PR Plan) ✅ COMPLETE

- Archived broad refactoring phase tracked through the original 7-PR plan.

### Phase 3: Test Fixes ✅ COMPLETE

- Archived baseline test stabilization work.

### Phase 2: Full Backend Refactoring ✅ COMPLETE

- Archived full backend refactor milestone.

### Phase 1: TypeScript/Tailwind/Vite Migration ✅ COMPLETE

- Archived frontend migration to TypeScript, Tailwind CSS, and Vite.

Note: Phase numbering reflects original historical labels where applicable.
