## 1. Shared infra

- [x] 1.1 Add `quick-h5/src/utils/validate.ts` with `required` / `mobile` / `email` / `assert` (toast on fail)
- [x] 1.2 Extend `usePagedList` to accept optional reactive filters merged into fetch params; filter change resets to page 1
- [x] 1.3 Ensure list pages can place status (or equivalent) chips under search without breaking `QbSearchBar` (page-level or optional slot; minimal API change)

## 2. System — user / role / dept

- [x] 2.1 User list: status filter + `QbJsonCardFields` (dept/roles/phone/email/sex when present); keep `#status` slot
- [x] 2.2 User form: add email/sex/remark; validate account/nickname/dept/roles required and mobile/email format when filled; always submit `deptId`/`roleIds` on edit
- [x] 2.3 Role list: name keyword + status filter; card columns for roleKey/sort/remark; form validates name + roleKey
- [x] 2.4 Dept list: name keyword + status if API allows; card fields for leader/phone; form validates deptName and parent selection

## 3. System — config / dict / oauth / file

- [x] 3.1 Config: search name + built-in filter; columns for key/value/remark; form validates name/key/value; keep built-in key readonly rules
- [x] 3.2 Dict type/data: type name+status and data label search; richer columns; forms validate type name/type and data label/value
- [x] 3.3 OAuth client: name+status search; columns for apiPath/tokenTimeout/captcha; form validates clientId/name/apiPath; keep secret mask
- [x] 3.4 File classify + file: classify name search and richer columns; file name (+ classify filter if options easy); upload still requires classify; classify form validates key+name

## 4. Monitor pages

- [x] 4.1 Job: name+status filters; columns for group/Cron/invokeTarget; no Cron editor UI
- [x] 4.2 Job log: job name + status filters; columns for cost/message as available
- [x] 4.3 Login log: userName + status; columns for location/browser
- [x] 4.4 Operlog: title keyword + status; columns for cost/method; keep detail for long text
- [x] 4.5 Online: userName search; columns for dept/IP/loginTime; force logout keeps confirm
- [x] 4.6 Slow SQL: URI keyword (+ source filter if simple); columns for cost/type/operator; keep full SQL detail

## 5. Verify

- [x] 5.1 For each list API used above, confirm filter query params exist; if missing, degrade to keyword-only and note in page comment
- [x] 5.2 Smoke checklist: every in-scope list has keyword (+ filter when supported); write forms block invalid submit; permissions/built-in rules unchanged; no import/export; no job Cron edit
- [x] 5.3 Mark related OpenSpec tasks complete after smoke
