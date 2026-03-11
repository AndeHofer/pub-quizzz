# AGENTS.md — pub-quizzz

Guidance for AI coding agents working in this repository.

---

# Instructions:

- Do not write markdown files or text files with implementation summaries unless you are instructed to do so!!!
- When you are instructed to write APIs, write an OpenAPI specification for the API endpoints you are implementing. Then
  generate the API Interfaces from the specification and lastly, create an implementation of that interface.
- All APIs need to have integration tests.
- All business logic needs to have unit tests.
- Always remove unused imports.
- Document all the steps you plan to do in a "progress.md" file before you start coding. This file should be updated as
  you make progress and should include a task list of what was done, what is left to do, and any blockers you encounter.
- Ensure to document your progress after every single step in case copilot crashes.
- Document your idea of what the project should be in a concise form in a "project.md". Update this whenever
  requirements change.
- Minimize the commands executed that need to be confirmed by user input. E.g. do not run individual tests, but run all
  tests and then fix all at once.
- Do not put files outside of the project structure as you may not have permission to access them e.g. in /tmp. Delete
  the files afterwards again.
- Before confirming that features are working, ensure to run all the tests performed in the CICD pipeline, as well as
  frontend type check is successful.

## Backend

- Use Spring boot for the backend.
- Use Java 21 for the backend.
- Use the openapi-generator-maven-plugin to generate API interfaces from OpenAPI specifications.
- Use JUnit for testing the backend.
- Use Mockito for mocking dependencies in backend tests.
- Use Spring Test for integration testing the backend.
- Use Spring Security for authentication and authorization in the backend.
- Use Lombok for reducing boilerplate code in the backend.
- Use MapStruct for mapping between entities and DTOs in the backend.

## Business Rules (Critical)

- Each quiz must have exactly **8 questions**
- Questions 1–4 must have exactly **4 hints**
- Questions 5–8 must have exactly **3 hints**

## Frontend

- UI text is in **German** — keep all user-facing strings in German


