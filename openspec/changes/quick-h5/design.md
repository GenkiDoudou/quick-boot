## Context

- 管理端：`quick-ui`（Vue3 + Element Plus）已对接 Sa-Token `POST /login`、`GET /auth/me` 与 OAuth Client Basic（混淆）。
- 参考工程：`bak/h5`（uni-app Vue3 + uView Pro），含任务等业务页与另一套后端契约，不能直接当本仓库运行入口。
- 用户已确认：新建 `quick-h5/`；方案 2（官方脚手架 + 按需移植）；多端（H5 + 微信小程序验收）；砍掉任务业务页；独立 OAuth client `quick-h5`。
- 详设与实现计划：`docs/superpowers/specs/2026-08-12-quick-h5-design.md`、`docs/superpowers/plans/2026-08-12-quick-h5.md`。

## Goals / Non-Goals

**Goals:**

- 落地可独立安装的 `quick-h5` uni-app 工程。
- 用 `quick-h5` client 完成账号密码登录、会话保持、`/auth/me` 展示、退出。
- H5 与微信小程序脚手架可跑通（小程序至少能打开工程）。
- 后端种子写入启用中的 `quick-h5` OAuth client（`check_captcha=0` 便于第一版联调）。

**Non-Goals:**

- 任务/分类/四象限等业务域与对应后端 API。
- 社交登录、短信登录。
- App / Harmony 上架发布。
- 与 `quick-ui` 共享前端工程或强绑定 monorepo workspace。
- 整仓复制 `bak/h5`（含演示页与 aitodo 页面）。

## Decisions

1. **工程创建：官方/社区 uni Vue3 脚手架 + 移植，而非整目录拷贝**  
   - 理由：用户选定方案 2，减少 bak 业务残留。  
   - 备选：整目录拷贝再裁剪（更快但噪音大）——不做默认。

2. **目录：`quick-h5/` 与 `quick-ui` 并列**  
   - 独立 `pnpm`；版本尽量贴近 `bak/h5` 已验证的 `@dcloudio/*` / uview-pro 组合。

3. **认证协议对齐 `quick-ui`**  
   - 未登录：`Authorization: Basic` + XOR 盐 `QuickBootOAuth1` 混淆 `clientId:clientSecret`。  
   - 已登录：`Authorization: Bearer <accessToken>`。  
   - 成功码：`R.code === 200`；登录后必须调 `/auth/me`。  
   - 备选：复用 `quick-ui` client——用户要求独立 client，否决。

4. **OAuth 种子：Flyway `V19__oauth_client_quick_h5.sql`（或下一可用版本号）**  
   - `clientId=quick-h5`，开发 secret `quick-h5-secret`，`api_path_patterns=/**`，`status=0`，`check_captcha=0`。  
   - 前端 `.env.development` 写入同名凭据（可提交开发示例值）。

5. **页面范围固定：login / home / mine**  
   - mine 提供退出；第一版可不配 tabBar。

6. **HTTP：自研 `uni.request` 封装**  
   - 不依赖 bak 中不解包 `R` 的旧拦截器；小程序与 H5 共用直连 `VITE_APP_BASE_API`（第一版不强制 Vite 代理，减少双端差异）。

7. **验证码**  
   - 第一版依赖 client `check_captcha=0`；若全局强制验证码再另补对接。

## Risks / Trade-offs

- [脚手架与 bak 依赖版本冲突] → 版本贴近 bak；UnoCSS 非必须。  
- [小程序无法用浏览器代理] → 独立可访问 baseUrl；文档写清关闭合法域名校验。  
- [secret 误提交生产密钥] → 仅开发示例值；生产用占位。  
- [degit/模板网络失败] → 允许以 bak 的配置层为骨架再建空页面，仍禁止拷贝业务页。

## Migration Plan

1. 合入 Flyway 种子并启动后端确认 `quick-h5` client。  
2. 新增 `quick-h5`、联调 H5 登录。  
3. 验证 `dev:mp-weixin` 产物可导入微信开发者工具。  
4. 回滚：删除 `quick-h5/` 包与对应 migration（已执行库需手工删 client 行）。

## Open Questions

- （无阻塞）Flyway 文件名以仓库当前最大版本 +1 为准（计划写 V19；若已有更新迁移则顺延）。
