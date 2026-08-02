## Why

管理页仍按旧 OAuth 客户端模型（grantTypes / scopes / redirect / 验密揭密）编写，而实体与接口已收敛为 `SysOauthClient`（apiPathPatterns、tokenTimeout、checkCaptcha 等）；前端也未使用 `quick-ui/src/packages` 标准组件。需要前后端对齐新模型，并补齐增删改，才能真正管理客户端。

## What Changes

- 后端在已有 `POST /sys/oauthclient/page` 上补齐 `POST /add`、`POST /update`、`GET|POST /remove`
- 新增时服务端自动生成明文 `clientSecret`，仅在创建响应中回传一次；列表/分页不返回 secret
- 修改不变更 secret；增删改后清理 `findByClientId` 缓存
- **BREAKING（前端）**：重写 `oauthClient` API 与页面，废弃旧 `/system/oauth-clients` 及 grant/scope/redirect/reveal 调用
- 管理页改用 `C7JsonTable` / `C7Dialog` / `C7Select` / `C7Switch` / `C7Copy` 等 packages 组件

## Capabilities

### New Capabilities

- `oauth-client-admin`: OAuth 客户端管理（分页查询、新增自动发密、修改、删除）及对应 packages 管理 UI 行为

### Modified Capabilities

- （无）现有 `openspec/specs/` 无已归档的 oauth-client 管理需求契约需改写

## Impact

- 后端：`SysOauthClientController` / `ISysOauthClientService` / `SysOauthClientServiceImpl`、缓存注解
- 前端：`quick-ui/src/api/system/oauthClient.js`、`quick-ui/src/views/system/oauthClient/index.vue`
- 依赖设计文档：`docs/superpowers/specs/2026-08-01-oauth-client-packages-ui-design.md`
- 登录 Client Basic 读客户端配置的路径不变，但依赖缓存失效正确性
