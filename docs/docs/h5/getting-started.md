# 移动端快速上手

## 前置

1. 后端已启动（`http://127.0.0.1:9993`）
2. Flyway 已包含 `quick-h5` OAuth client（如 `V19__oauth_client_quick_h5.sql`）
3. 测试账号与管理端相同（如 `admin` / `admin123`）

## 安装

```bash
cd quick-h5
pnpm install
# 若 pnpm 异常：npm install --legacy-peer-deps
```

## 运行与构建

```bash
# H5
pnpm dev:h5
pnpm build:h5

# 微信小程序
pnpm dev:mp-weixin
pnpm build:mp-weixin
# 产物：dist/build/mp-weixin 或 dist/dev/mp-weixin
```

## 开发环境变量（摘要）

| 变量 | 说明 |
|------|------|
| `VITE_APP_BASE_API` | H5 常用 `/dev-api`（Vite 代理到 9993） |
| `VITE_APP_BASE_API_NATIVE` | 非 H5 直连，如 `http://127.0.0.1:9993` |
| `VITE_OAUTH_CLIENT_ID` / `VITE_OAUTH_CLIENT_SECRET` | 默认 `quick-h5` / `quick-h5-secret` |

可选 Lite RUM：`VITE_APP_LITE_RUM_ENABLED`、`VITE_APP_LITE_RUM_APP_ID=quick-h5`。

## 单测

```bash
pnpm test
```
