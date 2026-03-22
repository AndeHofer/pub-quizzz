# Project: Pub Quizzz

Pub Quizzz is a Spring Boot 4.x web application for managing and running pub quizzes. Admins create quizzes, record team results, and view leaderboards. Players access quizzes through a public frontend.

## Architecture

- **Backend:** Spring Boot 4.x, Java 21, Spring Security, Spring Data JPA (H2 dev / configurable prod DB), Lombok, MapStruct, Jakarta Validation
- **Frontend:** TypeScript, Vite, Tailwind CSS v4 — built into `src/main/resources/static` via `frontend-maven-plugin`
- **API:** REST; new API endpoints follow OpenAPI-first workflow (spec → generated interfaces → implementation)
- **Auth:** Session-based, role-based (`ADMIN` / `USER`); all `/admin/**` endpoints require `ADMIN` role enforced via `@PreAuthorize`

## Business Rules

- Each quiz must have exactly **8 questions**
- Questions 1–4 must have exactly **4 hints**; questions 5–8 must have exactly **3 hints**
- A team may submit results for a given quiz only once (unique constraint on `team_id + quiz_id`)

## Key Design Decisions

- Custom typed exception hierarchy (`ResourceNotFoundException`, `BusinessValidationException`, `ImageStorageException`) handled centrally by `GlobalExceptionHandler`
- Bean Validation (`@Valid`) on all request DTOs; manual format checks removed from service layer
- All `any` types replaced with proper TypeScript interfaces in `types.ts`
- UI text is in **German**

