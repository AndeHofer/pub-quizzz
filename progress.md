# Progress

## Open Tasks

### Remove Misleading Remember-Me Checkbox

- [x] Remove the checkbox from the generated login page without changing automatic user or admin authentication
  behavior.
- [x] Add an integration test for login credentials, CSRF, and the absence of the checkbox; retain existing remember-me
  tests.
- [x] Run full Maven verification: 365 backend tests, 120 frontend tests, and frontend type checking passed.

### Restore Successful Login Logging

- [x] Restore the original successful-authentication log message without restoring statistics persistence.
- [x] Add unit and integration coverage for password and automatic login logging.
- [x] Run full Maven verification: 364 backend tests, 120 frontend tests, and frontend type checking passed.

### Remove Login Statistics

- [x] Remove the admin statistics UI, navigation, frontend types, and page-specific tests.
- [x] Remove the statistics API, successful-login database recording, and obsolete persistence code and tests.
- [x] Drop the legacy statistics table on startup and after old-backup restores, including failed restores.
- [x] Exclude the legacy statistics table from new backups.
- [x] Add migration, legacy-restore, API removal, authentication, and failed-login logging coverage.
- [x] Run full Maven verification: 362 backend tests, 120 frontend tests, and frontend type checking passed.
- [x] Remove stale generated login-statistics assets and verify the packaged JAR contains no obsolete statistics files.
- Added JAR exclusions for old incremental-build copies and removed the two identified generated files from
  `target/classes`.
- Added an authenticated integration test confirming the old statistics page is unavailable.
- Blockers: None.

### User-Only Remember-Me Login

- [x] Implement user-only signed cookies, session-only admin access, key validation, and local secret protection.
- [x] Cover renewal, security, and a full process restart; Maven verification passed (365 backend, 123 frontend tests).
- [ ] Verify the Docker build and Compose configuration; Docker is unavailable here.
- Note: Deployment requires a stable random `REMEMBER_ME_KEY` and HTTPS.

### Docker Build Verification

- [ ] Run `docker build --tag pub-quizzz:toolchain-update .` in an environment with Docker installed. Blocked here:
  Docker is not installed or available on `PATH`.

## Finished Phases

### Remove Misleading Remember-Me Checkbox ✅ COMPLETE

- Removed the generated checkbox without changing automatic user or admin login behavior.
- Full Maven verification passed with 365 backend tests, 120 frontend tests, and frontend type checking.

### Restore Successful Login Logging ✅ COMPLETE

- Restored the original INFO message for password and remember-me authentication without database recording.
- Full Maven verification passed with 364 backend tests, 120 frontend tests, and frontend type checking.

### Remove Login Statistics ✅ COMPLETE

- Removed the page, successful-login database recording, API, and legacy database table, including after old-backup
  restores.
- Full Maven verification passed with 362 backend tests, 120 frontend tests, and frontend type checking.

Older finished phases are tracked in `progress_archive.md`.
