# 管理端快速上手

## 前置

1. 后端已在 **9993** 启动（见 [后端上手](/docs/backend/getting-started)）
2. 已安装 Node.js LTS + pnpm 9

## 安装与开发

```bash
cd quick-ui
pnpm install
pnpm dev
```

开发服务器端口以 Vite 控制台为准。

## 关键环境变量（`.env.development`）

| 变量 | 开发常用值 | 说明 |
|------|------------|------|
| `VITE_APP_BASE_API` | `/dev-api` | 经 Vite 代理到后端，避免 CORS |
| `VITE_OAUTH_CLIENT_ID` | `quick-ui` | 首方客户端 |
| `VITE_OAUTH_CLIENT_SECRET` | `quick-ui-secret` | 与种子 client 一致（勿提交生产密钥） |

生产构建使用 `.env.production`（如 `VITE_APP_BASE_API=/prod-api`）。

## 构建

```bash
pnpm build:prod
```

产物目录：`quick-ui/dist/`（Jenkins 发布到目标机 `www/ui`）。

## 登录

种子账号与后端相同：`admin` / `admin123`。
