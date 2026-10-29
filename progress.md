# Progress

## Open Tasks

### Docker Build Verification

- [ ] Run `docker build --tag pub-quizzz:toolchain-update .` in an environment with Docker installed. Blocked here:
  Docker is not installed or available on `PATH`.

## Finished Phases

### Toolchain Dependency Updates ✅ COMPLETE

- Upgraded the Maven-managed frontend Node.js version to 26.13.0 LTS and Node type definitions to 26.6.6.
- Updated Axios to 1.21.0, Maven Wrapper to 3.10.0, and the Maven Docker build image to 3.10.0.
- Full Maven verification passed with Java 25: 333 backend tests, TypeScript check, and 104 frontend tests.
- Docker build verification remains blocked because Docker is unavailable in this environment.

### Remove Leaderboard Tile Dividers ✅ COMPLETE

- Removed the three redundant desktop right-border utilities from the leaderboard tiles on the start page.
- No styling test was added, in accordance with the project rules.
- Full Maven verification passed with 333 backend tests, TypeScript check, and 104 frontend tests.

### Remove Cache Metrics Logging ✅ COMPLETE

- Removed the scheduled cache-metrics collector, its unit tests, metric properties, and Caffeine statistics tracking.
- Removed scheduling because no other scheduled task exists.
- Retained Caffeine caching, cache annotations, cache invalidation, and cache invalidation logging.
- Full Maven verification passed with 328 backend tests, TypeScript check, and 104 frontend tests.

Older finished phases are tracked in `progress_archive.md`.
