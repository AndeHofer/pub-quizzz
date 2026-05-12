# Progress

## Open Tasks

- [ ] Set `JAVA_HOME` correctly in local/CI environment
- [ ] Run backend verification (`.\mvnw.cmd -Dtest=GlobalExceptionHandlerTest test` or full `.\mvnw.cmd test`)

## Finished Phases

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

### Phase 49: Stable Quiz-Details Header Height (No Creator Jump) ✅ COMPLETE

- Reserved fixed visual space for title + creator subtitle in quiz-details header to prevent layout jump when creator
  text appears.
- Switched creator subtitle toggling from `display` changes to `visibility` changes so layout height remains stable.
- Verification passed: `npm run type-check`, `npm run build`, `mvn.cmd test`.
