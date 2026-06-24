# Progress

## Open Tasks

### Phase 127: Top-10 Einzelergebnisse Rangliste (In Progress)

- Step 6 in progress: UI-Feinschliff nach Nutzerfeedback.
  - Anforderung: Datum in neuer Top-Results-Rangliste entfernen, da im Quiztitel redundant.
  - Nächster Schritt: RED-Test + minimale Frontend-Anpassung.

- Step 1 in progress: Planung und Scope bestätigt.
  - Endpoint/Page-Naming fixiert: `/api/leaderboard/top-results`, `top-results-leaderboard.html`.
  - Globales Ranking: nur `totalPoints` (gleiche Punkte = gleicher Rang).
  - `quizRank` bleibt olympisch mit Tiebreaker (`total`, `5er`, `3er`) gemäß bestehender Logik.
- Step 2 done (TDD RED): Failing Tests für neue Top-Results-Rangliste hinzugefügt in:
  - `src/test/java/com/ande/pubquizzz/service/ResultServiceLeaderboardTest.java`
  - `src/test/java/com/ande/pubquizzz/controller/UserLeaderboardControllerTest.java`
  - abgedeckt: cap=10, gleiche Punkte = gleicher globaler Rang, mehrfaches Quiz erlaubt, `quizRank`-Berechnung,
    Endpoint `/api/leaderboard/top-results`.
- Step 3 done (TDD GREEN): Backend für Top-Results implementiert:
  - DTO: `src/main/java/com/ande/pubquizzz/dto/TopResultLeaderboardEntry.java`
  - Repository-Query: `src/main/java/com/ande/pubquizzz/database/repositories/ResultRepository.java`
    (`findTopResultsScoreBreakdownRaw` mit Shape `[quizId, quizDate, teamId, teamName, total, fives, threes]`)
  - Service-Logik + Cache: `src/main/java/com/ande/pubquizzz/service/ResultService.java`
    (`getTopResultsLeaderboard`, globales Ranking nur nach Punkten, `quizRank` mit bestehender olympischer
    Tiebreak-Logik)
  - Controller-Endpoint: `src/main/java/com/ande/pubquizzz/controller/UserLeaderboardController.java`
    (`GET /api/leaderboard/top-results`)
  - Cache-Namen erweitert: `src/main/java/com/ande/pubquizzz/config/CacheConfig.java`
  - Cache-Annotation-Test erweitert: `src/test/java/com/ande/pubquizzz/service/CacheAnnotationsTest.java`
- Step 4 done: Frontend für neue Rangliste ergänzt:
  - Typ: `src/main/webapp/src/js/types.ts` (`TopResultLeaderboardEntry`)
  - Seite: `src/main/webapp/src/top-results-leaderboard.html`
  - Script: `src/main/webapp/src/js/top-results-leaderboard.ts`
  - Navigation aktualisiert in:
    - `src/main/webapp/src/index.html`
    - `src/main/webapp/src/points-leaderboard.html`
    - `src/main/webapp/src/medal-leaderboard.html`
    - `src/main/webapp/src/average-leaderboard.html`
    - `src/main/webapp/src/js/team.ts` (Backlink `source=top-results`)
  - Vite-Entry ergänzt: `src/main/webapp/vite.config.ts`
- Step 5 done: Verifikation erfolgreich:
  - `./mvnw.cmd "-Dtest=ResultServiceLeaderboardTest,UserLeaderboardControllerTest,CacheAnnotationsTest" test` (PASS)
  - `npm --prefix src/main/webapp run type-check` (PASS)
  - `./mvnw.cmd clean verify` (BUILD SUCCESS)
- Step 6 done (TDD RED/GREEN): Datum aus Top-Results-UI entfernt.
  - RED: neuer Frontend-Test `src/main/webapp/src/js/top-results-leaderboard.test.ts` prüft, dass `quizDate` nicht
    gerendert wird.
  - GREEN: `src/main/webapp/src/js/top-results-leaderboard.ts` angepasst (nur Quiztitel-Link, kein Datums-Subtext);
    Row-Markup in `buildTopResultsRowMarkup(...)` extrahiert.
  - Verifikation:
    - `npm --prefix src/main/webapp run test -- src/js/top-results-leaderboard.test.ts` (PASS)
    - `npm --prefix src/main/webapp run type-check` (PASS)
    - `./mvnw.cmd clean verify` (BUILD SUCCESS)
- Step 7 in progress: Follow-up zur Typbereinigung.
  - Anforderung: `quizDate` aus Frontend-Typ der Top-Results entfernen (nicht mehr benötigt im UI).
  - Nächster Schritt: RED über Test-Anpassung, dann Type-Cleanup.
- Step 7 done (TDD RED/GREEN): Frontend-Typ bereinigt (`quizDate` entfernt).
  - RED: `src/main/webapp/src/js/top-results-leaderboard.test.ts` Testdaten ohne `quizDate` gesetzt;
    `npm --prefix src/main/webapp run type-check` schlug erwartungsgemäß fehl (fehlendes Pflichtfeld).
  - GREEN: `quizDate` aus `TopResultLeaderboardEntry` entfernt in `src/main/webapp/src/js/types.ts`.
  - Verifikation:
    - `npm --prefix src/main/webapp run type-check` (PASS)
    - `./mvnw.cmd clean verify` (BUILD SUCCESS)
- Step 8 in progress: Navigation auf Leaderboard-Seiten vereinfachen.
  - Anforderung: Oben nur `← Startseite`, keine Querverlinkung zwischen Leaderboards.
  - Nächster Schritt: HTML-Navigationen bereinigen und verifizieren.
- Step 8 done: Top-Navigationen auf Leaderboard-Seiten bereinigt.
  - Angepasst in:
    - `src/main/webapp/src/points-leaderboard.html`
    - `src/main/webapp/src/medal-leaderboard.html`
    - `src/main/webapp/src/average-leaderboard.html`
    - `src/main/webapp/src/top-results-leaderboard.html`
  - Ergebnis: jeweils oben nur noch Link `← Startseite`.
  - Verifikation:
    - `npm --prefix src/main/webapp run type-check` (PASS)
    - `./mvnw.cmd clean verify` (BUILD SUCCESS)

### Phase 126: Fix Team Results Score-Row Index Crash ✅ COMPLETE

- Step 1 done (TDD RED): Added failing regression test in
  `src/test/java/com/ande/pubquizzz/service/ResultServiceTeamResultsTest.java` for non-empty
  `findScoresByQuizIds(...)` rows with shape `[quizId, teamId, teamName, totalPoints, fives, threes]`.
  - test reproduced manual `ClassCastException` in team-rank sorting path.
- Step 2 done (TDD GREEN): Fixed score-row index usage in
  `src/main/java/com/ande/pubquizzz/service/ResultService.java`:
  - `hasSameScore(...)` now reads score tuple from indexes `3/4/5` (total/fives/threes),
  - `compareScoreRowsDesc(...)` now compares indexes `3/4/5` instead of stale `2/3/4`.
- Step 3 done: Scanned `ResultService` raw-row consumers for similar index mismatches after teamId migration.
  - verified `findScoresByQuizIds` ranking in quiz summaries already uses `3/4/5`,
  - verified leaderboard and medal row mappings align with their updated query shapes,
  - no additional mismatches found.
- Step 4 done: Verification passed:
  - `./mvnw.cmd "-Dtest=ResultServiceTeamResultsTest" test` (PASS)
  - `./mvnw.cmd clean verify` (BUILD SUCCESS)
  - `npm --prefix src/main/webapp run type-check` (PASS)

### Phase 125: Team Detail URL Hard Cutover to teamId ✅ COMPLETE

- Step 1 done (TDD RED): Updated backend tests for id-based team detail route and DTO/query shapes in:
  - `src/test/java/com/ande/pubquizzz/controller/UserTeamControllerTest.java`
  - `src/test/java/com/ande/pubquizzz/service/ResultServiceTeamResultsTest.java`
  - `src/test/java/com/ande/pubquizzz/service/ResultServiceLeaderboardTest.java`
  - `src/test/java/com/ande/pubquizzz/service/ResultServiceQuizTest.java`
  - `src/test/java/com/ande/pubquizzz/controller/UserLeaderboardControllerTest.java`
  - `src/test/java/com/ande/pubquizzz/controller/UserQuizControllerTest.java`
- Step 2 done (TDD GREEN): Implemented backend hard cutover to id-based team detail lookup and teamId propagation in
  leaderboard/quiz DTOs:
  - `src/main/java/com/ande/pubquizzz/controller/UserTeamController.java`
  - `src/main/java/com/ande/pubquizzz/service/ResultService.java`
  - `src/main/java/com/ande/pubquizzz/database/repositories/ResultRepository.java`
  - `src/main/java/com/ande/pubquizzz/dto/PointsLeaderboardEntry.java`
  - `src/main/java/com/ande/pubquizzz/dto/AverageLeaderboardEntry.java`
  - `src/main/java/com/ande/pubquizzz/dto/MedalLeaderboardEntry.java`
  - `src/main/java/com/ande/pubquizzz/dto/QuizResultEntry.java`
  - `src/main/java/com/ande/pubquizzz/dto/QuizSummaryDTO.java`
- Step 3 done: Updated frontend types and links to `?teamId=` and id-based API calls:
  - `src/main/webapp/src/js/types.ts`
  - `src/main/webapp/src/js/team.ts`
  - `src/main/webapp/src/js/points-leaderboard.ts`
  - `src/main/webapp/src/js/average-leaderboard.ts`
  - `src/main/webapp/src/js/medal-leaderboard.ts`
  - `src/main/webapp/src/js/quiz.ts`
  - `src/main/webapp/src/js/quizzes.ts`
- Step 4 done: Verification passed:
  -
  `./mvnw.cmd "-Dtest=UserTeamControllerTest,ResultServiceTeamResultsTest,ResultServiceLeaderboardTest,ResultServiceQuizTest,UserLeaderboardControllerTest,UserQuizControllerTest" test` (
  PASS)
  - `./mvnw.cmd clean verify` (BUILD SUCCESS)
  - `npm --prefix src/main/webapp run type-check` (PASS)
- Step 5 done (follow-up from full verify): Updated cache annotation contract test for changed ResultService method
  signature:
  - `src/test/java/com/ande/pubquizzz/service/CacheAnnotationsTest.java`

### Phase 124: Remove Evictions From Cache Metrics ✅ COMPLETE

- Step 1 done (TDD RED): Updated failing formatter/snapshot expectations in
  `src/test/java/com/ande/pubquizzz/service/CacheMetricsLoggingServiceTest.java`:
    - removed `evictions` / `deltaEvictions` constructor arguments and assertions,
    - asserted `EVICTIONS` header no longer appears,
    - kept aligned `|`-column checks and totals assertions for remaining columns.
- Step 2 done (TDD GREEN): Refactored
  `src/main/java/com/ande/pubquizzz/service/CacheMetricsLoggingService.java` to remove eviction metrics end-to-end:
    - dropped eviction fields from `CacheMetricsSnapshot`,
    - removed eviction totals and delta tracking from collection/aggregation,
    - removed eviction column from pretty table row/separator/header/TOTAL rendering,
    - retained adaptive-width alignment for remaining columns.
- Step 3 done: Verification passed:
    - `./mvnw.cmd "-Dtest=CacheMetricsLoggingServiceTest" test` (PASS)
    - `./mvnw.cmd clean verify` (BUILD SUCCESS)
    - `npm --prefix src/main/webapp run type-check` (PASS)

### Phase 123: Short Cache Invalidation Logs ✅ COMPLETE

- Step 1 done (TDD RED): Added failing tests for short invalidation log entries in:
    - `src/test/java/com/ande/pubquizzz/cache/CacheInvalidationLoggingAspectTest.java`
    - `src/test/java/com/ande/pubquizzz/controller/AdminCacheControllerTest.java`
- Step 2 done (TDD GREEN): Implemented centralized automatic invalidation logging via aspect and aligned manual log
  wording:
    - added `src/main/java/com/ande/pubquizzz/cache/CacheInvalidationLoggingAspect.java`
    - updated `src/main/java/com/ande/pubquizzz/controller/AdminCacheController.java`
    - automatic log format: `Cache invalidated: all caches (trigger=<Class>.<method>)`
    - manual log format: `Cache invalidated: all caches (trigger=admin, cleared=<n>)`
- Step 3 done: Verification passed:
    - `./mvnw.cmd "-Dtest=CacheInvalidationLoggingAspectTest,AdminCacheControllerTest" test` (PASS)
    - `./mvnw.cmd clean verify` (BUILD SUCCESS)
    - `npm --prefix src/main/webapp run type-check` (PASS)

### Phase 122: Align Cache Metrics Table Columns ✅ COMPLETE

- Step 1 done (TDD RED): Added failing formatter test expectations for aligned `|` columns and long cache-name coverage
  in:
    - `src/test/java/com/ande/pubquizzz/service/CacheMetricsLoggingServiceTest.java`
- Step 2 done (TDD GREEN): Reworked pretty metrics logging into adaptive-width table rendering with aligned separators:
    - `src/main/java/com/ande/pubquizzz/service/CacheMetricsLoggingService.java`
    - added shared table helpers for row and separator rendering
    - no cache-name truncation; column width adapts to longest cache name/value content
    - totals emitted as aligned table row (`TOTAL`) instead of free-form text
- Step 3 done: Verification passed:
    - `./mvnw.cmd "-Dtest=CacheMetricsLoggingServiceTest" test` (PASS)
    - `./mvnw.cmd clean verify` (BUILD SUCCESS)
    - `npm --prefix src/main/webapp run type-check` (PASS)

### Phase 121: Pretty Aggregated Cache Metrics Log ✅ COMPLETE

- Step 1 done (TDD RED): Added failing tests for pretty aggregated cache metrics formatting in:
    - `src/test/java/com/ande/pubquizzz/service/CacheMetricsLoggingServiceTest.java`
- Step 2 done (TDD GREEN): Replaced per-cache log lines with one readable aggregated pretty log entry per interval in:
    - `src/main/java/com/ande/pubquizzz/service/CacheMetricsLoggingService.java`
    - added formatter path (`buildPrettyMetricsLog(...)`) and single-entry emission from `logCacheMetrics()`
- Step 3 done: Full verification passed:
    - `./mvnw.cmd clean verify` (BUILD SUCCESS)
    - `npm --prefix src/main/webapp run type-check` (PASS)

### Phase 120: Cache Hit/Miss Logging ✅ COMPLETE

- Step 1 done (TDD RED): Added failing tests for cache metrics snapshot collection and delta behavior:
    - `src/test/java/com/ande/pubquizzz/service/CacheMetricsLoggingServiceTest.java`
- Step 2 done (TDD GREEN): Implemented periodic cache metrics logger service:
    - `src/main/java/com/ande/pubquizzz/service/CacheMetricsLoggingService.java`
    - logs per-cache metrics: hits, misses, hitRate, evictions, deltaHits, deltaMisses, deltaEvictions
    - skips non-Caffeine caches and respects enable flag
- Step 3 done: Enabled scheduler and cache stats support:
    - `src/main/java/com/ande/pubquizzz/PubQuizzzApplication.java` (`@EnableScheduling`)
    - `src/main/java/com/ande/pubquizzz/config/CacheConfig.java` (`recordStats()`)
- Step 4 done: Added configurable properties:
    - `src/main/resources/application.properties`
        - `app.cache.metrics.enabled=true`
        - `app.cache.metrics.log-interval-ms=60000`
- Step 5 done: Verification passed:
    - `./mvnw.cmd "-Dtest=CacheMetricsLoggingServiceTest" test` (PASS)
    - `./mvnw.cmd clean verify` (BUILD SUCCESS)
    - `npm --prefix src/main/webapp run type-check` (PASS)

### Phase 119: Global Cache Invalidation on Successful Writes ✅ COMPLETE

- Step 1 done (TDD RED): Updated cache annotation contract tests to require shared global invalidation annotation on
  all write methods:
    - `src/test/java/com/ande/pubquizzz/service/CacheAnnotationsTest.java`
    - (and aligned service-specific cache annotation expectation in
      `src/test/java/com/ande/pubquizzz/service/NewsServiceTest.java`)
- Step 2 done (TDD GREEN): Introduced shared annotation for clean-all eviction with default
  `beforeInvocation=false` behavior (only after successful write):
    - `src/main/java/com/ande/pubquizzz/cache/InvalidateAllAppCaches.java`
- Step 3 done: Refactored write services to use shared clean-all annotation, removed per-method fine-grained eviction
  blocks:
    - `src/main/java/com/ande/pubquizzz/service/QuizService.java`
    - `src/main/java/com/ande/pubquizzz/service/ResultService.java`
    - `src/main/java/com/ande/pubquizzz/service/TeamService.java`
    - `src/main/java/com/ande/pubquizzz/service/NewsService.java`
- Step 4 done: Verified cache admin endpoint/service behavior and cache contracts still pass:
    - `./mvnw.cmd "-Dtest=CacheAnnotationsTest,NewsServiceTest,CacheAdminServiceTest,AdminCacheControllerTest" test` (
      PASS)
- Step 5 done: Full verification passed:
    - `./mvnw.cmd clean verify` (BUILD SUCCESS)
    - `npm --prefix src/main/webapp run type-check` (PASS)

### Phase 118: Backend Caching + Admin Cache Invalidate ✅ COMPLETE

- Step 1 done (TDD RED/GREEN): Added cache admin tests first, then implemented backend cache admin feature:
    - tests added:
        - `src/test/java/com/ande/pubquizzz/service/CacheAdminServiceTest.java`
        - `src/test/java/com/ande/pubquizzz/controller/AdminCacheControllerTest.java`
    - implementation added:
        - `src/main/java/com/ande/pubquizzz/service/CacheAdminService.java`
        - `src/main/java/com/ande/pubquizzz/controller/AdminCacheController.java`
        - `src/main/java/com/ande/pubquizzz/config/CacheConfig.java`
    - app wiring updated:
        - `src/main/java/com/ande/pubquizzz/PubQuizzzApplication.java` (`@EnableCaching`)
        - `pom.xml` (cache + caffeine dependencies)
- Step 2 done: Added service-level caching and write-triggered cache invalidation:
    - `src/main/java/com/ande/pubquizzz/service/QuizService.java`
    - `src/main/java/com/ande/pubquizzz/service/ResultService.java`
    - `src/main/java/com/ande/pubquizzz/service/TeamService.java`
    - `src/main/java/com/ande/pubquizzz/service/NewsService.java`
- Step 3 done (TDD RED/GREEN): Added/updated backend tests for caching contract and compatibility:
    - added `src/test/java/com/ande/pubquizzz/service/CacheAnnotationsTest.java`
    - updated `src/test/java/com/ande/pubquizzz/service/NewsServiceTest.java`
    - updated `src/test/java/com/ande/pubquizzz/controller/SecurityTestConfig.java` with test cache manager bean.
- Step 4 done (TDD RED/GREEN): Added admin UI button + frontend behavior/tests for manual cache clear-all:
    - updated `src/main/webapp/src/admin/admin_main.html` (button + status message container)
    - updated `src/main/webapp/src/js/admin_functions.ts` (invalidate handler)
    - updated `src/main/webapp/src/js/admin_functions.test.ts` (new test coverage)
- Step 5 done: Full verification passed:
    - `./mvnw.cmd clean verify` (BUILD SUCCESS)
    - `npm --prefix src/main/webapp run type-check` (PASS)

### Phase 117: Shared Safe Query Logging for Security Handlers ✅ COMPLETE

- Step 1 done (TDD RED): Added failing tests in
  `src/test/java/com/ande/pubquizzz/security/LoggingAuthenticationEntryPointTest.java` to require:
  - sanitized full query-string logging (`\r`, `\n`, `\t`, `\uXXXX`),
  - query string length logging,
  - truncation marker for oversized query strings.
- Step 2 done (TDD GREEN): Implemented shared helper and removed duplication:
  - added `src/main/java/com/ande/pubquizzz/security/SecurityLogHelper.java`,
  - refactored `LoggingAuthenticationEntryPoint` to log `queryString` + `queryStringLength` using helper,
  - refactored `LoggingAccessDeniedHandler` to use the same helper for session/header/query sanitization.
- Step 3 done: Verification passed:
  - `./mvnw.cmd "-Dtest=LoggingAuthenticationEntryPointTest,LoggingAccessDeniedHandlerTest" test` (PASS)
  - `npm --prefix src/main/webapp run type-check` (PASS)
  - `npm --prefix src/main/webapp run build` (PASS)
  - `./mvnw.cmd clean verify` (BUILD SUCCESS)

### Phase 116: Safe Full Query String Logging in 403 Handler ✅ COMPLETE

- Step 1 done (TDD RED): Added failing tests in
  `src/test/java/com/ande/pubquizzz/security/LoggingAccessDeniedHandlerTest.java` for:
  - full query string logging with CR/LF/tab/control-char escaping,
  - logged original query length,
  - truncation marker for oversized query strings.
- Step 2 done (TDD GREEN): Implemented sanitized full query-string logging in
  `src/main/java/com/ande/pubquizzz/security/LoggingAccessDeniedHandler.java`:
  - added `queryString` + `queryStringLength` log fields,
  - added control-character escaping (`\r`, `\n`, `\t`, `\uXXXX`),
  - added bounded output with truncation suffix (`...[truncated]`) at max 4096 chars.
- Step 3 done: Verification passed:
  - `./mvnw.cmd -Dtest=LoggingAccessDeniedHandlerTest test` (PASS)
  - `npm --prefix src/main/webapp run type-check` (PASS)
  - `npm --prefix src/main/webapp run build` (PASS)
  - `./mvnw.cmd clean verify` (BUILD SUCCESS)

### Phase 115: Security Package Javadocs ✅ COMPLETE

- Step 1 done: Enumerated all classes under `src/main/java/com/ande/pubquizzz/security` and reviewed class-level Javadoc
  coverage:
    - `LoggingAuthenticationEntryPoint` (already documented in Phase 114),
    - `LoggingAccessDeniedHandler` (needed class-level Javadoc),
    - `SecurityConfig` (needed class-level Javadoc),
    - `CustomUserDetailsService` (needed class-level Javadoc).
- Step 2 done: Added/refined class-level Javadocs to explain purpose and runtime usage for:
    - `src/main/java/com/ande/pubquizzz/security/LoggingAccessDeniedHandler.java`
    - `src/main/java/com/ande/pubquizzz/security/SecurityConfig.java`
    - `src/main/java/com/ande/pubquizzz/security/CustomUserDetailsService.java`
    - plus internal class docs in `SecurityConfig` for nested security filters.
- Step 3 done: Verified annotation/Javadoc ordering and removed unnecessary inline implementation comments in
  `CustomUserDetailsService` to keep code clean.

### Phase 114: Javadoc for LoggingAuthenticationEntryPoint ✅ COMPLETE

- Step 1 done: Inspected `LoggingAuthenticationEntryPoint` and drafted class-level Javadoc scope (purpose +
  mode-specific behavior).
- Step 2 done: Added Javadoc to explain usage in Spring Security flow:
    - invoked as `AuthenticationEntryPoint` for unauthenticated protected requests,
    - API-style requests return `401` + JSON,
    - browser-style requests delegate redirect to `/login`.
- Step 3 done: Verified Javadoc formatting and link targets (`AuthenticationEntryPoint`,
  `LoginUrlAuthenticationEntryPoint`).

### Phase 105: News Section Rebuild (In Progress)

- Step 1 done: Finalized design decisions with user.
  - New dedicated page: `src/main/webapp/src/admin/news.html`
  - No modals for news CRUD.
  - New boolean flag: `showOnHomePage`
  - Homepage behavior: show latest 3 entries where `showOnHomePage=true`
- Step 2 done: Prepared implementation todo list and identified impacted backend/frontend/tests files.
- Step 3 done (TDD RED/GREEN): Backend contract and business logic updated for `showOnHomePage`.
  - RED: Added/updated failing tests first in:
    - `src/test/java/com/ande/pubquizzz/service/NewsServiceTest.java`
    - `src/test/java/com/ande/pubquizzz/controller/AdminNewsControllerTest.java`
    - `src/test/java/com/ande/pubquizzz/controller/UserNewsControllerTest.java`
  - GREEN: Implemented backend changes in:
    - `src/main/java/com/ande/pubquizzz/database/entities/News.java`
    - `src/main/java/com/ande/pubquizzz/dto/NewsDTO.java`
    - `src/main/java/com/ande/pubquizzz/dto/CreateNewsRequest.java`
    - `src/main/java/com/ande/pubquizzz/dto/UpdateNewsRequest.java`
    - `src/main/java/com/ande/pubquizzz/database/repositories/NewsRepository.java`
    - `src/main/java/com/ande/pubquizzz/service/NewsService.java`
  - Result: homepage news query now returns latest 3 where `showOnHomePage=true`; admin CRUD reads/writes flag.
- Step 4 done (TDD RED/GREEN): Rebuilt news admin UI to dedicated one-page flow without modals.
  - RED: Added failing frontend tests first in `src/main/webapp/src/js/admin_news_page.test.ts`.
  - GREEN:
    - Added new page `src/main/webapp/src/admin/news.html`.
    - Added new module `src/main/webapp/src/js/admin_news_page.ts`.
    - Wired admin dashboard news navigation in:
      - `src/main/webapp/src/admin/admin_main.html`
      - `src/main/webapp/src/js/admin_functions.ts`
    - Removed legacy modal-based news module/tests:
      - deleted `src/main/webapp/src/js/admin_news.ts`
      - deleted `src/main/webapp/src/js/admin_news.test.ts`
    - Added Vite entry for new page in `src/main/webapp/vite.config.ts`.
    - Extended TS news type with boolean flag in `src/main/webapp/src/js/types.ts`.
- Step 5 done: Backup/restore compatibility aligned with new news column.
  - Updated test schemas and inserts in:
    - `src/test/java/com/ande/pubquizzz/service/BackupServiceTest.java`
    - `src/test/java/com/ande/pubquizzz/service/BackupRestoreListenerTest.java`
  - Added compatibility migration safeguard in:
    - `src/main/java/com/ande/pubquizzz/listener/BackupRestoreListener.java`
- Step 6 done: Full verification passed.
  - `npm --prefix src/main/webapp run test` (PASS)
  - `npm --prefix src/main/webapp run type-check` (PASS)
  - `npm --prefix src/main/webapp run build` (PASS)
  - `./mvnw.cmd clean verify` (BUILD SUCCESS)
- Step 7 done (hotfix): Added startup-safe schema guard for legacy DBs missing `news.show_on_home_page`.
  - Root cause observed in runtime logs: live DB schema lacked `show_on_home_page` while JPA query expected it.
  - Added guard component:
    - `src/main/java/com/ande/pubquizzz/config/SchemaCompatibilityGuard.java`
  - Added unit test first (RED/GREEN):
    - `src/test/java/com/ande/pubquizzz/config/SchemaCompatibilityGuardTest.java`
  - Verification:
    - `./mvnw.cmd -Dtest=SchemaCompatibilityGuardTest test` (PASS)
    - `npm --prefix src/main/webapp run test` (PASS)
    - `npm --prefix src/main/webapp run type-check` (PASS)
    - `npm --prefix src/main/webapp run build` (PASS)
    - `./mvnw.cmd clean verify` (BUILD SUCCESS)
- Step 8 done (hotfix): Restored missing event-field authoring description on new news admin page.
  - Root cause: during migration from modal-based news UI to dedicated page, the `newsAuthoringHint` block was not
    carried over.
  - RED/GREEN:
    - Added failing test in `src/main/webapp/src/js/admin_news_page.test.ts` for hint markup rendering.
    - Implemented `buildNewsAuthoringHintMarkup()` and wired it in `src/main/webapp/src/js/admin_news_page.ts`.
    - Added hint container to `src/main/webapp/src/admin/news.html`.
  - Verification:
    - `npm --prefix src/main/webapp run test -- src/js/admin_news_page.test.ts` (PASS)
    - `npm --prefix src/main/webapp run test` (PASS)
    - `npm --prefix src/main/webapp run type-check` (PASS)
    - `npm --prefix src/main/webapp run build` (PASS)
    - `./mvnw.cmd clean verify` (BUILD SUCCESS)

### Phase 106: Security Log Correlation Fields ✅ COMPLETE

- Step 1 done: Added additional request correlation fields to 401/403 security logs.
  - Target handlers:
    - `src/main/java/com/ande/pubquizzz/security/LoggingAuthenticationEntryPoint.java`
    - `src/main/java/com/ande/pubquizzz/security/LoggingAccessDeniedHandler.java`
  - Fields: `sessionId`, `sessionValid`, `remoteAddr`, `userAgent`, `forwardedFor`, `forwardedProto`, `forwardedHost`.
- Step 2 done (TDD RED/GREEN): Added/extended unit tests for logging output in:
  - `src/test/java/com/ande/pubquizzz/security/LoggingAuthenticationEntryPointTest.java`
  - `src/test/java/com/ande/pubquizzz/security/LoggingAccessDeniedHandlerTest.java`
- Step 3 done: Implemented logging changes in production handlers.
- Step 4 done: Full verification commands passed:
  - `npm --prefix src/main/webapp run test`
  - `npm --prefix src/main/webapp run type-check`
  - `npm --prefix src/main/webapp run build`
  - `./mvnw.cmd clean verify` (BUILD SUCCESS)

### Phase 107: Logout Action Option Injection Removal ✅ COMPLETE

- Step 1 done: Removed unused runtime option injection from frontend relogin flow while preserving behavior.
  - Target runtime file:
    - `src/main/webapp/src/js/logout-action.ts`
  - Adapted tests to concrete dependencies (`httpClient`, `window.location.replace`):
    - `src/main/webapp/src/js/logout-action.test.ts`
    - `src/main/webapp/src/js/403.test.ts`
- Step 2 done: Kept click wiring behavior unchanged in index/403 entrypoints.
- Step 3 done: Full verification commands passed:
  - `npm --prefix src/main/webapp run test`
  - `npm --prefix src/main/webapp run type-check`
  - `npm --prefix src/main/webapp run build`
  - `./mvnw.cmd clean verify` (BUILD SUCCESS)

### Phase 108: 403 Module Export Simplification ✅ COMPLETE

- Step 1 done: Removed redundant re-export from `403.ts` and kept behavior-only wiring.
  - Target file:
    - `src/main/webapp/src/js/403.ts`
  - Test updates in:
    - `src/main/webapp/src/js/403.test.ts`
- Step 2 done: Verified frontend + backend full pipelines after simplification.
  - `npm --prefix src/main/webapp run test` (PASS)
  - `npm --prefix src/main/webapp run type-check` (PASS)
  - `npm --prefix src/main/webapp run build` (PASS)
  - `./mvnw.cmd clean verify` (BUILD SUCCESS)

### Phase 109A: Frontend Test-Seam Cleanup (Low Risk) ✅ COMPLETE

- Step 1 done: Removed explicit test-only API from admin API loader.
  - Target files:
    - `src/main/webapp/src/js/admin-api-loader.ts`
    - `src/main/webapp/src/js/admin-api-loader.test.ts`
- Step 2 done: Removed logout trigger injection seam from index wiring while keeping behavior unchanged.
  - Target files:
    - `src/main/webapp/src/js/index.ts`
    - `src/main/webapp/src/js/index-logout.test.ts`
- Step 3 done: Run full verification commands:
  - `npm --prefix src/main/webapp run test`
  - `npm --prefix src/main/webapp run type-check`
  - `npm --prefix src/main/webapp run build`
  - `./mvnw.cmd clean verify` (BUILD SUCCESS)

### Phase 109B: Frontend Test-Seam Cleanup (Optional/Moderate) ✅ COMPLETE

- Step 1 done: Removed redirect/scheduler injection seam from auth-expiry helper and adapted tests.
  - Target files:
    - `src/main/webapp/src/js/auth-session.ts`
    - `src/main/webapp/src/js/auth-session.test.ts`
- Step 2 done: Removed redundant leading `export {}` where safe.
  - Candidate files:
    - `src/main/webapp/src/js/admin_logs.ts`
    - `src/main/webapp/src/js/admin_login_stats.ts`
    - `src/main/webapp/src/js/create_quiz.ts`
    - `src/main/webapp/src/js/create_result.ts`
- Step 3 done: Run full verification commands:
  - `npm --prefix src/main/webapp run test`
  - `npm --prefix src/main/webapp run type-check`
  - `npm --prefix src/main/webapp run build`
  - `./mvnw.cmd clean verify` (BUILD SUCCESS)

### Phase 110: Remove Redundant relogin Query Flag ✅ COMPLETE

- Step 1 done (TDD RED): Updated relogin/logout tests to expect redirect to `/login` (without query marker) and verified
  failing state.
  - Target file:
    - `src/main/webapp/src/js/logout-action.test.ts`
- Step 2 done (TDD GREEN): Updated runtime redirect target from `/login?relogin=1` to `/login`.
  - Target file:
    - `src/main/webapp/src/js/logout-action.ts`
- Step 3 done: Run full verification commands:
  - `npm --prefix src/main/webapp run test`
  - `npm --prefix src/main/webapp run type-check`
  - `npm --prefix src/main/webapp run build`
  - `./mvnw.cmd clean verify` (BUILD SUCCESS)

### Phase 111: Spring-Only Logout Navigation ✅ COMPLETE

- Step 1 done (TDD RED/GREEN): Added backend security tests for default Spring logout redirect and CSRF-required logout.
  - Target file:
    - `src/test/java/com/ande/pubquizzz/security/SecurityAccessTest.java`
- Step 2 done (TDD GREEN): Kept default Spring logout target (`/login?logout`) and satisfied new backend tests without
  custom logout success URL.
- Step 3 done (TDD RED/GREEN): Replaced frontend JS-triggered logout with native Spring form POST logout and updated
  frontend tests.
  - Target files:
    - `src/main/webapp/src/index.html`
    - `src/main/webapp/src/js/index.ts`
    - `src/main/webapp/src/js/index-logout.test.ts`
    - `src/main/webapp/src/403.html`
    - `src/main/webapp/src/js/403.test.ts`
- Step 4 done: Removed obsolete logout action module/usages once no longer referenced.
  - Candidate files:
    - `src/main/webapp/src/js/logout-action.ts`
    - `src/main/webapp/src/js/logout-action.test.ts`
- Step 5 done: Run full verification commands:
  - `npm --prefix src/main/webapp run test`
  - `npm --prefix src/main/webapp run type-check`
  - `npm --prefix src/main/webapp run build`
  - `./mvnw.cmd clean verify` (BUILD SUCCESS)

### Phase 112: Remove Default Signed-Out Banner ✅ COMPLETE

- Step 1 done (TDD RED/GREEN): Updated security integration expectation so logout success redirects to `/login` (without
  `?logout`) and verified failing then passing state.
  - Target file:
    - `src/test/java/com/ande/pubquizzz/security/SecurityAccessTest.java`
- Step 2 done (TDD GREEN): Configured explicit Spring logout success URL to `/login`.
  - Target file:
    - `src/main/java/com/ande/pubquizzz/security/SecurityConfig.java`
- Step 3 done: Run full verification commands:
  - `npm --prefix src/main/webapp run test`
  - `npm --prefix src/main/webapp run type-check`
  - `npm --prefix src/main/webapp run build`
  - `./mvnw.cmd clean verify` (BUILD SUCCESS)

### Phase 113: 403 Relogin Must Force Logout ✅ COMPLETE

- Step 1 done (TDD RED/GREEN): Added failing tests proving 403 "Neu Anmelden" path should log out authenticated user
  before showing login, then implemented until green.
  - Target tests:
    - `src/test/java/com/ande/pubquizzz/security/SecurityAccessTest.java`
    - `src/main/webapp/src/js/403.test.ts`
- Step 2 done (TDD GREEN): Replaced 403 relogin link with logout POST form and populated CSRF hidden field from cookie.
  - Target files:
    - `src/main/webapp/src/403.html`
    - `src/main/webapp/src/js/403.ts`
    - `src/main/webapp/src/js/403.test.ts`
- Step 3 done: Run full verification commands:
  - `npm --prefix src/main/webapp run test`
  - `npm --prefix src/main/webapp run type-check`
  - `npm --prefix src/main/webapp run build`
  - `./mvnw.cmd clean verify` (BUILD SUCCESS)

## Finished Phases

### Phase 104: Low-Risk Axios CSRF Migration ✅ COMPLETE

- Migrated frontend admin/API paths to shared HTTP client and lazy admin API loading.
- Removed obsolete CSRF helper frontend files.
- Full verification passed (`npm test`, `npm type-check`, `npm build`, `./mvnw.cmd clean verify`).

### Phase 103: Neuigkeiten Inline Multi-Event Calendar Actions ✅ COMPLETE

- Added hidden metadata + inline marker support for multiple calendar events.
- Added Google/ICS inline actions with validation and fallback behavior.
- Frontend tests and full verification passed.

### Phase 102: Frontend Auth-Expiry Rollout ✅ COMPLETE

- Rolled out centralized auth-expiry handling across high-risk frontend/admin paths.
- Prevented duplicate error UI when redirect is already scheduled.
- Full backend+frontend verification passed.
