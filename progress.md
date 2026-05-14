# Progress

## Open Tasks

- [x] Phase 72: Clarify and normalize `AGENTS.md` wording for agent instructions
- [x] Phase 72: Keep instruction source explicit (`AGENTS.md` authoritative, `README.md` informational)
- [x] Phase 72: Run full verification (`npm run type-check`, `npm run build`, `./mvnw.cmd test`)

- [x] Phase 71: Add extensible app usage statistics table for authentication success events
- [x] Phase 71: Persist `AUTH_SUCCESS` events with username (string) and timestamp through authentication listener
- [x] Phase 71: Add unit/integration tests for usage-event persistence and run full verification (`npm run type-check`, `npm run build`, `./mvnw.cmd test`)

- [x] Phase 70: Align leaderboard top navigation rows so available side links are shown in first line with `Startseite`
  where requested
- [x] Phase 70: Keep medaillen left backlink (`Punkterangliste`) on second line while moving `Durchschnittsrangliste` to
  first line
- [x] Phase 70: Run full verification (`npm run type-check`, `npm run build`, `./mvnw.cmd test`)

- [x] Phase 69: Add directional top navigation row to public leaderboard pages
- [x] Phase 69: Keep `Startseite` top link and add page-specific left/right neighbor links (Punkte -> right Medaillen;
  Medaillen -> left Punkte + right Durchschnitt; Durchschnitt -> left Medaillen)
- [x] Phase 69: Run full verification (`npm run type-check`, `npm run build`, `./mvnw.cmd test`)

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

- Phase 69 verification status:
    - `npm run type-check` (in `src/main/webapp`) passed
    - `npm run build` (in `src/main/webapp`) passed
    - `./mvnw.cmd test` passed after setting `JAVA_HOME` in-command to
      `C:\Program Files\Eclipse Adoptium\jdk-25.0.3.9-hotspot`

- Phase 70 verification status:
    - `npm run type-check` (in `src/main/webapp`) passed
    - `npm run build` (in `src/main/webapp`) passed
    - `./mvnw.cmd test` passed after setting `JAVA_HOME` in-command to
      `C:\Program Files\Eclipse Adoptium\jdk-25.0.3.9-hotspot`

- Phase 71 verification status:
    - `npm run type-check` (in `src/main/webapp`) passed
    - `npm run build` (in `src/main/webapp`) passed
    - `./mvnw.cmd test` passed after setting `JAVA_HOME` in-command to
      `C:\Program Files\Eclipse Adoptium\jdk-25.0.3.9-hotspot`

- Phase 72 verification status:
  - `npm run type-check` (in `src/main/webapp`) passed
  - `npm run build` (in `src/main/webapp`) passed
  - `./mvnw.cmd test` passed after setting `JAVA_HOME` in-command to
    `C:\Program Files\Eclipse Adoptium\jdk-25.0.3.9-hotspot`

## Finished Phases

### Phase 72: Instruction File Cleanup and Clarification ✅ COMPLETE

- Cleaned up wording in `AGENTS.md` for clarity and consistency (progress retention, Maven/NUL wording, folder-ignore
  wording).
- Explicitly documented instruction-source precedence: `README.md` is informational; executable agent instructions are
  in `AGENTS.md`.
- Kept all existing behavioral rules unchanged (`no push/commit`, no worktrees/branches, test requirements,
  German UI text, business rules).
- Verification passed: `npm run type-check` (webapp), `npm run build` (webapp), and `./mvnw.cmd test`.

### Phase 71: Persist Authentication Usage Events in Extensible Statistics Table ✅ COMPLETE

- Added new generic usage-events persistence model with table `app_usage_event` via JPA entity
  `src/main/java/com/ande/pubquizzz/database/entities/UsageEvent.java`.
- Stored authentication success events as `AUTH_SUCCESS` with requested username string + timestamp via
  `UsageEventService` and listener integration.
- Wired login success path in `AuthenticationEventListener` to persist usage rows without changing login behavior.
- Added tests for persistence and event flow:
  - `UsageEventPersistenceTest`
  - `UsageEventServiceTest`
  - `AuthenticationEventListenerTest`
  - `AuthenticationUsageEventPersistenceTest`
- Verification passed: `npm run type-check` (webapp), `npm run build` (webapp), and `./mvnw.cmd test`.

### Phase 70: Leaderboard Top Row Link Placement Refinement ✅ COMPLETE

- Updated leaderboard navigation placement so available destination links appear in the first row with
  `&larr; Startseite`
  where requested.
- `points-leaderboard.html`: first row now contains left `&larr; Startseite` and right `Medaillenspiegel &rarr;`;
  removed
  second-row placeholder layout.
- `medal-leaderboard.html`: first row now contains left `&larr; Startseite` and right `Durchschnittsrangliste &rarr;`,
  with
  `&larr; Punkterangliste` kept on second row.
- Kept `average-leaderboard.html` navigation as previously confirmed.
- Verification passed: `npm run type-check` (webapp), `npm run build` (webapp), and `./mvnw.cmd test`.
