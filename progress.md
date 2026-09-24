# Progress

## Open Tasks

### Table Hover Contrast Correction

- [x] Added a static CSS assertion requiring the global row-hover selector to be restricted to hover-capable devices.
- [x] Scoped the existing global `tr:hover` rule to `@media (hover: hover)` without changing theme tokens, result
  markup,
  toggle behavior, or intentional detail-row backgrounds.
- [x] Focused regression verification passed: `npm run test -- src/css/theme-imports.test.ts` (1 file / 16 tests).
- [x] Full verification passed:
  - `npm run test` (23 files / 115 tests)
  - `npm run type-check` (no diagnostics)
  - `npm run build` (Vite 8.3.0, 115 modules)
  - `.\mvnw.cmd verify` (`BUILD SUCCESS` in 32.303 s, 321 backend tests, embedded frontend 23 files / 115 tests)
- [x] Added a static CSS assertion requiring desktop table hover feedback to use the theme interaction background.
- [x] Changed global desktop table hover feedback from `bg-gray-50` to `--pq-highlight-bg`.
- [x] Focused regression verification passed: `npm run test -- src/css/theme-imports.test.ts` (1 file / 17 tests).
- [x] Full verification passed:
  - `npm run test` (23 files / 116 tests)
  - `npm run type-check` (no diagnostics)
  - `npm run build` (Vite 8.3.0, 115 modules)
  - `.\mvnw.cmd verify` (`BUILD SUCCESS` in 32.530 s, 321 backend tests, embedded frontend 23 files / 116 tests)
- [x] Audited all public hover states after the Christmas homepage report: only `hover:bg-gray-50`, `hover:bg-gray-100`,
  and `hover:bg-gray-200` bypass the theme background mapping; text-only, shadow-only, dark-button, and admin-only
  hover states are not equivalent foreground/background defects.
- [x] Added focused static coverage for themed neutral hover utility mapping.
- [x] Mapped those three public neutral hover utilities to `--pq-highlight-bg` through the shared theme token layer.
- [x] Focused regression verification passed: `npm run test -- src/css/theme-imports.test.ts` (1 file / 18 tests).
- [x] Full verification passed:
  - `npm run test` (23 files / 117 tests)
  - `npm run type-check` (no diagnostics)
  - `npm run build` (Vite 8.3.0, 115 modules)
  - `.\mvnw.cmd verify` (`BUILD SUCCESS` in 36.237 s, 321 backend tests, embedded frontend 23 files / 117 tests)
- [x] Added a failing markup test for contrast-safe hover feedback on the four remaining homepage navigation cards.
- [x] Added the shared neutral hover utility to the `Quiz Ergebnisse`, `Quiz ansehen`, `Spielregeln`, and
  `Admin-Bereich` cards.
- [x] Focused regression verification passed: `npm run test -- src/js/homepage-card-hover.test.ts` (1 file / 1 test).
- [x] Full verification passed:
  - `npm run test` (24 files / 118 tests)
  - `npm run type-check` (no diagnostics)
  - `npm run build` (Vite 8.3.0, 115 modules)
  - `.\mvnw.cmd verify` (`BUILD SUCCESS` in 37.796 s, 321 backend tests, embedded frontend 24 files / 118 tests)
- Blockers: none.

### Public Theme Contrast Audit

- [x] Traced the Christmas rules-page defect to unmapped `bg-yellow-50` and `bg-green-50` callout backgrounds
  combined with tokenized light foreground text.
- [x] Audited the public UI and confirmed the same token-boundary defect in quiz-detail answer/note callouts,
  dark-surface `text-black` controls, blue links, modal surfaces, and dark-theme form controls.
- [x] Added focused CSS regression coverage for generic link, callout, modal, and form-control token mappings; the
  new assertions failed as expected before the missing contrast tokens existed.
- [x] Added minimal shared mappings for links, callouts, modals, controls, and `text-black`; supplied dark-theme token
  values and moved Halloween's duplicated field rule into the shared contract. Darkened Christmas, Halloween, and New
  Year primary buttons to preserve white-label contrast.
- [x] Focused regression verification passed: `npm run test -- src/css/theme-imports.test.ts` (1 file / 12 tests).
- [x] Full verification passed:
  - `npm run test` (22 files / 109 tests)
  - `npm run type-check` (no diagnostics)
  - `npm run build` (Vite 8.3.0, 115 modules)
  - `./mvnw.cmd verify` (`BUILD SUCCESS` in 32.428 s, 321 backend tests, embedded frontend 22 files / 109 tests)
- Blockers: none.
- Blockers: none.

### Theme Motif Visibility And Spacing

- [x] Audited every non-standard theme: Jänner is the clearest low-contrast motif case; Dezember, März, April, and
  Juni also need stronger decoration variables on pale surfaces.
- [x] Confirmed every theme repeats at least one motif within a corner cluster; desktop body and container secondary
  clusters also share the lower-right area.
- [x] Added CSS structure tests for unique motifs per cluster, separated desktop secondary decoration, and the
  strengthened Jänner variables; the assertions failed as expected before the changes existed.
- [x] Revised all non-standard theme motif strings, repositioned the shared desktop body secondary cluster to the
  lower left, and calibrated decoration colors/opacities for visibility on pale surfaces.
- [x] Focused regression verification passed: `npm run test -- src/css/theme-imports.test.ts` (1 file / 15 tests).
- [x] Full verification passed:
  - `npm run test` (22 files / 112 tests)
  - `npm run type-check` (no diagnostics)
  - `npm run build` (Vite 8.3.0, 115 modules)
  - `./mvnw.cmd verify` (`BUILD SUCCESS` in 37.540 s, 321 backend tests, embedded frontend 22 files / 112 tests)
- Blockers: none.

### Random Public Motif Layouts

- [x] Approved scope: select one of four safe cluster layouts on each public page load, without storage, API, backend,
  motif-string randomization, or changes to admin pages.
- [x] Added a focused initializer test for the generated `data-motif-layout` attribute; it failed as expected before
  layout selection existed.
- [x] Set the random layout attribute in the public initializer and added four fixed CSS-only layout variants.
- [x] Focused regression verification passed: `npm run test -- src/js/public_theme_init.test.ts` (1 file / 4 tests).
- [x] Full verification passed:
  - `npm run test` (22 files / 113 tests)
  - `npm run type-check` (no diagnostics)
  - `npm run build` (Vite 8.3.0, 115 modules)
  - `./mvnw.cmd verify` (`BUILD SUCCESS` in 32.638 s, 321 backend tests, embedded frontend 22 files / 113 tests)
- Blockers: none.

### Team Result Detail Simplification

- [x] Confirmed the expanded team-result table renders a dedicated header row solely for the blue question-number
  badges; the point cells already convey the requested data without it.
- [x] Added focused dependency-free rendering coverage for a detail row containing only point values; a DOM-based test
  was intentionally not introduced because this frontend test setup has no `jsdom` dependency.
- [x] Removed the team-only question-number header and unused badge import.
- [x] Focused regression verification passed: `npm run test -- src/js/team-result-rendering.test.ts` (1 file / 1 test).
- [x] Full verification passed:
  - `npm run test` (23 files / 114 tests)
  - `npm run type-check` (no diagnostics)
  - `npm run build` (Vite 8.3.0, 115 modules)
  - `./mvnw.cmd verify` (`BUILD SUCCESS` in 35.464 s, 321 backend tests, embedded frontend 23 files / 114 tests)
- Blockers: none.

### German Umlaut Display Cleanup

- [x] Scope approved: replace German UI-text transliterations such as `ae`, `oe`, and `ue` with literal umlauts;
  retain internal identifiers, URLs, API values, existing HTML entities, and historical records.
- [x] Added regression coverage and replaced active preview/log UI transliterations with literal umlauts.
- [x] Verification passed:
    - `npm run test` (22 files / 107 tests)
    - `npm run type-check` (no diagnostics)
    - `npm run build` (Vite 8.3.0, 115 modules)
    - `./mvnw.cmd verify` (`BUILD SUCCESS` in 36.435 s, 321 backend tests, embedded frontend 22 files / 107 tests)
- Blockers: none.

### Legacy December Identifier Cleanup

- [x] Scope approved: retain `december` as the sole internal identifier and `Dezember` as German UI text; remove only
  the obsolete `dezember` compatibility behavior and its active tests.
- [x] Confirmed the active source already has no `dezember` normalization branch or legacy test cases; no production
  source change was required.
- [x] Verification passed:
    - `npm run test` (22 files / 107 tests)
    - `npm run type-check` (no diagnostics)
    - `npm run build` (Vite 8.3.0, 115 modules)
    - `./mvnw.cmd verify` (`BUILD SUCCESS` in 32.884 s, 321 backend tests, embedded frontend 22 files / 107 tests)
- Blockers: none.

### Theme Preview Table Simplification

- [x] Scope approved: remove the visible description column from the admin theme preview table while retaining the
  theme registry descriptions as internal metadata.
- [x] Added regression coverage and removed the description column from dynamic rows and static markup.
- [x] Verification passed:
    - `npm run test` (22 files / 108 tests)
    - `npm run type-check` (no diagnostics)
    - `npm run build` (Vite 8.3.0, 115 modules)
    - `./mvnw.cmd verify` (`BUILD SUCCESS` in 36.676 s, 321 backend tests, embedded frontend 22 files / 108 tests)
- Blockers: none.

### Theme Motif Cluster Variation

- [x] Approved visual direction: replace repeated full-surface motif fields with theme-specific primary upper-right
  and secondary lower-left clusters, leaving the content center quieter.
- [x] Approved responsive behavior: desktop retains complementary exterior body clusters; mobile keeps only a compact
  primary container cluster.
- [x] Added CSS structure regression coverage for primary/secondary cluster variables, distinct theme motif mixes, and
  mobile primary-only behavior.
- [x] Replaced the shared motif mechanism and migrated all month/event themes to distinct primary/secondary corner
  compositions; desktop uses complementary exterior clusters and the central content area stays quiet.
- [x] Verification passed:
    - `npm run test` (22 files / 107 tests)
    - `npm run type-check` (no diagnostics)
    - `npm run build` (Vite 8.3.0, 115 modules)
    - `./mvnw.cmd verify` (`BUILD SUCCESS` in 32.750 s, 321 backend tests, embedded frontend 22 files / 107 tests)
- Blockers: none.

### Calendar Theme Activation

- [x] Approved design: public pages resolve themes in this priority order: tab-local admin preview override, active
  event,
  Vienna calendar month, then `standard` fallback.
- [x] Approved timezone policy: resolve the application calendar day in `Europe/Vienna`; pure resolver tests inject
  date-only input and timezone conversion tests inject instants.
- [x] Approved event rules: Easter (Good Friday through Easter Monday), Halloween (24-31 October), Christmas (24-26
  December), and New Year (31 December-1 January), all inclusive.
- [x] Added TDD coverage and implemented a pure Gregorian Easter/calendar resolver with an ordered typed event registry.
- [x] Integrated public theme resolution: tab-local preview override, active event, Vienna month, then `standard`
  fallback;
  admin pages remain neutral because they do not load the public initializer.
- [x] Updated preview messaging so `Standard` clears the override and resumes automatic selection.
- [x] Verification passed:
    - `npm run test` (22 files / 105 tests)
    - `npm run type-check` (no diagnostics)
    - `npm run build` (Vite 8.3.0, 115 modules)
    - `./mvnw.cmd verify` (`BUILD SUCCESS` in 37.177 s, 321 backend tests, embedded frontend 22 files / 105 tests)
- Blockers: none.

### Theme Preview Design

- [x] Explored the shared frontend style system, Vite multi-page setup, public pages, and neutral admin layout.
- [x] Finalized scope: preview themes apply only to public pages; all admin pages remain neutral.
- [x] Finalized selection model: all admins can choose `Standard`, `November`, `Dezember`, `Jänner`, `Halloween`.
- [x] Implemented tab-local persistence via `sessionStorage` only; no backend/API/session persistence changes.
- [x] Added dedicated admin preview page and admin navigation wiring.
- [x] Added public theme initializer and markup test coverage for public vs admin pages.
- [x] Added isolated CSS token + theme override layer for November, Dezember, Jänner, Halloween.
- [x] Split shared theme tokens and each theme override into dedicated CSS files while retaining the existing preview
  behavior. `styles.css` imports `themes/tokens.css` plus one CSS file per seasonal/event theme; added regression
  coverage in `css/theme-imports.test.ts` (verified RED then GREEN).
- [x] Renamed internal `jaenner` identifiers and CSS filename to English `january`, retained the user-facing German
  label `Jänner`, and verified RED then GREEN through dedicated theme/CSS tests.
- [x] Added approved shared Unicode-plus-CSS seasonal decoration layer to all non-standard public themes; admin pages
  remain neutral and decoration-free. `tokens.css` owns the responsive pseudo-element mechanism while each theme file
  supplies its motif and atmosphere variables. `theme-imports.test.ts` verified RED then GREEN.
- [x] Moved non-standard theme decoration from the body background to the public container background so it remains
  visible on mobile full-width containers. `theme-imports.test.ts` verified RED then GREEN; all direct container
  children retain a higher stacking level than the decoration.
- [x] Removed the mobile top body strip for non-standard themes, renamed internal December identifiers to English
  (`december`/`december.css`) with one-time legacy `dezember` preview normalization, and strengthened Halloween text,
  field, and placeholder contrast. Dedicated tests verified RED then GREEN.
- [x] Mapped dynamically rendered `text-gray-900` score values to the active theme primary text color, resolving dark
  Halloween score text on dark surfaces. Added regression coverage and verified RED then GREEN.
- [x] Added an equally strong desktop-only body decoration behind the existing container decoration; mobile continues
  to use only the container layer. The shared theme variables prevent per-theme duplication; regression coverage
  verified RED then GREEN.
- [x] Added all remaining month themes plus Easter, Christmas, and New Year to the admin-only preview catalogue.
  The selector now renders exclusively from the shared registry; automatic calendar activation remains disabled.
- [x] Verification passed:
    - `npm run test` (20 files / 77 tests)
    - `npm run type-check` (no diagnostics)
    - `npm run build` (Vite 8.3.0, 114 modules)
    - `./mvnw.cmd verify` (`BUILD SUCCESS` in 34.821 s, 321 backend tests, embedded frontend 20 files / 77 tests)
- Blockers: none.

## Finished Phases

### Phase 135: Leaderboard Table 20px Gap Fix ✅ COMPLETE

- Scoped `mt-0` override for leaderboard tables to neutralize global table top margin.
- Added regression coverage in `leaderboard-pages-markup.test.ts`.
- Full verification passed (`npm run test`, `npm run type-check`, `npm run build`, `./mvnw.cmd verify`).

### Phase 134: ResultService Leaderboard Split ✅ COMPLETE

- Extracted shared ranking and title formatting logic into `RankingUtils` and `QuizTitleFormatter`.
- Moved leaderboard responsibilities to `LeaderboardService` and rewired controller/tests.
- Full verification passed (`npm run test`, `npm run type-check`, `npm run build`, `./mvnw.cmd verify`).

### Phase 133: Leaderboard Tab Visibility Bugfix ✅ COMPLETE

- Added missing `leaderboardYearTabs` containers and hide rule for 0-1 years.
- Added focused frontend tests and retained full verification coverage.
- Full verification passed (`npm run test`, `npm run type-check`, `npm run build`, `./mvnw.cmd verify`).

Older finished phases are tracked in `progress_archive.md`.
