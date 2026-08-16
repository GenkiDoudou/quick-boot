# quick-h5

uni-app（Vue3 + Vite + uView Pro）多端客户端，对接 quickboot 后端登录。

## 前置

1. 后端已启动（默认 `http://127.0.0.1:9993`）
2. Flyway 已包含 `quick-h5` OAuth client（`V19__oauth_client_quick_h5.sql`）
3. 开发凭据（见 `.env.development`）：
   - H5：`VITE_APP_BASE_API=/dev-api`（Vite 代理到 9993，避免 CORS）
   - 非 H5：`VITE_APP_BASE_API_NATIVE=http://127.0.0.1:9993`
   - `VITE_OAUTH_CLIENT_ID=quick-h5` / `VITE_OAUTH_CLIENT_SECRET=quick-h5-secret`
4. 测试账号与管理端相同（如 `admin` / `admin123`）
5. 后端 dev 已开启 `qc.security.firewall.cors`（直连跨域时可用）
6. Lite RUM（可选）：`VITE_APP_LITE_RUM_ENABLED=true`，`VITE_APP_LITE_RUM_APP_ID=quick-h5`；上报 `/monitor/liteTrace/rum/ingest`，与管理端共用控制台（按 appId 区分）

## 安装与运行

```bash
cd quick-h5
# 推荐 pnpm；若本机 corepack/pnpm 异常可用 npm
pnpm install
# 或：npm install --legacy-peer-deps

# H5
pnpm dev:h5
pnpm build:h5

# 微信小程序（用微信开发者工具打开产物目录，开发期可关闭合法域名校验）
pnpm dev:mp-weixin
pnpm build:mp-weixin
# 产物：dist/build/mp-weixin 或 dist/dev/mp-weixin
```

## 单测

```bash
pnpm test
```

## 页面

- 登录 `/pages/login/login`
- Tab：首页 `/pages/home/home`（快捷入口 · 消息 · 今天待办）
- Tab：工作台 `/pages/workbench/workbench`（后台可配置菜单，当前 mock）
- Tab：我的 `/pages/mine/mine`（个人信息 / 联系我们 / 关于·清缓存 / 退出）
- 子页：`profile` · `contact` · `about`

设计说明：`docs/superpowers/specs/2026-08-12-quick-h5-design.md`  
静态原型：`docs/demo/quick-h5-tab-prototype.html`

参考工程：`bak/h5`（仅作配置与 UI 参考，非运行入口）。
