# Progress

## Open Tasks

- [x] Phase 95: Write and validate design spec for Vite-managed 403 page visual alignment
- [x] Phase 95: Move 403 page from `public` to Vite `src` entry and remove generated-css hardcoding
- [x] Phase 95: Keep security forwarding behavior unchanged (`/403.html`, HTTP 403)
- [x] Phase 95: Run verification (`./mvnw.cmd test`, `npm run type-check`, `npm run build`)

- [x] Phase 94: Add RED tests for global custom 403 page behavior (unit + security integration)
- [x] Phase 94: Implement global access-denied forward to static `403.html` while keeping 403 logging
- [x] Phase 94: Run targeted verification (
  `./mvnw.cmd "-Dtest=LoggingAccessDeniedHandlerTest,ForbiddenPageAccessTest,SecurityAccessTest" test`)
- [x] Phase 94: Run full verification (`./mvnw.cmd test`, `npm run type-check`,
  `./mvnw.cmd "-DskipTests" "frontend:npm@npm run build" "frontend:npm@npm run type-check"`)

## Finished Phases

### Phase 95: Vite-Managed 403 Page with Index Visual Language ✅ COMPLETE

- Wrote approved design spec in `docs/superpowers/specs/2026-06-07-403-vite-page-design.md` and implementation plan in
  `docs/superpowers/plans/2026-06-07-403-vite-page.md`.
- Removed brittle public-page variant (`src/main/webapp/public/403.html`) that hardcoded generated CSS asset names.
- Added Vite-managed 403 source page in `src/main/webapp/src/403.html` using the same visual language as `index.html` (
  card layout, spacing, typography, German UI text, responsive behavior).
- Registered 403 as a Vite entry in `src/main/webapp/vite.config.ts` (`403: './403.html'`) so hashed assets are injected
  automatically.
- Preserved security behavior: 403 handler still returns HTTP 403 and forwards to `/403.html`.
- Added/updated test assertion in `src/test/java/com/ande/pubquizzz/security/ForbiddenPageAccessTest.java` to ensure the
  served 403 page matches the expected layout class signature.
- Verification passed:
  - `./mvnw.cmd "-Dtest=ForbiddenPageAccessTest" test` (RED first, then GREEN after migration)
  - `./mvnw.cmd "-Dtest=LoggingAccessDeniedHandlerTest,ForbiddenPageAccessTest,SecurityAccessTest" test`
  - `./mvnw.cmd test` (`BUILD SUCCESS`, backend + frontend tests)
  - `npm run type-check` (in `src/main/webapp`)
  - `npm run build` (in `src/main/webapp`, includes generated `resources/static/403.html`)

### Phase 94: Global Custom 403 Error Page (No Countdown) ✅ COMPLETE

- Added new static error page `src/main/webapp/public/403.html` with German copy and actions (`Zur Startseite` as
  primary, `Zum Login` as secondary).
- Updated `src/main/java/com/ande/pubquizzz/security/LoggingAccessDeniedHandler.java` to keep existing security logging
  but return a real `403` while forwarding internally to `/403.html`.
- Extended unit tests in `src/test/java/com/ande/pubquizzz/security/LoggingAccessDeniedHandlerTest.java` to assert
  forwarding behavior.
- Added integration-style security test `src/test/java/com/ande/pubquizzz/security/ForbiddenPageAccessTest.java` proving
  non-admin access to admin endpoint is forbidden and custom page content is available.
- Verification passed:
  - `./mvnw.cmd "-Dtest=LoggingAccessDeniedHandlerTest,ForbiddenPageAccessTest,SecurityAccessTest" test`
  - `./mvnw.cmd test` (`BUILD SUCCESS`, backend + frontend tests)
  - `npm run type-check` (in `src/main/webapp`)
  - `./mvnw.cmd "-DskipTests" "frontend:npm@npm run build" "frontend:npm@npm run type-check"` (`BUILD SUCCESS`; note:
    this execution path runs frontend build successfully, while standalone type-check was validated via
    `npm run type-check`)

### Phase 93: Login Security Hardening ✅ COMPLETE

- Added integration tests for `/login` cache headers and CSRF enforcement.
- Implemented explicit non-cache headers for `/login` responses.
- Verification passed: `npm run test`, `npm run type-check`, `npm run build`, `./mvnw.cmd test`.

### Phase 92: Include Neuigkeiten + Usage Events in Backup/Restore ✅ COMPLETE

- Extended backup export/restore coverage to include `news` and `app_usage_event` tables.
- Added RED/GREEN tests for SQL dump inclusion and restore cycle behavior.
- Verification passed: `npm run test`, `npm run type-check`, `npm run build`, `./mvnw.cmd test`.
