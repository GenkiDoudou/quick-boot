## Context

See `proposal.md` for motivation. Product design: `docs/superpowers/specs/2026-08-16-quick-h5-ops-suite-design.md`.

H5 already has authenticated HTTP, `hasPermi`, workbench via `h5Workbench`, and CRUD patterns from user/role/dept. PC APIs for config/dict/oauthClient/file/fileClassify and monitor modules already exist; this change is primarily client pages + menu seed.

## Goals / Non-Goals

**Goals:**

- Ship two capability batches with shared UX conventions and no new BFF
- Adapt mixed pagination shapes (`POST .../page` vs `GET .../list` with pageNum/pageSize) behind H5 API modules
- Seed H5 menu entries under workbench without inventing new perms strings

**Non-Goals:**

- Import/export, Cron editors, full PC field parity
- New backend controllers for H5
- Menu `client_type` schema

## Decisions

### 1. Two OpenSpec capabilities, one change, two implementation batches

- **Choice:** `quick-h5-ops-system` then `quick-h5-ops-monitor`; single change `quick-h5-ops-suite`
- **Why:** Matches approved delivery; one PR/change history, clear smoke gates
- **Alternative:** Two changes — rejected (shared conventions would duplicate)

### 2. No H5 BFF; mirror quick-ui API paths

- **Choice:** H5 `api` modules call the same URLs as PC (`/sys/*`, `/system/*`, `/monitor/*`)
- **Why:** Avoids duplicate contracts; backend authz already in place
- **Alternative:** Aggregate “ops” endpoints — rejected as out of scope

### 3. Pagination adapter at API layer

- **Choice:** Each module returns a normalized `{ rows, total }` (or compatible with `usePagedList`); GET-list modules wrap query params
- **Why:** Pages stay consistent with existing system pages
- **Alternative:** Fork list components per protocol — rejected

### 4. Job: operate-only on mobile

- **Choice:** List + changeStatus + run + deep-link to jobLog; create/edit Cron on PC only
- **Why:** Cron/misconfig risk on small screens; approved decision
- **Alternative:** Full job form — rejected for phase one

### 5. File upload requires classify

- **Choice:** Upload flow MUST select a classify from enabled classifies before calling `POST /system/file/upload/{classify}`
- **Why:** Backend requires classify; aligns with PC
- **Alternative:** Default classify hardcode — rejected

### 6. OAuth secret masking

- **Choice:** Detail shows masked secret by default; explicit reveal (and optional copy) after load
- **Why:** Reduce shoulder-surfing / screenshot leakage on mobile
- **Alternative:** Always show like PC — rejected for H5

### 7. Menu seed layout

- **Choice:** Under workbench root: extend system group + add monitor `M` with C children; `path` starts with `/pages/`; F nodes reuse PC perms; grant `role_id=1`
- **Why:** Same convention as menu-perm design
- **Alternative:** Flat tiles only — rejected (grouping matches workbench UX)

## Risks / Trade-offs

- [Pagination mismatch bugs] → Per-module smoke with empty/one-page/multi-page data
- [File choose API differs H5 vs MP] → Implement and verify H5 first; document MP follow-up
- [Secret leakage] → Mask + reveal; avoid logging response bodies
- [Large surface area] → Gate merge on batch 1 smoke before batch 2

## Migration Plan

1. Deploy Flyway menu seed (inert until H5 pages exist)
2. Ship batch 1 H5 pages + pages.json; restart `pnpm dev:h5` for local
3. Smoke system modules; then ship batch 2
4. Rollback: remove/disable H5 menu rows or revert H5 build; seed rows do not affect PC routers if `/pages/` filtered

## Open Questions

- Exact next Flyway version number — pick at apply time from latest `V*` in `quickboot-app`
