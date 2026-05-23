# 常见问题

## 文档站看不到新页面？

1. 必须在 **`docs` 目录**执行 `pnpm dev`，不是 `quick-ui`。
2. 修改 `nav.ts` / `sidebar.ts` 后**重启** dev 服务。
3. 顶栏点击 **指南 / 后端手册 / 前端手册 / 部署** 进入对应侧栏。

## 登录返回 401 或 code 30002

- **30002**：Client HMAC 失败。检查 `VITE_APP_CLIENT_ID`、`VITE_APP_CLIENT_SIGN_KEY` 与库表 `sys_oauth_client` 中明文 secret 一致。
- 确认请求 path 签名时**不含 query**。
- 多实例生产需 Redis，否则 nonce 防重放失效。

## 能登录但菜单为空

- 用户是否分配角色；角色是否分配菜单。
- `getRouters` 接口是否 200；浏览器 Network 查看响应。

## 前端 API 404

- 后端是否已启动在 **9992**。
- `.env.development` 中 `VITE_APP_BASE_API=/dev-api` 是否与 `vite.config.js` proxy 一致。

## Flyway 启动失败

- 勿手工改 H2/MySQL 表不同步脚本。
- 检查 `db/migration` 是否冲突；开发可备份后删除 `./data/qcc` 重建（仅 dev）。

## Jasypt 启动报错

```bash
-Djasypt.encryptor.password=你的主密钥
```

## OAuth 授权页打不开

- 需已登录管理端；`authorize` 路由由菜单或直链进入。
- 白名单与 Client `api_path_patterns` 是否包含相关路径。

## 更多

- [OAuth2 集成](../backend/modules/oauth2)
- [联调测试](../deploy/local-testing)
