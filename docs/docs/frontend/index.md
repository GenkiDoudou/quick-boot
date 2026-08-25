# 管理端概述（quick-ui）

`quick-ui/` 是 Vue 3 管理后台：对接 quickboot 登录、动态菜单与系统/监控等业务页。

## 技术栈

- Vue 3.5、Vite 5、Pinia、Vue Router 4
- Element Plus、Axios、Vitest
- **C7** 组件（`src/packages`）：JSON 驱动表格/表单、上传、字典标签等
- 包管理：**pnpm 9**（`packageManager=pnpm@9.0.0`）

## 定位

| 能力 | 说明 |
|------|------|
| 动态路由 | 登录后拉取菜单路由并 `addRoute` |
| 权限 | 指令与插件控制按钮/路由 |
| 请求 | Bearer Token + OAuth Client 凭证（开发默认 `quick-ui`） |
| 列表范式 | 业务列表优先 C7 JSON 表格 + 列配置 |

与后端、H5 的关系见 [后端概述](/docs/backend/)、[移动端概述](/docs/h5/)。
