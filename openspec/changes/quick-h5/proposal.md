## Why

管理端 `quick-ui` 无法覆盖手机浏览器 / 微信小程序场景；仓库已有 `bak/h5`（uni-app + uView Pro）可作参考，但尚未在 monorepo 中落地可对接本仓库后端的 H5 客户端。需要新增独立 `quick-h5` 包，完成脚手架与登录闭环，作为后续移动业务的底座。

## What Changes

- 新增 monorepo 包 `quick-h5/`：uni-app Vue3 + Vite + uView Pro + Pinia（官方/社区脚手架新建，从 `bak/h5` 按需移植，非整仓复制）。
- 第一版页面：登录、首页、我的；**不包含**任务/分类/四象限等业务页。
- 登录对齐现有 `POST /login`、`GET /auth/me`；HTTP 层使用与 `quick-ui` 相同的 OAuth Client Basic 混淆协议 + Bearer token；统一解包 `R`（`code === 200`）。
- 后端 Flyway 种子新增独立 OAuth client：`clientId=quick-h5`（与管理端 client 分离；第一版 `check_captcha=0`）。
- 验收：`pnpm dev:h5` 可登录联调；`pnpm dev:mp-weixin` 产物可被微信开发者工具打开。
- 文档：`quick-h5/README.md`；根/模块 README 增加入口说明（如适用）。
- `bak/h5` 仅作参考，不作为运行入口。

## Capabilities

### New Capabilities

- `quick-h5-client`: uni-app 多端客户端工程（H5 / 微信小程序）、登录态、与后端认证对接约定。
- `oauth-client-quick-h5`: 独立 OAuth client `quick-h5` 的种子与凭据约定。

### Modified Capabilities

- （无；不修改既有主 specs 的业务需求。）

## Impact

- 前端：新增 `quick-h5/`（独立 pnpm 工程）；不改 `quick-ui` 业务逻辑（可复制 `oauthClientBasic` 算法）。
- 后端：`quickboot-app` Flyway migration 插入 `sys_oauth_client`；复用现有 `/login`、`/auth/me`，无新业务 API。
- 依赖：uni-app / uview-pro / pinia 等（仅 `quick-h5`）。
- 参考：`docs/superpowers/specs/2026-08-12-quick-h5-design.md`、`docs/superpowers/plans/2026-08-12-quick-h5.md`。
