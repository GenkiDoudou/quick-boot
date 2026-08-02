# 从 bak 最小迁移 boot / UI / docs 设计

| 项 | 内容 |
|----|------|
| 日期 | 2026-07-25 |
| 状态 | 已确认待实施 |
| 来源 | `bak/quickboot`、`bak/quick-ui`、`bak/docs` |
| 目标 | 仓库根目录可运行的最小三件套：登录后端、登录+组件前端、基础文档站 |

## 1. 背景与目标

当前仓库根目录几乎为空，完整工程在 `bak/`（gitignore）。需要迁出最基本可运行子集：

- **后端**：账号密码登录链路可跑通（含 getInfo / getRouters / logout）
- **前端**：登录页 + 布局壳 + `src/packages`（C7 组件）
- **文档**：VitePress + guide 基础文档

`bak/` 保留不动。

## 2. 已确认决策

| # | 决策 | 选择 |
|---|------|------|
| 1 | 目标位置 | 仓库根下 `quickboot/`、`quick-ui/`、`docs/` |
| 2 | 后端边界 | 可跑通登录 + 必需依赖；去掉 OAuth2/报表/定时任务/代码生成等 |
| 3 | 前端边界 | 登录 + 布局壳 + packages + 路由/权限/请求基建；去掉业务页 |
| 4 | 文档边界 | 仅 guide + 站点脚手架 |
| 5 | 迁移方式 | 从 bak 拷贝后裁剪，保证能本地启动 |
| 6 | 数据库 | 最小 DDL/初始化 SQL + 精简配置 |

迁移策略：**方案 A — 整树拷贝后按模块裁剪**（相对白名单精确拷贝更稳，相对整拷禁用更干净）。

## 3. 成功标准

1. `cd quickboot && mvn clean install -DskipTests` 通过
2. `mvn -pl quickboot-web spring-boot:run` 可启动
3. `cd quick-ui && pnpm i && pnpm dev` 可打开登录页，完成登录后进入 layout 壳（首页可为欢迎/空壳）
4. `cd docs && pnpm i && pnpm dev` 可浏览 guide
5. 产物中不包含：OAuth2 服务端/客户端实现、报表模块、代码生成、监控/系统业务管理前端页、根目录大 dump SQL

## 4. 目标目录结构

```text
/
├── quickboot/          # Maven 多模块（瘦身）
├── quick-ui/           # Vue3 + Vite（登录 + 组件）
├── docs/               # VitePress（guide）
│   └── superpowers/specs/   # 本设计文档所在（可保留）
└── bak/                # 不改动，仍为参考源
```

协作文件（如需要）：可从 `bak/AGENTS.md`、`bak/DESIGN.md` 按需拷到根目录；**非本次必做**，实施计划中可列为可选。

## 5. 后端裁剪设计

### 5.1 模块

| 模块 | 处理 |
|------|------|
| `quickboot-common` | 保留并按编译依赖瘦身 |
| `quickboot-core` | 保留（如 LoginLockService） |
| `quickboot-system` | 保留但删除非登录业务包 |
| `quickboot-web` | 保留；删除 oauth2 与非登录入口 |
| `quickboot-report` | **删除**整模块 |
| `quickboot-tools` | **删除**整模块 |

父 `pom.xml` 去掉已删 module 及仅服务它们的 dependencyManagement 条目（仅删确无引用的，避免无关大改）。

### 5.2 `quickboot-system` 包级边界

**保留：**

- `web.system.user`（含 authcache、datascope）
- `web.system.menu`
- `web.system.role`
- `web.system.dept`（登录数据范围常依赖）
- `web.system.online`（AuthController 引用）
- `web.system.logininfor`（登录日志写入；不迁前端管理页）

**删除：**

- `web.system.config` / `dict` / `notice` / `file` / `importtask` / `exporttask`
- `web.system.oauthprovider` 及 oauth **客户端管理**相关 Controller/CRUD
- `web.system.operlog` / `slowsql`
- `web.monitor.**`

**特例（避免与「删 oauthclient」冲突）：**  
`AuthController` 依赖 `web.system.oauthclient.service.AuthLoginService`（账号密码登录核心，非 OAuth2 页面管理）。实施时**必须保留该登录服务及其直接依赖**；可整包暂留 `oauthclient` 中登录所需类，或把 `AuthLoginService` 抽到 `user`/`auth` 包后再删 OAuth 管理代码。禁止为「目录名含 oauth」而误删登录服务。

若删除后其它保留类仍引用已删类型：优先删除调用点或改为最小 no-op；避免为大段已删功能留桩。

### 5.3 `quickboot-web`

**保留：**

- `WebApplication`、基础 `config` / `exception`
- `auth.AuthController` 及登录必需的 Sa-Token / WebSecurity 配置类
- 验证码相关（若 `qc.login.captcha-enabled` 为 true 时的依赖）；默认可按 bak 配置，本地示例可关闭验证码便于冒烟

**删除：**

- `auth.oauth2.**`
- `bridge.**`（报表等）
- 非登录业务 Controller（若有）

### 5.4 `quickboot-common`

保留登录与 Web 基建：`api`、`exception`、`security`、`mybatis`、`cache`、`servlet`、`validation`、`captcha`（若需要）等。

删除明显仅服务已删模块的包（如 oauth2、excel 导入导出、exporttask/importtask 等），以编译通过为准逐项确认，不强行一次删光导致反复。

### 5.5 数据库与配置

- 提供最小 SQL：用户/角色/菜单/部门及关联表 + 默认管理员账号
- 来源：从 bak 已有 DDL/dump **抽取**相关表，不整份拷贝 dump
- `application*.yml`（或等价配置）精简为本地 MySQL + Redis（Sa-Token 若依赖）示例；密钥用占位符

## 6. 前端裁剪设计

### 6.1 保留

- 工程脚手架：`package.json`、`vite.config.*`、`pnpm-lock.yaml`、env 文件、`index.html`、`public`
- `src/views/login.vue`、`index.vue`、`error/*`、`redirect/*`
- `src/layout/**`
- `src/packages/**`（全部 C7 组件）
- `src/api/login.js`、`menu.js`、`common`（登录相关）
- `src/router`、`store`、`permission.js`、`utils`、`plugins`、基础 `components`（SvgIcon 等）
- `src/views/dev/*`（组件演示页，便于验证 packages）

### 6.2 删除

- `src/views/system/**`、`monitor/**`、`tool/**`、`oauth/**`
- `src/api/system`、`monitor`、`tool`、`oauth`、`report`、`export`、`import`
- 路由/静态菜单中对已删页面的引用

### 6.3 登录后行为

- 走原有 `/login` → token → `getInfo` / `getRouters`
- 库中业务菜单可为空或仅首页；进入 layout + 欢迎/空首页即可
- 不实现系统管理 CRUD 页

## 7. Docs 裁剪设计

### 7.1 保留

- `.vitepress` 脚手架（精简 `nav` / `sidebar`，仅 Guide）
- `docs/guide/*`（introduction、installation、quick-start、faq、contributing 等已有 guide 文）
- 站点 `index.md`、`public` 基础静态资源
- 本迁移设计文档路径 `docs/superpowers/specs/`（实施产物）

### 7.2 删除

- 根级大 dump：`*-dump.sql`
- `oauth2-integration-guide.md`、`quickboot-module-analysis.md`
- `superpowers/` 下除本 specs 外的历史材料（若从 bak 整拷后清理；注意勿删本次新建的 design）
- `updatelog`（或不保留）
- `docs/backend`、`docs/frontend`、`docs/design`、`docs/deploy`、`docs/sdd`、`docs/skill` 等深文档目录

## 8. 实施顺序

1. 从 `bak` 拷贝 `quickboot`、`quick-ui`、`docs` 到仓库根（排除 `node_modules`、`target`、`dist`、`.vitepress/cache`）
2. 后端：删 report/tools 模块 → 瘦 system/web/common → 修父 pom → `mvn install`
3. 前端：删业务 views/api → 修 router/store 引用 → `pnpm i` + 登录冒烟
4. docs：删非 guide 内容 → 精简 sidebar/nav → `pnpm i` + 打开 guide
5. 落最小 SQL + 精简配置示例
6. 对照第 3 节成功标准做冒烟验证

## 9. 明确不做

- 不改动 `bak/`
- 不重写架构、不改 Java/Vue 包名或品牌
- 不顺手重构、不扩大功能（用户/角色管理页、OAuth2 等后续再加）
- 不自动 `git commit` / `push`（除非用户另行要求）

## 10. 风险与处理

| 风险 | 处理 |
|------|------|
| 登录链路隐藏依赖（operlog 注解、配置表等） | 编译/启动报错时最小修补：删注解调用或保留只读最小依赖 |
| 前端动态路由依赖后端菜单 | 初始化 SQL 给首页路由；或前端兜底欢迎页 |
| 验证码/Redis 阻塞本地冒烟 | 配置示例默认关闭验证码或文档标明依赖 |
| docs 整拷覆盖已写 specs | 拷贝时保护 `docs/superpowers/specs/2026-07-25-*.md`，或先拷 docs 再写回本文件 |

## 11. 下一步

用户审阅本 spec 无异议后，再编写详细 implementation plan（writing-plans），然后按计划实施。
