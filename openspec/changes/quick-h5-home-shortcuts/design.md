## Context

See `proposal.md`. Product design: `docs/superpowers/specs/2026-08-16-quick-h5-home-shortcuts-design.md`.

Workbench already exposes role-filtered H5 C menus via `GET /system/menu/h5Workbench`. Home still uses mock shortcuts.

## Goals / Non-Goals

**Goals:**

- Persist per-user home shortcut preference; resolve defaults server-side
- Expose GET list / GET candidates / POST save only
- Wire H5 home + settings page; keep message/todo mock shells

**Non-Goals:**

- Real message/todo APIs; PC admin for user shortcuts; PUT/DELETE; bloating `/auth/me`

## Decisions

### 1. Server-side aggregation

- **Choice:** `GET h5HomeShortcuts` returns the final grid after preference ∩ permission
- **Why:** Single source of truth; prevents unauthorized menu ids from client
- **Alternative:** Client merges workbench + preference — rejected

### 2. Candidate pool = workbench leaves

- **Choice:** Flatten the same C + `/pages/` + visible rules as `buildH5Workbench`
- **Why:** No second menu tree; authorization stays consistent
- **Alternative:** Separate「首页候选」menu root — deferred

### 3. Empty save restores default

- **Choice:** `POST .../save` with `menuIds: []` deletes preference rows
- **Why:** Clear UX for「恢复默认」without DELETE verb
- **Alternative:** Empty means empty grid — rejected per product design

### 4. Max 8 + ordered defaults constant

- **Choice:** Hard cap 8; default ordered `menu_id` list in backend constant (aligned to Flyway H5 ids at apply time)
- **Why:** Simple mobile grid; defaults versioned with code
- **Alternative:** Config table for defaults — overkill for v1

### 5. HTTP verbs

- **Choice:** GET + POST only (`.../save`)
- **Why:** Project convention — no PUT/DELETE
- **Alternative:** PUT overwrite — rejected by user

## Risks / Trade-offs

- [Default menu_id drift across envs] → Resolve ids from Flyway H5 menus during apply; document in constant comment
- [Duplicate filter logic vs workbench] → Extract shared「list H5 page menus for user」helper
- [Preference rows orphaned after menu delete] → Intersection on read drops them; save rewrites clean set

## Migration Plan

1. Flyway create table (no data migration)
2. Deploy API then H5 pages
3. Rollback: drop table / ignore endpoints; home can temporarily empty-grid if API removed

## Open Questions

- Exact default `menu_id` ordered list — finalize at apply against current Flyway H5 C nodes (user/dept/role/online preferred)
