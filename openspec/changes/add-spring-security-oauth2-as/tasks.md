## 1. P0 模块与依赖骨架

- [x] 1.1 新建 Maven 模块 `quickboot-auth`，并在父 POM / `quickboot-web` 中引入
- [x] 1.2 为 auth/web 增加 Spring Security、Authorization Server、OAuth2 Resource Server、OAuth2 Client 依赖（Boot 4 对齐）
- [x] 1.3 定义 auth → system 的用户查询端口接口（避免循环依赖）与 system 侧实现骨架

## 2. P0 数据与种子客户端

- [x] 2.1 增加 `sys_user` DDL/迁移与最小用户仓储（含密码哈希字段）
- [x] 2.2 接入 SAS JDBC schema（`oauth2_registered_client` / `oauth2_authorization` / consent）
- [x] 2.3 种子第一方 client `quick-ui`（允许 password + refresh），启动后可查询到

## 3. P0 AS + password 扩展 + RS

- [x] 3.1 配置 AS SecurityFilterChain（`/oauth2/**`）与应用链 Resource Server JWT
- [x] 3.2 实现 password 扩展 grant（Converter + Provider + TokenGenerator 注册）
- [x] 3.3 实现 `OAuth2TokenCustomizer`：用户 Token 写入 `token_kind=user` 与 `sub=userId`
- [x] 3.4 实现 `POST /auth/login`、`POST /auth/refresh`、`GET /auth/me`（me 拒绝 client token）
- [x] 3.5 放行匿名登录相关路径；保护其余业务/API；冒烟：登录 → Bearer → `/auth/me`

## 4. P1 社交登录与绑定

- [x] 4.1 增加 `sys_oauth_user_bind` DDL 与仓储（唯一约束 registration_id + external_subject）
- [x] 4.2 配置至少一个 IdP 的 `spring.security.oauth2.client.registration.*`（可用环境变量注入 secret）
- [x] 4.3 实现 oauth2Login 回调处理：已绑定直接解析本地用户
- [x] 4.4 实现未绑定流程 API：pending / auto-create / bind（含冲突拒绝）
- [x] 4.5 社交完成后用同一 TokenGenerator 发用户 JWT；冒烟：建号、绑定、二次直登

## 5. P2 外部 App 与客户端管理

- [x] 5.1 实现 `/system/oauth-clients` CRUD，持久化到 RegisteredClientRepository
- [x] 5.2 校验：非第一方 client 禁止配置 password grant
- [x] 5.3 联调 authorization_code（含 redirect_uri 校验与 consent 策略：第一方可跳过）
- [x] 5.4 （可选）种子/配置 `client_credentials` 机机 client，Customizer 写 `token_kind=client`；断言不能访问 `/auth/me`
- [x] 5.5 编写外部对接说明（端点、示例 client、与原型页对照）

## 6. 前端与收尾

- [x] 6.1 调整 `quick-ui` 登录为 `POST /auth/login` + Bearer 拦截器（替换旧 Sa-Token/旧 OAuth 路径假设）
- [x] 6.2 社交入口跳转 `/oauth2/authorization/{id}` 与绑定/落地页对接
- [x] 6.3 更新 README 或模块说明：如何配置 issuer、客户端种子、社交 secret
- [x] 6.4 对照四份 delta specs 做验收清单自检（密码/社交/AS/RS）
