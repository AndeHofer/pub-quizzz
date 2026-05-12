# Progress

## Open Tasks

- [x] Phase 50: Return created quiz payload (incl. `quizId`) from `POST /admin/create-quiz`
- [x] Phase 50: Keep create/edit page open after save and switch create flow to update mode after first save
- [x] Phase 50: Update controller tests for changed create endpoint response
- [x] Phase 50: Run frontend verification (`npm run type-check`, `npm run build`)
- [x] Phase 50: Attempt backend verification (`.\mvnw.cmd test`) and record environment blocker

- [x] Phase 49: Reserve stable header space on quiz-details to prevent creator subtitle layout jump
- [x] Phase 49: Switch creator subtitle visibility handling without changing layout height
- [x] Phase 49: Run full verification (`npm run type-check`, `npm run build`, `mvn.cmd test`)
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

### Phase 48: Dynamic Quiz-Details Header with Creator ✅ COMPLETE

- Replaced static quiz-details heading with dynamic title handling: default `Quiz ansehen`, selected quiz title when a
  quiz is chosen.
- Added optional smaller subtitle `erstellt von <creator>` when detail payload contains a creator.
- Reset heading and subtitle to default when selection is cleared or detail loading fails.
- Verification passed: `npm run type-check`, `npm run build`, `mvn.cmd test`.
