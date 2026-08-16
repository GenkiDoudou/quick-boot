## 1. Backend persistence

- [x] 1.1 Add Flyway migration for `sys_user_h5_home_shortcut` (user_id, menu_id, order_num, unique pair, timestamps)
- [x] 1.2 Add Entity + Mapper for the preference table with Chinese comments
- [x] 1.3 Define ordered default `menu_id` constant from current Flyway H5 C menus (prefer user/dept/role/online)

## 2. Backend APIs

- [x] 2.1 Extract or reuse helper to list authorized H5 page menus (same rules as `buildH5Workbench` leaves)
- [x] 2.2 Implement resolve final shortcuts (preference ∩ candidates, else defaults ∩ candidates, max 8)
- [x] 2.3 Add GET `/system/menu/h5HomeShortcuts` and GET `/system/menu/h5HomeShortcutCandidates`
- [x] 2.4 Add POST `/system/menu/h5HomeShortcuts/save` (validate ≤8 and ⊆ candidates; empty array clears rows); no PUT/DELETE

## 3. H5 client

- [x] 3.1 Extend `api/system/menu.ts` with fetch shortcuts / candidates / save helpers
- [x] 3.2 Update `pages/home/home.vue`: load real shortcuts onShow, navigate, edit entry; keep message/todo mock; no mock shortcut fallback
- [x] 3.3 Add `pages/home/shortcuts.vue` (select/order ≤8, save, restore default) and register in `pages.json`

## 4. Verify

- [x] 4.1 Smoke: new user sees defaults; save persists; empty save restores defaults; revoked menu disappears
- [x] 4.2 Confirm only GET/POST used; message/todo still mock shells
- [x] 4.3 Mark OpenSpec tasks complete after smoke
