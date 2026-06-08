# Progress

## Open Tasks

### Phase 104: Low-Risk Axios CSRF Migration (In Progress)

- Step 1 done: Planned incremental frontend-only migration; backend Spring Security CSRF unchanged.
- Step 2 done (TDD RED/GREEN): Migrated `admin_news.ts` to axios/http client.
- Step 3 done (TDD RED/GREEN): Introduced shared `admin-api.ts` and migrated `admin_functions.ts` to shared `apiFetch`.
- Step 4 done (TDD RED/GREEN): Migrated remaining CSRF-helper consumers:
  - `create_quiz.ts`
  - `create_result.ts`
  - `register_user.ts`
  - `admin_results.ts`
  - `403.ts`
- Step 5 done: Removed obsolete frontend CSRF helper files:
  - deleted `src/main/webapp/src/js/csrf.ts`
  - deleted `src/main/webapp/src/js/csrf.test.ts`
- Step 6 done: Full verification passed:
  - `npm --prefix src/main/webapp run test`
  - `npm --prefix src/main/webapp run type-check`
  - `npm --prefix src/main/webapp run build`
  - `./mvnw.cmd clean verify`
- Step 7 done: Cleaned up project progress tracking structure to keep `progress.md` short and focused (active + last 3
  finished phases).
- Step 8 done: Verification after cleanup passed:
  - `npm --prefix src/main/webapp run test`
  - `npm --prefix src/main/webapp run type-check`
  - `npm --prefix src/main/webapp run build`
  - `./mvnw.cmd clean verify`
- Step 9 started: Optimize frontend bundle impact after axios migration.
  - Goal: avoid loading axios bundle on pages that do not use admin API logic.
  - Plan: lazy-load `admin-api` inside admin-only modules that currently import it at top-level.
- Step 9 done (TDD RED/GREEN): Added lazy admin API loader and migrated admin modules to runtime-loading.
  - RED verification:
    - Added `src/main/webapp/src/js/admin-api-loader.test.ts`.
    - `npm --prefix src/main/webapp run test -- src/js/admin-api-loader.test.ts` (FAIL expected: missing module before
      implementation).
  - GREEN implementation:
    - Added `src/main/webapp/src/js/admin-api-loader.ts` with cached dynamic import for `apiFetch`.
    - Updated modules to use lazy `getApiFetch()` instead of top-level `apiFetch` import:
      - `src/main/webapp/src/js/create_quiz.ts`
      - `src/main/webapp/src/js/create_result.ts`
      - `src/main/webapp/src/js/register_user.ts`
      - `src/main/webapp/src/js/admin_results.ts`
  - Focused test verification:
    -
    `npm --prefix src/main/webapp run test -- src/js/admin-api-loader.test.ts src/js/admin-api.test.ts src/js/403.test.ts` (
    PASS)
- Step 10 done: Full verification passed after bundle optimization work.
  - `npm --prefix src/main/webapp run test` (PASS)
  - `npm --prefix src/main/webapp run type-check` (PASS)
  - `npm --prefix src/main/webapp run build` (PASS)
  - `./mvnw.cmd clean verify` (BUILD SUCCESS)
- Step 11 started: Final admin consistency pass.
  - Migrate `admin_login_stats.ts` and `admin_logs.ts` from direct `fetch` to shared `apiFetch` loader path.
  - Simplify `403.ts` trigger API to named dependency object while preserving behavior.
- Step 11 done (TDD RED/GREEN): Completed final consistency pass.
  - RED verification:
    - Updated `403.test.ts` to named dependency object invocation.
    - `npm --prefix src/main/webapp run test -- src/js/403.test.ts` (FAIL expected against old positional API).
  - GREEN implementation:
    - `403.ts`: simplified `triggerRelogin` to use one options object (`logoutExecutor`, `redirect`,
      `refreshCsrfExecutor`).
    - `admin_login_stats.ts`: replaced direct `fetch` with lazy `getApiFetch()` shared path.
    - `admin_logs.ts`: replaced direct `fetch` with lazy `getApiFetch()` shared path.
  - Focused verification:
    -
    `npm --prefix src/main/webapp run test -- src/js/403.test.ts src/js/admin_logs.test.ts src/js/admin_login_stats.test.ts` (
    PASS)
- Step 12 done: Full verification passed after final consistency pass.
  - `npm --prefix src/main/webapp run test` (PASS)
  - `npm --prefix src/main/webapp run type-check` (PASS)
  - `npm --prefix src/main/webapp run build` (PASS)
  - `./mvnw.cmd clean verify` (BUILD SUCCESS)
- Step 13 started: Homepage logout button rollout with shared relogin/logout action.
  - Plan: add visible logout button in `index.html`, wire in `index.ts`, extract shared logout action from `403.ts`, and
    cover with unit tests via TDD.

## Finished Phases

### Phase 103: Neuigkeiten Inline Multi-Event Calendar Actions ✅ COMPLETE

- Added hidden metadata + inline marker support for multiple events.
- Added Google/ICS inline actions with strict validation and safe fallback behavior.
- Added/updated frontend tests and passed full verification.

### Phase 102: Frontend Auth-Expiry Rollout ✅ COMPLETE

- Rolled out centralized auth-expiry handling across high-risk admin/frontend fetch paths.
- Prevented duplicate error UI when redirect is already scheduled.
- Passed full backend+frontend verification pipeline.

### Phase 101: Session Expiry UX + API/Auth Response Consistency ✅ COMPLETE

- Implemented API-style unauthenticated JSON `401` handling while preserving browser redirect behavior.
- Added auth-expiry helper in frontend and wired create-result save flow.
- Set `server.servlet.session.timeout=4h` and verified full pipeline.
