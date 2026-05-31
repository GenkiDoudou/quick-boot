# 前端概述

`quick-ui` 是 QuickBoot 的 **Vue 3 管理后台**，在 RuoYi 风格架构上增强 **C7 组件库** 与 **OAuth2 / Client 签名** 能力，与 `quickboot-web`（默认 9992）配合使用。

## 技术栈

| 类别 | 依赖 |
|------|------|
| 框架 | Vue 3.5、Vue Router 4、Pinia 2 |
| UI | Element Plus 2.x、@element-plus/icons-vue |
| 构建 | Vite 5、Sass |
| HTTP | Axios |
| 工程化 | unplugin-auto-import、unplugin-vue-components、vite-plugin-svg-icons |
| 测试 | Vitest |
| 包管理 | pnpm 9 |

## 定位与特点

1. **JSON 驱动列表**：`C7JsonTable` + `C7JsonTableColumn` 统一列表页开发模式
2. **动态权限路由**：登录后 `getRouters` 拉取菜单并 `router.addRoute`
3. **按钮级权限**：`v-hasPermi`、`v-hasRole`，全局 `$auth`
4. **请求签名**：所有 API 自动附加 Client HMAC（`utils/clientSign.js`）
5. **OAuth2 运维界面**：客户端、IdP 提供方、授权确认页、登录页联邦入口

## 运行

```bash
cd quick-ui
pnpm i
pnpm dev        # 开发
pnpm build:prod # 生产构建
```

环境变量见 `quick-ui/.env.development` / `.env.production`（`VITE_APP_*`）。

## 目录速览

| 路径 | 职责 |
|------|------|
| `src/api/` | REST 封装 |
| `src/views/` | 业务页面 |
| `src/packages/` | C7 组件库 |
| `src/components/` | 布局、Cron、图标选择等 |
| `src/router/` | 常量路由 |
| `src/store/` | Pinia（user、permission、settings…） |
| `src/layout/` | 主框架（侧栏、TagsView） |
| `src/utils/` | request、auth、dict、clientSign |
| `src/plugins/` | `$tab`、`$auth`、`$modal`、`$download` |
| `src/monitor/` | 用户行为采集 SDK（见 [文档](./modules/user-behavior-monitor)） |
| `src/test/` | 单元测试（如 `test/monitor/`） |
| `permission.js` | 路由守卫 |

## 与后端协作

| 能力 | 前端 | 后端 |
|------|------|------|
| 登录 Token | Cookie `Admin-Token` + Header Bearer | Sa-Token |
| 菜单路由 | `getRouters` | `AuthController` |
| 字典 | `useDict` | 字典 API |
| Client 签名 | `request` 拦截器 | `ClientSignService` |
| OAuth 授权页 | `oauth/authorize.vue` | `/oauth2/doConfirm` |

## 相关文档

- [项目结构](./structure)
- [业务页面总览](./modules/index)
- [C7JsonTableColumn](../frontend/components/通用组件/c7-json-table-column)（列渲染）
- [OAuth2 集成](../backend/modules/oauth2)
