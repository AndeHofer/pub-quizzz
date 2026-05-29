# Progress

## Open Tasks

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

## Finished Phases

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
