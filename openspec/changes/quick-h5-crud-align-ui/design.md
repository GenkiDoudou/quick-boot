## Context

See `proposal.md`. Product design: `docs/superpowers/specs/2026-08-16-quick-h5-crud-align-ui-design.md`.

H5 already has CRUD shells, `QbJsonCardFields`, and ops-suite pages. Alignment is practical field/search/validation parity with quick-ui, not a full C7 port.

## Goals / Non-Goals

**Goals:**

- Shared validation + filterable paging
- System and monitor pages: richer search/list/form where APIs allow
- One delivery batch covering all in-scope pages

**Non-Goals:**

- Import/export, Cron editors, QbJsonForm engine, undeployed modules (menu/RUM/logHub)

## Decisions

### 1. Filters via usePagedList + page-level chips

- **Choice:** Extend `usePagedList` with optional reactive filters merged into fetcher; pages add status chips below search
- **Why:** Minimal change to QbSearchBar; matches “keyword + 1–2 filters”
- **Alternative:** Full searchColumns JSON bar — rejected for phase one

### 2. validate.ts without schema library

- **Choice:** Small helpers + toast on fail
- **Why:** Matches approved decision; low dependency
- **Alternative:** async-validator / zod — rejected

### 3. List meta via QbJsonCardFields where beneficial

- **Choice:** Prefer columns config on CRUD and log lists
- **Why:** Consistency with user-list sample
- **Alternative:** Hand markup only — rejected

### 4. Degrade filters when API lacks param

- **Choice:** Ship keyword-only if backend cannot filter; document in page comment
- **Why:** Avoid fake UI
- **Alternative:** New backend endpoints — out of scope

### 5. Deliver all modules in one apply

- **Choice:** Shared first, then system, then monitor, smoke last
- **Why:** User chose one-shot delivery
- **Alternative:** Phased PRs — deferred

## Risks / Trade-offs

- [Large diff / regression] → Module checklist smoke; keep permission rules untouched
- [Card overcrowding] → stack + showIfProp; omit tertiary fields
- [Phone desensitization from API] → Display as returned; validation on form input only

## Migration Plan

1. Land shared utils (safe alone)
2. Update pages module-by-module
3. Rollback: revert page files; shared utils can remain unused

## Open Questions

- Exact filter support per list API — verify at apply time against controllers; degrade if missing
