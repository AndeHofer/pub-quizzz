# Progress

## Open Tasks

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

## Phase 28: Bildantwort fuer Fragen 5-8 ✅ COMPLETE

- Added nullable `answerImageUrl` on `Question` and propagated it through request/response DTOs
- Questions 5-8 now accept answer text, answer image, or both; questions 1-4 still require answer text for finished
  status
- Added multipart handling for `answer_image_q5..q8` in `AdminQuizController`
- Extended image lifecycle cleanup in `QuizService` (update diff cleanup, quiz delete, orphan cleanup)
- Frontend `create_quiz.ts` now supports answer image upload/preview for questions 5-8 and mirrors finished readiness
  rule
- Added tests for finished checker, controller multipart handling, service cleanup behavior, and persistence
- Verification: backend tests passing, frontend type-check/build passing
