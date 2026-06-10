# Progress

## Open Tasks

### Phase 117: Shared Safe Query Logging for Security Handlers ✅ COMPLETE

- Step 1 done (TDD RED): Added failing tests in
  `src/test/java/com/ande/pubquizzz/security/LoggingAuthenticationEntryPointTest.java` to require:
  - sanitized full query-string logging (`\r`, `\n`, `\t`, `\uXXXX`),
  - query string length logging,
  - truncation marker for oversized query strings.
- Step 2 done (TDD GREEN): Implemented shared helper and removed duplication:
  - added `src/main/java/com/ande/pubquizzz/security/SecurityLogHelper.java`,
  - refactored `LoggingAuthenticationEntryPoint` to log `queryString` + `queryStringLength` using helper,
  - refactored `LoggingAccessDeniedHandler` to use the same helper for session/header/query sanitization.
- Step 3 done: Verification passed:
  - `./mvnw.cmd "-Dtest=LoggingAuthenticationEntryPointTest,LoggingAccessDeniedHandlerTest" test` (PASS)
  - `npm --prefix src/main/webapp run type-check` (PASS)
  - `npm --prefix src/main/webapp run build` (PASS)
  - `./mvnw.cmd clean verify` (BUILD SUCCESS)

### Phase 116: Safe Full Query String Logging in 403 Handler ✅ COMPLETE

- Step 1 done (TDD RED): Added failing tests in
  `src/test/java/com/ande/pubquizzz/security/LoggingAccessDeniedHandlerTest.java` for:
  - full query string logging with CR/LF/tab/control-char escaping,
  - logged original query length,
  - truncation marker for oversized query strings.
- Step 2 done (TDD GREEN): Implemented sanitized full query-string logging in
  `src/main/java/com/ande/pubquizzz/security/LoggingAccessDeniedHandler.java`:
  - added `queryString` + `queryStringLength` log fields,
  - added control-character escaping (`\r`, `\n`, `\t`, `\uXXXX`),
  - added bounded output with truncation suffix (`...[truncated]`) at max 4096 chars.
- Step 3 done: Verification passed:
  - `./mvnw.cmd -Dtest=LoggingAccessDeniedHandlerTest test` (PASS)
  - `npm --prefix src/main/webapp run type-check` (PASS)
  - `npm --prefix src/main/webapp run build` (PASS)
  - `./mvnw.cmd clean verify` (BUILD SUCCESS)

### Phase 115: Security Package Javadocs ✅ COMPLETE

- Step 1 done: Enumerated all classes under `src/main/java/com/ande/pubquizzz/security` and reviewed class-level Javadoc
  coverage:
    - `LoggingAuthenticationEntryPoint` (already documented in Phase 114),
    - `LoggingAccessDeniedHandler` (needed class-level Javadoc),
    - `SecurityConfig` (needed class-level Javadoc),
    - `CustomUserDetailsService` (needed class-level Javadoc).
- Step 2 done: Added/refined class-level Javadocs to explain purpose and runtime usage for:
    - `src/main/java/com/ande/pubquizzz/security/LoggingAccessDeniedHandler.java`
    - `src/main/java/com/ande/pubquizzz/security/SecurityConfig.java`
    - `src/main/java/com/ande/pubquizzz/security/CustomUserDetailsService.java`
    - plus internal class docs in `SecurityConfig` for nested security filters.
- Step 3 done: Verified annotation/Javadoc ordering and removed unnecessary inline implementation comments in
  `CustomUserDetailsService` to keep code clean.

### Phase 114: Javadoc for LoggingAuthenticationEntryPoint ✅ COMPLETE

- Step 1 done: Inspected `LoggingAuthenticationEntryPoint` and drafted class-level Javadoc scope (purpose +
  mode-specific behavior).
- Step 2 done: Added Javadoc to explain usage in Spring Security flow:
    - invoked as `AuthenticationEntryPoint` for unauthenticated protected requests,
    - API-style requests return `401` + JSON,
    - browser-style requests delegate redirect to `/login`.
- Step 3 done: Verified Javadoc formatting and link targets (`AuthenticationEntryPoint`,
  `LoginUrlAuthenticationEntryPoint`).

### Phase 105: News Section Rebuild (In Progress)

- Step 1 done: Finalized design decisions with user.
  - New dedicated page: `src/main/webapp/src/admin/news.html`
  - No modals for news CRUD.
  - New boolean flag: `showOnHomePage`
  - Homepage behavior: show latest 3 entries where `showOnHomePage=true`
- Step 2 done: Prepared implementation todo list and identified impacted backend/frontend/tests files.
- Step 3 done (TDD RED/GREEN): Backend contract and business logic updated for `showOnHomePage`.
  - RED: Added/updated failing tests first in:
    - `src/test/java/com/ande/pubquizzz/service/NewsServiceTest.java`
    - `src/test/java/com/ande/pubquizzz/controller/AdminNewsControllerTest.java`
    - `src/test/java/com/ande/pubquizzz/controller/UserNewsControllerTest.java`
  - GREEN: Implemented backend changes in:
    - `src/main/java/com/ande/pubquizzz/database/entities/News.java`
    - `src/main/java/com/ande/pubquizzz/dto/NewsDTO.java`
    - `src/main/java/com/ande/pubquizzz/dto/CreateNewsRequest.java`
    - `src/main/java/com/ande/pubquizzz/dto/UpdateNewsRequest.java`
    - `src/main/java/com/ande/pubquizzz/database/repositories/NewsRepository.java`
    - `src/main/java/com/ande/pubquizzz/service/NewsService.java`
  - Result: homepage news query now returns latest 3 where `showOnHomePage=true`; admin CRUD reads/writes flag.
- Step 4 done (TDD RED/GREEN): Rebuilt news admin UI to dedicated one-page flow without modals.
  - RED: Added failing frontend tests first in `src/main/webapp/src/js/admin_news_page.test.ts`.
  - GREEN:
    - Added new page `src/main/webapp/src/admin/news.html`.
    - Added new module `src/main/webapp/src/js/admin_news_page.ts`.
    - Wired admin dashboard news navigation in:
      - `src/main/webapp/src/admin/admin_main.html`
      - `src/main/webapp/src/js/admin_functions.ts`
    - Removed legacy modal-based news module/tests:
      - deleted `src/main/webapp/src/js/admin_news.ts`
      - deleted `src/main/webapp/src/js/admin_news.test.ts`
    - Added Vite entry for new page in `src/main/webapp/vite.config.ts`.
    - Extended TS news type with boolean flag in `src/main/webapp/src/js/types.ts`.
- Step 5 done: Backup/restore compatibility aligned with new news column.
  - Updated test schemas and inserts in:
    - `src/test/java/com/ande/pubquizzz/service/BackupServiceTest.java`
    - `src/test/java/com/ande/pubquizzz/service/BackupRestoreListenerTest.java`
  - Added compatibility migration safeguard in:
    - `src/main/java/com/ande/pubquizzz/listener/BackupRestoreListener.java`
- Step 6 done: Full verification passed.
  - `npm --prefix src/main/webapp run test` (PASS)
  - `npm --prefix src/main/webapp run type-check` (PASS)
  - `npm --prefix src/main/webapp run build` (PASS)
  - `./mvnw.cmd clean verify` (BUILD SUCCESS)
- Step 7 done (hotfix): Added startup-safe schema guard for legacy DBs missing `news.show_on_home_page`.
  - Root cause observed in runtime logs: live DB schema lacked `show_on_home_page` while JPA query expected it.
  - Added guard component:
    - `src/main/java/com/ande/pubquizzz/config/SchemaCompatibilityGuard.java`
  - Added unit test first (RED/GREEN):
    - `src/test/java/com/ande/pubquizzz/config/SchemaCompatibilityGuardTest.java`
  - Verification:
    - `./mvnw.cmd -Dtest=SchemaCompatibilityGuardTest test` (PASS)
    - `npm --prefix src/main/webapp run test` (PASS)
    - `npm --prefix src/main/webapp run type-check` (PASS)
    - `npm --prefix src/main/webapp run build` (PASS)
    - `./mvnw.cmd clean verify` (BUILD SUCCESS)
- Step 8 done (hotfix): Restored missing event-field authoring description on new news admin page.
  - Root cause: during migration from modal-based news UI to dedicated page, the `newsAuthoringHint` block was not
    carried over.
  - RED/GREEN:
    - Added failing test in `src/main/webapp/src/js/admin_news_page.test.ts` for hint markup rendering.
    - Implemented `buildNewsAuthoringHintMarkup()` and wired it in `src/main/webapp/src/js/admin_news_page.ts`.
    - Added hint container to `src/main/webapp/src/admin/news.html`.
  - Verification:
    - `npm --prefix src/main/webapp run test -- src/js/admin_news_page.test.ts` (PASS)
    - `npm --prefix src/main/webapp run test` (PASS)
    - `npm --prefix src/main/webapp run type-check` (PASS)
    - `npm --prefix src/main/webapp run build` (PASS)
    - `./mvnw.cmd clean verify` (BUILD SUCCESS)

### Phase 106: Security Log Correlation Fields ✅ COMPLETE

- Step 1 done: Added additional request correlation fields to 401/403 security logs.
  - Target handlers:
    - `src/main/java/com/ande/pubquizzz/security/LoggingAuthenticationEntryPoint.java`
    - `src/main/java/com/ande/pubquizzz/security/LoggingAccessDeniedHandler.java`
  - Fields: `sessionId`, `sessionValid`, `remoteAddr`, `userAgent`, `forwardedFor`, `forwardedProto`, `forwardedHost`.
- Step 2 done (TDD RED/GREEN): Added/extended unit tests for logging output in:
  - `src/test/java/com/ande/pubquizzz/security/LoggingAuthenticationEntryPointTest.java`
  - `src/test/java/com/ande/pubquizzz/security/LoggingAccessDeniedHandlerTest.java`
- Step 3 done: Implemented logging changes in production handlers.
- Step 4 done: Full verification commands passed:
  - `npm --prefix src/main/webapp run test`
  - `npm --prefix src/main/webapp run type-check`
  - `npm --prefix src/main/webapp run build`
  - `./mvnw.cmd clean verify` (BUILD SUCCESS)

### Phase 107: Logout Action Option Injection Removal ✅ COMPLETE

- Step 1 done: Removed unused runtime option injection from frontend relogin flow while preserving behavior.
  - Target runtime file:
    - `src/main/webapp/src/js/logout-action.ts`
  - Adapted tests to concrete dependencies (`httpClient`, `window.location.replace`):
    - `src/main/webapp/src/js/logout-action.test.ts`
    - `src/main/webapp/src/js/403.test.ts`
- Step 2 done: Kept click wiring behavior unchanged in index/403 entrypoints.
- Step 3 done: Full verification commands passed:
  - `npm --prefix src/main/webapp run test`
  - `npm --prefix src/main/webapp run type-check`
  - `npm --prefix src/main/webapp run build`
  - `./mvnw.cmd clean verify` (BUILD SUCCESS)

### Phase 108: 403 Module Export Simplification ✅ COMPLETE

- Step 1 done: Removed redundant re-export from `403.ts` and kept behavior-only wiring.
  - Target file:
    - `src/main/webapp/src/js/403.ts`
  - Test updates in:
    - `src/main/webapp/src/js/403.test.ts`
- Step 2 done: Verified frontend + backend full pipelines after simplification.
  - `npm --prefix src/main/webapp run test` (PASS)
  - `npm --prefix src/main/webapp run type-check` (PASS)
  - `npm --prefix src/main/webapp run build` (PASS)
  - `./mvnw.cmd clean verify` (BUILD SUCCESS)

### Phase 109A: Frontend Test-Seam Cleanup (Low Risk) ✅ COMPLETE

- Step 1 done: Removed explicit test-only API from admin API loader.
  - Target files:
    - `src/main/webapp/src/js/admin-api-loader.ts`
    - `src/main/webapp/src/js/admin-api-loader.test.ts`
- Step 2 done: Removed logout trigger injection seam from index wiring while keeping behavior unchanged.
  - Target files:
    - `src/main/webapp/src/js/index.ts`
    - `src/main/webapp/src/js/index-logout.test.ts`
- Step 3 done: Run full verification commands:
  - `npm --prefix src/main/webapp run test`
  - `npm --prefix src/main/webapp run type-check`
  - `npm --prefix src/main/webapp run build`
  - `./mvnw.cmd clean verify` (BUILD SUCCESS)

### Phase 109B: Frontend Test-Seam Cleanup (Optional/Moderate) ✅ COMPLETE

- Step 1 done: Removed redirect/scheduler injection seam from auth-expiry helper and adapted tests.
  - Target files:
    - `src/main/webapp/src/js/auth-session.ts`
    - `src/main/webapp/src/js/auth-session.test.ts`
- Step 2 done: Removed redundant leading `export {}` where safe.
  - Candidate files:
    - `src/main/webapp/src/js/admin_logs.ts`
    - `src/main/webapp/src/js/admin_login_stats.ts`
    - `src/main/webapp/src/js/create_quiz.ts`
    - `src/main/webapp/src/js/create_result.ts`
- Step 3 done: Run full verification commands:
  - `npm --prefix src/main/webapp run test`
  - `npm --prefix src/main/webapp run type-check`
  - `npm --prefix src/main/webapp run build`
  - `./mvnw.cmd clean verify` (BUILD SUCCESS)

### Phase 110: Remove Redundant relogin Query Flag ✅ COMPLETE

- Step 1 done (TDD RED): Updated relogin/logout tests to expect redirect to `/login` (without query marker) and verified
  failing state.
  - Target file:
    - `src/main/webapp/src/js/logout-action.test.ts`
- Step 2 done (TDD GREEN): Updated runtime redirect target from `/login?relogin=1` to `/login`.
  - Target file:
    - `src/main/webapp/src/js/logout-action.ts`
- Step 3 done: Run full verification commands:
  - `npm --prefix src/main/webapp run test`
  - `npm --prefix src/main/webapp run type-check`
  - `npm --prefix src/main/webapp run build`
  - `./mvnw.cmd clean verify` (BUILD SUCCESS)

### Phase 111: Spring-Only Logout Navigation ✅ COMPLETE

- Step 1 done (TDD RED/GREEN): Added backend security tests for default Spring logout redirect and CSRF-required logout.
  - Target file:
    - `src/test/java/com/ande/pubquizzz/security/SecurityAccessTest.java`
- Step 2 done (TDD GREEN): Kept default Spring logout target (`/login?logout`) and satisfied new backend tests without
  custom logout success URL.
- Step 3 done (TDD RED/GREEN): Replaced frontend JS-triggered logout with native Spring form POST logout and updated
  frontend tests.
  - Target files:
    - `src/main/webapp/src/index.html`
    - `src/main/webapp/src/js/index.ts`
    - `src/main/webapp/src/js/index-logout.test.ts`
    - `src/main/webapp/src/403.html`
    - `src/main/webapp/src/js/403.test.ts`
- Step 4 done: Removed obsolete logout action module/usages once no longer referenced.
  - Candidate files:
    - `src/main/webapp/src/js/logout-action.ts`
    - `src/main/webapp/src/js/logout-action.test.ts`
- Step 5 done: Run full verification commands:
  - `npm --prefix src/main/webapp run test`
  - `npm --prefix src/main/webapp run type-check`
  - `npm --prefix src/main/webapp run build`
  - `./mvnw.cmd clean verify` (BUILD SUCCESS)

### Phase 112: Remove Default Signed-Out Banner ✅ COMPLETE

- Step 1 done (TDD RED/GREEN): Updated security integration expectation so logout success redirects to `/login` (without
  `?logout`) and verified failing then passing state.
  - Target file:
    - `src/test/java/com/ande/pubquizzz/security/SecurityAccessTest.java`
- Step 2 done (TDD GREEN): Configured explicit Spring logout success URL to `/login`.
  - Target file:
    - `src/main/java/com/ande/pubquizzz/security/SecurityConfig.java`
- Step 3 done: Run full verification commands:
  - `npm --prefix src/main/webapp run test`
  - `npm --prefix src/main/webapp run type-check`
  - `npm --prefix src/main/webapp run build`
  - `./mvnw.cmd clean verify` (BUILD SUCCESS)

### Phase 113: 403 Relogin Must Force Logout ✅ COMPLETE

- Step 1 done (TDD RED/GREEN): Added failing tests proving 403 "Neu Anmelden" path should log out authenticated user
  before showing login, then implemented until green.
  - Target tests:
    - `src/test/java/com/ande/pubquizzz/security/SecurityAccessTest.java`
    - `src/main/webapp/src/js/403.test.ts`
- Step 2 done (TDD GREEN): Replaced 403 relogin link with logout POST form and populated CSRF hidden field from cookie.
  - Target files:
    - `src/main/webapp/src/403.html`
    - `src/main/webapp/src/js/403.ts`
    - `src/main/webapp/src/js/403.test.ts`
- Step 3 done: Run full verification commands:
  - `npm --prefix src/main/webapp run test`
  - `npm --prefix src/main/webapp run type-check`
  - `npm --prefix src/main/webapp run build`
  - `./mvnw.cmd clean verify` (BUILD SUCCESS)

## Finished Phases

### Phase 104: Low-Risk Axios CSRF Migration ✅ COMPLETE

- Migrated frontend admin/API paths to shared HTTP client and lazy admin API loading.
- Removed obsolete CSRF helper frontend files.
- Full verification passed (`npm test`, `npm type-check`, `npm build`, `./mvnw.cmd clean verify`).

### Phase 103: Neuigkeiten Inline Multi-Event Calendar Actions ✅ COMPLETE

- Added hidden metadata + inline marker support for multiple calendar events.
- Added Google/ICS inline actions with validation and fallback behavior.
- Frontend tests and full verification passed.

### Phase 102: Frontend Auth-Expiry Rollout ✅ COMPLETE

- Rolled out centralized auth-expiry handling across high-risk frontend/admin paths.
- Prevented duplicate error UI when redirect is already scheduled.
- Full backend+frontend verification passed.
