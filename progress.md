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

### Task 1: OpenAPI Spec + DTO ✅ COMMITTED
- Created `src/main/resources/openapi/leaderboard-api.yaml`
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
