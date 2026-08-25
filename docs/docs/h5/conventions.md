# 移动端关键约定

## 多端 API 基址

- **H5**：优先走相对前缀 + Vite 代理（`VITE_APP_BASE_API=/dev-api`），避免浏览器 CORS。
- **小程序 / App**：使用 `VITE_APP_BASE_API_NATIVE` 直连后端；开发期可在微信开发者工具关闭合法域名校验。
- 生产 H5：常与 Nginx `/h5/` 同域，API 走 `/prod-api/` 反代（见 `deploy/nginx`）。

## OAuth Client

- 使用独立客户端 `quick-h5`，勿与管理端 `quick-ui` 混用密钥。
- 种子数据由 Flyway 维护；改 secret 须同步后端库表与各端 env。

## 请求与权限

- 统一走封装好的请求层；Token 存储与刷新策略遵循现网 `utils` / `store` 实现。
- 菜单/工作台若依赖后端配置，以接口返回为准；本地 mock 仅开发过渡。

## 观测（可选）

- Lite RUM 开启时使用独立 `appId`（如 `quick-h5`），与管理端共用监控控制台、按 appId 区分。

## 规范

- 业务代码注释与命名遵循 `code_formater.md`、`AGENTS.md`。
- 勿提交真实 client secret 与生产 env。
