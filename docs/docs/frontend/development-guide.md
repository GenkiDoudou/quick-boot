# 前端开发规范

摘要自 `sdd/前端代码规范.md` 与 `DESIGN.md`，以 `quick-ui` 源码为准。

## 目录约定

| 目录 | 用途 |
|------|------|
| `src/api/` | 仅 HTTP，不写 UI |
| `src/views/` | 页面级 `.vue` |
| `src/packages/` | C7 组件，全局注册 |
| `src/components/` | 布局级小块（Cron、SvgIcon） |

## 列表页（强制）

- 使用 **C7JsonTable** + JSON 列配置
- 布局/样式对齐 **`views/system/config/index.vue`**
- 颜色、间距、状态样式遵循仓库根 **`DESIGN.md`**

## 权限

- 按钮：`v-hasPermi="['system:xxx:yyy']"`
- 脚本：`checkPermission` / `$auth.hasPermi`

## 请求

- 统一 `utils/request.js`，勿新建 axios 实例
- 所有请求自动 **Client HMAC**（`clientSign.js`）
- `baseURL` = `import.meta.env.VITE_APP_BASE_API`（开发常为 `/dev-api`）

## 命名与脚本

- 页面：`views/<域>/<模块>/index.vue`
- API：`api/<域>/<模块>.js`，导出函数 camelCase
- 组件：C7 前缀 PascalCase，经 `installPackages` 注册

## 构建

```bash
pnpm dev          # 端口 8800，代理到 9992
pnpm build:prod   # 提交前至少执行一次
```

## 相关

- [列表页模板](./list-page-template)
- [项目结构](./structure)
