# Spring Security OAuth2 / Authorization Server

## 本地默认

- **数据库**：进程内 MariaDB Embedded（mariadb4j，默认 `127.0.0.1:3307` / 库 `quickboot`）
- **数据目录**：`./data/mariadb`（二进制解压缓存 `./data/mariadb-base`）
- **Redis**：进程内 Luban-RDS（默认 `127.0.0.1:6379`）
- **端口**：`9993`
- **Issuer**：`http://127.0.0.1:9993`

## 启动

```bash
cd quickboot
mvn -pl quickboot-app -am install -DskipTests
mvn -pl quickboot-app spring-boot:run
```

- JDBC：与 `application-dev.yml` 中 `spring.datasource.url` 一致（默认 `jdbc:mariadb://127.0.0.1:3307/quickboot`）
- 用户：`root`，密码空

关闭嵌入式 MariaDB：`qc.dev.embedded-mariadb.enabled=false`，并自行配置外部 MariaDB/MySQL。  
关闭嵌入式 Redis：`qc.dev.embedded-redis.enabled=false`，并自行配置外部 Redis。

首次切换自 H2 时不会自动迁移旧 `./data/quickboot.mv.db`；可归档后使用新数据目录。

## 认证概要

| 能力 | 说明 |
|------|------|
| 管理端密码登录 | `POST /auth/login`（JSON：username/password）；**无用户 JWT 时**须 `Authorization: Basic`（混淆后的 clientId:clientSecret）→ 用户 JWT |
| Refresh | `POST /auth/refresh`（同样依赖无 JWT 时的 Basic；client 须与授权记录一致） |
| 当前用户 | `GET /auth/me`（拒绝 `token_kind=client`） |
| 社交登录 | `GET /oauth2/authorization/{registrationId}`（如 `gitee`） |
| 未绑定 | 跳转前端 bind 页；`GET /auth/social/pending`、`POST /auth/social/auto-create`、`POST /auth/social/bind` |
| 已绑定落地 | 跳转前端 complete 页；`GET /auth/social/complete` 取 JWT |
| AS 端点 | `/oauth2/authorize`、`/oauth2/token`、`/oauth2/jwks` |
| 客户端管理 | `/system/oauth-clients` CRUD（MyBatis-Plus ↔ `oauth2_registered_client`）；菜单「系统管理 → 客户端管理」；`POST .../reveal-secret` 校验当前用户密码后回显明文密钥 |

种子账号：`admin` / `admin123`  
种子客户端（`client_secret` **明文**入库；用户密码仍 BCrypt）：

- `quick-ui` / `quick-ui-secret`：password + refresh + authorization_code（第一方，可跳过 consent）
- `quick-h5` / `quick-h5-secret`：H5 / 微信小程序客户端（`sys_oauth_client`，见 `V19__oauth_client_quick_h5.sql`）
- `demo-app` / `demo-app-secret`：authorization_code（需 consent）
- `job-runner` / `job-runner-secret`：client_credentials → `token_kind=client`

前端工程：

- 管理端：`quick-ui/`
- 移动端：`quick-h5/`（uni-app；详见该目录 README）

任意 RegisteredClient 只要注册了 `password` grant，即可走 password 发牌（管理端创建时可选）。  
社交发牌客户端由配置 `qc.oauth.social-issue-client-id` 指定（开发默认与种子 `quick-ui` 一致）。

前端（`quick-ui`）无 Token 请求自动带混淆 Basic，环境变量：

- `VITE_OAUTH_CLIENT_ID` / `VITE_OAUTH_CLIENT_SECRET`（开发默认 quick-ui / quick-ui-secret）
- 算法：固定盐 XOR + URL-safe Base64（混淆≠加密；生产须 HTTPS）

## 社交 IdP（Gitee）

在环境变量中注入真实凭据（占位 `gitee-disabled` 仅保证启动）：

```bash
set GITEE_CLIENT_ID=your-id
set GITEE_CLIENT_SECRET=your-secret
```

回调：`{baseUrl}/login/oauth2/code/gitee`（本地一般为 `http://127.0.0.1:9993/login/oauth2/code/gitee`）

无真 IdP 时可用：

```http
POST /auth/social/dev/pending
{"registrationId":"gitee","externalSubject":"10001","displayName":"demo"}
```

（需 `qc.auth.social.dev-mock=true`）

## 外部 App 对接（authorization_code）

1. 在 `/system/oauth-clients` 注册 client（勿配置 password grant）
2. 浏览器打开：  
   `GET /oauth2/authorize?response_type=code&client_id=demo-app&redirect_uri=http://127.0.0.1:8080/callback&scope=openid api.read`
3. 未登录时跳转 Spring 默认 `/login`（表单会话登录，账号同 `admin`）
4. consent（非第一方）后带 `code` 回跳
5. 换票：

```bash
curl -u demo-app:demo-app-secret -X POST http://127.0.0.1:9993/oauth2/token \
  -d "grant_type=authorization_code" \
  -d "code=..." \
  -d "redirect_uri=http://127.0.0.1:8080/callback"
```

机机 client_credentials：

```bash
curl -u job-runner:job-runner-secret -X POST http://127.0.0.1:9993/oauth2/token \
  -d "grant_type=client_credentials" \
  -d "scope=api.read"
```

该 Token 访问 `/auth/me` 应被拒绝。

原型对照：`docs/demo/oauth2-auth-flow-prototype.html`
