# Progress

## Open Tasks

- [x] Phase 55: Remove unused `/api/is-admin` and `/api/version` endpoints
- [x] Phase 55: Keep bootstrap behavior and cache header unchanged
- [x] Phase 55: Update tests and run full verification

## Finished Phases

### Phase 55: Remove Deprecated User Endpoints (`/api/is-admin`, `/api/version`) ✅ COMPLETE

- Removed unused mapped endpoints `/api/is-admin` and `/api/version` from `UserController`; kept only
  `/api/bootstrap` with unchanged payload and cache behavior.
- Kept admin-role evaluation as an internal helper method without exposing a dedicated endpoint.
- Updated `UserControllerTest` to remove old endpoint tests and keep bootstrap-focused coverage.
- Verified there are no remaining source references to `/api/is-admin` or `/api/version`.
- Verification passed: `npm run type-check`, `npm run build`, `./mvnw.cmd -Dtest=UserControllerTest test`,
  `./mvnw.cmd test` (`BUILD SUCCESS`, 201 tests, 0 failures).

### Phase 54: Bootstrap Endpoint with 1-Hour HTTP Cache (No Browser Storage) ✅ COMPLETE

- Added `GET /api/bootstrap` in `UserController` returning both `isAdmin` and `version` via new DTO
  `BootstrapResponse`.
- Configured response caching with `Cache-Control: max-age=3600, must-revalidate, private` to use HTTP caching for one
  hour without `sessionStorage`.
- Reworked index bootstrap logic in `index.ts` to remove `sessionStorage` keys and fetch only `/api/bootstrap` for admin
  card visibility and version badge.
- Added controller tests for bootstrap endpoint payload, authentication behavior, and cache header.
- Verification passed: `npm run type-check`, `npm run build`, `./mvnw.cmd -Dtest=UserControllerTest test`,
  `./mvnw.cmd test` (`BUILD SUCCESS`, 205 tests, 0 failures).

### Phase 53: Tighten Unauthenticated Access to Login + Favicon Only ✅ COMPLETE

- Updated `SecurityConfig` to keep only `/favicon.ico` as `permitAll`; all other paths now require authentication unless
  separately constrained by role.
- Updated `SecurityAccessTest` to assert unauthenticated requests to `/assets/**`, `/static/**`, and `/uploads/**`
  redirect to `/login`.
- Kept existing checks that protected API/admin paths redirect unauthenticated users and remain forbidden for non-admin
  authenticated users.
- Verification passed: `./mvnw.cmd test` -> `BUILD SUCCESS` (202 tests, 0 failures, 0 errors, 0 skipped).
