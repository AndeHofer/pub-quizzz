# Progress

## Open Tasks

- [x] Phase 68: Migrate `admin/register_user.html` to public-template layout style used by non-admin pages
- [x] Phase 68: Keep existing register form behavior/IDs and admin navigation actions unchanged
- [x] Phase 68: Run full verification (`npm run type-check`, `npm run build`, `./mvnw.cmd test`)

- [x] Phase 67A: Remove `changed` usage from admin results UI and shared frontend types
- [x] Phase 67A: Remove `changed` from API DTO mapping/serialization while keeping DB column intact
- [x] Phase 67A: Update tests affected by removing `changed` from API contract
- [x] Phase 67A: Run full verification (`npm run type-check`, `npm run build`, `./mvnw.cmd test`)

- [x] Phase 66: Update admin results sorting tie-breaker so same-date entries are ordered by total points descending
- [x] Phase 66: Keep stable fallback ordering for same date and same total points
- [x] Phase 66: Run full verification (`npm run type-check`, `npm run build`, `./mvnw.cmd test`)

- [x] Phase 65: Refactor admin results row layout to render Q1-Q8/Gesamt as a full-width nested table per result block
- [x] Phase 65: Adjust grouped table styling so desktop no longer clips points columns while keeping mobile behavior
- [x] Phase 65: Run full verification (`npm run type-check`, `npm run build`, `./mvnw.cmd test`)

- [x] Phase 64: Restructure admin results table so per-result block repeats second header row (`Q1`-`Q8`, `Gesamt`)
  inside tbody
- [x] Phase 64: Update grouped results rendering/styles to keep all points columns visible and visually grouped
- [x] Phase 64: Run full verification (`npm run type-check`, `npm run build`, `./mvnw.cmd test`)

- [x] Phase 63: Fix admin results grouped table so Q1-Q8 values are visible again
- [x] Phase 63: Run full verification (`npm run type-check`, `npm run build`, `./mvnw.cmd test`)

- Phase 61 verification status:
  - `npm run type-check` (in `src/main/webapp`) passed
  - `npm run build` (in `src/main/webapp`) passed
  - `./mvnw.cmd test` passed after setting `JAVA_HOME` in-command to
    `C:\Program Files\Eclipse Adoptium\jdk-25.0.3.9-hotspot`

- Phase 62 verification status:
  - `npm run type-check` (in `src/main/webapp`) passed
  - `npm run build` (in `src/main/webapp`) passed
  - `./mvnw.cmd test` passed after setting `JAVA_HOME` in-command to
    `C:\Program Files\Eclipse Adoptium\jdk-25.0.3.9-hotspot`

- Phase 63 verification status:
  - `npm run type-check` (in `src/main/webapp`) passed
  - `npm run build` (in `src/main/webapp`) passed
  - `./mvnw.cmd test` passed after setting `JAVA_HOME` in-command to
    `C:\Program Files\Eclipse Adoptium\jdk-25.0.3.9-hotspot`

- Phase 64 verification status:
  - `npm run type-check` (in `src/main/webapp`) passed
  - `npm run build` (in `src/main/webapp`) passed
  - `./mvnw.cmd test` passed after setting `JAVA_HOME` in-command to
    `C:\Program Files\Eclipse Adoptium\jdk-25.0.3.9-hotspot`

- Phase 65 verification status:
  - `npm run type-check` (in `src/main/webapp`) passed
  - `npm run build` (in `src/main/webapp`) passed
  - `./mvnw.cmd test` passed after setting `JAVA_HOME` in-command to
    `C:\Program Files\Eclipse Adoptium\jdk-25.0.3.9-hotspot`

- Phase 66 verification status:
  - `npm run type-check` (in `src/main/webapp`) passed
  - `npm run build` (in `src/main/webapp`) passed
  - `./mvnw.cmd test` passed after setting `JAVA_HOME` in-command to
    `C:\Program Files\Eclipse Adoptium\jdk-25.0.3.9-hotspot`

- Phase 67A verification status:
  - `npm run type-check` (in `src/main/webapp`) passed
  - `npm run build` (in `src/main/webapp`) passed
  - `./mvnw.cmd test` passed after setting `JAVA_HOME` in-command to
    `C:\Program Files\Eclipse Adoptium\jdk-25.0.3.9-hotspot`

- Phase 68 verification status:
  - `npm run type-check` (in `src/main/webapp`) passed
  - `npm run build` (in `src/main/webapp`) passed
  - `./mvnw.cmd test` passed after setting `JAVA_HOME` in-command to
    `C:\Program Files\Eclipse Adoptium\jdk-25.0.3.9-hotspot`

## Finished Phases

### Phase 68: `register_user` Switched to Public-Template Layout Style ✅ COMPLETE

- Reworked `src/main/webapp/src/admin/register_user.html` to use the same modern card/container layout style as the
  non-admin/public pages (`bg-gray-50`, centered responsive container, card section blocks).
- Added top contextual link `&larr; Admin Bereich`, favicon, and standardized page title styling consistent with recent
  admin pages using the same template language.
- Kept existing register workflow contract unchanged by preserving all relevant element IDs and actions
  (`username`, `password`, `role`, `registerUserBtn`, `backBtn`, `message`) used by `register_user.ts`.
- Kept UI text in German and aligned input placeholders/buttons with German wording.
- Verification passed: `npm run type-check` (webapp), `npm run build` (webapp), and `./mvnw.cmd test` (`BUILD
  SUCCESS`, 206 tests, 0 failures, 0 errors, 0 skipped).

### Phase 67A: Remove `changed` from UI/API Contract (DB Column Kept for Safe Rollout) ✅ COMPLETE

- Removed visual changed-marker usage from `src/main/webapp/src/js/admin_results.ts` so result points render without
  appended `*` markers.
- Removed `changed` from shared frontend and backend answer DTO contracts:
  `src/main/webapp/src/js/types.ts` and `src/main/java/com/ande/pubquizzz/dto/AnswerScoreDTO.java`.
- Updated backend mapping/serialization to stop exposing `changed` in API responses:
  `src/main/java/com/ande/pubquizzz/mapper/ResultMapper.java` and
  `src/main/java/com/ande/pubquizzz/service/ResultService.java`.
- Updated impacted controller/service tests to remove `changed` setter/assertion expectations while preserving behavior
  coverage (`UserQuizControllerTest`, `UserTeamControllerTest`, `ResultServiceDeleteUpdateTest`,
  `ResultServiceTeamResultsTest`).
- Kept persistence field/DB column unchanged intentionally for safe phased rollout (Phase B will remove entity field +
  DB
  column/migration).
- Verification passed: `npm run type-check` (webapp), `npm run build` (webapp), and `./mvnw.cmd test` (`BUILD
  SUCCESS`, 206 tests, 0 failures, 0 errors, 0 skipped).

### Phase 66: Same-Date Results Sorted by Higher Total Points First ✅ COMPLETE

- Updated `compareResultsNewestFirst` in `src/main/webapp/src/js/admin_results.ts` so sorting now applies tie-breakers
  in this order: `quizDate` DESC, then `totalPoints` DESC, then `resultsId` DESC.
- Added robust total-points fallback (`undefined`/non-number -> `0`) so ordering remains deterministic.
- Verification passed: `npm run type-check` (webapp), `npm run build` (webapp), and `./mvnw.cmd test` (`BUILD
  SUCCESS`, 206 tests, 0 failures, 0 errors, 0 skipped).
