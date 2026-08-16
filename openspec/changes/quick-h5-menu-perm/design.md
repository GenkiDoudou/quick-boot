## Context

See `proposal.md` for motivation. Approved product design: `docs/superpowers/specs/2026-08-16-quick-h5-menu-perm-design.md`.

Current H5 uses `mock/workbenchMenus.ts` and does not store `permissions` from `/auth/me`. Backend already computes role permissions and PC routers via `sys_menu` + role menus. Phase one reuses that model without a new menu table or admin “client type” field.

## Goals / Non-Goals

**Goals:**

- Persist session permissions on H5; gate list/form actions with the same `perms` strings as PC
- Serve role-filtered H5 workbench groups via a dedicated lightweight API
- Seed optional H5 directory/entry rows (`path` under `/pages/`) while reusing existing `F` permission nodes

**Non-Goals:**

- Admin UI fields for `client_type` / dual path (phase two)
- Configurable button labels/order
- Real-time permission push after role changes
- DOM-removing `v-hasPermi` directive as the primary mechanism on uni-app

## Decisions

### 1. Dedicated `GET /sys/menu/h5Workbench` instead of reusing `/getRouters`

- **Choice:** New endpoint returning `{ id, title, items[{ id, label, path, icon?, orderNum }] }[]`
- **Why:** `/getRouters` Map shape is Vue Router–oriented; workbench needs flat groups/tiles
- **Alternative:** Client-side transform of `/getRouters` — rejected (fragile, mixes PC component paths)

### 2. H5 node convention without schema migration (phase one)

- **Choice:** Treat `menu_type=C` with `path` starting with `/pages/` as H5 entries; `M` as group titles under the H5 branch; `F` never as tiles
- **Why:** Avoids DDL/admin form work now; role checkbox UX unchanged
- **Alternative:** New `client_type` column — deferred to phase two

### 3. Permission UX: `hasPermi` + `v-if`

- **Choice:** Utility/`usePermission` mirroring PC `*:*:*` OR semantics; bind with `v-if`
- **Why:** uni-app DOM remove directives are unreliable vs Vue web
- **Alternative:** Port `v-hasPermi` removeChild — rejected for phase one

### 4. Implementation order

1. Store + hasPermi + system page buttons (immediate value)
2. Backend h5Workbench + menu seed SQL
3. Workbench wiring
4. Cross-role smoke

### 5. Mock demotion

- Static mock MUST NOT be the default fallback on API failure (would leak fake entries)
- May remain as offline/dev-only switch, default off

## Risks / Trade-offs

- [PC/H5 same tree misconfigured path] → Strict `/pages/` filter on h5Workbench; document convention for operators
- [Missing F nodes hide all buttons] → Seed/checklist for `system:user|role|dept:*`; smoke tests
- [Token restored without me] → App launch MUST fetchMe when token present
- [Convention vs explicit client_type] → Accept temporary convention; phase two adds fields if needed

## Migration Plan

1. Deploy backend h5Workbench (safe if unused)
2. Ship H5 store/permissions/buttons (works with existing F perms even before H5 C nodes)
3. Insert H5 menu rows + role grants
4. Switch workbench to API
5. Rollback: revert H5 workbench to previous build; leave seed menus inert for PC if paths unused by getRouters

## Open Questions

- Exact Flyway version filename / whether to grant H5 menus to admin role only vs common role in seed — decide at apply time from existing seed patterns
