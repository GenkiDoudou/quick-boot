## 1. API wrappers

- [x] 1.1 Add remove helpers for user / role / oauthClient if missing (align existing backend remove endpoints)
- [x] 1.2 Add remove helper for monitor job if missing
- [x] 1.3 Confirm config remove API already used by H5 remains correct

## 2. List delete actions

- [x] 2.1 User list: delete with confirm; block userId=1; `system:user:remove`
- [x] 2.2 Role list: delete with confirm; block roleId=1; `system:role:remove`
- [x] 2.3 Config list: verify confirm + builtin block + `system:config:remove`
- [x] 2.4 OAuth client list: delete with confirm; `system:oauthClient:remove`
- [x] 2.5 Job list: delete with confirm; `monitor:job:remove`

## 3. Form mode=view + list view links

- [x] 3.1 Add `mode=view` (isView) to user/role/dept/config/dict type/data/oauthClient/fileClassify forms
- [x] 3.2 Add list「查看」on those modules gated by query|list OR; navigate with mode=view
- [x] 3.3 Leave operlog / jobLog / slowSql detail routes as-is

## 4. Missing read-only pages

- [x] 4.1 Job read-only form/detail + list view link; no Cron editor
- [x] 4.2 Login log read-only page + list view link
- [x] 4.3 Online user read-only page + list view link (force logout stays on list)
- [x] 4.4 File metadata read-only page + list view link; register pages in `pages.json`

## 5. Verify

- [x] 5.1 Smoke delete rules and view-only forms (no save)
- [x] 5.2 Confirm every system/monitor list has a view entry
- [x] 5.3 Mark OpenSpec tasks complete after smoke
