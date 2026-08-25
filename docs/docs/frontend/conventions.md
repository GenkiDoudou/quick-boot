# 管理端关键约定

## 请求层

- 页面与组件通过 `src/api/**` 发请求，**不要**在页面内裸用 axios。
- `src/utils/request.js` 统一携带 Token；无 Token 时按约定附带 OAuth Client 凭证（见 `.env*` 中 `VITE_OAUTH_*`）。
- 开发代理：`VITE_APP_BASE_API=/dev-api` 须与 `vite` 代理目标（后端 9993）一致。

## 路由与权限

- 静态路由 + 登录后动态路由（后端菜单）。
- 按钮级：`v-hasPermi` / `v-hasRole`；全局权限辅助见 `plugins/auth`。

## 列表 / 表单范式

- 业务列表优先 **C7 JSON 表格** + 列配置，样式与交互对齐现有系统页（如参数配置页）。
- 新增页面先检索 `packages` 与同类 `views`，复用后再扩展。

## 编码与注释

- 命名、目录、注释要求以 `code_formater.md`、`AGENTS.md` 为准。
- 生产环境变量与密钥勿提交仓库。
