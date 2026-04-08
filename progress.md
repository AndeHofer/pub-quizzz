# Progress

## Open Tasks

- [x] Phase 34 Follow-up 13: Center text reveal buttons within cards using shared grid slot layout
- [x] Phase 34 Follow-up 13: Run full verification with `mvn.cmd test`

- [x] Phase 34 Follow-up 12: Center text reveal buttons while preserving no-jump behavior for short text
- [x] Phase 34 Follow-up 12: Run full verification with `mvn.cmd test`

- [x] Phase 34 Follow-up 11: Align text-only reveal button and revealed text baseline/spacing
- [x] Phase 34 Follow-up 11: Run full verification with `mvn.cmd test`

- [x] Phase 34 Follow-up 10: Keep text reveal blocks same size with button and revealed one-line text
- [x] Phase 34 Follow-up 10: Remove quiz title/date card from quiz-details page
- [x] Phase 34 Follow-up 10: Run full verification with `mvn.cmd test`

- [x] Phase 34 Follow-up 9: Keep text-only reveal sections at constant height before and after reveal
- [x] Phase 34 Follow-up 9: Run full verification with `mvn.cmd test`

- [x] Phase 34 Follow-up 8: Use light green answer background while keeping note background yellow
- [x] Phase 34 Follow-up 8: Reduce layout jump for text-only reveals and keep media reveals expandable
- [x] Phase 34 Follow-up 8: Run full verification with `mvn.cmd test`

- [x] Phase 34 Follow-up 7: Remove quiz-details load button and auto-load selected quiz from dropdown
- [x] Phase 34 Follow-up 7: Run full verification with `mvn.cmd test`

- [x] Phase 34 Follow-up 6: Refactor derived quiz title helper signature to remove unused title argument
- [x] Phase 34 Follow-up 6: Run full verification with `mvn.cmd test`

- [x] Phase 34 Follow-up 5: Remove quiz title from API/frontend contracts and derive display title from pubDate
- [x] Phase 34 Follow-up 5: Remove admin create/edit title input and payload handling
- [x] Phase 34 Follow-up 5: Update affected tests to assert derived month-year titles
- [x] Phase 34 Follow-up 5: Run full verification with `mvn.cmd test`

- [x] Phase 34 Follow-up 4: Reduce reveal jump by removing per-item button layout slot after click
- [x] Phase 34 Follow-up 4: Add smooth expand/collapse transitions for hint/answer/note blocks
- [x] Phase 34 Follow-up 4: Keep progress entries in English for new updates
- [x] Phase 34 Follow-up 4: Run full verification with `mvn.cmd test`

- [x] Phase 34 Follow-up 3: White/black Stil nur für "Quiz laden" und per-item Reveal-Buttons anwenden
- [x] Phase 34 Follow-up 3: Per-item Button-Platz beim Aufdecken stabil halten (kein Box-Höhenwechsel)
- [x] Phase 34 Follow-up 3: Vollverifikation mit `mvn.cmd test` ausführen

- [x] Phase 34 Follow-up 2: Einmalige Reveal-Buttons für Hinweis/Antwort/Anmerkung umsetzen (nach Klick ausblenden)
- [x] Phase 34 Follow-up 2: Globalen Toggle so anpassen, dass beim Ausblenden alle Reveal-Buttons zurückkommen
- [x] Phase 34 Follow-up 2: Button-Farben auf neutralen Slate-Stil anpassen
- [x] Phase 34 Follow-up 2: Vollverifikation mit `mvn.cmd test` ausführen

- [x] Phase 34 Follow-up: Anmerkung nur bei befülltem Wert rendern (nicht null/leer)
- [x] Phase 34 Follow-up: Hinweise/Antwort/Anmerkung initial verstecken und pro Block klickbar machen
- [x] Phase 34 Follow-up: Globalen Toggle "Alle aufdecken / Alle ausblenden" auf Quiz-Details-Seite ergänzen
- [x] Phase 34 Follow-up: Vollverifikation mit `mvn.cmd test` ausführen

- [x] Phase 34 Quiz-Details Seite: Plan erstellen und bestehende Quiz-Datenpfade prüfen
- [x] Phase 34 Quiz-Details Seite: User-API Endpoint für Quiz-Details bereitstellen (`GET /api/quizzes/{quizId}/detail`)
- [x] Phase 34 Quiz-Details Seite: Neue Frontend-Seite mit Quiz-Auswahl + Fragen/Hinweisen/Antworten implementieren
- [x] Phase 34 Quiz-Details Seite: Navigation/Bundling anpassen (`index.html`, `vite.config.ts`)
- [x] Phase 34 Quiz-Details Seite: Controller-Tests ergänzen und Vollverifikation (`mvn.cmd test`) ausführen

- [x] Phase 33 Follow-up: Split Team table into separate Punkte and Rang columns
- [x] Phase 33 Follow-up: Show medals in Team rank column for ranks 1-3
- [x] Phase 33 Follow-up: Run full verification (`mvn.cmd test`) after Team UI changes

- [x] Phase 33 Team ranking column: repair broken `ResultService.getResultsForTeam()` compilation/runtime logic
- [x] Phase 33 Team ranking column: ensure API DTO fields `quizRank` + `participantCount` are serialized in controller
  test fixture
- [x] Phase 33 Team ranking column: run full verification (`mvn.cmd test`) and confirm green build

- [x] Phase 32 Follow-up 9: Remove legacy `/api/leaderboard` endpoint from `UserLeaderboardController`
- [x] Phase 32 Follow-up 9: Update controller/security tests to use `/api/leaderboard/points`
- [x] Phase 32 Follow-up 9: Re-run tests and full verification
- [x] Phase 32 Follow-up 8: Change index leaderboard card separators from horizontal to vertical (desktop)
- [x] Phase 32 Follow-up 8: Re-run tests and full verification
- [x] Phase 32 Follow-up 7: Align medal assignment with per-quiz ranking tie rules (points, 5s, 3s)
- [x] Phase 32 Follow-up 7: Add regression test for medal mismatch scenario
- [x] Phase 32 Follow-up 7: Re-run tests and full verification
- [x] Phase 32 Follow-up 6: Points leaderboard assigns shared rank for equal total points
- [x] Phase 32 Follow-up 6: Remove points ranking tie-breakers
- [x] Phase 32 Follow-up 6: Re-run tests and full verification
- [x] Phase 32 Follow-up 5: Average leaderboard assigns shared rank for equal averages
- [x] Phase 32 Follow-up 5: Remove average ranking tie-breakers
- [x] Phase 32 Follow-up 5: Re-run tests and full verification
- [x] Phase 32 Follow-up 4: Remove legacy `leaderboard.html` page
- [x] Phase 32 Follow-up 4: Update build inputs and verify no remaining references
- [x] Phase 32 Follow-up 4: Re-run tests and full verification
- [x] Phase 32 Follow-up 3: Remove points column from average leaderboard UI
- [x] Phase 32 Follow-up 3: Re-run tests and full verification
- [x] Phase 32 Follow-up 2: Medal leaderboard without team-name tie-breaker; equal medals share rank
- [x] Phase 32 Follow-up 2: Re-run tests and full verification
- [x] Phase 32 Follow-up: Exclude teams without medals from medal leaderboard
- [x] Phase 32 Follow-up: Remove points column/tie-breaker from medal leaderboard
- [x] Phase 32 Follow-up: Re-run tests and full verification
- [x] Plan Phase 32: Additional overall leaderboards (medals + average) with dedicated pages
- [x] Implement backend leaderboard extensions (API + service + repository + DTOs)
- [x] Implement frontend leaderboard pages and index card split into 3 vertical buttons
- [x] Add/update unit + controller tests for new leaderboard datasets/endpoints
- [x] Run full verification (`npm run type-check`, `npm run build`, `mvnw.cmd test` with skip npm flags)
- [x] Plan Phase 31: Sort "View Quizzes" by `pubDate` descending
- [x] Implement frontend sorting in `viewQuizzes` (newest first, invalid/missing dates last)
- [x] Run full verification (`npm run type-check`, `npm run build`, `mvnw.cmd test` with skip npm flags)
- [x] Read `instructions.md` and identify new housekeeping rule
- [x] Update `AGENTS.md` to include the new `progress.md` housekeeping rule
- [x] Clean `progress.md` to keep only 3 finished features
- [x] Move older finished features into `progress_archive.md`
- [x] Plan Phase 28: Image answers for questions 5-8 (text or image or both)
- [x] Implement backend model + service/controller adjustments for answer images
- [x] Implement frontend create/edit support for answer images (questions 5-8)
- [x] Add/update unit + integration + persistence tests
- [x] Run full verification (`mvnw.cmd test`, `npm run type-check`, `npm run build`)
- [x] Analyze restore failure with `quiz_document` foreign key dependency
- [x] Implement restore order fix + backup table inclusion for `quiz_document`
- [x] Add regression tests for backup/restore with `quiz_document`
- [x] Run verification for backup/restore fix
- [x] Analyze legacy-restore schema mismatch for `question.answer_image_url`
- [x] Implement post-restore schema compatibility patching
- [x] Add regression test for legacy backup import compatibility
- [x] Run verification for legacy restore compatibility fix
- [x] Blockers: none

## Phase 32: Additional Overall Leaderboards + Dedicated Pages ✅ COMPLETE

- Backend extended with two new leaderboard datasets:
  - `GET /api/leaderboard/medals` (Olympic medal ranking with tie rule `1,1,3`)
  - `GET /api/leaderboard/average` (average points including participations and total points)
- Existing behavior preserved:
  - `GET /api/leaderboard` remains points leaderboard
  - `GET /api/leaderboard/points` added as explicit endpoint
- New DTOs added: `MedalLeaderboardEntry`, `AverageLeaderboardEntry`
- `ResultRepository` extended with aggregate queries for medal and average leaderboards
- `ResultService` extended with ranking logic and deterministic ordering
- Frontend extended:
  - new pages: `points-leaderboard.html`, `medal-leaderboard.html`, `average-leaderboard.html`
  - new scripts: `medal-leaderboard.ts`, `average-leaderboard.ts`
  - `index.html` leaderboard card split into 3 vertical buttons: Punkte, Medaillen, Durchschnitt
  - Team back-link now depends on source leaderboard page
- Tests added/updated:
  - `UserLeaderboardControllerTest` for new endpoints
  - `ResultServiceLeaderboardTest` for medal and average logic
- Follow-up applied:
  - Medal leaderboard excludes teams without medals
  - Medal leaderboard removed points column and points tie-breaker
  - Medal leaderboard ranks equal medal tuples with shared rank (no team-name tie-breaker)
- Verification: `npm run type-check` + `npm run build` (in `src/main/webapp`) and
  `./mvnw.cmd --% -Dskip.npm=true -Dskip.installnodenpm=true test` passed

## Phase 31: Sort Quizzes by `pubDate` DESC ✅ COMPLETE

- Updated `viewQuizzes()` in `src/main/webapp/src/js/admin_functions.ts` to sort quizzes before rendering
- Sorting rule: valid `pubDate` first in descending order (newest to oldest)
- Missing/invalid `pubDate` values are placed at the end
- Deterministic fallback sort for ties/missing dates: `quizId` descending
- Verification: `npm run type-check` (in `src/main/webapp`), `npm run build` (in `src/main/webapp`),
  `./mvnw.cmd --% -Dskip.npm=true -Dskip.installnodenpm=true test` all passing

## Phase 30: Legacy Backup Compatibility for answer_image_url ✅ COMPLETE

- Added post-restore schema compatibility step in `BackupRestoreListener` after `RUNSCRIPT`
- Ensures legacy backups can be imported by adding missing `question.answer_image_url` column with
  `ALTER TABLE ... ADD COLUMN IF NOT EXISTS`
- Ensures `quiz_document` table exists for older backups that predate document feature
- Added `BackupRestoreListenerTest` coverage for importing a legacy SQL backup without new schema elements
- Verification: backend tests passing, frontend type-check/build passing
