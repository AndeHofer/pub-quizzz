# Progress

## Open Tasks

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

## Finished Phases

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

### Phase 65: Full-Width Per-Result Points Mini-Table (No Clipped Q/Gesamt Columns) ✅ COMPLETE

- Refactored `src/main/webapp/src/js/admin_results.ts` result block rendering to keep top-level rows at 3 columns
  (`Team`, `Quiz Datum`, `Aktionen`) and render Q1-Q8/Gesamt in a nested full-width points table below each block.
- Removed mixed 3+9-column row layout pressure so points headers/values no longer compete with top-level column widths.
- Updated `src/main/webapp/src/css/styles.css` with dedicated nested points-table styles (`result-points-*`) including
  full-width layout, compact centered numeric columns, and horizontal overflow handling only inside the points section.
- Updated `src/main/webapp/src/admin/results.html` to remove the forced top-level table min width so desktop layout can
  fit naturally while preserving mobile resilience.
- Verification passed: `npm run type-check` (webapp), `npm run build` (webapp), and `./mvnw.cmd test` (`BUILD
  SUCCESS`, 206 tests, 0 failures, 0 errors, 0 skipped).
