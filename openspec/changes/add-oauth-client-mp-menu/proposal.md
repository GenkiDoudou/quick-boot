## Why

脚手架已具备进程内 Authorization Server 与 `/system/oauth-clients` REST，但客户端仍走 JDBC `RegisteredClientRepository`，且前端无可用「客户端管理」菜单（`/getRouters` 为空）。需要用 MyBatis-Plus 统一持久化同一 SAS 表，并挂上管理端菜单/页面，便于维护 OAuth2 注册客户端。

## What Changes

- 以 `oauth2_registered_client` 为唯一数据源，新增 MyBatis-Plus Entity/Mapper
- 实现 `MybatisRegisteredClientRepository`，替换 AS 使用的 JDBC Repository；种子客户端仍写入同表
- 管理 API 保持 `/system/oauth-clients` REST；Service 层基于 MP；移除/停用 `JdbcTemplate` 裸 SQL 查询辅助
- `/getRouters` 写死「系统管理 → 客户端管理」菜单
- quick-ui：`oauthClient.js` 改对接新 REST；精简 `system/oauthClient/index` 页面字段以匹配当前 View

## Capabilities

### New Capabilities

- `oauth-client-admin`: OAuth2 注册客户端的 MyBatis-Plus 持久化、管理 REST、种子与 AS 仓库适配、前端菜单与维护页

### Modified Capabilities

- （无）主规格目录尚无已归档的 oauth 客户端管理 capability；本变更为新增能力。与进行中的 `add-spring-security-oauth2-as` 互补但不修改其 delta 契约文件。

## Impact

- 后端：`quickboot-auth`（RegisteredClientRepository Bean）、`quickboot-system`（Entity/Mapper/Service/Controller、ScaffoldCompat getRouters）
- 前端：`quick-ui` API 与 `views/system/oauthClient`
- 数据：仅既有表 `oauth2_registered_client`，无新表
- 依赖：MyBatis-Plus（已有）；不再依赖 JDBC RegisteredClientRepository 作为运行时实现
