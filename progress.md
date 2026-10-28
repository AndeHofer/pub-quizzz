# Progress

## Open Tasks

None.

## Finished Phases

### Leaderboard Year Default And Tab Order ✅ COMPLETE

- Multi-year leaderboards now always default to the newest available year, regardless of the calendar year.
- Year tabs render newest-first with „Alle“ last; explicit all-years selections persist as `?year=all`.
- Applied the shared behavior to points, medals, average, and Top 10 leaderboards.
- Updated the existing test without adding tests; full verification passed with 22 frontend files / 104 tests and 333
  backend tests.

### Compact Quiz Overview Titles ✅ COMPLETE

- Reused the Top 10 compact mobile quiz-title formatter on the quiz overview.
- Reduced the mobile Quiz column to 72px so winner team names receive the released width; desktop widths remain
  unchanged.
- Preserved full desktop titles, accessible labels, and tooltips without adding tests.
- Full Maven verification passed: production build, TypeScript check, 22 frontend files / 104 tests, and 333 backend
  tests.

### Public Navigation White-Flicker Fix ✅ COMPLETE

- Added an immediate themed root background and synchronous pre-CSS bootstrap to all 10 public pages.
- Consolidated the duplicated bootstrap into one shared classic script referenced before CSS.
- Persisted the resolved theme and motif layout for stable same-tab navigation with restricted-storage fallbacks.
- Confirmed generated output retains the shared bootstrap before CSS; full verification passed with 22 frontend files /
  104
  tests and 333 backend tests.

Older finished phases are tracked in `progress_archive.md`.
