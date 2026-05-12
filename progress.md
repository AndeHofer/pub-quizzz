# Progress

## Open Tasks

- None.

## Finished Phases

### Phase 52: Remove Spring Security Ignore Warnings for Static Paths ✅ COMPLETE

- Removed `WebSecurityCustomizer` ignore configuration from `SecurityConfig` to follow Spring Security guidance and
  eliminate startup warnings from `WebSecurity.performBuild`.
- Kept static/public paths in `HttpSecurity.authorizeHttpRequests(...).permitAll(...)` and added `/static/**` there so
  all previously ignored startup-warning paths remain explicitly permitted.
- Extended `SecurityAccessTest` with a `/static/**` unauthenticated access check (no redirect to `/login`).
- Verification passed after setting `JAVA_HOME` in the shell session:
  - `./mvnw.cmd test` -> `BUILD SUCCESS` (202 tests, 0 failures, 0 errors, 0 skipped)
  - no `WebSecurity.performBuild` warnings found in the Maven test output.

### Phase 53: Tighten Unauthenticated Access to Login + Favicon Only ✅ COMPLETE

- Updated `SecurityConfig` to keep only `/favicon.ico` as `permitAll`; all other paths now require authentication unless
  separately constrained by role.
- Updated `SecurityAccessTest` to assert unauthenticated requests to `/assets/**`, `/static/**`, and `/uploads/**`
  redirect to `/login`.
- Kept existing checks that protected API/admin paths redirect unauthenticated users and remain forbidden for non-admin
  authenticated users.
- Verification passed: `./mvnw.cmd test` -> `BUILD SUCCESS` (202 tests, 0 failures, 0 errors, 0 skipped).

### Phase 50: Create-Quiz Save Flow Switches to Update + No Redirect on Edit Save ✅ COMPLETE

- Changed `POST /admin/create-quiz` to return created `QuizDTO` so frontend can capture `quizId` after first save.
- Updated `create_quiz.ts` save handling so first save in create mode switches page into edit mode, later saves use
  update endpoint, and edit save no longer navigates away.
- Updated `AdminQuizControllerTest` create endpoint tests to validate JSON response payload (`quizId`).
- Verification: `npm run type-check`, `npm run build` passed; `.\mvnw.cmd test` blocked because `JAVA_HOME` is not
  defined correctly in this environment.
