# Progress

## Open Tasks

- [x] Phase 91: Confirm homepage Neuigkeiten should be first section and display only date (no time)
- [x] Phase 91: Add/adjust frontend test to enforce date-only rendering for news timestamps
- [x] Phase 91: Move Neuigkeiten section to first card on homepage and switch formatter to date-only
- [x] Phase 91: Run verification (`npm run test`, `npm run type-check`, `npm run build`, `./mvnw.cmd test`)

- [x] Phase 90: Confirm homepage placement target for Neuigkeiten directly below leaderboard row
- [x] Phase 90: Move Neuigkeiten section in `index.html` to directly under Punkte/Medaillen/Durchschnitt row
- [x] Phase 90: Run verification (`npm run test`, `npm run type-check`, `npm run build`, `./mvnw.cmd test`)

- [x] Phase 89: Review frontend review-fix scope and identify shared admin helper extraction points (
  `admin_functions.ts`, `admin_news.ts`, related tests)
- [x] Phase 89: Apply frontend fixes (escaped quiz action IDs, robust response.ok handling, shared helper reuse without
  circular imports)
- [x] Phase 89: Expand frontend tests (`admin_news.test.ts`, `news.test.ts`) for requested unhappy
  paths/date-newline-tie-break coverage
- [x] Phase 89: Run targeted frontend verification (`npm run test -- src/js/news.test.ts src/js/admin_news.test.ts` and
  `npm run type-check`)

- [x] Phase 88: Add failing frontend tests for Neuigkeiten sort+limit helper and admin news action/request helper
  coverage
- [x] Phase 88: Refactor Neuigkeiten public/admin frontend flow (button-driven admin actions + modal/table management)
- [x] Phase 88: Run targeted frontend verification (`npm run test -- src/js/news.test.ts src/js/admin_news.test.ts`)

- [x] Phase 87: Add RED frontend tests for Neuigkeiten public/admin modules (`news.test.ts`, `admin_news.test.ts`)
- [x] Phase 87: Implement Neuigkeiten frontend modules and wire homepage/admin UI (`news.ts`, `admin_news.ts`,
  `types.ts`, `index.ts`, `index.html`, `admin_main.html`, `admin_functions.ts`)
- [x] Phase 87: Run targeted frontend tests (`npm run test -- src/js/news.test.ts src/js/admin_news.test.ts`)

- [x] Phase 86: Add/adjust failing Neuigkeiten backend tests for admin create `201`, admin full-list repository usage,
  and safe delete flow
- [x] Phase 86: Implement Neuigkeiten backend quality fixes in service/repository/controller (sorted full list + delete
  by loaded entity + create returns `201`)
- [x] Phase 86: Run targeted Neuigkeiten backend test command (
  `./mvnw.cmd "-Dtest=NewsServiceTest,AdminNewsControllerTest,UserNewsControllerTest" test`)

- [x] Phase 85: Expand Neuigkeiten backend tests for admin CRUD/security/404 behavior and service
  clamp/preserve-createdAt rules
- [x] Phase 85: Run targeted Neuigkeiten backend test command (
  `./mvnw.cmd "-Dtest=NewsServiceTest,AdminNewsControllerTest,UserNewsControllerTest" test`)

- [x] Phase 84: Brainstorm and lock Neuigkeiten scope (logged-in users can read, only admins can create/edit/delete)
- [x] Phase 84: Define Neuigkeiten data shape and display constraints (`Titel`, `Text`, `Erstellt am`; newest first;
  limit 3 on homepage)
- [x] Phase 84: Write approved Neuigkeiten design spec and implementation plan
- [x] Phase 84: Implement backend Neuigkeiten entity/repository/service/controllers with admin CRUD and user read
  endpoint
- [x] Phase 84: Implement frontend Neuigkeiten section on homepage and admin management actions
- [x] Phase 84: Add/update backend integration tests, backend unit tests, and frontend unit tests for Neuigkeiten
- [x] Phase 84: Run full verification (`npm run test`, `npm run type-check`, `npm run build`, `./mvnw.cmd test`)

- [x] Phase 84 (Task 2+3): Add failing backend tests for `NewsService`, `AdminNewsController`, and `UserNewsController`
- [x] Phase 84 (Task 2+3): Run targeted backend subset and capture RED failure evidence
- [x] Phase 84 (Task 2+3): Implement backend Neuigkeiten entity/repository/DTOs/service/controllers
- [x] Phase 84 (Task 2+3): Re-run targeted backend subset and capture GREEN pass evidence

- [x] Phase 83: Add rollback-based restore safety so startup restore is fail-safe (restore all-or-rollback)
- [x] Phase 83: Add failing backend tests for DB-restore failure and upload-swap failure rollback behavior
- [x] Phase 83: Keep pending restore diagnostics behavior while ensuring rollback snapshot cleanup on success
- [x] Phase 83: Run full verification (`npm run test`, `npm run type-check`, `npm run build`, `./mvnw.cmd test`)

- [x] Phase 82: Stream backup import in controller/service (`InputStream` instead of `byte[]`) to remove heap-heavy
  buffering
- [x] Phase 82: Enforce restore archive limits (entry count, single entry size, total uncompressed size) with validation
  errors
- [x] Phase 82: Ensure staged restore directory is cleaned on all restore-staging failures
- [x] Phase 82: Add/adjust backend tests (controller + service) for streamed restore and archive guardrails
- [x] Phase 82: Run full verification (`npm run test`, `npm run type-check`, `npm run build`, `./mvnw.cmd test`)

- [x] Phase 81: Re-run full project audit and capture findings in `findings.md`
- [x] Phase 81: Fix Finding 1 (stored XSS via unescaped document filename rendering in `create_quiz.ts`)
- [x] Phase 81: Add frontend unit tests that fail on unescaped document filenames/attributes in document list rendering
- [x] Phase 81: Run full verification (`npm run test`, `npm run type-check`, `npm run build`, `./mvnw.cmd test`)

- [x] Phase 80: Harden `admin_functions.ts` against XSS in modal/table/status-message rendering paths
- [x] Phase 80: Add frontend tests that fail on unescaped HTML/script injection in admin helper rendering
- [x] Phase 80: Run full verification (`npm run test`, `npm run type-check`, `npm run build`, `./mvnw.cmd test`)

- [x] Phase 79: Fix CSRF header/cookie validation mismatch for authenticated SPA multipart admin requests
- [x] Phase 79: Resolve 403 warn logging user identity from Spring Security context (avoid false `anonymous`)
- [x] Phase 79: Run full verification (`npm run test`, `npm run type-check`, `npm run build`, `./mvnw.cmd test`)

- [x] Phase 78: Add security-layer warn logging for all HTTP 403 responses (including CSRF denials)
- [x] Phase 78: Add backend test coverage for access-denied logging behavior and keep existing 403 response contract
- [x] Phase 78: Run full verification (`npm run test`, `npm run type-check`, `npm run build`, `./mvnw.cmd test`)

- [x] Phase 77C: Expose CSRF token source to browser clients so admin mutation fetch calls can send valid CSRF headers
- [x] Phase 77C: Add backend test coverage proving authenticated admin GET responses issue an `XSRF-TOKEN` cookie
- [x] Phase 77C: Run full verification (`npm run test`, `npm run type-check`, `npm run build`, `./mvnw.cmd test`)

- [x] Phase 77B: Add frontend CSRF token helper and wire all admin mutation fetch calls
- [x] Phase 77B: Add frontend unit tests for CSRF header helper behavior (cookie + meta fallback)
- [x] Phase 77B: Run full verification (`npm run test`, `npm run type-check`, `npm run build`, `./mvnw.cmd test`)

- [x] Phase 77: Re-enable CSRF protection for session-based security and align backend tests with enforced CSRF
- [x] Phase 77: Add backend security regression tests proving admin mutation endpoints reject missing CSRF tokens
- [x] Phase 77: Run full verification (`npm run test`, `npm run type-check`, `npm run build`, `./mvnw.cmd test`)

- [x] Phase 76A: Group Logback lines into multiline log events so stacktraces stay attached to error entries
- [x] Phase 76A: Extend admin log DTO/frontend rendering to expose and display full multiline raw event content
- [x] Phase 76A: Add/adjust backend and frontend tests for multiline grouping + stacktrace search behavior
- [x] Phase 76A: Run full verification (`npm run type-check`, `npm run build`, `./mvnw.cmd test`)

- [x] Phase 76: Add admin API endpoint for live Logback log-stream with server-side search/filter/limit
- [x] Phase 76: Add admin Wartung log-stream page (`logs.html`) with German UI and URL-synced filters
- [x] Phase 76: Add backend/frontend tests for log API + log-stream rendering behavior
- [x] Phase 76: Run full verification (`npm run type-check`, `npm run build`, `./mvnw.cmd test`)

- [x] Phase 75B: Move monthly login stats admin navigation from `Ergebnisse & Auswertung` to `Benutzerverwaltung`
- [x] Phase 75B: Run full verification (`npm run type-check`, `npm run build`, `./mvnw.cmd test`)

- [x] Phase 75: Add backend admin endpoint for monthly login stats grouped by role (`USER`, `ADMIN`) using persisted
  `AUTH_SUCCESS` events
- [x] Phase 75: Add backend unit + integration tests for monthly login role aggregation and admin API contract
- [x] Phase 75: Add new admin page for monthly login stats by role and wire navigation + Vite entry
- [x] Phase 75: Run full verification (`npm run type-check`, `npm run build`, `./mvnw.cmd test`)

- [x] Phase 74: Wire frontend `npm test` to run Vitest in CI-friendly mode
- [x] Phase 74: Integrate frontend tests into Maven `test` phase via frontend-maven-plugin
- [x] Phase 74: Run full verification (`npm run type-check`, `npm run build`, `./mvnw.cmd test`) and confirm Maven
  triggers Vitest

- [x] Phase 73: Make homepage version badge render as clickable GitHub link using `v<version>` label
- [x] Phase 73: Add frontend unit test for version badge link markup generation (TDD red/green)
- [x] Phase 73: Run full verification (`npm run type-check`, `npm run build`, `./mvnw.cmd test`)

- [x] Phase 72: Clarify and normalize `AGENTS.md` wording for agent instructions
- [x] Phase 72: Keep instruction source explicit (`AGENTS.md` authoritative, `README.md` informational)
- [x] Phase 72: Run full verification (`npm run type-check`, `npm run build`, `./mvnw.cmd test`)

- [x] Phase 71: Add extensible app usage statistics table for authentication success events
- [x] Phase 71: Persist `AUTH_SUCCESS` events with username (string) and timestamp through authentication listener
- [x] Phase 71: Add unit/integration tests for usage-event persistence and run full verification (`npm run type-check`, `npm run build`, `./mvnw.cmd test`)

- [x] Phase 70: Align leaderboard top navigation rows so available side links are shown in first line with `Startseite`
  where requested
- [x] Phase 70: Keep medaillen left backlink (`Punkterangliste`) on second line while moving `Durchschnittsrangliste` to
  first line
- [x] Phase 70: Run full verification (`npm run type-check`, `npm run build`, `./mvnw.cmd test`)

- [x] Phase 69: Add directional top navigation row to public leaderboard pages
- [x] Phase 69: Keep `Startseite` top link and add page-specific left/right neighbor links (Punkte -> right Medaillen;
  Medaillen -> left Punkte + right Durchschnitt; Durchschnitt -> left Medaillen)
- [x] Phase 69: Run full verification (`npm run type-check`, `npm run build`, `./mvnw.cmd test`)

- [x] Phase 68: Migrate `admin/register_user.html` to public-template layout style used by non-admin pages
- [x] Phase 68: Keep existing register form behavior/IDs and admin navigation actions unchanged
- [x] Phase 68: Run full verification (`npm run type-check`, `npm run build`, `./mvnw.cmd test`)

- [x] Phase 67A: Remove `changed` usage from admin results UI and shared frontend types
- [x] Phase 67A: Remove `changed` from API DTO mapping/serialization while keeping DB column intact
- [x] Phase 67A: Update tests affected by removing `changed` from API contract
- [x] Phase 67A: Run full verification (`npm run type-check`, `npm run build`, `./mvnw.cmd test`)

- [x] Phase 66: Update admin results sorting tie-breaker so same-date entries are ordered by total points descending
- [x] Phase 66: Keep stable fallback ordering for same date and same total points
- [x] Phase 66: Run full verification (`npm run type-check`, `npm run build`, `./mvnw.cmd test`)

- [x] Phase 65: Refactor admin results row layout to render Q1-Q8/Gesamt as a full-width nested table per result block
- [x] Phase 65: Adjust grouped table styling so desktop no longer clips points columns while keeping mobile behavior
- [x] Phase 65: Run full verification (`npm run type-check`, `npm run build`, `./mvnw.cmd test`)

- [x] Phase 64: Restructure admin results table so per-result block repeats second header row (`Q1`-`Q8`, `Gesamt`)
  inside tbody
- [x] Phase 64: Update grouped results rendering/styles to keep all points columns visible and visually grouped
- [x] Phase 64: Run full verification (`npm run type-check`, `npm run build`, `./mvnw.cmd test`)

- [x] Phase 63: Fix admin results grouped table so Q1-Q8 values are visible again
- [x] Phase 63: Run full verification (`npm run type-check`, `npm run build`, `./mvnw.cmd test`)

- Phase 61 verification status:
  - `npm run type-check` (in `src/main/webapp`) passed
  - `npm run build` (in `src/main/webapp`) passed
  - `./mvnw.cmd test` passed after setting `JAVA_HOME` in-command to
    `C:\Program Files\Eclipse Adoptium\jdk-25.0.3.9-hotspot`

- Phase 62 verification status:
  - `npm run type-check` (in `src/main/webapp`) passed
  - `npm run build` (in `src/main/webapp`) passed
  - `./mvnw.cmd test` passed after setting `JAVA_HOME` in-command to
    `C:\Program Files\Eclipse Adoptium\jdk-25.0.3.9-hotspot`

- Phase 63 verification status:
  - `npm run type-check` (in `src/main/webapp`) passed
  - `npm run build` (in `src/main/webapp`) passed
  - `./mvnw.cmd test` passed after setting `JAVA_HOME` in-command to
    `C:\Program Files\Eclipse Adoptium\jdk-25.0.3.9-hotspot`

- Phase 64 verification status:
  - `npm run type-check` (in `src/main/webapp`) passed
  - `npm run build` (in `src/main/webapp`) passed
  - `./mvnw.cmd test` passed after setting `JAVA_HOME` in-command to
    `C:\Program Files\Eclipse Adoptium\jdk-25.0.3.9-hotspot`

- Phase 65 verification status:
  - `npm run type-check` (in `src/main/webapp`) passed
  - `npm run build` (in `src/main/webapp`) passed
  - `./mvnw.cmd test` passed after setting `JAVA_HOME` in-command to
    `C:\Program Files\Eclipse Adoptium\jdk-25.0.3.9-hotspot`

- Phase 66 verification status:
  - `npm run type-check` (in `src/main/webapp`) passed
  - `npm run build` (in `src/main/webapp`) passed
  - `./mvnw.cmd test` passed after setting `JAVA_HOME` in-command to
    `C:\Program Files\Eclipse Adoptium\jdk-25.0.3.9-hotspot`

- Phase 67A verification status:
  - `npm run type-check` (in `src/main/webapp`) passed
  - `npm run build` (in `src/main/webapp`) passed
  - `./mvnw.cmd test` passed after setting `JAVA_HOME` in-command to
    `C:\Program Files\Eclipse Adoptium\jdk-25.0.3.9-hotspot`

- Phase 68 verification status:
  - `npm run type-check` (in `src/main/webapp`) passed
  - `npm run build` (in `src/main/webapp`) passed
  - `./mvnw.cmd test` passed after setting `JAVA_HOME` in-command to
    `C:\Program Files\Eclipse Adoptium\jdk-25.0.3.9-hotspot`

- Phase 69 verification status:
    - `npm run type-check` (in `src/main/webapp`) passed
    - `npm run build` (in `src/main/webapp`) passed
    - `./mvnw.cmd test` passed after setting `JAVA_HOME` in-command to
      `C:\Program Files\Eclipse Adoptium\jdk-25.0.3.9-hotspot`

- Phase 70 verification status:
    - `npm run type-check` (in `src/main/webapp`) passed
    - `npm run build` (in `src/main/webapp`) passed
    - `./mvnw.cmd test` passed after setting `JAVA_HOME` in-command to
      `C:\Program Files\Eclipse Adoptium\jdk-25.0.3.9-hotspot`

- Phase 71 verification status:
    - `npm run type-check` (in `src/main/webapp`) passed
    - `npm run build` (in `src/main/webapp`) passed
    - `./mvnw.cmd test` passed after setting `JAVA_HOME` in-command to
      `C:\Program Files\Eclipse Adoptium\jdk-25.0.3.9-hotspot`

- Phase 72 verification status:
  - `npm run type-check` (in `src/main/webapp`) passed
  - `npm run build` (in `src/main/webapp`) passed
  - `./mvnw.cmd test` passed after setting `JAVA_HOME` in-command to
    `C:\Program Files\Eclipse Adoptium\jdk-25.0.3.9-hotspot`

- Phase 73 verification status:
  - `npm exec vitest run src/js/version-badge.test.ts` failed first (module missing), then passed after implementation
  - `npm run type-check` (in `src/main/webapp`) passed
  - `npm run build` (in `src/main/webapp`) passed
  - `./mvnw.cmd test` passed after setting `JAVA_HOME` in-command to
    `C:\Program Files\Eclipse Adoptium\jdk-25.0.3.9-hotspot`

- Phase 74 verification status:
    - `./mvnw.cmd test` failed first after Maven integration because `npm test` still used placeholder script (
      `Error: no test specified`)
    - `npm run test` (in `src/main/webapp`) passed after switching script to `vitest run` (1 test file, 1 test)
    - `npm run type-check` (in `src/main/webapp`) passed
    - `npm run build` (in `src/main/webapp`) passed
    - `./mvnw.cmd test` passed and now includes frontend step `frontend:2.0.0:npm (npm run test)` invoking Vitest

- Phase 75 verification status:
    - `npm run test` (in `src/main/webapp`) passed (2 files, 4 tests)
    - `npm run type-check` (in `src/main/webapp`) passed
    - `npm run build` (in `src/main/webapp`) passed
    - `./mvnw.cmd test` passed after setting `JAVA_HOME` in-command to
      `C:\Program Files\Eclipse Adoptium\jdk-25.0.3.9-hotspot`
    - Maven output includes frontend Vitest step `frontend:2.0.0:npm (npm run test)`

- Phase 75B verification status:
    - `npm run type-check` (in `src/main/webapp`) passed
    - `npm run build` (in `src/main/webapp`) passed
    - `./mvnw.cmd test` passed after setting `JAVA_HOME` in-command to
      `C:\Program Files\Eclipse Adoptium\jdk-25.0.3.9-hotspot`
    - Maven output includes frontend Vitest step `frontend:2.0.0:npm (npm run test)`

- Phase 76 verification status:
    - `npm run test` (in `src/main/webapp`) initially failed (`document is not defined` in `admin_logs.test.ts`);
      fixed by making `escapeHtml` Node-test-safe without DOM dependency
    - `npm run type-check` (in `src/main/webapp`) passed
    - `npm run build` (in `src/main/webapp`) passed
    - `./mvnw.cmd test` passed after setting `JAVA_HOME` in-command to
      `C:\Program Files\Eclipse Adoptium\jdk-25.0.3.9-hotspot`
    - Maven output includes frontend Vitest step `frontend:2.0.0:npm (npm run test)`

- Phase 76A verification status:
    - `./mvnw.cmd "-Dtest=AdminLogServiceTest,AdminUserControllerTest" test` failed first after adding multiline
      tests (expected TDD red), then passed after implementing event grouping
    - `npm run test` (in `src/main/webapp`) passed (3 files, 9 tests)
    - `npm run type-check` (in `src/main/webapp`) passed
    - `npm run build` (in `src/main/webapp`) passed
    - `./mvnw.cmd test` passed after setting `JAVA_HOME` in-command to
      `C:\Program Files\Eclipse Adoptium\jdk-25.0.3.9-hotspot`
    - Maven output includes frontend Vitest step `frontend:2.0.0:npm (npm run test)`

- Phase 77 verification status:
  - `./mvnw.cmd "-Dtest=SecurityAccessTest,AdminUserControllerTest,AdminResultControllerTest" test` failed first
    after adding missing-CSRF assertions (expected TDD red with CSRF still disabled), then passed after enabling CSRF
    in `SecurityConfig`
  - `./mvnw.cmd test` failed once after enabling CSRF because legacy controller tests missed `.with(csrf())`
    (`AdminQuizControllerTest`, `AdminBackupControllerTest`), then passed after updating tests
  - `npm run test` (in `src/main/webapp`) passed (3 files, 9 tests)
  - `npm run type-check` (in `src/main/webapp`) passed
  - `npm run build` (in `src/main/webapp`) passed
  - `./mvnw.cmd test` passed after setting `JAVA_HOME` in-command to
    `C:\Program Files\Eclipse Adoptium\jdk-25.0.3.9-hotspot`
  - Maven output includes frontend Vitest step `frontend:2.0.0:npm (npm run test)`

- Phase 77B verification status:
  - `npm run test -- src/js/csrf.test.ts` failed first (`Cannot find module './csrf'`) as expected TDD red,
    then passed after implementing `csrf.ts`
  - `npm run test` (in `src/main/webapp`) passed (4 files, 12 tests)
  - `npm run type-check` (in `src/main/webapp`) passed
  - `npm run build` (in `src/main/webapp`) passed
  - `./mvnw.cmd test` passed after setting `JAVA_HOME` in-command to
    `C:\Program Files\Eclipse Adoptium\jdk-25.0.3.9-hotspot`
  - Maven output includes frontend Vitest step `frontend:2.0.0:npm (npm run test)`

- Phase 77C verification status:
  - `./mvnw.cmd "-Dtest=AdminQuizControllerTest" test` failed first (`No cookie with name 'XSRF-TOKEN'`) after
    adding the new cookie assertion test (expected TDD red), then passed after updating `SecurityConfig`
  - `npm run test` (in `src/main/webapp`) passed (4 files, 12 tests)
  - `npm run type-check` (in `src/main/webapp`) passed
  - `npm run build` (in `src/main/webapp`) passed
  - `./mvnw.cmd test` passed after setting `JAVA_HOME` in-command to
    `C:\Program Files\Eclipse Adoptium\jdk-25.0.3.9-hotspot`
  - Maven output includes frontend Vitest step `frontend:2.0.0:npm (npm run test)`
  - After real-browser repro still showed `403 /admin/create-quiz`, added frontend fallback bootstrap token fetch in
    `csrf.ts` and re-ran verification (`npm run test`, `npm run type-check`, `npm run build`, `./mvnw.cmd test`) —
    all passed

- Phase 78 verification status:
  - `./mvnw.cmd "-Dtest=LoggingAccessDeniedHandlerTest" test` failed first (expected TDD red because handler class
    was not implemented yet), then passed after adding `LoggingAccessDeniedHandler`
  -
  `./mvnw.cmd "-Dtest=LoggingAccessDeniedHandlerTest,SecurityAccessTest,AdminUserControllerTest,AdminResultControllerTest" test`
  passed after wiring custom `AccessDeniedHandler` in `SecurityConfig`
  - `npm run test` (in `src/main/webapp`) passed (4 files, 14 tests)
  - `npm run type-check` (in `src/main/webapp`) passed
  - `npm run build` (in `src/main/webapp`) passed
  - `./mvnw.cmd test` passed after setting `JAVA_HOME` in-command to
    `C:\Program Files\Eclipse Adoptium\jdk-25.0.3.9-hotspot`
  - Maven output includes frontend Vitest step `frontend:2.0.0:npm (npm run test)`

- Phase 79 verification status:
  - `./mvnw.cmd "-Dtest=LoggingAccessDeniedHandlerTest,AdminQuizControllerTest" test` failed first (expected TDD
    red from new assertions), then passed after CSRF request-handler and logging identity fixes
  - `npm run test` (in `src/main/webapp`) passed (4 files, 14 tests)
  - `npm run type-check` (in `src/main/webapp`) passed
  - `npm run build` (in `src/main/webapp`) passed
    - `./mvnw.cmd test` passed after setting `JAVA_HOME` in-command to
      `C:\Program Files\Eclipse Adoptium\jdk-25.0.3.9-hotspot`
    - Maven output includes frontend Vitest step `frontend:2.0.0:npm (npm run test)`

- Phase 80 verification status:
  - `npm run test -- src/js/admin_functions.test.ts` passed after adding admin rendering XSS safety tests
  - `npm run test` (in `src/main/webapp`) passed (5 files, 17 tests)
  - `npm run type-check` (in `src/main/webapp`) passed
  - `npm run build` (in `src/main/webapp`) passed
    - `./mvnw.cmd test` passed after setting `JAVA_HOME` in-command to
      `C:\Program Files\Eclipse Adoptium\jdk-25.0.3.9-hotspot`
    - Maven output includes frontend Vitest step `frontend:2.0.0:npm (npm run test)`

- Phase 81 verification status:
  - `npm run test -- src/js/create_quiz_documents.test.ts` failed first (`Cannot find module './create_quiz_documents'`)
    as expected TDD red,
    then passed after implementing the rendering helper
  - `npm run test` (in `src/main/webapp`) passed (6 files, 18 tests)
  - `npm run type-check` (in `src/main/webapp`) passed
  - `npm run build` (in `src/main/webapp`) passed
    - `./mvnw.cmd test` passed after setting `JAVA_HOME` in-command to
      `C:\Program Files\Eclipse Adoptium\jdk-25.0.3.9-hotspot`
    - Maven output includes frontend Vitest step `frontend:2.0.0:npm (npm run test)`

- Phase 82 verification status:
  - `./mvnw.cmd "-Dtest=AdminBackupControllerTest,BackupServiceTest" test` produced expected TDD red at first due to
    restore API contract change (InputStream vs byte[] usage in tests/callers)
  - `./mvnw.cmd "-Dtest=BackupServiceTest,AdminBackupControllerTest,BackupRestoreListenerTest" test` passed after
    updating restore callers/tests and implementing streamed restore guardrails
  - `npm run test` (in `src/main/webapp`) passed (6 files, 18 tests)
  - `npm run type-check` (in `src/main/webapp`) passed
  - `npm run build` (in `src/main/webapp`) passed
    - `./mvnw.cmd test` passed after setting `JAVA_HOME` in-command to
      `C:\Program Files\Eclipse Adoptium\jdk-25.0.3.9-hotspot`
    - Maven output includes frontend Vitest step `frontend:2.0.0:npm (npm run test)`

- Phase 83 verification status:
  -
  `./mvnw.cmd "-Dtest=BackupRestoreListenerTest#applyRestore_whenRunscriptFails_rollsBackToPreviousDatabaseState+applyRestore_whenUploadSwapFails_rollsBackPreviousUploadsAndDatabase" test`
  initially failed in test-compile (expected TDD red) because `BackupRestoreListener` had no `replaceUploads(...)`
  override point yet
  - `./mvnw.cmd "-Dtest=BackupRestoreListenerTest,BackupServiceTest,AdminBackupControllerTest" test` passed after
    implementing rollback snapshot orchestration and helper methods
  - `npm run test` (in `src/main/webapp`) passed (6 files, 18 tests)
  - `npm run type-check` (in `src/main/webapp`) passed
  - `npm run build` (in `src/main/webapp`) passed
  - `./mvnw.cmd test` passed after setting `JAVA_HOME` in-command to
    `C:\Program Files\Eclipse Adoptium\jdk-25.0.3.9-hotspot`
  - Maven output includes frontend Vitest step `frontend:2.0.0:npm (npm run test)`

## Finished Phases

### Phase 91: Neuigkeiten First on Homepage + Date-Only Timestamp ✅ COMPLETE

- Moved `Neuigkeiten` to the first card position in `src/main/webapp/src/index.html` (now directly below header/version
  and above leaderboard links).
- Updated news timestamp formatting in `src/main/webapp/src/js/news.ts` to render date only (`de-AT` medium date),
  removing time display.
- Added frontend test coverage in `src/main/webapp/src/js/news.test.ts` to enforce date-only rendering (no `HH:mm`).
- Verification passed: `npm run test -- src/js/news.test.ts`, `npm run test`, `npm run type-check`, `npm run build` (in
  `src/main/webapp`), and `./mvnw.cmd test`.

### Phase 90: Move Neuigkeiten Below Leaderboard Row ✅ COMPLETE

- Moved the `Neuigkeiten` section in `src/main/webapp/src/index.html` to sit directly below the leaderboard row (
  `Punkte`, `Medaillen`, `Durchschnitt`) and above the remaining main navigation cards.
- Kept all existing IDs/classes (`newsSection`, `newsList`) unchanged so no JavaScript logic changes were required.
- Verification passed: `npm run test`, `npm run type-check`, `npm run build` (in `src/main/webapp`) and
  `./mvnw.cmd test`.

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
