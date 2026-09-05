# Progress

## Open Tasks

### Phase 133: Leaderboard Tab Visibility Bugfix ✅ COMPLETE

- Step 1 done: Investigated why no tabs were visible on leaderboard pages.
  - Shared JS year-tab logic is present and built.
  - Root cause confirmed: leaderboard HTML pages are currently missing the `leaderboardYearTabs` container element.
- Step 2 done: Added focused frontend tests for container-backed rendering and the new rule to hide tabs unless at least
  2 years exist.
- Step 3 done: Added the missing tab container to the four leaderboard pages and suppressed rendering for 0-1 years.
- Step 4 done: Focused frontend tests and full project verification passed.
  - `npm --prefix src/main/webapp run test -- leaderboard-page.test.ts leaderboard-pages-markup.test.ts`
  - `npm --prefix src/main/webapp run test`
  - `npm --prefix src/main/webapp run type-check`
  - `npm --prefix src/main/webapp run build`
  - `./mvnw.cmd verify`

### Phase 132: Leaderboard Year Tabs ✅ COMPLETE

- Step 1 done: Added backend controller/service tests first for optional `year` leaderboard queries and
  `/api/leaderboard/years`.
- Step 2 done: Implemented year-aware repository queries, service caching keys, and controller endpoints.
- Step 3 done: Added integration tests for all four leaderboard endpoints with year-specific recalculation.
- Step 4 done: Added frontend tests for dynamic year tabs shared by the four leaderboard pages.
- Step 5 done: Implemented shared year tabs UI and year-aware refetch logic for the four leaderboard pages only.
- Step 6 done: Full verification passed.
  - `npm --prefix src/main/webapp run test`
  - `npm --prefix src/main/webapp run type-check`
  - `npm --prefix src/main/webapp run build`
  - `./mvnw.cmd verify`

### Phase 131: Top-Results Cache Invalidation Bugfix ✅ COMPLETE

- Step 1 done: Investigated why the Top 10 page did not reflect an additional result.
  - Frontend confirmed: leaderboard pages fetch once on window `load`; there is no live refresh.
  - Backend confirmed: `leaderboard.topResults` is cacheable but was not included in global cache invalidation.
- Step 2 done: Added integration regression test `TopResultsLeaderboardCacheInvalidationIntegrationTest`.
  - Verified RED first: after `POST /admin/results`, `/api/leaderboard/top-results` still returned the old cached
    leader.
- Step 3 done: Added `TOP_RESULTS_LEADERBOARD` to `@InvalidateAllAppCaches` so result create/update/delete clears the
  top-results cache too.
- Step 4 done: Full verification passed.
  - `npm --prefix src/main/webapp run test`
  - `npm --prefix src/main/webapp run type-check`
  - `npm --prefix src/main/webapp run build`
  - `./mvnw.cmd verify`

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
