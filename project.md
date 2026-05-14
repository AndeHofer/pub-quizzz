# Project: Pub Quizzz

Pub Quizzz is a Spring Boot 4.x web application for managing and running pub quizzes. Admins create quizzes, record team results, and view leaderboards. Players access quizzes through a public frontend.

## Architecture

- **Backend:** Spring Boot 4.x, Java 21, Spring Security, Spring Data JPA (H2 dev / configurable prod DB), Lombok, MapStruct, Jakarta Validation
- **Frontend:** TypeScript, Vite, Tailwind CSS v4 — built into `src/main/resources/static` via `frontend-maven-plugin`
- **API:** REST; controllers use Spring MVC annotations directly (`@RestController`, `@GetMapping`, etc.)
- **Auth:** Session-based, role-based (`ADMIN` / `USER`); all `/admin/**` endpoints require `ADMIN` role enforced via `@PreAuthorize`

## Business Rules

- Each quiz must have exactly **8 questions**
- Questions 1–4 must have exactly **4 hints**; questions 5–8 must have exactly **3 hints**
- For questions 5–8, the answer can be text, image, or both (questions 1–4 still require text answer)
- A team may submit results for a given quiz only once (unique constraint on `team_id + quiz_id`)

## Key Design Decisions

- Custom typed exception hierarchy (`ResourceNotFoundException`, `BusinessValidationException`, `ImageStorageException`) handled centrally by `GlobalExceptionHandler`
- Bean Validation (`@Valid`) on all request DTOs; manual format checks removed from service layer
- All `any` types replaced with proper TypeScript interfaces in `types.ts`
- UI text is in **German**
- Public quiz details are available for authenticated users via `GET /api/quizzes/{quizId}/detail` and rendered in a dedicated `quiz-details.html` page with explicit quiz selection
- Quiz detail includes a per-question `Punkte pro Team` action that opens a modal with team points for the selected question (based on `GET /api/quizzes/{quizId}/results` answer scores)
- Quiz images (hint images) stored in `${app.upload.dir}/` and served publicly via `/uploads/**`
- Quiz documents (any file type) stored in `${app.upload.dir}/documents/` and served only to `ADMIN` users; managed in the quiz edit form
- Quizzes can optionally store an author/creator (`Urheber`) as free text, editable in admin create/edit flow
- App usage statistics are persisted in extensible event table `app_usage_event`; currently `AUTH_SUCCESS` writes username (string) and timestamp on successful login for future admin analytics expansion
