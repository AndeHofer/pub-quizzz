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

## Final Status

- **Backend tests**: 61/61 passing (`mvn clean test`)
- **Frontend type check**: 0 errors (`npm run type-check`)
- **Frontend build**: succeeds (`vite build`)



## Key Discoveries

- Spring Boot 4.x `@WebMvcTest` requires explicit security auto-config imports to load `HttpSecurity` bean
- `@WithMockUser` requires `SecurityMockMvcConfigurers.springSecurity()` applied via `MockMvcBuilderCustomizer`
- Spring Boot 4.x Jackson 3.x: `@WebMvcTest` does NOT provide `com.fasterxml.jackson.databind.ObjectMapper` bean — use
  `new ObjectMapper()` in tests
- TypeScript files without `import`/`export` are treated as global scripts — duplicate functions cause TS errors; fix
  with `export {}`
- LSP errors in editor are Lombok false-positives; Maven compilation is the source of truth
