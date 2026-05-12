# Instructions:

- Do not write markdown files or text files with implementation summaries unless you are instructed to do so!!!
- All APIs need to have integration tests.
- All business logic needs to have unit tests.
- Always remove unused imports.
- Document all the steps you plan to do in a "progress.md" file before you start coding. This file should be updated as
  you make progress and should include a task list of what was done, what is left to do, and any blockers you encounter.
- Ensure to document your progress after every single step in case copilot crashes.
- Keep only 3 finished features of less (not more than 50 lines) in progress.md and move older to progress_archive.md to
  keep the progress.md clean and
  short.
- progress.md and progress_archive.md should be written in English.
- Minimize the commands executed that need to be confirmed by user input. E.g. do not run individual tests, but run all
  tests and then fix all at once.
- Do not put files outside the project structure as you may not have permission to access them e.g. in /tmp. Delete
  the files afterwards again.
- Before confirming that features are working, ensure to run all the tests performed in the CICD pipeline, as well as
  frontend type check is successful.
- run maven always in windows style to avoid creating nul files
- ignore folder like node_modules, target

## Backend

- Use Spring boot for the backend.
- Use Java 25 for the backend.
- Use JUnit for testing the backend.
- Use Mockito for mocking dependencies in backend tests.
- Use Spring Test for integration testing the backend.
- Use Spring Security for authentication and authorization in the backend.
- Use Lombok for reducing boilerplate code in the backend.
- Use MapStruct for mapping between entities and DTOs in the backend.

## Frontend

- Use TypeScript for the frontend.
- Use Tailwind CSS for styling the frontend.
- Use Vite for the frontend build tool.
