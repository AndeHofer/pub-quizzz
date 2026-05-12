# Progress

## Open Tasks

- [x] Phase 52: Remove `WebSecurityCustomizer` ignore rules causing startup warnings
- [x] Phase 52: Keep public static resource access via `permitAll` matchers in `HttpSecurity`
- [x] Phase 52: Attempt security-focused verification/tests and record environment blocker

- [x] Set `JAVA_HOME` for this shell session and run full backend verification

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

### Phase 51: Resource-Not-Found Handling Without Generic Error Noise ✅ COMPLETE

- Added dedicated `NoResourceFoundException` handling in `GlobalExceptionHandler` to return HTTP 404 with a safe JSON
  message (`Ressource nicht gefunden.`).
- Reduced log severity for missing static resources to `DEBUG` to avoid noisy `Unexpected error` stack traces from bot
  probes like `/images/m.php`.
- Added unit test coverage in `GlobalExceptionHandlerTest` for `NoResourceFoundException` mapping.
- Verification: attempted targeted test run `./mvnw.cmd -Dtest=GlobalExceptionHandlerTest test`, blocked because
  `JAVA_HOME` is not defined correctly in this environment.

### Phase 50: Create-Quiz Save Flow Switches to Update + No Redirect on Edit Save ✅ COMPLETE

- Changed `POST /admin/create-quiz` to return created `QuizDTO` so frontend can capture `quizId` after first save.
- Updated `create_quiz.ts` save handling so first save in create mode switches page into edit mode, later saves use
  update endpoint, and edit save no longer navigates away.
- Updated `AdminQuizControllerTest` create endpoint tests to validate JSON response payload (`quizId`).
- Verification: `npm run type-check`, `npm run build` passed; `.\mvnw.cmd test` blocked because `JAVA_HOME` is not
  defined correctly in this environment.
