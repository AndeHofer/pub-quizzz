# Progress: Migration to TypeScript/Tailwind/Vite + Full Refactoring

## Phase 1: TypeScript/Tailwind/Vite Migration ✅ COMPLETE

- [x] Initial setup
    - [x] Create `src/webapp` directory structure
    - [x] Initialize `package.json`
    - [x] Configure `tsconfig.json`
    - [x] Configure `vite.config.ts`
    - [x] Setup Tailwind CSS (v4 zero-config)
- [x] Maven integration
    - [x] Add `frontend-maven-plugin` to `pom.xml`
- [x] Asset migration
    - [x] Move `src/main/resources/static/js` and `src/main/resources/static/css` to `src/main/webapp/src`
    - [x] Refactor JS files to TS
    - [x] Refactor CSS to Tailwind v4
- [x] Frontend fixes
    - [x] Added `export {}` to `admin_functions.ts`, `create_quiz.ts`, `register_user.ts` to resolve duplicate global
      function TS errors
    - [x] Added `type-check` script to `package.json`
    - [x] Consolidated duplicate `window.addEventListener('load', ...)` in `admin_functions.ts`
    - [x] Removed `console.log` calls from frontend files
    - [x] Fixed `index.html` — added `lang="de-AT"` and `<meta charset="UTF-8">`

## Phase 2: Full Backend Refactoring ✅ COMPLETE

- [x] Added MapStruct to `pom.xml` (dependency + `mapstruct-processor` + `lombok-mapstruct-binding`)
- [x] Added `spring-security-test` as test dependency to `pom.xml`
- [x] Created mapper interfaces: `QuizMapper`, `ResultMapper`, `TeamMapper`, `UserMapper`
- [x] Replaced `toDTO()` methods in all four services with MapStruct mappers
- [x] Fixed `CreateResultRequest` — `@Data` + `@NoArgsConstructor` on outer class and inner `AnswerSubmission`
- [x] Removed `password` field from `UserDTO`
- [x] Fixed fully-qualified class names in `ResultService` and `AdminResultController`
- [x] Added `@Repository` to `UserRepository`
- [x] Deleted stale `src/test/resources/QUESTIONS.sql`
- [x] Fixed `vite.config.ts` input path bug

## Phase 3: Test Fixes ✅ COMPLETE

- [x] Created `src/test/resources/application-local.properties` with test values for env vars + in-memory H2
- [x] Fixed `ResultServiceCreateTest` — added `@Mock ResultMapper resultMapper` + stub
- [x] Created 4 controller integration tests (`AdminQuizControllerTest`, `AdminResultControllerTest`,
  `AdminTeamControllerTest`, `AdminUserControllerTest`)
- [x] Fixed `@WebMvcTest` import to Spring Boot 4.x package (`org.springframework.boot.webmvc.test.autoconfigure`)
- [x] Fixed controller tests for Spring Boot 4.x:
    - Added security auto-config imports: `SecurityAutoConfiguration`, `ServletWebSecurityAutoConfiguration`,
      `SecurityFilterAutoConfiguration`
    - Created `SecurityTestConfig` with `MockMvcBuilderCustomizer` applying
      `SecurityMockMvcConfigurers.springSecurity()`
    - This ensures `@WithMockUser` works correctly and unauthenticated tests get proper 302 redirect

## Phase 4: Refactoring (7-PR plan) ✅ COMPLETE

### Task 1: Custom Exception Hierarchy ✅ COMMITTED
- Created `BusinessValidationException`, `ResourceNotFoundException`
- Replaced all `IllegalArgumentException` throws in services with typed exceptions

### Task 2: Global Exception Handler ✅ COMMITTED
- Created `ImageStorageException`, `GlobalExceptionHandler` (`@RestControllerAdvice`)
- Removed all try/catch from controllers — exceptions handled centrally
- `GlobalExceptionHandler` added to `@Import` in all `@WebMvcTest` controller tests

### Task 3: Bean Validation on DTOs ✅ COMMITTED
- Added `@NotBlank`/`@NotNull`/`@Size`/`@Min`/`@Max` to `CreateUserRequest`, `CreateQuizRequest`, `CreateResultRequest`
- Added `@Valid` to all controller method parameters
- Removed manual null/blank/size checks from services (now delegated to validation)
- Added `@UniqueConstraint(team_id, quiz_id)` to `Result` entity

### Task 4: XSS Fix + Method Security ✅ COMMITTED
- Fixed DOM XSS in `admin_functions.ts`: replaced inline `onclick` with `data-*` + `addEventListener`
- Replaced `window.onclick =` with `window.addEventListener('click', ...)`
- Added `@EnableMethodSecurity` to `SecurityConfig`
- Added `@PreAuthorize("hasRole('ADMIN')")` to all 4 admin controllers

### Task 5: Code Duplication ✅ COMMITTED
- Extracted `applyQuestionsToQuiz()` in `QuizService`
- Extracted `createAndSaveUser()` in `UserService`
- Added `calculateTotalPoints()` to `Result` entity + `ResultTest.java`
- Updated `ResultMapper` to use `result.calculateTotalPoints()`
- Created `utils.ts` with `showMessage`/`goBack`; used in `register_user.ts` and `create_quiz.ts`

### Task 6: API Design ✅ COMMITTED
- Created `CreateTeamRequest` DTO with `@NotBlank`
- Created `UpdateQuizDatesRequest` DTO with `@NotNull` on both dates
- `AdminTeamController.createTeam` now accepts `@RequestBody @Valid CreateTeamRequest` (was `@RequestParam`)
- `PUT /quiz/{id}/dates` → `PATCH /quiz/{id}/dates` with `UpdateQuizDatesRequest`
- `QuizService.updateQuiz` now throws `ResourceNotFoundException` instead of returning `Optional`
- `admin_functions.ts` `createTeam` now sends JSON body; invalidates `_admin_teams_cache` on success
- Updated `AdminTeamControllerTest` + `AdminQuizControllerTest` for new contracts

### Task 7: Frontend Type Safety ✅ COMMITTED
- Created `types.ts` with `HintDTO`, `QuestionDTO`, `QuizDTO`, `TeamDTO`, `AnswerScoreDTO`, `ResultDTO`, `LeaderboardEntry`, `UserDTO`
- Replaced all `any` in `admin_functions.ts` with proper types from `types.ts`
- Cache invalidation: `_admin_quizzes_cache` cleared on `deleteQuiz`; `_admin_teams_cache` cleared on `deleteTeam` and `createTeam`
- Replaced local `Hint`/`Question`/`Quiz` interfaces in `create_quiz.ts` with `QuizDTO` from `types.ts`
- Added `.catch()` to `index.ts` is-admin fetch

## Final Status (Phase 1–4)

- **Backend tests**: 61/61 passing (`mvn clean test`)
- **Frontend type check**: 0 errors (`npm run type-check`)
- **Frontend build**: succeeds (`vite build`)

## Phase 5: User All-Time Leaderboard

### Task 1: DTO ✅ COMMITTED
- Created `AllTimeLeaderboardEntry` DTO (rank, teamName, totalPoints, quizCount)

### Task 2: Repository Query ✅ COMMITTED
- Added `findAllTimeLeaderboardRaw()` JPQL to `ResultRepository` — groups by team, sums points, counts distinct quizzes

### Task 3: Service Method + Unit Tests ✅ COMMITTED
- Added `getAllTimeLeaderboard()` to `ResultService` — maps `Object[]` rows, assigns rank in Java
- Created `ResultServiceLeaderboardTest` with 2 unit tests (63 total, all passing)

### Task 4: Controller + Integration Tests ✅ COMMITTED
- Created `UserLeaderboardController` at `GET /api/leaderboard`
- Created `UserLeaderboardControllerTest` with 3 `@WebMvcTest` tests (66 total, all passing)

### Task 5: Frontend Types + Vite Config ✅ COMMITTED
- Added `AllTimeLeaderboardEntry` interface to `types.ts`
- Added `leaderboard` entrypoint to `vite.config.ts`

### Task 6: Leaderboard Frontend Page ✅ COMMITTED
- Created `leaderboard.html` and `leaderboard.ts`
- Workaround: medal emojis use `\uD83E\uDD47` Unicode escapes (Rolldown panics on emoji in template literals)

### Task 7: Home Page Nav Hub ✅ COMMITTED
- `index.html` replaced with card-based navigation hub (Gesamtrangliste + hidden Admin Panel card)
- `index.ts` simplified — only reveals admin card via `/api/is-admin`; removed old `goToAdmin` function

### Task 8: Full Verification ✅
- Backend: 66/66 tests passing (`mvn.cmd test`)
- Frontend: 0 type errors (`npm run type-check`)
- Full build: `mvn.cmd clean package -DskipTests` — BUILD SUCCESS

## Phase 6: Style Unification (Less Color) ✅ COMPLETE

### Goal

Apply the clean, neutral visual style from `index.html` to all other pages. User feedback: "less color".

### Changes

- `styles.css`: Neutralized all colors:
  - `button`/`.admin-btn`: `bg-green-500` → `bg-gray-700`, hover `bg-green-600` → `bg-gray-800`
  - `.secondary-btn`: `bg-blue-500` → `bg-gray-500`, hover `bg-blue-700` → `bg-gray-600`
  - `h2` border: `border-green-500` → `border-gray-200`
  - `th`: `bg-green-500 text-white` → `bg-gray-100 text-gray-700`
  - `body`: `bg-gray-100` → `bg-gray-50`
- `leaderboard.html`: Back-link `text-blue-600` → `text-gray-500 hover:text-gray-700`

### Verification

- Backend: 66/66 tests passing (`mvn.cmd clean test`)
- Frontend: 0 type errors (`npm run type-check`)

## Phase 7: Remove Export/Leaderboard from Admin Panel ✅ COMPLETE

### Goal

Remove "Ergebnisse exportieren" and "Rangliste anzeigen" buttons from the admin panel and all associated backend code.

### Changes

- `admin_main.html`: Removed "Ergebnisse exportieren" and "Rangliste anzeigen" buttons
- `admin_functions.ts`: Removed `exportResults()`, `viewLeaderboard()` functions, `LeaderboardEntry` import, and window
  registrations
- `types.ts`: Removed `LeaderboardEntry` interface
- `AdminResultController.java`: Removed `GET /admin/leaderboard` and `GET /admin/results/export` endpoints
- `ResultService.java`: Removed `getLeaderboard()` and `exportResultsCsv()` methods (fixed a duplicate-class bug
  introduced during edit)
- `ResultRepository.java`: Removed `findAllOrderByTotalPointsDesc()` and `findByQuizIdOrderByTotalPointsDesc()` queries
- `LeaderboardEntry.java`: Deleted (DTO no longer needed)
- `ResultMapper.java`: Removed `toLeaderboardEntry()` method
- `AdminResultControllerTest.java`: Removed `getLeaderboard_returnsRankedEntries()` test

### Note

`ResultServiceLeaderboardTest` and `getAllTimeLeaderboard()` were **not** removed — they serve the public leaderboard (
`GET /api/leaderboard` via `UserLeaderboardController`).

### Verification

- Backend: 65/65 tests passing (`mvn.cmd clean test`)
- Frontend: 0 type errors (`npm run type-check`)

## Phase 8: In-Memory H2 for Tests ✅ COMPLETE

### Goal

Stop tests from writing into the file-based H2 database. Use a transient in-memory H2 database that is destroyed when
the JVM exits.

### Changes

- Created `src/test/resources/application-test.properties` with `jdbc:h2:mem:testdb`, `ddl-auto=create-drop`, temp
  upload dir, and no log file
- `DatabaseTest.java`: `@ActiveProfiles("local")` → `@ActiveProfiles("test")`, added `@Transactional` (auto-rollback per
  test)
- `HintPersistenceTest.java`: same changes

### Verification

- Backend: 65/65 tests passing (`mvn.cmd clean test`)
- Frontend: 0 type errors (`npm run type-check`)
- `./data/` directory: **not modified** during test run (confirmed by comparing file list before and after)
- Log confirms in-memory URL active: `HikariPool-1 - Added connection conn0: url=jdbc:h2:mem:testdb`

## Phase 9: BigIntegrationTest — Manual DB Seeder ✅ COMPLETE

### Goal

Create a manually-triggered test that seeds the local file-based H2 database with realistic base data
(5 quizzes, 10 teams, 40 results) to support manual UI testing. The test must be excluded from normal
`mvn test` runs.

### Changes

- `pom.xml`:
  - Added `excludedTestGroups` property (default: `manual`) to allow command-line override
  - Surefire `<excludedGroups>` now references `${excludedTestGroups}` instead of hardcoded `manual`
- `src/test/java/com/ande/pubquizzz/BigIntegrationTest.java`: New file
  - `@Tag("manual")` — excluded from normal test runs
  - `@ActiveProfiles("local")` — connects to file-based H2 at `./data/pubquizzz`
  - No `@Transactional` — data persists after test for manual inspection
  - `@BeforeEach` clears results → quizzes → teams in FK-safe order
  - Seeds: 5 themed German-language quizzes (8 questions each, correct hint counts per business rules),
    10 teams with humorous German names, 40 results with deterministic point spread
  - Asserts: counts (5/10/40), quiz structure, answer count (via `findByIdWithAnswers`), point ordering
- `src/main/java/com/ande/pubquizzz/database/repositories/ResultRepository.java`:
  - Added `findByIdWithAnswers(Long id)` — JPQL with `JOIN FETCH r.answers` to avoid
    `LazyInitializationException` when accessing answers outside a session

### How to run

```
mvn.cmd test -Dgroups=manual -DexcludedTestGroups=
```

### Verification

- Manual test: 1/1 passing, DB seeded with 5 quizzes / 10 teams / 40 results
- Normal test suite: 65/65 passing (`mvn.cmd clean test`) — `BigIntegrationTest` correctly excluded
- Frontend: 0 type errors (`npm run type-check`)

## Phase 10: Enforce Max 5 Points Per Question ✅ COMPLETE

### Goal

The frontend dropdown only offers `{0, 1, 2, 3, 5}` (4 is not a valid score, max is 5), but the backend
had no upper-bound enforcement. The seed test also used invalid values up to 10. Fix all layers.

### Changes

- `src/main/java/com/ande/pubquizzz/dto/CreateResultRequest.java`:
  - Added `@Max(value = 5, message = "Punkte dürfen maximal 5 sein")` to `AnswerSubmission.points`
- `src/test/java/com/ande/pubquizzz/BigIntegrationTest.java`:
  - Rewrote `pointTemplates` — all values now from `{0, 1, 2, 3, 5}` only (range 4–40 pts/quiz)
- `src/test/java/com/ande/pubquizzz/controller/AdminResultControllerTest.java`:
  - Fixed `createResult_withValidRequest_returnsCreated` — previously used `points = i` for i=1..8,
    which set questions 6–8 to 6, 7, 8 (invalid). Now uses `{5, 3, 5, 3, 5, 3, 5, 0}` (all valid).
  - Added `createResult_withPointsAboveMax_returnsBadRequest` — asserts that `points = 6` returns 400.

### Verification

- Normal test suite: 66/66 passing (`mvn.cmd clean test`)
- Manual seed test: 1/1 passing (`mvn.cmd test -Dgroups=manual -DexcludedTestGroups=`)
- Frontend: 0 type errors (`npm run type-check`)

## Phase 11: Rename Team + Edit/Delete Result in Admin Panel ✅ COMPLETE

### Goal

Allow admins to rename teams and to edit or delete individual results directly from the admin panel.

### Changes

#### Backend

- `UpdateTeamRequest.java`: New DTO with `@NotBlank name` field
- `TeamService.renameTeam(Long id, String newName)`: Validates team exists and name not already taken (German error
  messages), saves and returns updated `TeamDTO`
- `AdminTeamController`: Added `PUT /admin/team/{id}` endpoint accepting `UpdateTeamRequest`
- `UpdateResultRequest.java`: New DTO with `@NotNull teamId` and 8 `AnswerSubmission` entries (reuses
  `CreateResultRequest.AnswerSubmission`)
- `ResultService.deleteResult(Long id)`: Validates result exists, deletes it
- `ResultService.updateResult(Long id, UpdateResultRequest)`: Validates result and team exist, replaces answers, saves
- `AdminResultController`: Added `DELETE /admin/results/{id}` and `PUT /admin/results/{id}` endpoints
- `ResultRepository.findByIdWithAnswers`: Extended query to also `JOIN FETCH r.team` and `JOIN FETCH r.quiz` (prevents
  `LazyInitializationException` on LAZY associations)

#### Frontend (`admin_functions.ts`)

- Added module-level `_admin_results_cache` and `_admin_last_quiz_id_filter` variables
- `viewResults()` refactored to accept optional `quizIdOverride?: string | null` (avoids re-prompting after edit/delete)
- `renameTeam(teamId, currentName)`: prompts for new name, calls `PUT /admin/team/{id}`, refreshes team list
- `deleteResult(resultId)`: confirms, calls `DELETE /admin/results/{id}`, re-renders results table from cache
- `editResult(resultId)`: prompts for new team and 8 point values, calls `PUT /admin/results/{id}`, re-renders table
- Teams table: added "Umbenennen" button per row
- Results table: added "Bearbeiten" and "Löschen" buttons per row

#### Tests

- `TeamServiceRenameTest`: 4 unit tests (success, not-found, same-name-allowed, duplicate-name-rejected)
- `ResultServiceDeleteUpdateTest`: 4 unit tests (delete success, delete not-found, update success, update not-found)
- `AdminTeamControllerTest`: 3 new `@WebMvcTest` tests (rename success, rename 404, rename blank name)
- `AdminResultControllerTest`: 4 new `@WebMvcTest` tests (delete success, delete 404, update success, update invalid
  points)

### Verification

- Backend: 81/81 tests passing (`mvn.cmd clean test`)
- Frontend: 0 type errors (`npm run type-check`)

## Phase 12: Fix Team Delete FK Violation + Danger Confirmation ✅ COMPLETE

### Goal

Deleting a team with existing results caused `JdbcSQLIntegrityConstraintViolationException` (FK violation on
`RESULT.TEAM_ID`). Fix the delete to cascade results, and show a clear danger warning before the admin confirms.

### Changes

- `ResultRepository.java`: Added `deleteByTeamTeamsId(Long teamId)` — Spring Data derived delete; removes all results
  for the team (answers cascade via existing `CascadeType.ALL` on `Result.answers`)
- `TeamService.java`: Added `ResultRepository` as constructor dependency; `deleteTeam()` now calls
  `resultRepository.deleteByTeamTeamsId(id)` before `teamRepository.deleteById(id)`
- `admin_functions.ts`: `deleteTeam()` confirmation message now warns:
  `"ACHTUNG: Alle Ergebnisse dieses Teams werden unwiderruflich gelöscht!"`
- `TeamServiceDeleteTest.java` (new): 2 unit tests — verifies results are deleted before team (using `InOrder`), and
  not-found returns false without touching `resultRepository`
- `TeamServiceRenameTest.java`: Added `@Mock ResultRepository resultRepository` (required now that `TeamService` has the
  new dependency)

### Verification

- Backend: 83/83 tests passing (`mvn.cmd clean test`)
- Frontend: 0 type errors (`npm run type-check`)

## Phase 13: Fix Quiz Delete FK Violation + Danger Confirmation ✅ COMPLETE

### Goal

Same root cause as Phase 12: deleting a quiz with existing results caused a FK violation on `RESULT.QUIZ_ID`.

### Changes

- `ResultRepository.java`: Added `deleteByQuizQuizId(Long quizId)`
- `QuizService.java`: Added `ResultRepository` dependency; `deleteQuiz()` now calls
  `resultRepository.deleteByQuizQuizId(id)` before `quizRepository.deleteById(id)`
- `admin_functions.ts`: `deleteQuiz()` confirmation now warns:
  `"ACHTUNG: Alle Ergebnisse dieses Quiz werden unwiderruflich gelöscht!"`
- `QuizServiceDeleteTest.java` (new): 2 unit tests — verifies delete order and not-found path

### Verification

- Backend: 85/85 tests passing (`mvn.cmd clean test`)
- Frontend: 0 type errors (`npm run type-check`)

## Key Discoveries

- Spring Boot 4.x `@WebMvcTest` requires explicit security auto-config imports to load `HttpSecurity` bean
- `@WithMockUser` requires `SecurityMockMvcConfigurers.springSecurity()` applied via `MockMvcBuilderCustomizer`
- Spring Boot 4.x Jackson 3.x: `@WebMvcTest` does NOT provide `com.fasterxml.jackson.databind.ObjectMapper` bean — use
  `new ObjectMapper()` in tests
- TypeScript files without `import`/`export` are treated as global scripts — duplicate functions cause TS errors; fix
  with `export {}`
- LSP errors in editor are Lombok false-positives; Maven compilation is the source of truth

## Phase 14: Add Optional Title Field to Quiz ✅ COMPLETE

### Goal

Allow admins to give quizzes a human-readable title (e.g. "2026 Jänner") for display in the admin UI dropdown and quiz
list.

### Changes

- `Quiz.java`: `@Column(nullable = true) private String title`
- `QuizDTO.java`, `QuizDetailDTO.java`: `private String title`
- `CreateQuizRequest.java`: `private String title` (optional)
- `QuizService.java`: sets title in `createQuiz()` and `updateQuizFull()`
- `types.ts`: `title?: string` in `QuizDTO`
- `create_quiz.html`: Titel text input above pubDate
- `create_quiz.ts`: reads/populates title field
- `admin_functions.ts`: `quizDisplayTitle()` helper + `GERMAN_MONTHS` array; Titel column in quiz list; quiz dropdown
  uses display title
- `AdminQuizControllerTest`: 2 new title tests

### Verification

- Backend: 87/87 tests passing (`mvn.cmd clean test`)
- Frontend: 0 type errors (`npm run type-check`)

## Phase 15: Two Images Per Hint (`imageUrlAtStart` + `imageUrlAsHint`) ✅ COMPLETE

### Goal

Replace the single optional `imageUrl` per `Hint` with two distinct optional images:

- `imageUrlAtStart` — shown when the question is first displayed
- `imageUrlAsHint` — shown when the hint is revealed

Both are optional and nullable. Old image files are deleted from disk when a quiz is fully updated.

### Changes

#### Backend

- `Hint.java`: removed `imageUrl`; added `imageUrlAtStart` (`image_url_at_start`) and `imageUrlAsHint` (
  `image_url_as_hint`), both `@Column(nullable = true)`
- `QuizDetailDTO.HintDetailDTO`: replaced `imageUrl` with `imageUrlAtStart` + `imageUrlAsHint`
- `CreateQuizRequest.HintData`: replaced `imageUrl` with `imageUrlAtStart` + `imageUrlAsHint`
- `QuizService.buildHints()`: sets both new fields from `HintData`
- `QuizService`: added `ImageStorageService` dependency; `updateQuizFull()` snapshots all old image URLs before
  `questions.clear()`, then calls `imageStorageService.delete()` for each after save
- `AdminQuizController.injectImageUrls()`: two part names per hint slot — `hint_atstart_q{q}_h{h}` and
  `hint_ashint_q{q}_h{h}`
- `ImageStorageService`: added `delete(String url)` — resolves filename from `/uploads/<name>`, calls
  `Files.deleteIfExists()`; no-op for null or unrecognised URL format

#### Frontend

- `types.ts` `HintDTO`: replaced `imageUrl?` with `imageUrlAtStart?` + `imageUrlAsHint?`; removed stale `imageUrl?` from
  `QuestionDTO`
- `create_quiz.ts`: two file inputs per hint ("Bild: Am Anfang", "Bild: Als Hinweis"), each with preview `<img>`;
  updated `populateFormForEdit` and form submit to use new field names and part names

#### Tests (+11 new)

- `ImageStorageServiceTest`: 3 new tests for `delete()` (removes file, no-op for null, no-op for missing file) — 9 total
- `HintPersistenceTest`: replaced 2 old `imageUrl` tests with 4 new tests covering `imageUrlAtStart`, `imageUrlAsHint`,
  both null, both set — 6 total
- `QuizServiceUpdateTest` (new): 2 unit tests verifying `imageStorageService.delete()` is called with old URLs on
  `updateQuizFull()`
- `QuizServiceDeleteTest`: added `@Mock ImageStorageService imageStorageService` (required by new constructor
  dependency)
- `AdminQuizControllerTest`: 4 new multipart tests (atStart image, asHint image, PUT with new part names, old part name
  ignored)

### Schema note

Hibernate `ddl-auto=update` adds the two new nullable columns on next startup. The orphaned `image_url` column is left
in place (harmless on H2).

### Verification

- Backend: 98/98 tests passing (`mvn.cmd clean test`)
- Frontend: 0 type errors (`npm run type-check`)

---

## Phase 16: Backup/Restore ✅ COMPLETE

### Design

- Export: H2 `SCRIPT TO` SQL dump + upload files zipped via `ZipOutputStream` + `StreamingResponseBody`
- Import: staged to `app.backup.restore-dir` on disk — requires manual application restart to apply
- Restore applied on `ApplicationStartedEvent` (before Tomcat accepts HTTP) by `BackupRestoreListener`
- `DROP ALL OBJECTS DELETE FILES` then `RUNSCRIPT FROM` to restore DB
- Single-pass ZIP extraction: validates + extracts simultaneously; cleans up on failure

### ZIP structure

```
pubquizzz-backup-YYYY-MM-DD.zip
├── database.sql
└── uploads/
    └── <files>
```

### API

- `GET /admin/backup/export` → `application/zip` download
- `POST /admin/backup/import` (multipart `file`) → `200 text/plain` German confirmation message

### New Files

- `BackupService.java` — `createBackup()` + `stageRestore()`
- `AdminBackupController.java` — export + import endpoints
- `BackupRestoreListener.java` — `ApplicationStartedEvent` hook
- `BackupServiceTest.java` — 6 unit tests
- `AdminBackupControllerTest.java` — 5 controller tests

### Modified Files

- `application.properties` — `app.backup.restore-dir=/data/pending-restore`
- `application-test.properties` — `app.backup.restore-dir=${java.io.tmpdir}/pending-restore-test`
- `admin_main.html` — "Datenbank-Backup" section with export button, import form
- `admin_functions.ts` — `exportBackup()` + `importBackup()` functions

### Commits

- `7ea3d20` — BackupService (GREEN)
- `5336fec` — AdminBackupController (GREEN)
- `fba0ac3` — BackupRestoreListener
- `48408c9` — properties
- `ab55678` — frontend UI

### Verification

- Backend: 109/109 tests passing (`mvn.cmd clean test`)
- Frontend: 0 type errors (`npm run type-check`)

## Phase 17: Quiz Draft Saving + Finished Indicator ✅ COMPLETE

### Goal

Allow admins to save incomplete ("draft") quizzes, and replace the "Fragen" (question count) column
in the admin quiz list with a ✅/❌ "Fertig" finished indicator.

### Finished Definition

A quiz is **finished** when:
- It has exactly 8 questions
- Every question has a non-blank `questionText` and non-blank `answer`
- Every hint has either a non-blank `hintText` OR a non-null `imageUrlAsHint`
  (`imageUrlAtStart` alone does **not** count as a filled hint)

### Changes

#### Backend

- `Hint.java`: `@Column(nullable = false)` → `nullable = true`, removed `@NotNull` from `hintText`
- `QuizDTO.java`: Removed `int questionCount`, added `boolean finished`
- `QuizFinishedChecker.java` (new): Static utility in `mapper` package — `isFinished(Quiz)` implements
  the finished definition above
- `QuizMapper.java`: Replaced `questionCount` expression with `finished` via `QuizFinishedChecker.isFinished(quiz)`
- `CreateQuizRequest.java`: Removed `@NotBlank` from `questionText` and `answer` in `QuestionData` —
  blank strings are now accepted for draft quizzes

#### Frontend

- `types.ts`: Replaced `questionCount?: number` with `finished?: boolean` in `QuizDTO`
- `admin_functions.ts`: "Fragen" header → "Fertig"; cell renders `✅` or `❌` based on `finished`
- `create_quiz.ts`: Removed `required` attribute from question text and answer inputs

#### Tests (+10 new)

- `AdminQuizControllerTest.java`: Fixed 4 `setQuestionCount` → `setFinished`; fixed jsonPath assertion;
  added `createQuiz_withBlankQuestionText_isAllowedAsDraft` test (16 total)
- `QuizFinishedCheckerTest.java` (new): 8 unit tests for all `isFinished()` branches including the
  `imageUrlAsHint`-only hint case and `imageUrlAtStart`-only (should be false) case
- `HintPersistenceTest.java`: Added `hintWithNullTextAndImageAsHintIsPersisted` (7 total)

### Verification

- Backend: 119/119 tests passing (`mvn.cmd clean test`)
- Frontend: 0 type errors (`npm run type-check`)

---

## Phase 18: Team Detail Page ✅ COMPLETE

**Goal:** Clicking a team name in the Gesamtrangliste navigates to a team detail page showing all quiz results for that team, sorted newest-first, with expandable per-question score breakdown.

### Backend

- `src/main/java/com/ande/pubquizzz/dto/TeamResultEntry.java`: New response DTO (`quizDate`, `totalPoints`, `answers`)
- `src/main/java/com/ande/pubquizzz/database/repositories/ResultRepository.java`: Added `findByTeamNameOrderByPubDateDesc` JPQL query with JOIN FETCH
- `src/main/java/com/ande/pubquizzz/service/ResultService.java`: Added `getResultsForTeam(String teamName)` method
- `src/main/java/com/ande/pubquizzz/controller/UserTeamController.java`: New `GET /api/teams/{teamName}/results` REST controller
- Tests: `ResultServiceTeamResultsTest` (3 unit tests) + `UserTeamControllerTest` (3 integration tests)

### Frontend

- `src/main/webapp/src/js/types.ts`: Added `TeamResultEntry` interface
- `src/main/webapp/src/js/leaderboard.ts`: Team name cells now render as `<a>` links to `/team.html?team=<name>`
- `src/main/webapp/src/team.html`: New team detail page HTML (mobile-first Tailwind)
- `src/main/webapp/src/js/team.ts`: Fetch + render logic with expandable per-question detail rows
- `src/main/webapp/vite.config.ts`: Added `team: './team.html'` entry

### Verification

- Backend: 132/132 tests passing (`.\mvnw.cmd test`)
- Frontend: build succeeds (`npm run build`), `team.html` + `team-*.js` emitted to `src/main/resources/static/`
- Two git commits: backend (`feat: add GET /api/teams/{teamName}/results endpoint with tests`) + frontend (`feat: add team detail page with expandable quiz result breakdown`)

---

## Phase 19: Quiz Archive Page ✅ COMPLETE

**Goal:** A new "Quiz Archiv" navigation card on the home page links to `quizzes.html` — a list of all quizzes (title, date, team count) sorted newest-first. Clicking a quiz title navigates to `quiz.html?id=<quizId>` showing a ranked table of teams with total points, plus expandable per-question score breakdown (Olympic-style ranking).

### Backend

- `src/main/java/com/ande/pubquizzz/dto/QuizSummaryDTO.java`: New DTO (`quizId`, `quizTitle`, `pubDate`, `teamCount`)
- `src/main/java/com/ande/pubquizzz/dto/QuizResultEntry.java`: New DTO (`rank`, `teamName`, `totalPoints`, `answers`)
- `src/main/java/com/ande/pubquizzz/database/repositories/QuizRepository.java`: Added `findAllWithResultCount()` JPQL query grouping quizzes with result count, ordered by pubDate DESC
- `src/main/java/com/ande/pubquizzz/database/repositories/ResultRepository.java`: Added `findByQuizIdWithTeamAndAnswers` JPQL query with JOIN FETCH
- `src/main/java/com/ande/pubquizzz/service/ResultService.java`: Added `getQuizSummaries()` and `getResultsForQuiz(Long quizId)` with Olympic ranking
- `src/main/java/com/ande/pubquizzz/controller/UserQuizController.java`: New controller with `GET /api/quizzes` and `GET /api/quizzes/{quizId}/results`
- Tests: `ResultServiceQuizTest` (4 unit tests) + `UserQuizControllerTest` (4 integration tests)

### Frontend

- `src/main/webapp/src/js/types.ts`: Added `QuizSummaryDTO` and `QuizResultEntry` interfaces
- `src/main/webapp/src/index.html`: Added "Quiz Archiv" navigation card between Gesamtrangliste and Admin Panel
- `src/main/webapp/vite.config.ts`: Added `quizzes: './quizzes.html'` and `quiz: './quiz.html'` entries
- `src/main/webapp/src/quizzes.html`: New quiz list page HTML (mobile-first Tailwind)
- `src/main/webapp/src/js/quizzes.ts`: Fetch + render logic for quiz list
- `src/main/webapp/src/quiz.html`: New quiz results page HTML with rank, team, points, details columns
- `src/main/webapp/src/js/quiz.ts`: Fetch + render logic with Olympic medals, team links, expandable detail rows

### Verification

- Backend: 141/141 tests passing (`mvn test`)
- Frontend: build succeeds (`npm run build`), `quizzes.html` + `quiz.html` + assets emitted to `src/main/resources/static/`
- Two git commits: backend (`feat: add quiz archive API - GET /api/quizzes and GET /api/quizzes/{id}/results with Olympic ranking`) + frontend (`feat: add Quiz Archiv frontend pages - quizzes list and quiz results with Olympic ranking display`)

---

## Phase 20: Admin Section Mobile-Friendly ✅ COMPLETE

### Goal

Make all three admin pages usable on mobile phones. The non-admin pages were already mobile-first (Tailwind utility
classes with `sm:` breakpoints); the admin pages still used old fixed-size CSS class definitions.

### Problems fixed

- No `<meta viewport>` on any admin page → browser zoomed out on mobile
- `.container` fixed `p-[30px]` → clipped on narrow screens
- `.container.small` fixed `max-w-[500px]` → too wide on small phones
- `.modal-content` `w-4/5 max-w-[800px]` → barely fit on 375px screen
- `body` `mt-[50px]` → wasted space on mobile
- `th/td` `p-3` → too much padding on small screens
- 12-column results table → completely unusable without scroll
- Multiple `onclick=` / `onchange=` inline event handlers remaining in admin HTML/TS

### Changes

#### `styles.css`

- `body`: `mt-[50px]` → `mt-4 sm:mt-[50px]`
- `.container`: `p-[30px]` → `px-4 py-5 sm:p-[30px]`
- `.container.small`: `max-w-[500px]` → `max-w-full sm:max-w-[500px]`
- `.modal-content`: `my-[5%] p-[30px] w-4/5 max-w-[800px] max-h-[80vh]` →
  `my-[2%] sm:my-[5%] p-4 sm:p-[30px] w-[97%] sm:w-4/5 sm:max-w-[800px] max-h-[90vh] sm:max-h-[80vh]`
- `th, td`: `p-3` → `p-1.5 sm:p-3 text-sm sm:text-base`

#### `admin_main.html`, `create_quiz.html`, `register_user.html`

- Added `<meta name="viewport" content="width=device-width, initial-scale=1">` to all three

#### `admin_functions.ts`

- `renderTable`: wraps output in `<div class="overflow-x-auto">...</div>`
- `viewResults` inline table: wrapped in `<div class="overflow-x-auto">...</div>`
- `buildAddResultForm`: replaced `onclick="closeModal()"` on Abbrechen button with `id="add-result-cancel-btn"`; wired
  in `showAddResultModal`
- `window.addEventListener('load')`: added ID-based event listeners for all `admin_main.html` buttons (`createQuizBtn`,
  `viewQuizzesBtn`, `createTeamBtn`, `viewTeamsBtn`, `addResultBtn`, `viewResultsBtn`, `createUserBtn`, `viewUsersBtn`,
  `backBtn`, `modalCloseBtn`) — removed all corresponding inline `onclick=` from HTML

#### `admin_main.html`

- Replaced all `onclick=` on buttons with `id=` attributes
- Replaced `onclick="closeModal()"` on modal close span with `id="modalCloseBtn"`

#### `create_quiz.ts`

- Hint file inputs no longer use `onchange="previewHintImage(...)"` — replaced with `addEventListener('change', ...)`
  after appending each question section to the DOM
- Removed `previewHintImage` from global window registration (no longer needed)
- `backBtn` in `create_quiz.html` wired via `addEventListener` in load handler

#### `register_user.html` + `register_user.ts`

- Replaced `onclick="registerUser()"` and `onclick="goBack()"` with `id="registerUserBtn"` and `id="backBtn"`
- Wired both in `register_user.ts` load handler

### Verification

- Backend: 141/141 tests passing (`mvn test`)
- Frontend: 0 type errors (`npm run type-check`)
- Build: succeeds (`npm run build`)

---

## Phase 21: Fix hint_text NOT NULL — Flyway removed ✅ COMPLETE

### Problem

`ddl-auto=update` only adds new columns; it never alters existing constraints. Phase 17 changed
`Hint.hintText` to `nullable = true` in the entity, but the live database still had
`hint_text NOT NULL` from its original creation. Any quiz edit that saved a hint with null text
failed with `JdbcSQLIntegrityConstraintViolationException: NULL not allowed for column "HINT_TEXT"`.

### Solution

Remove the live Portainer DB file and redeploy. Hibernate `ddl-auto=update` recreates all tables
from the current entity schema, which has `hint_text` nullable from the start. No migration tooling
needed.

A Flyway-based migration was attempted first but could not be confirmed working on Portainer (not
appearing in startup logs despite correct config). Flyway was removed entirely to keep the setup simple.

### Changes

- `pom.xml`: Removed `flyway-core` dependency
- `application.properties`: Removed 3 Flyway config lines; restored `logging.level.root=INFO`
- `application-test.properties`: Removed `spring.flyway.enabled=false`
- `src/main/resources/db/`: Deleted entire directory (including `V2__fix_hint_text_nullable.sql`)

### Portainer deploy steps

1. Take a manual backup via the admin UI
2. Delete the H2 DB volume (`h2_data`) on Portainer
3. Deploy the new image — Hibernate recreates the schema with nullable `hint_text`

---

## Phase 22: Fix Orphaned Image Files ✅ COMPLETE

### Problem

Two scenarios caused image files to accumulate on disk and never be deleted:

1. **Quiz deleted** — `deleteQuiz()` removed DB records but never deleted the quiz's image files from disk
2. **Backup restore** — old images deleted between backups were re-introduced from the backup zip on each restore

The backup size kept growing even without new data being added.

### Changes

#### Fix 1: Delete images on quiz delete (`QuizService.deleteQuiz`)

- Replaced `existsById` check with `findById` to load the quiz (and its hints via EAGER fetch)
- Snapshot all image URLs from hints before cascade-delete
- After DB delete, call `imageStorageService.delete()` for each URL

#### Fix 2: `DELETE /admin/cleanup-images` endpoint

- `CleanupResult.java`: Response DTO (`deletedCount`, `deletedFiles`)
- `ImageStorageService.cleanupOrphanedImages(Set<String> referencedUrls)`: Lists all files in upload dir, deletes any
  not in the referenced set
- `QuizService.cleanupOrphanedImages()`: Collects all URLs from DB, delegates to `ImageStorageService`
- `AdminQuizController`: `DELETE /admin/cleanup-images` — returns `CleanupResult` JSON

#### Tests (+7 new, 150 total)

- `QuizServiceDeleteTest`: 2 new tests — `deletesAllImageFiles`, `noImages_doesNotCallImageDelete`
- `ImageStorageServiceTest`: 3 new tests — `cleanup_deletesOrphanedFile`, `cleanup_keepsReferencedFile`,
  `cleanup_emptyDir_returnsZero`
- `AdminQuizControllerTest`: 2 new tests — `cleanupImages_returnsDeletedCount`,
  `cleanupImages_nothingToDelete_returnsZero`

### To clean up existing Portainer uploads

After deploying, click "Verwaiste Bilder löschen" in the admin panel (Wartung section), or call
`DELETE /admin/cleanup-images`. Returns JSON with count and names of deleted files.

### Phase 22 addendum: Trigger cleanup from UI + on restore ✅

- `admin_main.html`: Added "Wartung" section with "Verwaiste Bilder löschen" button (`cleanupImagesBtn`)
- `admin_functions.ts`: Added `cleanupImages()` — calls `DELETE /admin/cleanup-images`, shows result message in German
- `BackupRestoreListener`: Now accepts `QuizService` as constructor dependency; calls `cleanupOrphanedImages()`
  automatically after every successful restore
- `BackupRestoreListenerTest`: Updated constructor call; added 2 new tests (`applyRestore_callsCleanupAfterRestore`,
  `noPendingRestore_doesNotCallCleanup`)

### Verification

- Backend: 155/155 tests passing (`mvn test`)
- Frontend: 0 type errors (`npm run type-check`)

---

## Phase 24: "Sieger" Column on Quiz Archiv Page ✅ COMPLETE

### Goal

Replace the "Datum" column on `quizzes.html` with a "Sieger" (winner) column showing the team with
the highest total points for each quiz, displayed as a link to their team detail page.

### Changes

#### Backend

- `QuizSummaryDTO.java`: Added `String winnerTeamName` (nullable)
- `ResultRepository.java`: Added `findWinnerTeamNamesByQuizIds(@Param("quizIds") List<Long> quizIds)` — single-query
  batch JPQL that finds, for each quiz ID, the team whose total point sum equals the maximum total point sum for that
  quiz (handles ties by returning multiple rows; service keeps first)
- `ResultService.getQuizSummaries()`: Collects quiz IDs with at least one result, calls
  `findWinnerTeamNamesByQuizIds` in one round-trip, builds a `Map<Long, String>` of quizId → first winner name,
  populates `winnerTeamName` on each DTO

#### Frontend

- `types.ts`: Added `winnerTeamName?: string | null` to `QuizSummaryDTO`
- `quizzes.html`: Renamed `<th>Datum</th>` → `<th>Sieger</th>`
- `quizzes.ts`: Winner cell renders as `<a href="/team.html?team=...">name</a>` when present, or `—` when null

#### Tests (+3 new unit tests, +1 updated controller test, 155 total)

- `ResultServiceQuizTest`: Added `getQuizSummaries_withResults_populatesWinnerTeamName`,
  `getQuizSummaries_withNoResults_winnerTeamNameIsNull`, `getQuizSummaries_withTiedWinners_returnsOneWinnerName`
- `ResultServiceQuizTest`: Updated `getQuizSummaries_returnsSortedNewestFirst` to stub
  `findWinnerTeamNamesByQuizIds` (required by new service logic)
- `UserQuizControllerTest`: Updated `getQuizSummaries_authenticated_returnsList` to assert `winnerTeamName` in JSON

### Verification

- Backend: 155/155 tests passing (`mvn test`)
- Frontend: 0 type errors (`npm run type-check`)
- Frontend build: succeeds (`npm run build`)

---

## Phase 25: Tiebreaker — More 5-point Answers Win, Then More 3-point Answers ✅ COMPLETE

### Goal

When teams are tied on total points, the team with more 5-point answers wins. If still tied, the team with
more 3-point answers wins. This applies to both:

- `quizzes.html` — Sieger (winner) column
- `quiz.html` — Olympic ranking table

### Changes

#### Backend

- `Result.java`: Added `countAnswersWithPoints(int points)` helper method
- `ResultService.java`:
  - Added `RESULT_COMPARATOR` static constant: `totalPoints DESC → count(5) DESC → count(3) DESC`
  - `getResultsForQuiz()`: replaced plain `totalPoints` sort with `RESULT_COMPARATOR`; Olympic rank
    now advances only when `RESULT_COMPARATOR.compare(...) != 0` (teams are only same-rank when equal
    on all three criteria)
  - `getQuizSummaries()`: replaced `findWinnerTeamNamesByQuizIds` DB approach with `findScoresByQuizIds`
    + Java tiebreaker using the same three criteria
- `ResultRepository.java`:
  - Removed `findWinnerTeamNamesByQuizIds` (couldn't express tiebreaker in JPQL)
  - Added `findScoresByQuizIds(@Param("quizIds") List<Long> quizIds)` — returns `(quizId, teamName,
    totalPoints, fivesCount, threesCount)` per result in one query; tiebreaker resolved in Java

#### Tests (+5 new, 3 updated, 158 total)

- `ResultServiceQuizTest`: Replaced `getQuizSummaries_withTiedWinners_returnsOneWinnerName` with
  `getQuizSummaries_tieOnTotal_winnerHasMoreFives` and
  `getQuizSummaries_tieOnTotalAndFives_winnerHasMoreThrees`
- `ResultServiceQuizTest`: Updated `getQuizSummaries_withResults_populatesWinnerTeamName` to use
  `findScoresByQuizIds` stub; updated `getQuizSummaries_returnsSortedNewestFirst` same
- `ResultServiceQuizTest`: Added `getResultsForQuiz_tieOnTotal_rankedByFivesDesc` and
  `getResultsForQuiz_tieOnTotalAndFives_rankedByThreesDesc`

### Verification

- Backend: 158/158 tests passing (`mvn test`)
- Frontend: 0 type errors (`npm run type-check`)
- Frontend build: succeeds (`npm run build`)

### Goal

Comprehensive code quality improvement pass covering backend dead code, N+1 queries, service clarity, frontend XSS/UX
fixes, and new unit tests.

### Backend Changes

- `BackupService.java`: Two-pass ZIP validation — validates structure before extracting; cleans up on failure
- `ResultRepository.java`: Added `findByQuizIdWithAnswers()` with JOIN FETCH to fix N+1 on result loading
- `QuizRepository.java`: Added `findAllReferencedImageUrls()` to fix N+1 in `cleanupOrphanedImages()`
- `ResultService.java`: Extracted `toAnswerScoreDTO()` helper; replaced O(n²) sort with `record ResultWithPoints`
- `TeamRepository.java`: Removed unused `findByTeamName()` method
- `UserController.java`: Removed unused `@Slf4j` import
- `AllTimeLeaderboardEntry.java`: Removed unused `@AllArgsConstructor`
- `Hint.java`: Removed stale `nullable=true` on `hint_text` (already nullable; was redundant noise)
- `QuizService.java`: Import cleanup (removed unused imports)
- `DatabaseTest.java`: Removed commented-out assertion on line 188
- `AdminQuizControllerTest.java`: Removed duplicate `import java.util.List`

### Frontend Changes

- `utils.ts`: Extracted shared helpers `escapeHtml`, `getMedal`, `numberBadge`, `toggleDetail` (previously duplicated
  across `quiz.ts`, `leaderboard.ts`, `team.ts`, `quizzes.ts`)
- `admin_functions.ts`: Fixed XSS in `renderTable` (use `escapeHtml`); added `response.ok` checks; replaced `alert()`
  with `showModal` in `deleteUser`; added `console.error` in catch blocks
- `quiz.ts`, `leaderboard.ts`, `quizzes.ts`, `team.ts`: Updated to use shared helpers from `utils.ts`

### New Tests

- `UserServiceTest.java` (10 tests): Full coverage of user CRUD and validation paths
- `QuizServiceCreateTest.java` (6 tests): Quiz creation validation including hint-count rules
- `UserControllerTest.java` (3 tests): `@WebMvcTest` coverage for `UserController`
- `BackupServiceTest.java` (+1 test): ZIP validation path

### Verification

- Backend: 152/152 tests passing (`mvn test`)
- Frontend: 0 type errors (`npm run type-check`)

---

## Phase 26: Version in Admin Header ✅ COMPLETE

### Goal

Show the deployed Maven version in the admin panel `<h1>` header, e.g. `Admin Bereich (1.0.1-SNAPSHOT)`, fetched live
from the backend at page load.

### Changes

#### Backend

- `pom.xml`: Added `build-info` execution to `spring-boot-maven-plugin` so `META-INF/build-info.properties` is generated
  at build time (provides the `BuildProperties` Spring bean)
- `UserController.java`: Added `BuildProperties` constructor dependency (via `@RequiredArgsConstructor`); added
  `GET /api/version` endpoint returning `{ "version": "<maven.version>" }`; endpoint is protected by existing
  `anyRequest().authenticated()` in `SecurityConfig`

#### Frontend

- `admin_functions.ts`: Inside `window.addEventListener('load', ...)`, after `cleanupImagesBtn` wiring, added a
  `fetch('/api/version')` call that appends ` (version)` to the `<h1>` text content. Silently ignores errors.

#### Tests (4 new)

- `UserControllerTest.java` (new): 4 `@WebMvcTest` tests:
  - `getVersion_authenticated_returnsVersion` — asserts `{ "version": "1.0.0-TEST" }` JSON
  - `getVersion_unauthenticated_redirectsToLogin` — asserts 302 redirect
  - `isAdmin_withAdminRole_returnsTrue` — asserts `{ "admin": true }`
  - `isAdmin_withUserRole_returnsFalse` — asserts `{ "admin": false }`

### Verification

- Backend: 162/162 tests passing (`mvn test`)
- Frontend: 0 type errors (`npm run type-check`)
- Frontend build: succeeds (`npm run build`)
