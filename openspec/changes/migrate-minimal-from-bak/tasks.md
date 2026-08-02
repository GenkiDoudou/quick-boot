## 1. Copy from bak (protect specs)

- [x] 1.1 Backup/protect `docs/superpowers/specs/2026-07-25-minimal-migrate-from-bak-design.md` before any docs overwrite
- [x] 1.2 Copy `bak/quickboot` → `quickboot/` excluding `node_modules`, `target`, `logs`, `.git`
- [x] 1.3 Copy `bak/quick-ui` → `quick-ui/` excluding `node_modules`, `dist`
- [x] 1.4 Copy `bak/docs` into root `docs/` excluding `node_modules`, `.vitepress/cache`, `dist`; restore protected specs if overwritten
- [x] 1.5 Verify `bak/` is unchanged

## 2. Backend module trim

- [x] 2.1 Remove `quickboot-report` and `quickboot-tools` directories; drop them from parent `pom.xml` modules
- [x] 2.2 Trim parent POM dependencyManagement entries that only served removed modules (only if unused)
- [x] 2.3 In `quickboot-system`, delete non-login packages: config/dict/notice/file/importtask/exporttask/oauthprovider/operlog/slowsql/monitor/**
- [x] 2.4 Preserve password-login `AuthLoginService` and its direct dependencies under oauthclient (or extract to user/auth); remove oauth client CRUD only
- [x] 2.5 In `quickboot-web`, delete `auth/oauth2/**`, `bridge/**`, and non-login controllers
- [x] 2.6 Trim `quickboot-common` packages clearly unused by remaining login stack (compile-driven)
- [x] 2.7 Fix compile errors with minimal call-site removals/no-ops (prefer delete calls over large stubs)
- [x] 2.8 Run `mvn clean install -DskipTests` in `quickboot/` until green

## 3. Minimal SQL and config

- [x] 3.1 Extract minimal DDL/seed for user/role/menu/dept (+ relations) and default admin from bak SQL sources
- [x] 3.2 Place SQL under an agreed path (e.g. `quickboot/scripts` or `docs` adjacent sql folder) without shipping full dump files
- [x] 3.3 Trim `application*.yml` example for local MySQL/Redis; set captcha disabled or document how to disable for smoke
- [x] 3.4 Smoke: start `quickboot-web` and confirm login API responds with seeded admin (when DB/Redis available)

## 4. Frontend trim

- [x] 4.1 Delete `src/views/system`, `monitor`, `tool`, `oauth`
- [x] 4.2 Delete `src/api` modules for system/monitor/tool/oauth/report/export/import; keep login/menu/common
- [x] 4.3 Keep `src/packages/**`, layout, login/index/error/redirect, router/store/permission/utils/plugins, base components; keep `views/dev` optional demos
- [x] 4.4 Fix router/store/static imports broken by deletions
- [x] 4.5 Run `pnpm i` in `quick-ui/` and verify login page loads; smoke login against backend when available

## 5. Docs trim

- [x] 5.1 Delete docs root `*-dump.sql`, oauth/module analysis extras, updatelog, and deep trees (`docs/backend`, `frontend`, `design`, `deploy`, `sdd`, `skill`, etc.)
- [x] 5.2 Keep `.vitepress` scaffold, `docs/guide/*`, site `index.md`, `public`; slim nav/sidebar to Guide only
- [x] 5.3 Confirm `docs/superpowers/specs/2026-07-25-minimal-migrate-from-bak-design.md` still exists
- [x] 5.4 Run `pnpm i` and `pnpm dev` in `docs/`; verify Guide pages open

## 6. Acceptance

- [x] 6.1 Confirm success criteria: backend install + start, UI login shell, docs guide, no report/tools/oauth server baseline, no dump SQL
- [x] 6.2 Mark OpenSpec tasks complete and note any deferred follow-ups (e.g. further common package slimming)

### Deferred / notes

- `quickboot-common` 未做极致删包（编译驱动下仍保留 excel/file/oauth2 等 common 能力）；后续可继续削。
- 登录联调冒烟依赖本机可达的 MySQL（及 jasypt 解密后的数据源配置）；本次验证了 `mvn clean install -DskipTests`、UI `build:prod`、docs `build`，完整 `/login` 联调待环境可用后再做。
- Flyway 保留 `V1__business.sql`（含 admin 种子），说明见 `quickboot/scripts/README-minimal-login.md`；已删除原 V2–V5（Quartz/Jimu）。
- 前端为 C7/工具类保留了少量 `api/system|import|export` 占位 stub，避免 packages 编译失败；非业务管理页。
