# Progress

## Open Tasks

- [x] Phase 57: Plan and execute maintainability refactor pass (shared frontend quiz helpers, admin dead-code cleanup,
  ResultService validation dedup)
- [x] Phase 57: Extract shared frontend quiz helper module and adopt in `create_result.ts`, `quiz-details.ts`,
  `quizzes.ts`
- [x] Phase 57: Remove unused/dead admin result modal remnants in `admin_functions.ts`
- [x] Phase 57: Consolidate duplicated create/update answer validation paths in `ResultService`
- [x] Phase 57: Run full verification (`npm run type-check`, `npm run build`, `./mvnw.cmd test`)

- [x] Phase 56: Plan result create/edit page flow (separate HTML/TS, quiz-page style)
- [x] Phase 56: Implement frontend result create/edit page and admin navigation wiring
- [x] Phase 56: Align backend result API behavior/validation for create + edit (including disallowing 4 points)
- [x] Phase 56: Update controller/service tests for new result create/edit behavior
- [x] Phase 56: Run full verification (`npm run type-check`, `npm run build`, `./mvnw.cmd test`)
- [x] Phase 56: Keep create-result page open after first save and switch to edit mode (no quiz/team changes afterward)
- [x] Phase 56: Refine create-result layout (non-admin template, quiz/team side-by-side, Q1-4 and Q5-8 two-column)
- [x] Phase 56: Show only finished quizzes on `quiz-details` and `quizzes.html`

- Phase 57 verification status:
  - `npm run type-check` (in `src/main/webapp`) passed
  - `npm run build` (in `src/main/webapp`) passed
  - `./mvnw.cmd test` passed after setting `JAVA_HOME` in-command to
    `C:\Program Files\Eclipse Adoptium\jdk-25.0.3.9-hotspot`

## Finished Phases

### Phase 57: Maintainability Refactor (Shared Helpers + Dead Code Cleanup + Validation Dedup) ✅ COMPLETE

- Added shared frontend quiz helper module `src/main/webapp/src/js/quiz-utils.ts` and adopted it in
  `create_result.ts`, `quiz-details.ts`, and `quizzes.ts` for consistent newest-first sorting, finished filtering,
  and display-title behavior.
- Simplified `create_result.ts` question input rendering and create->edit success path without changing UX behavior
  (first save remains on page, switches to edit mode, and locks quiz/team selection).
- Removed unused admin result modal remnants from `admin_functions.ts` (`buildAddResultForm`, `onSaveAddResult`, and
  stale caches) and cleaned the `editResult` signature.
- Consolidated duplicated result answer validation in `ResultService` into a shared internal validator while
  preserving validation rules and exception messages.
- Removed unused repository method `findAverageLeaderboardRaw` from `ResultRepository`.
- Verification passed: `npm run type-check` (webapp), `npm run build` (webapp), and `./mvnw.cmd test` (`BUILD
  SUCCESS`, 206 tests, 0 failures, 0 errors, 0 skipped).

### Phase 55: Remove Deprecated User Endpoints (`/api/is-admin`, `/api/version`) ✅ COMPLETE

- Removed unused mapped endpoints `/api/is-admin` and `/api/version` from `UserController`; kept only
  `/api/bootstrap` with unchanged payload and cache behavior.
- Kept admin-role evaluation as an internal helper method without exposing a dedicated endpoint.
- Updated `UserControllerTest` to remove old endpoint tests and keep bootstrap-focused coverage.
- Verified there are no remaining source references to `/api/is-admin` or `/api/version`.
- Verification passed: `npm run type-check`, `npm run build`, `./mvnw.cmd -Dtest=UserControllerTest test`,
  `./mvnw.cmd test` (`BUILD SUCCESS`, 201 tests, 0 failures).

### Phase 54: Bootstrap Endpoint with 1-Hour HTTP Cache (No Browser Storage) ✅ COMPLETE

- Added `GET /api/bootstrap` in `UserController` returning both `isAdmin` and `version` via new DTO
  `BootstrapResponse`.
- Configured response caching with `Cache-Control: max-age=3600, must-revalidate, private` to use HTTP caching for one
  hour without `sessionStorage`.
- Reworked index bootstrap logic in `index.ts` to remove `sessionStorage` keys and fetch only `/api/bootstrap` for admin
  card visibility and version badge.
- Added controller tests for bootstrap endpoint payload, authentication behavior, and cache header.
- Verification passed: `npm run type-check`, `npm run build`, `./mvnw.cmd -Dtest=UserControllerTest test`,
  `./mvnw.cmd test` (`BUILD SUCCESS`, 205 tests, 0 failures).
