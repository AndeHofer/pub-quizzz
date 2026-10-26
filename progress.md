# Progress

## Open Tasks

None.

## Finished Phases

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

### Phase 135: Leaderboard Table 20px Gap Fix ✅ COMPLETE

- Scoped `mt-0` override for leaderboard tables to neutralize global table top margin.
- Added regression coverage in `leaderboard-pages-markup.test.ts`.
- Full verification passed (`npm run test`, `npm run type-check`, `npm run build`, `./mvnw.cmd verify`).

Older finished phases are tracked in `progress_archive.md`.
