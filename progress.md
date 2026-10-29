# Progress

## Open Tasks

### Previous Quiz Navigation

- [x] Add the right-side navigation link to the public quiz-results page.
- [x] Select the chronologically previous finished quiz and wrap from the oldest to the newest quiz.
- [x] Add unit and markup tests, then run the full CI verification suite.

### Preserve Leaderboard Year in Navigation

- [x] Inspect the failing frontend tests caused by the new next-leaderboard link.
- [x] Update affected test expectations and add coverage for retaining the selected year.
- [x] Run the complete CI verification suite, including frontend type checking.

### Docker Build Verification

- [ ] Run `docker build --tag pub-quizzz:toolchain-update .` in an environment with Docker installed. Blocked here:
  Docker is not installed or available on `PATH`.

## Finished Phases

### Remove Cache Metrics Logging ✅ COMPLETE

- Removed the scheduled cache-metrics collector, its unit tests, metric properties, and Caffeine statistics tracking.
- Removed scheduling because no other scheduled task exists.
- Retained Caffeine caching, cache annotations, cache invalidation, and cache invalidation logging.
- Full Maven verification passed with 328 backend tests, TypeScript check, and 104 frontend tests.

### Preserve Leaderboard Year in Navigation ✅ COMPLETE

- Deferred next-leaderboard link initialization until the page load event, preventing the frontend test environment from
  accessing an unavailable `document` object during module import.
- Preserved the selected year, including `Alle` (`?year=all`), after changing the filter and navigating to the next
  leaderboard.
- Added coverage for next-link markup and year preservation; full Maven verification passed with 328 backend tests,
  TypeScript checking, and 105 frontend tests.

### Previous Quiz Navigation ✅ COMPLETE

- Added a right-side link on the public quiz-results page showing the chronologically previous finished quiz and its
  name; navigation wraps from the oldest quiz to the newest.
- Kept the results page functional if navigation loading fails; the link remains hidden in that case.
- Added unit and markup coverage; full Maven verification passed with 328 backend tests, TypeScript checking, and 111
  frontend tests.

Older finished phases are tracked in `progress_archive.md`.
