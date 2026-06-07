# Progress

## Open Tasks

- [x] Phase 100: Build categorized cleanup inventory (unused code, duplicates, complexity, UI consistency)
- [x] Phase 100: Execute Batch A low-risk dedup/dead-code cleanups with tests
- [x] Phase 100: Execute Batch B UI consistency cleanups with tests
- [x] Phase 100: Run verification (`./mvnw.cmd test`, `npm run type-check`, `npm run build`)

### Phase 100: Repo-Wide Cleanup Audit (In Progress)

- Step 1 done: Initialized Phase 100 open-task checklist and moved Phase 99 completed block to Finished Phases to keep
  active section clean.
- Step 2 done: Built initial inventory candidates.
  - Duplicates: `readHttpErrorMessage(...)` duplicated in `admin_functions.ts` and `admin_news.ts`; escape helper
    exported redundantly via `leaderboard-common.ts` while shared helper exists in `html-utils.ts`.
  - UI consistency: remaining `alert(...)` delete-user error branches in `admin_functions.ts`.
  - Complexity hotspot (deferred for this pass unless low-risk): internal duplication in `ResultService`.
  - Security-sensitive area explicitly protected from behavior changes: `/login` redirect guard,
    `LoggingAccessDeniedHandler`,
    and 403 relogin flow.
- Next: Start Batch A with TDD by adding failing frontend test(s) for shared HTTP error helper behavior, then refactor
  to
  shared utility.

- Batch A update: Added RED/GREEN helper tests in `src/main/webapp/src/js/http-utils.test.ts` and implemented shared
  helper `readHttpErrorMessage(...)` in `src/main/webapp/src/js/http-utils.ts`.
- Batch A update: Removed duplicated local HTTP error helper logic from `admin_functions.ts` and `admin_news.ts` by
  importing shared helper.
- Batch A update: Removed duplicated leaderboard escape wrapper export from `leaderboard-common.ts`; leaderboard pages
  now
  import `escapeHtml` directly from `html-utils.ts`.
- Batch A verification passed:
  - `npm run test -- src/js/http-utils.test.ts`
  - `npm run test && npm run type-check && npm run build` (in `src/main/webapp`)

- Batch B update: Replaced remaining delete-user `alert(...)` error branches in `admin_functions.ts` with existing modal
  error UX (`showModal('Fehler', showError(...))`) while keeping German messages.
- Batch B verification passed:
  - `npm run test && npm run type-check && npm run build` (in `src/main/webapp`)

- Final verification for Phase 100 passed:
  - `./mvnw.cmd test` (`BUILD SUCCESS`, tests run: 270 backend + 44 frontend)
  - `npm run type-check && npm run build` (in `src/main/webapp`)

- Dead code decision: `DevToolsController` kept for now (no explicit safe-removal evidence in repository references).

- Batch C update: Simplified internal `ResultService` duplication without behavior changes.
  - Added shared answer mapping helper `mapAnswerScores(Result)` and reused it in quiz/team result projections.
  - Added shared score-row comparator helper `compareScoreRowsDesc(Object[], Object[])` and reused it for team ranking.
  - Kept all ranking, title, and validation behavior unchanged.
- Batch C verification passed:
  -
  `./mvnw.cmd "-Dtest=ResultServiceQuizTest,ResultServiceTeamResultsTest,ResultServiceLeaderboardTest,ResultServiceDeleteUpdateTest,ResultServiceCreateTest" test`
- Final frontend re-verification after Batch C passed:
  - `npm run type-check && npm run build` (in `src/main/webapp`)

### Phase 100: Repo-Wide Cleanup Audit ✅ COMPLETE

- Reduced frontend duplication by introducing shared `readHttpErrorMessage(...)` in `http-utils.ts` and removing
  duplicated
  local implementations.
- Removed unnecessary complexity/duplication in leaderboard escaping by importing `escapeHtml` directly from
  `html-utils.ts` instead of re-export wrapper usage.
- Standardized admin delete-user error UX to existing modal pattern (no behavior/security change, German UI kept).
- Simplified `ResultService` internals via shared answer-mapping and score-row comparator helpers without changing API
  behavior.
- Kept security/relogin behavior unchanged and retained `DevToolsController` pending explicit safe-removal evidence.
- Verification passed: frontend tests/type-check/build, targeted ResultService Maven suite, and full `./mvnw.cmd test`.

- [x] Phase 99: Add failing security test for authenticated `/login` behavior (regression for NoResourceFound `login`)
- [x] Phase 99: Configure Spring Security form login success/target handling to avoid authenticated `/login` dead-end
- [x] Phase 99: Verify with targeted + full backend/frontend pipeline

- [x] Phase 98: Add RED frontend tests for reliable "Neu Anmelden" logout retry behavior
- [x] Phase 98: Implement forced-CSRF-refresh logout retry and redirect to `/login?relogin=1` via replace navigation
- [x] Phase 98: Keep no-error UX (always navigate to login even if logout attempts fail)
- [x] Phase 98: Run verification (`./mvnw.cmd test`, `npm run test -- src/js/403.test.ts src/js/csrf.test.ts`,
  `npm run type-check`, `npm run build`)

## Finished Phases

### Phase 99: Authenticated `/login` Redirect Guard ✅ COMPLETE

- RED observed: `SecurityAccessTest.loginPage_authenticated_redirectsToIndex` fails (actual 200 login page, expected 3xx
  `/`).
- GREEN implemented: `AuthenticatedLoginRedirectFilter` added before `DefaultLoginPageGeneratingFilter` to redirect
  authenticated `GET /login` to `/`.
- Verification passed:
    - `./mvnw.cmd "-Dtest=SecurityAccessTest#loginPage_authenticated_redirectsToIndex" test`
    - `npm run type-check && npm run build` (in `src/main/webapp`)
    - `./mvnw.cmd test` (`BUILD SUCCESS`)

### Phase 98: Reliable "Neu Anmelden" Fresh-Start Flow ✅ COMPLETE

- Added RED tests first for retry/fresh-token behavior:
    - `src/main/webapp/src/js/403.test.ts`
    - `src/main/webapp/src/js/csrf.test.ts`
- Implemented `withRefreshedCsrfHeaders(...)` in `src/main/webapp/src/js/csrf.ts` to force bootstrap refresh and then
  rebuild CSRF headers.
- Updated `src/main/webapp/src/js/403.ts` relogin flow:
    - first logout attempt with current ensured CSRF header,
    - on `403`, force CSRF refresh and retry logout once,
    - always continue to login without user-facing error,
    - use `window.location.replace('/login?relogin=1')` to reduce stale-history effects.
- Verification passed:
    - `npm run test -- src/js/403.test.ts src/js/csrf.test.ts`
    - `./mvnw.cmd "-Dtest=LoggingAccessDeniedHandlerTest,SecurityAccessTest,ForbiddenPageAccessTest" test`
    - `npm run type-check` and `npm run build` (in `src/main/webapp`)
    - `./mvnw.cmd test` (`BUILD SUCCESS`, backend + frontend)

### Phase 97: Fix POST Login CSRF 403 Handling (No POST Forward to `/403.html`) ✅ COMPLETE

- Added tests for invalid login CSRF redirect and no-forward POST handling in `LoggingAccessDeniedHandlerTest` and
  `SecurityAccessTest`.
- Updated `LoggingAccessDeniedHandler` with method-aware branching (GET/HEAD forward, login invalid-CSRF redirect, other
  non-GET plain 403).
- Verification passed: focused security tests + full Maven test pipeline + frontend checks.
