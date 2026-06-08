# Findings (Temporary)

Generated: 2026-05-29

## Ranked Findings

1. **Critical** - Stored XSS via unescaped uploaded document filename rendering
    - Files:
        - `src/main/webapp/src/js/create_quiz.ts`
        - `src/main/java/com/ande/pubquizzz/service/DocumentStorageService.java`
    - Problem: `originalFilename` is attacker-controlled and rendered via `innerHTML` without escaping.
    - Impact: Admin-session script execution when opening quiz document list.
    - Direction: Render with DOM APIs (`textContent`, `setAttribute`) and sanitize output.

2. **Critical** - Backup import is memory-heavy (DoS/OOM risk)
    - Files:
        - `src/main/java/com/ande/pubquizzz/controller/AdminBackupController.java`
        - `src/main/java/com/ande/pubquizzz/service/BackupService.java`
        - `src/main/resources/application.properties`
    - Problem: `file.getBytes()` + ZIP `readAllBytes()` with large upload limits.
    - Impact: Heap exhaustion, instability.
    - Direction: Stream upload/extraction, enforce size/entry/compression limits.

3. **Critical** - Restore process is destructive but not atomic
    - Files:
        - `src/main/java/com/ande/pubquizzz/listener/BackupRestoreListener.java`
        - `src/main/java/com/ande/pubquizzz/service/BackupService.java`
    - Problem: Restore can partially apply and still continue startup.
    - Impact: Corrupted/partial data state.
    - Direction: Temp-restore + validation + atomic switch; fail safe on errors.

4. **High** - Container runs as root
    - Files:
        - `Dockerfile`
        - `docker-compose.yml`
    - Problem: Runtime user is root.
    - Impact: Higher blast radius after compromise.
    - Direction: Run as non-root UID/GID, tighten writable mounts.

5. **High** - Upload type/content trust too permissive
    - Files:
        - `src/main/java/com/ande/pubquizzz/service/DocumentStorageService.java`
        - `src/main/java/com/ande/pubquizzz/controller/AdminQuizController.java`
    - Problem: MIME/type metadata is client-driven and weakly constrained.
    - Impact: Active-content upload and unsafe serving behavior.
    - Direction: Allowlist + server-side type checks + safer download headers.

6. **High** - Additional frontend XSS sinks beyond Phase 80
    - Files:
        - `src/main/webapp/src/js/create_result.ts`
        - `src/main/webapp/src/js/quiz-details.ts`
        - `src/main/webapp/src/js/index.ts`
    - Problem: Unsafe `innerHTML` with unescaped data in multiple render paths.
    - Impact: DOM injection risks on admin/public pages.
    - Direction: Replace unsafe sinks with DOM-safe rendering and escaping boundaries.

7. **High** - Upload writes can orphan files on failure
    - Files:
        - `src/main/java/com/ande/pubquizzz/controller/AdminQuizController.java`
        - `src/main/java/com/ande/pubquizzz/service/QuizService.java`
        - `src/main/java/com/ande/pubquizzz/service/DocumentStorageService.java`
    - Problem: File persistence can happen before request succeeds end-to-end.
    - Impact: Filesystem/DB drift and cleanup burden.
    - Direction: Stage files and finalize after transactional success, or rollback cleanup.

8. **High** - Uniqueness checks are race-prone
    - Files:
        - `src/main/java/com/ande/pubquizzz/service/UserService.java`
        - `src/main/java/com/ande/pubquizzz/service/TeamService.java`
        - `src/main/java/com/ande/pubquizzz/service/ResultService.java`
        - `src/main/java/com/ande/pubquizzz/exception/GlobalExceptionHandler.java`
    - Problem: check-then-save pattern can still violate DB constraints under concurrency.
    - Impact: Intermittent duplicate/500 errors.
    - Direction: catch/translate integrity violations to stable API errors (400/409).

9. **Medium** - EAGER graph loading hurts scalability
    - Files:
        - `src/main/java/com/ande/pubquizzz/database/entities/Quiz.java`
        - `src/main/java/com/ande/pubquizzz/database/entities/Question.java`
        - `src/main/java/com/ande/pubquizzz/service/QuizService.java`
    - Problem: broad eager loading of quiz/questions/hints.
    - Impact: unnecessary query and memory overhead.
    - Direction: LAZY + explicit fetch joins/projections per use case.

10. **Medium** - Admin logs endpoint reparses whole file per request
    - Files:
        - `src/main/java/com/ande/pubquizzz/service/AdminLogService.java`
    - Problem: full-file read/parse on each request.
    - Impact: latency/memory pressure with large logs.
    - Direction: tail/streamed reads + bounded windows.

11. **Medium** - Security telemetry includes sensitive details
    - Files:
        - `src/main/java/com/ande/pubquizzz/security/LoggingAccessDeniedHandler.java`
        - `src/main/java/com/ande/pubquizzz/listener/AuthenticationEventListener.java`
    - Problem: verbose session/token/auth context in logs.
    - Impact: valuable metadata exposure via logs.
    - Direction: minimize/redact sensitive fields.

12. **Medium** - Coverage gaps in high-risk modules
    - Files (examples):
        - `src/main/webapp/src/js/create_quiz.ts`
        - `src/main/webapp/src/js/create_result.ts`
        - `src/main/webapp/src/js/quiz-details.ts`
        - backup/restore backend failure-path tests
    - Problem: limited automated coverage where risk is highest.
    - Impact: regression probability remains elevated.
    - Direction: add targeted unit/integration tests for failure and security-sensitive paths.

## Current Focus

- Completed: **Finding 1** (stored XSS in quiz document list rendering).
- Completed: **Finding 2** (streamed backup import and restore archive guardrails).
- Completed: **Finding 3** (restore process atomicity / partial-apply risk).
- Next candidate: **Finding 4** (container runs as root).
