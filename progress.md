# Progress

## Open Tasks

- [x] Read `instructions.md` and identify new housekeeping rule
- [x] Update `AGENTS.md` to include the new `progress.md` housekeeping rule
- [x] Clean `progress.md` to keep only 3 finished features
- [x] Move older finished features into `progress_archive.md`
- [ ] Blockers: none

## Phase 27: Quiz Documents (Upload/Download/Delete) ✅ COMPLETE

- Added document management for quizzes in edit mode (upload/list/download/delete)
- Implemented backend persistence/service/controller endpoints under `/admin/**` with ADMIN-only access
- Added frontend document UI in `create_quiz.html` + `create_quiz.ts`
- Ensured quiz delete also deletes all associated documents
- Verification: backend tests passing, frontend type-check/build passing

## Phase 26: Version in Admin Header ✅ COMPLETE

- Added backend version endpoint `GET /api/version` using `BuildProperties`
- Added frontend header enhancement to append deployed Maven version in admin page
- Added `UserControllerTest` coverage for version/admin role endpoints
- Verification: backend tests passing, frontend type-check/build passing

## Phase 25: Tiebreaker Ranking Rules ✅ COMPLETE

- Implemented tiebreaker comparator: total points DESC, then count of 5-point answers DESC, then count of 3-point
  answers DESC
- Applied rule to quiz winner resolution and quiz ranking tables
- Updated repository/service/test coverage for new ranking logic
- Verification: backend tests passing, frontend type-check/build passing
