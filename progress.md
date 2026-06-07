# Progress

## Open Tasks

- [x] Phase 103: Add inline multi-event calendar links in Neuigkeiten text (Google + ICS)
- [x] Phase 103: Add/adjust frontend unit tests for marker parsing, metadata mapping, and calendar generation
- [x] Phase 103: Run full verification (`./mvnw.cmd test`, `npm run test`, `npm run type-check`, `npm run build`)

- [x] Phase 102: Roll out shared auth-expiry handling to remaining high-risk admin fetch flows
- [x] Phase 102: Add/adjust frontend tests for auth-expiry helper usage in migrated modules
- [x] Phase 102: Run full verification (`./mvnw.cmd test`, `npm run test`, `npm run type-check`, `npm run build`)

### Phase 103: Neuigkeiten Inline Multi-Event Calendar Actions (In Progress)

- Step 1 started: Confirmed design constraints and authoring format.
  - Hidden metadata in text via HTML comment JSON with required fields per event: `title`, `start`, `end`, `location`.
  - Visible markers in body text: `[event-date:<id>]Label[/event-date]`, multiple markers allowed.
  - Time values without offset are interpreted as Europe/Vienna local time.
- Step 2 done (TDD RED/GREEN): Added failing frontend tests for event metadata parsing, inline marker mapping, required
  title handling, Google URL generation, and ICS generation; then implemented frontend logic in `news.ts` to pass them.
  - Added exports and logic for `extractEventMetaFromText`, `buildGoogleCalendarUrl`, `buildIcsContent`.
  - Implemented inline body-text rendering of marker labels as clickable date actions with `Google Kalender` and
    `ICS herunterladen`.
  - Implemented strict validation (`title`, `start`, `end`, `location`, `end > start`) and non-clickable fallback for
    invalid/missing event mappings.
  - Preserved existing header date rendering and existing escaping/newline behavior.
  - Verified RED/GREEN cycle with focused test run:
    - `npm --prefix src/main/webapp run test -- src/js/news.test.ts` (PASS)
- Step 3 done: Full verification passed.
  - `npm --prefix src/main/webapp run test` (PASS)
  - `npm --prefix src/main/webapp run type-check` (PASS)
  - `npm --prefix src/main/webapp run build` (PASS)
  - `./mvnw.cmd test` (BUILD SUCCESS)
- Step 4 started: Add admin tooltip guidance for inline event marker and hidden metadata format in Neuigkeiten section.
- Step 4 done (TDD RED/GREEN): Added failing test for tooltip authoring hint markup, then implemented tooltip UI and
  content in admin Neuigkeiten section.
  - Added `buildNewsAuthoringHintMarkup()` in `admin_news.ts`.
  - Wired tooltip rendering in `initAdminNewsActions()` and added `#newsAuthoringHint` container in
    `admin_main.html`.
  - Added/updated test in `admin_news.test.ts`.
  - Verified RED/GREEN cycle with focused test run:
    - `npm --prefix src/main/webapp run test -- src/js/admin_news.test.ts` (PASS)
- Step 5 done: Re-verified full pipeline after tooltip addition.
  - `npm --prefix src/main/webapp run test` (PASS)
  - `npm --prefix src/main/webapp run type-check` (PASS)
  - `npm --prefix src/main/webapp run build` (PASS)
  - `./mvnw.cmd test` (BUILD SUCCESS)
- Step 6 started: Change event description source to optional metadata text (`text`) only.
  - Requirement: if `events.<id>.text` is present/non-empty, use it in Google/ICS description.
  - If `events.<id>.text` is missing/empty, do not send any description text.
- Step 6 done (TDD RED/GREEN): Added failing tests for optional metadata `text` behavior, then implemented minimal
  changes in calendar generation.
  - `news.ts`: added optional `text` on event metadata and removed fallback to visible news text for calendar
    descriptions.
  - Google URL now includes `details` only when metadata `text` is present.
  - ICS now includes `DESCRIPTION` only when metadata `text` is present.
  - `admin_news.ts` tooltip example updated with optional `text` and explanation.
  - Focused tests passed:
    - `npm --prefix src/main/webapp run test -- src/js/news.test.ts src/js/admin_news.test.ts` (PASS)
- Step 7 done: Full verification passed after optional metadata `text` change.
  - `npm --prefix src/main/webapp run test` (PASS)
  - `npm --prefix src/main/webapp run type-check` (PASS)
  - `npm --prefix src/main/webapp run build` (PASS)
  - `./mvnw.cmd test` (BUILD SUCCESS)

### Phase 103: Neuigkeiten Inline Multi-Event Calendar Actions ✅ COMPLETE

- Added support for hidden event metadata in news text via `<!--event {"events": {...}}-->`.
- Added support for multiple inline markers in body text via `[event-date:<id>]Label[/event-date]`, where only the label
  is visible and clickable.
- Added inline date action UI (`Google Kalender`, `ICS herunterladen`) in news body text without DB/API schema changes.
- Enforced strict event validation (`title`, `start`, `end`, `location`, `end > start`), with safe plain-text fallback
  when metadata is missing/invalid or IDs do not match.
- Interpreted offset-less `start`/`end` values as Europe/Vienna local time for Google Calendar (`ctz`) and ICS
  (`DTSTART;TZID=Europe/Vienna`, `DTEND;TZID=Europe/Vienna`).
- Added/updated frontend unit tests in `news.test.ts` for parsing, mapping, required-title behavior, and calendar
  payload generation.

### Phase 102: Frontend Auth-Expiry Rollout (In Progress)

- Step 1 started: Inventory and migration plan for remaining JSON-parse risk areas.
  - Prioritized admin modules with direct `fetch` + `json()`/text parsing paths:
    `admin_news.ts`, `admin_results.ts`, `admin_logs.ts`, `admin_login_stats.ts`, `register_user.ts`,
    and selected `create_quiz.ts` save/document requests.
  - Plan: reuse shared auth-session helper and suppress duplicate error UI when redirect is already scheduled.
- Step 2 done: Migrated high-risk admin/frontend fetch paths to shared auth-expiry handling.
  - Updated modules:
    - `admin_functions.ts`
    - `admin_news.ts`
    - `admin_results.ts`
    - `admin_logs.ts`
    - `admin_login_stats.ts`
    - `register_user.ts`
    - `create_quiz.ts`
    - `create_result.ts` (`fetchJson` load path hardened)
  - Added auth-expired short-circuit handling to avoid duplicate error UI when redirect is already scheduled.
- Step 3 done: Updated frontend tests for mocked fetch response cloning.
  - `admin_news.test.ts` mock response now includes `clone()` support to match real `Response` behavior used by
    auth-session checks.
- Step 4 done: Verification passed.
  - `npm --prefix src/main/webapp run test` (PASS)
  - `npm --prefix src/main/webapp run type-check` (PASS)
  - `npm --prefix src/main/webapp run build` (PASS)
  - `./mvnw.cmd test` (BUILD SUCCESS)

### Phase 102: Frontend Auth-Expiry Rollout ✅ COMPLETE

- Rolled out centralized auth-expiry detection/redirect handling across remaining high-risk admin fetch modules.
- Reduced chance of HTML/JSON parse mismatch errors in admin operations beyond create-result flow.
- Preserved existing German user messaging and existing relogin/security behavior.
- Ensured full backend+frontend verification pipeline is green after rollout.

### Phase 101: Session Expiry UX + API/Auth Response Consistency (In Progress)

- Step 1 done: Root-cause investigation completed for HTML/JSON mix-up risk.
  - Confirmed current behavior can redirect unauthenticated API calls to `/login` HTML (`SecurityAccessTest`).
  - Confirmed current logging covers 403 deny path but not unauthenticated entry-point decisions.
  - Confirmed save flow risk area in `create_result.ts` for invalid-session transitions.
- Step 2 done: Agreed product direction.
  - Session timeout policy: `4h` idle.
  - UX on expired save: inline German message, then delayed redirect to `/login`.
  - Keep Spring default login page (no custom login page now).
- Step 3 done (TDD RED/GREEN): Added failing security tests for unauthenticated JSON/API requests, then implemented
  centralized unauthenticated API JSON handling with logging.
  - Added `LoggingAuthenticationEntryPoint` and wired API-only matcher-based entry point in `SecurityConfig`.
  - Preserved browser-page redirect behavior while API-like requests now return JSON `401`.
  - Added/updated tests:
    - `SecurityAccessTest`
    - `LoggingAuthenticationEntryPointTest`
    - user controller unauthenticated expectations for `/api/**` now assert `401`.
- Step 4 done (TDD RED/GREEN): Added shared frontend auth-expiry helper and wired create-result save flow.
  - Added `auth-session.ts` + `auth-session.test.ts`.
  - `create_result.ts` now detects auth-expired responses, shows inline German message, and schedules delayed redirect.
- Step 5 done: Set session idle timeout to 4h via `server.servlet.session.timeout=4h`.
- Step 6 done: Full verification passed.
  - `./mvnw.cmd test` (BUILD SUCCESS)
  - `npm --prefix src/main/webapp run type-check` (PASS)
  - `npm --prefix src/main/webapp run build` (PASS)

### Phase 101: Session Expiry UX + API/Auth Response Consistency ✅ COMPLETE

- Implemented 4h idle session timeout in application config.
- Introduced centralized `LoggingAuthenticationEntryPoint` for API-style unauthenticated requests:
  - API-like requests (`/api/**`, `Accept: application/json`, `X-Requested-With: XMLHttpRequest`) return JSON `401`.
  - Browser page requests keep redirect-to-login behavior.
- Added explicit unauthenticated logging for entry-point decisions (`mode=json|redirect`, method/path/user).
- Added frontend shared helper for auth-expired handling with inline German message + delayed redirect.
- Wired create-result save flow to use centralized auth-expiry detection, preventing HTML/JSON parsing mix-up failures.
- Added/updated backend integration tests and frontend unit tests for the new behavior.

## Finished Phases

### Phase 99: Authenticated `/login` Redirect Guard ✅ COMPLETE

- Added authenticated `GET /login` redirect guard to `/`.
- Verification passed: targeted security test + frontend type-check/build + full `./mvnw.cmd test`.

### Phase 98: Reliable "Neu Anmelden" Fresh-Start Flow ✅ COMPLETE

- Implemented logout retry with forced CSRF refresh and always-continue relogin redirect.
- Verification passed: focused frontend/backend tests + full pipeline.
