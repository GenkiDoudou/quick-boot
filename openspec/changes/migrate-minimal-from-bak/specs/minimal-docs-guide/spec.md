## ADDED Requirements

### Requirement: Docs site runs with Guide only
The repository root `docs/` VitePress site MUST start with `pnpm i` and `pnpm dev` and expose Guide documentation (introduction, installation, quick-start, and related guide pages). Navigation MUST focus on Guide and MUST NOT require backend/frontend deep module docs to be present.

#### Scenario: Dev server serves guide
- **WHEN** a developer starts the docs dev server
- **THEN** Guide pages are reachable from the site navigation

### Requirement: Non-baseline docs artifacts are excluded
The docs baseline MUST NOT include large SQL dump files at the docs root, oauth integration deep guides as required content, or deep trees such as backend/frontend/design/deploy/sdd/skill module documentation.

#### Scenario: Dump SQL absent
- **WHEN** a developer inspects the docs project root
- **THEN** `*-dump.sql` files are not present

### Requirement: Existing migration design specs are preserved
Existing files under `docs/superpowers/specs/` that document this migration (including `2026-07-25-minimal-migrate-from-bak-design.md`) MUST remain after copy/trim operations.

#### Scenario: Design spec survives docs trim
- **WHEN** docs are copied from bak and trimmed to Guide-only content
- **THEN** `docs/superpowers/specs/2026-07-25-minimal-migrate-from-bak-design.md` still exists
