## Context

See `proposal.md`. Product design: `docs/superpowers/specs/2026-08-16-quick-h5-crud-view-delete-design.md`.

Many H5 lists already support edit; delete is incomplete for user/role/oauth/job; view/detail is sparse except some monitor logs.

## Goals / Non-Goals

**Goals:**

- Consistent single-row delete with confirm and remove permission for five modules
- Read-only view via `mode=view` on existing forms; fill gaps with lightweight read-only pages
- Keep existing monitor `detail.vue` routes

**Non-Goals:**

- Multi-select batch delete, recycle bin, Cron editors, new backend permission codes, rewriting all details into mode=view

## Decisions

### 1. View via form query `mode=view`

- **Choice:** Reuse form pages; disable controls and hide save when `mode=view`
- **Why:** Matches approved product choice; less duplication than new detail pages for every CRUD
- **Alternative:** Dedicated detail.vue everywhere — rejected for CRUD modules

### 2. Keep existing detail.vue for long-text logs

- **Choice:** operlog / jobLog / slowSql unchanged
- **Why:** Already adequate; avoids dual maintenance this phase
- **Alternative:** Migrate to mode=view — deferred

### 3. Delete uses existing POST/GET remove APIs

- **Choice:** Add H5 api wrappers + list actions; no PUT/DELETE verbs beyond project norms (existing remove endpoints)
- **Why:** Backend already exposes remove for these domains
- **Alternative:** Soft-delete-only UI without API — impossible

### 4. View button permission OR of query/list

- **Choice:** `hasPermi(['mod:query','mod:list'])`
- **Why:** Aligns with PC-ish access; users who can list can open read-only detail
- **Alternative:** Require edit — rejected

## Risks / Trade-offs

- [Missed disabled control on a form] → Central `isView` flag on all interactive controls; smoke checklist per page
- [Job delete side effects] → Rely on backend validation; confirm copy warns
- [File/online/login read-only pages thin] → Show key fields only; actions stay on list where already exist

## Migration Plan

1. Ship API wrappers + delete actions first (safe alone)
2. Add mode=view + view links
3. Add missing read-only pages + pages.json
4. Rollback: revert H5 pages; no schema change

## Open Questions

- Exact remove URL shapes per module — verify against existing controllers at apply time (GET path vs POST body)
