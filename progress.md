# Progress

## Open Tasks

- [x] Plan Phase 31: Quiz anzeigen nach `pubDate` absteigend sortieren
- [x] Implement frontend sorting in `viewQuizzes` (neueste zuerst, ungültige/fehlende Daten zuletzt)
- [x] Run full verification (`npm run type-check`, `npm run build`, `mvnw.cmd test` with skip npm flags)
- [x] Read `instructions.md` and identify new housekeeping rule
- [x] Update `AGENTS.md` to include the new `progress.md` housekeeping rule
- [x] Clean `progress.md` to keep only 3 finished features
- [x] Move older finished features into `progress_archive.md`
- [x] Plan Phase 28: Bildantwort fuer Fragen 5-8 (Text oder Bild oder beides)
- [x] Implement backend model + service/controller adjustments for answer image
- [x] Implement frontend create/edit support for answer images (Fragen 5-8)
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

## Phase 31: Quiz anzeigen nach `pubDate` DESC ✅ COMPLETE

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

## Phase 29: Backup/Restore FK Fix for quiz_document ✅ COMPLETE

- Fixed restore table-drop order and robustness in `BackupRestoreListener` by dropping `quiz_document` before `quiz`
- Added `SET REFERENTIAL_INTEGRITY FALSE/TRUE` around table drops to avoid FK-order breakages from future schema changes
- Added `quiz_document` to backup SQL export table list in `BackupService`
- Added regression tests:
  - `BackupRestoreListenerTest`: restore succeeds with `quiz_document -> quiz` FK dependency
  - `BackupServiceTest`: SQL dump contains `QUIZ_DOCUMENT`
- Verification: backend tests passing, frontend type-check/build passing
