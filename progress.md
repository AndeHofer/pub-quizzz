# Progress

## Open Tasks

- [x] Phase 48: Show selected quiz title in `quiz-details` header instead of default title
- [x] Phase 48: Show optional smaller creator line as `erstellt von <creator>` when available
- [x] Phase 48: Reset header to default when no quiz is selected
- [x] Phase 48: Run full verification (`npm run type-check`, `npm run build`, `mvn.cmd test`)
- [x] Phase 47: Add optional `creator` field to quiz backend model and DTO/request flow
- [x] Phase 47: Add editable admin UI field labeled `Urheber` in create/edit quiz form
- [x] Phase 47: Ensure empty `Urheber` clears stored creator on edit
- [x] Phase 47: Update tests for creator field behavior
- [x] Phase 47: Run full verification (`npm run type-check`, `npm run build`, `mvn.cmd test`)
- [x] Phase 46: Add informative controller call logs to admin controllers lacking endpoint logs
- [x] Phase 46: Skip DevToolsController logging as requested
- [x] Phase 46: Run full verification (`npm run type-check`, `npm run build`, `mvn.cmd test`)
- [x] Phase 45: Plan admin status session cache for `/api/is-admin` on index page
- [x] Phase 45: Implement `sessionStorage` cache key `pub-quizzz-is-admin` and apply cached UI state first
- [x] Phase 45: Run full verification (`npm run type-check`, `npm run build`, `mvn.cmd test`)
- [x] Phase 43: Update index page version fetch to parse plain string response from `/api/version`
- [x] Phase 43: Keep existing badge rendering/session cache behavior with non-empty value guard
- [x] Phase 43: Run full verification (`npm run type-check`, `npm run build`, `mvn.cmd test`)
- [x] Phase 44: Add integration-style security test showing non-admin cannot access admin endpoints even with
  client-side tampering
- [x] Phase 44: Verify admin endpoint access remains forbidden for `ROLE_USER`
- [x] Blockers: Full verification run (`mvnw.cmd test`) was skipped in tool execution and still needs to be run

## Finished Phases

### Phase 48: Dynamic Quiz-Details Header with Creator ✅ COMPLETE

- Replaced static quiz-details heading with dynamic title handling: default `Quiz ansehen`, selected quiz title when a
  quiz is chosen.
- Added optional smaller subtitle `erstellt von <creator>` when detail payload contains a creator.
- Reset heading and subtitle to default when selection is cleared or detail loading fails.
- Verification passed: `npm run type-check`, `npm run build`, `mvn.cmd test`.

### Phase 47: Optional Quiz Creator (`Urheber`) in Backend + Admin Form ✅ COMPLETE

- Added optional `creator` to quiz domain/API flow (`Quiz`, `CreateQuizRequest`, `QuizDTO`, `QuizDetailDTO`) and
  persisted it in create/full-update service paths.
- Implemented admin create/edit form field labeled `Urheber (optional)` and wired payload + edit prefill in
  `create_quiz.ts`.
- Applied requested behavior where clearing `Urheber` in edit mode stores it as empty (`null`) by sending trimmed empty
  values as `null`.
- Updated unit/controller coverage for creator in service and quiz endpoints.
- Verification passed: `npm run type-check`, `npm run build`, `mvn.cmd test`.

### Phase 46: Informative Admin Controller Call Logging ✅ COMPLETE

- Added missing per-endpoint `log.info(...)` call logs in `AdminQuizController`, `AdminResultController`,
  `AdminTeamController`, and `AdminUserController`.
- Added `@Slf4j` to these controllers to support consistent logging.
- Included key request context in logs (IDs, optional filters, question/file counts, and date updates) while avoiding
  sensitive payload details.
- Kept `DevToolsController` logging intentionally unchanged per request.
- Verification passed: `npm run type-check`, `npm run build`, `mvn.cmd test`.
