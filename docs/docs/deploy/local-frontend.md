# 本地前端部署

```bash
cd quick-ui
pnpm i
pnpm dev
```

## 代理

`vite.config.js` 将 `/dev-api` 代理到 `http://localhost:9992` 并去掉前缀。

`.env.development` 示例：

```env
VITE_APP_BASE_API=/dev-api
VITE_APP_CLIENT_ID=quick-ui
VITE_APP_CLIENT_SIGN_KEY=<与库中 client_secret 明文一致>
```

浏览器访问 Vite 控制台打印的本地地址（`vite.config.js` 默认端口 **8800**）。

## 生产构建

```bash
pnpm build:prod
```

产物在 `dist/`，由 Nginx 托管；`VITE_APP_BASE_API` 指向网关或后端公网路径。

## 相关

- [联调测试](./local-testing)
