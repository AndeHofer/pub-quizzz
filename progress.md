# Progress

## Open Tasks

### Phase 130: Progress File Cleanup (In Progress)

- Step 1 done: Reviewed `AGENTS.md` constraints and current progress files.
  - Requirement confirmed: keep `progress.md` short (max 100 lines).
  - Requirement confirmed: keep at most 3 finished features in `progress.md`.
- Step 2 done: Reduced `progress.md` to active focus + latest finished phases only.
- Step 3 done: Kept historical details in `progress_archive.md` and removed oversized in-file history.
- Step 4 done: Verified `progress.md` is now concise and policy-compliant.

## Finished Phases

### Phase 129: DRY Refactor for Security Matcher + Leaderboard Init ✅ COMPLETE

- Backend: extracted shared API-style matcher and removed duplicate logic in security entrypoints/config.
- Frontend: added shared leaderboard page initializer and reused it across all leaderboard pages.
- Added tests first (RED), then implementation (GREEN):
  - `src/test/java/com/ande/pubquizzz/security/SecurityRequestMatchersTest.java`
  - `src/main/webapp/src/js/leaderboard-page.test.ts`
- Verification passed:
  - `npm --prefix src/main/webapp run test`
  - `npm --prefix src/main/webapp run type-check`
  - `npm --prefix src/main/webapp run build`
  - `./mvnw.cmd verify`

### Phase 128: Allow Internal Error Dispatch Without Public /error ✅ COMPLETE

- Security allows only `DispatcherType.ERROR` for internal error dispatches.
- `/error` is not public for normal requests; public routes remain restricted.
- Added dispatcher diagnostics in auth logs (`dispatcherType`, `errorRequestUri`, `errorStatusCode`).
- Verification passed:
  - `./mvnw.cmd "-Dtest=SecurityAccessTest,LoggingAuthenticationEntryPointTest" test`
  - `npm --prefix src/main/webapp run type-check`
  - `./mvnw.cmd verify`

### Phase 127: Top-10 Einzelergebnisse Rangliste ✅ COMPLETE

- Added backend endpoint and frontend page for top 10 single results.
- Follow-ups completed: removed date from UI, removed unused frontend `quizDate` type field, simplified leaderboard nav.
- Verification passed:
  - `./mvnw.cmd clean verify`
  - `npm --prefix src/main/webapp run type-check`
