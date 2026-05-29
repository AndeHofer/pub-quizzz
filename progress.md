# Progress

## Open Tasks

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

## Finished Phases

### Phase 81: Rebuilt Audit + Findings Register + Finding #1 XSS Fix ✅ COMPLETE

- Re-ran a full project audit and persisted the current ranked backlog to `findings.md` so findings are no longer lost
  between sessions.
- Added `src/main/webapp/src/js/create_quiz_documents.ts` to centralize quiz-document list markup and enforce escaping
  of untrusted filenames before injecting into HTML attributes/text.
- Updated `src/main/webapp/src/js/create_quiz.ts` to use the new helper in `loadDocuments()` instead of inline string
  interpolation of raw `doc.originalFilename`.
- Added frontend unit coverage in `src/main/webapp/src/js/create_quiz_documents.test.ts` proving malicious filenames are
  escaped in both visible text and `download` attribute context.
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
