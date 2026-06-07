# Progress

## Open Tasks

- [x] Phase 99: Add failing security test for authenticated `/login` behavior (regression for NoResourceFound `login`)
- [x] Phase 99: Configure Spring Security form login success/target handling to avoid authenticated `/login` dead-end
- [x] Phase 99: Verify with targeted + full backend/frontend pipeline

### Phase 99: Authenticated `/login` Redirect Guard ✅ COMPLETE

- RED observed: `SecurityAccessTest.loginPage_authenticated_redirectsToIndex` fails (actual 200 login page, expected 3xx
  `/`).
- GREEN implemented: `AuthenticatedLoginRedirectFilter` added before `DefaultLoginPageGeneratingFilter` to redirect
  authenticated `GET /login` to `/`.
- Verification passed:
    - `./mvnw.cmd "-Dtest=SecurityAccessTest#loginPage_authenticated_redirectsToIndex" test`
    - `npm run type-check && npm run build` (in `src/main/webapp`)
    - `./mvnw.cmd test` (`BUILD SUCCESS`)

- [x] Phase 98: Add RED frontend tests for reliable "Neu Anmelden" logout retry behavior
- [x] Phase 98: Implement forced-CSRF-refresh logout retry and redirect to `/login?relogin=1` via replace navigation
- [x] Phase 98: Keep no-error UX (always navigate to login even if logout attempts fail)
- [x] Phase 98: Run verification (`./mvnw.cmd test`, `npm run test -- src/js/403.test.ts src/js/csrf.test.ts`,
  `npm run type-check`, `npm run build`)

## Finished Phases

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

### Phase 96: 403 "Neu Anmelden" Logout-Then-Login Flow ✅ COMPLETE

- Replaced secondary action on 403 page with `Neu Anmelden` and added initial logout-then-login client behavior.
- Added unit tests for relogin client action and updated integration assertions for 403 page content.
- Verification passed: targeted frontend tests, targeted security tests, full Maven test pipeline, type-check, build.
