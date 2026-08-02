## Why

仓库根目录几乎为空，完整可运行工程仅在 `bak/`（gitignore）。需要迁出最小可本地启动的三件套（后端登录、前端登录+组件、基础文档站），作为后续功能增量的基线，而不是整库搬运业务模块。

## What Changes

- 从 `bak/` **拷贝后裁剪**到仓库根：`quickboot/`、`quick-ui/`、`docs/`（排除 `node_modules`/`target`/`dist` 等构建产物）
- **后端**：保留 `common`/`core`/`system`(瘦身)/`web`；删除 `report`/`tools`；去掉 OAuth2 服务端/客户端管理、报表 bridge 等非登录入口；保留账号密码登录（含 getInfo/getRouters/logout）及必需依赖（含 `AuthLoginService`）
- **前端**：保留登录页、布局壳、`src/packages`（C7）、路由/权限/请求基建；删除 system/monitor/tool/oauth 等业务页与对应 API
- **文档**：保留 VitePress 脚手架与 `guide`；删除 dump SQL、深文档目录与无关材料；保护已有 `docs/superpowers/specs/`
- 提供**最小 DDL/初始化 SQL**与精简本地配置示例（MySQL/Redis 等）
- **不改动** `bak/`；不重写架构、不改包名；不自动 commit

## Capabilities

### New Capabilities

- `minimal-backend-login`: 仓库根后端最小登录可运行基线（模块裁剪、登录 API、最小 SQL/配置）
- `minimal-ui-shell`: 仓库根前端登录页 + 布局壳 + C7 组件包，无业务管理页
- `minimal-docs-guide`: 仓库根 VitePress 仅 guide 的基础文档站

### Modified Capabilities

- （无：当前 `openspec/specs/` 尚无既有能力契约）

## Impact

- 新增根目录工程树：`quickboot/`、`quick-ui/`、`docs/`（docs 内部分路径可能已存在 specs）
- 运行依赖：JDK 17+、Maven、MySQL、Redis（若 Sa-Token 持久化需要）、Node/pnpm
- API：保留与 quick-ui 对接的登录相关接口；不保留 OAuth2/业务管理 API 作为基线承诺
- 参考源：`bak/` 只读；详细设计见 `docs/superpowers/specs/2026-07-25-minimal-migrate-from-bak-design.md`
