# AGENTS.md — pub-quizzz

Guidance for AI coding agents working in this repository.

---

## Project Overview

**pub-quizzz** is a Spring Boot 4.x web application for running pub quizzes. It uses:

- Java 21, Maven, Spring Boot (Web MVC, Security, Data JPA, Validation)
- H2 embedded file database (no external DB)
- Vanilla HTML/CSS/JavaScript frontend (no build pipeline, no npm)
- Lombok for boilerplate reduction

---

## Build & Run Commands

Use the Maven wrapper (`./mvnw` on Linux/Mac, `mvnw.cmd` on Windows).

```bash
# Build (skip tests)
./mvnw clean package -DskipTests

# Full build with tests
./mvnw clean verify

# Run locally (requires application-local.properties — see below)
./mvnw spring-boot:run -Dspring-boot.run.profiles=local

# Docker build and run
docker-compose up --build
```

---

## Test Commands

### Run all tests

```bash
./mvnw test
```

### Run a single test class

```bash
./mvnw test -Dtest=QuizHintValidationTest
./mvnw test -Dtest=ImageStorageServiceTest
```

### Run a single test method

```bash
./mvnw test -Dtest=QuizHintValidationTest#question1RejectsTooFewHints
./mvnw test -Dtest=HintPersistenceTest#hintsAreSavedWithCorrectOrder
```

### Test types

| File                      | Type                                 | Spring Context Required     |
|---------------------------|--------------------------------------|-----------------------------|
| `QuizHintValidationTest`  | Pure unit test                       | No                          |
| `ImageStorageServiceTest` | Unit test (`@TempDir`)               | No                          |
| `DatabaseTest`            | Integration test (`@SpringBootTest`) | Yes — needs `local` profile |
| `HintPersistenceTest`     | Integration test (`@SpringBootTest`) | Yes — needs `local` profile |

**Integration tests** use `@ActiveProfiles("local")` and connect to the real H2 file database.
They require `src/main/resources/application-local.properties` (gitignored — create it manually, see below).
Integration tests are **not isolated** — they modify the local database.

---

## Local Dev Setup

Create `src/main/resources/application-local.properties` (gitignored):

```properties
ADMIN_USER=<your-admin-username>
ADMIN_PASSWORD=<your-admin-password>
DEFAULT_USER=<your-default-username>
DEFAULT_PASSWORD=<your-default-password>
spring.datasource.url=jdbc:h2:file:./data/pubquizzz;AUTO_SERVER=TRUE
spring.datasource.username=sa
spring.datasource.password=
app.upload.dir=./data/uploads
```

---

## Code Style Guidelines

### Package Structure

```
com.ande.pubquizzz
  .config         — Spring @Configuration classes
  .controller     — @RestController classes
  .database
    .entities     — JPA @Entity classes
    .repositories — Spring Data JPA interfaces
  .dto            — Data transfer objects (no JPA, no logic)
  .security       — SecurityConfig, CustomUserDetailsService
  .service        — @Service business logic
```

### Naming Conventions

- **Classes**: `PascalCase` — `QuizService`, `AppUser`, `CreateQuizRequest`
- **Methods/fields**: `camelCase` — `getAllQuizzes`, `quizId`, `pubDate`
- **DB table names**: lowercase — `quiz`, `team`, `result`
- **DB column names**: `snake_case` where explicit — `hint_order`, `image_url`
- **Enums/constants**: `UPPER_SNAKE_CASE` — `ADMIN`, `USER`
- **Test methods**: descriptive `camelCase` — `hintsAreSavedWithCorrectOrder`

### Lombok Usage

```java
// Entities: selective Lombok — NOT @Data (avoids JPA equals/hashCode pitfalls)
@Getter
@Setter
@ToString
@RequiredArgsConstructor
public class Quiz { ...
}

// DTOs: @Data is acceptable
@Data
public class QuizDTO { ...
}

// Services/Controllers: always use @Slf4j for logging
@Slf4j
@Service
@RequiredArgsConstructor
public class QuizService { ...
}
```

### Dependency Injection

Always use constructor injection via `@RequiredArgsConstructor` + `private final` fields.
Never use `@Autowired` on fields.

```java

@Service
@RequiredArgsConstructor
public class QuizService {
    private final QuizRepository quizRepository;
    private final TeamRepository teamRepository;
}
```

### Service Layer

- Annotate read methods with `@Transactional(readOnly = true)`, write methods with `@Transactional`
- Log at the start of every public method: `log.info("Fetching quiz with ID: {}", id);`
- Convert entities to DTOs via a private `toDTO(Entity)` method — no external mapper library
- Throw `IllegalArgumentException` for business rule violations (controllers catch these)
- Return `Optional<T>` for single-entity lookups that may not exist

```java

@Transactional(readOnly = true)
public Optional<QuizDTO> getQuizById(Long id) {
    log.info("Fetching quiz with ID: {}", id);
    return quizRepository.findById(id).map(this::toDTO);
}
```

### Controller Layer

- No business logic — delegate everything to services
- Use `Optional`-chaining for 404 handling:

```java

@GetMapping("/quiz/{id}")
public ResponseEntity<QuizDTO> getQuizById(@PathVariable Long id) {
    return quizService.getQuizById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
}
```

- Catch `IllegalArgumentException` → 400 Bad Request with message body
- Catch `Exception` (fallback) → 500 with log + message body
- Delete endpoints return `boolean` from service; return 404 if `false`
- There is no `@ControllerAdvice` — each controller method catches exceptions individually
- Use section headers for grouping: `// ==================== Section Name ====================`

### Entity Design

- Primary keys: `long` (primitive) on entities, `Long` (boxed) on DTOs
- Use `@Embeddable` + `Serializable` for composite PKs, with manual `equals`/`hashCode`
- Persist enums as strings: `@Enumerated(EnumType.STRING)`
- Use `jakarta.validation` annotations on entity fields (`@NotNull`, `@Min`, `@Max`)
- DTOs have **no** validation annotations — validation happens in the service layer
- Initialize collection fields inline: `private List<Question> questions = new ArrayList<>();`

### Repository Conventions

- Use Spring Data derived queries for simple cases
- Use `@Query` with JPQL + `JOIN FETCH` for performance-critical queries
- Nested property navigation in derived queries: `findByQuiz_QuizId(Long quizId)`

### Imports

- No wildcard imports (`import java.util.*` is not used)
- Jakarta EE packages (not `javax.*`): `jakarta.persistence.*`, `jakarta.validation.*`
- Lombok annotations: `lombok.Getter`, `lombok.Setter`, etc.

---

## Business Rules (Critical)

The `Quiz.addQuestion()` method enforces these rules — violations throw `IllegalArgumentException`:

- Questions must be numbered 1–8
- Questions 1–4 must have exactly **4 hints**
- Questions 5–8 must have exactly **3 hints**

These rules are validated in `QuizService` before persisting. Do not bypass them.

---

## Frontend (Plain JavaScript)

- No build pipeline, no npm, no TypeScript — do not add any
- Vanilla HTML/CSS/JavaScript only
- One JS file per page: `index.js`, `admin_functions.js`, `create_quiz.js`, `register_user.js`
- Use `fetch()` with `async/await`; build HTML fragments via template literals
- UI text is in **German** — keep all user-facing strings in German

---

## Commit Message Style

Follow conventional commits: `type(scope): description`

Common types: `feat`, `fix`, `refactor`, `build`, `doc`, `test`  
Scope is usually `pubquizzz` or a specific filename.

Examples:

```
feat(pubquizzz): add hint ordering to quiz service
fix(pubquizzz): handle missing image file in storage service
test(pubquizzz): add unit tests for hint validation
```

---

## Key Files

| Path                                              | Purpose                                        |
|---------------------------------------------------|------------------------------------------------|
| `src/main/java/.../PubQuizzzApplication.java`     | Spring Boot entry point                        |
| `src/main/java/.../config/UserConfig.java`        | Seeds default users from env vars at startup   |
| `src/main/java/.../service/QuizService.java`      | Core quiz business logic                       |
| `src/main/resources/application.properties`       | Production config                              |
| `src/main/resources/application-local.properties` | Local dev config (gitignored, create manually) |
| `pom.xml`                                         | Maven build file                               |
| `Dockerfile`                                      | Two-stage build: Maven → JRE Alpine            |
| `docker-compose.yml`                              | App on port 8080, H2 data in `h2_data` volume  |
