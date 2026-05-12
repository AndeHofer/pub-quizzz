# Progress Archive

Archived phases moved out of `progress.md` to keep active progress short and focused.

## Archived Phases

### Phase 53: Tighten Unauthenticated Access to Login + Favicon Only ✅ COMPLETE

- Updated `SecurityConfig` to keep only `/favicon.ico` as `permitAll`; all other paths now require authentication unless
  separately constrained by role.
- Updated `SecurityAccessTest` to assert unauthenticated requests to `/assets/**`, `/static/**`, and `/uploads/**`
  redirect to `/login`.
- Kept existing checks that protected API/admin paths redirect unauthenticated users and remain forbidden for non-admin
  authenticated users.
- Verification passed: `./mvnw.cmd test` -> `BUILD SUCCESS` (202 tests, 0 failures, 0 errors, 0 skipped).

### Phase 52: Remove Spring Security Ignore Warnings for Static Paths ✅ COMPLETE

- Removed `WebSecurityCustomizer` ignore configuration from `SecurityConfig` to follow Spring Security guidance and
  eliminate startup warnings from `WebSecurity.performBuild`.
- Kept static/public paths in `HttpSecurity.authorizeHttpRequests(...).permitAll(...)` and added `/static/**` there so
  all previously ignored startup-warning paths remain explicitly permitted.
- Extended `SecurityAccessTest` with a `/static/**` unauthenticated access check (no redirect to `/login`).
- Verification passed after setting `JAVA_HOME` in the shell session:
  - `./mvnw.cmd test` -> `BUILD SUCCESS` (202 tests, 0 failures, 0 errors, 0 skipped)
  - no `WebSecurity.performBuild` warnings found in the Maven test output.

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

- Archived rollout of per-endpoint admin controller call logging with `@Slf4j` alignment and successful verification.

### Phase 45: Session-Cached Admin Visibility on Index ✅ COMPLETE

- Archived index admin-card session-cache behavior for `/api/is-admin` with safe hidden fallback and successful
  verification.

### Phase 42: Session-Cached Version Badge ✅ COMPLETE

- Archived index version badge session-cache behavior with fallback fetch (`cache: 'no-store'`) and successful
  verification.

### Phase 41: Smoother Page-to-Page Transitions ✅ COMPLETE

- Archived global scrollbar-gutter/body fade transitions and index vertical spacing normalization with shared page
  transition module rollout and verification.

### Phase 40: Public Desktop Width Consistency (`sm:max-w-3xl`) ✅ COMPLETE

- Archived desktop container width standardization to `sm:max-w-3xl` with verification.

### Phase 39: Archive Format Alignment ✅ COMPLETE

- Reworked archive structure to match `progress.md` style with concise, phase-based sections.

### Phase 38: Detailed History Migration ✅ COMPLETE

- Archived removed detailed history from `progress.md` into this file.

### Phase 34: Quiz-Details Iterative Enhancements (Archived Follow-up Stream) ✅ COMPLETE

- Consolidated the prior Phase 34 follow-up stream (UI reveal behavior, title derivation, modal/actions, and repeated
  verification) into one archived phase summary.

### Phase 33: Team Ranking Table Refinements ✅ COMPLETE

- Archived Team table split, medal rank display, service/DTO fixture fixes, and successful verification.

### Phase 32: Additional Overall Leaderboards + Follow-up Refinements ✅ COMPLETE

- Archived medals/average leaderboard implementation plus endpoint cleanup, tie-rule/ranking refinements, UI updates,
  and repeated full verification.

### Phase 31: Quizzes Sorted by `pubDate` DESC ✅ COMPLETE

- Archived frontend sorting update for quiz listing (newest first) with verification.

### Phase 30: Legacy Backup Schema Compatibility ✅ COMPLETE

- Archived compatibility fixes for legacy restore (`question.answer_image_url`, `quiz_document`) with regression
  coverage.

### Phase 29: Backup/Restore FK Fix for `quiz_document` ✅ COMPLETE

- Archived foreign-key restore order and backup table coverage fix.

### Phase 28: Image Answers for Questions 5-8 ✅ COMPLETE

- Archived support for text/image answers in questions 5-8 across backend, frontend, and tests.

### Phase 24: Winner Column on Quiz Archive Page ✅ COMPLETE

- Archived winner column addition for the quiz archive page.

### Phase 22: Orphaned Image Cleanup ✅ COMPLETE

- Archived fix for orphaned image file handling.

### Phase 21: `hint_text` NOT NULL / Flyway Removal Fix ✅ COMPLETE

- Archived schema/data fix tied to `hint_text` nullability and Flyway removal.

### Phase 20: Admin Mobile Responsiveness ✅ COMPLETE

- Archived admin section mobile-friendly improvements.

### Phase 19: Quiz Archive Page ✅ COMPLETE

- Archived implementation of the quiz archive page.

### Phase 18: Team Detail Page ✅ COMPLETE

- Archived implementation of team detail view.

### Phase 17: Quiz Draft Saving + Finished Indicator ✅ COMPLETE

- Archived quiz draft persistence and finished-state indicator.

### Phase 16: Backup/Restore ✅ COMPLETE

- Archived initial backup/restore implementation.

### Phase 15: Two Hint Images (`imageUrlAtStart` + `imageUrlAsHint`) ✅ COMPLETE

- Archived dual hint-image support for quiz questions.

### Phase 14: Optional Quiz Title Field ✅ COMPLETE

- Archived optional title field support for quizzes.

### Phase 13: Quiz Delete FK Violation + Confirmation ✅ COMPLETE

- Archived quiz delete FK violation fix and danger confirmation flow.

### Phase 12: Team Delete FK Violation + Confirmation ✅ COMPLETE

- Archived team delete FK violation fix and danger confirmation flow.

### Phase 11: Rename Team + Edit/Delete Result ✅ COMPLETE

- Archived team renaming and result edit/delete improvements in admin.

### Phase 10: Max 5 Points Per Question ✅ COMPLETE

- Archived enforcement of per-question 5-point maximum.

### Phase 9: BigIntegrationTest DB Seeder ✅ COMPLETE

- Archived manual DB seeder setup for integration testing.

### Phase 8: In-Memory H2 for Tests ✅ COMPLETE

- Archived migration of tests to in-memory H2 setup.

### Phase 7: Remove Export/Leaderboard from Admin Panel ✅ COMPLETE

- Archived admin panel cleanup removing export/leaderboard entries.

### Phase 6: Style Unification (Less Color) ✅ COMPLETE

- Archived UI style harmonization toward a less colorful theme.

### Phase 5: User All-Time Leaderboard ✅ COMPLETE

- Archived user-facing all-time leaderboard implementation.

### Phase 4: Refactoring (7-PR Plan) ✅ COMPLETE

- Archived broad refactoring phase tracked through the original 7-PR plan.

### Phase 3: Test Fixes ✅ COMPLETE

- Archived baseline test stabilization work.

### Phase 2: Full Backend Refactoring ✅ COMPLETE

- Archived full backend refactor milestone.

### Phase 1: TypeScript/Tailwind/Vite Migration ✅ COMPLETE

- Archived frontend migration to TypeScript, Tailwind CSS, and Vite.

Note: Phase numbering reflects original historical labels where applicable.
