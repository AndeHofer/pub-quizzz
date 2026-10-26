# Progress

## Open Tasks

None.

## Finished Phases

### Public Navigation White-Flicker Fix ✅ COMPLETE

- Added an immediate themed root background and synchronous pre-CSS bootstrap to all 10 public pages.
- Consolidated the duplicated bootstrap into one shared classic script referenced before CSS.
- Persisted the resolved theme and motif layout for stable same-tab navigation with restricted-storage fallbacks.
- Confirmed generated output retains the shared bootstrap before CSS; full verification passed with 22 frontend files /
  104
  tests and 333 backend tests.

### Compact Top 10 Quiz Titles ✅ COMPLETE

- Added compact `Mon. YY` mobile titles while retaining full titles from `sm` upward.
- Set the mobile Quiz column to 72px and allocated all released width to Team; desktop widths remain unchanged.
- Long custom mobile titles truncate safely while retaining the full tooltip.
- Preserved full accessible labels and tooltips; custom titles fall back unchanged.
- Full verification passed: production build, TypeScript check, 22 frontend files / 99 tests, and 333 backend tests.

### Public Page Mobile Padding ✅ COMPLETE

- Changed all 10 public outer containers to `px-1` on mobile while preserving `sm:px-4`.
- Left admin-page spacing unchanged.
- Full Maven verification passed: production build, TypeScript check, 22 frontend files / 97 tests, and 333 backend
  tests.

Older finished phases are tracked in `progress_archive.md`.
