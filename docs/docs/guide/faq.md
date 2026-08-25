# 常见问题

## 文档站看不到新页面？

1. 必须在 **`docs` 目录**执行 `pnpm dev`，不是 `quick-ui`。
2. 修改 `nav.ts` / `sidebar.ts` 后**重启** dev 服务。
3. 顶栏点击 **指南 / 后端 / 管理端 / 移动端** 进入对应侧栏。

## 所有接口返回 30402

- 含义：**Host 不允许**（主机防火墙）。
- 原因：请求头 `Host` 不在允许列表中。
- 常见情况：用 `http://127.0.0.1:9993` 访问但只配置了其它 Host；或生产未配置公网域名/IP。
- 处理：在对应 Profile 的 yml 中增加实际 `Host:端口`，开发环境通常已放行 `localhost` / `127.0.0.1`。

## 登录返回 401 或客户端凭证失败

- 检查 `VITE_OAUTH_CLIENT_ID` / `VITE_OAUTH_CLIENT_SECRET` 是否与后端种子客户端一致（管理端 `quick-ui`，H5 `quick-h5`）。
- 确认后端已启动且 Client 记录存在。
- 多实例生产需 Redis，否则 nonce / Token 相关能力可能异常。

## 能登录但菜单为空

- 用户是否分配角色；角色是否分配菜单。
- `getRouters` 接口是否 200；浏览器 Network 查看响应。

## 前端 API 404

- 后端是否已启动在 **9993**。
- `.env.development` 中 `VITE_APP_BASE_API=/dev-api` 是否与 Vite 代理一致。

## Flyway 启动失败

- 勿手工改库表导致与迁移脚本不一致。
- 检查 `db/migration` 是否冲突；开发可备份后清理嵌入式数据目录 `./data/mariadb` 再启动（仅 dev，慎用）。

## Jasypt 启动报错

若启用了配置加密，按当前 `application*.yml` 要求提供主密钥，例如：

```bash
-Djasypt.encryptor.password=你的主密钥
```

## 更多

- [后端约定](/docs/backend/conventions)
- [管理端约定](/docs/frontend/conventions)
- [移动端约定](/docs/h5/conventions)
- [快速上手](./quick-start)
