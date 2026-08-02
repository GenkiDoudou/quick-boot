## Context

实体 `SysOauthClient` / `SysOauthClientVo` 已收敛为：`clientId`、`clientSecret`、`clientName`、`apiPathPatterns`、`tokenTimeout`、`checkCaptcha`、`status`。后端仅有 `POST /sys/oauthclient/page`；`findByClientId` 带 Spring Cache（`sys-oauthClient#3600`）。前端管理页与 API 仍指向旧 `/system/oauth-clients` 模型。权威产品决策见 `docs/superpowers/specs/2026-08-01-oauth-client-packages-ui-design.md`。

## Goals / Non-Goals

**Goals:**

- 补齐 add / update / remove，与 page 组成可管理闭环
- 新增自动生成明文 secret，仅创建响应回传一次；分页不泄露 secret
- 管理页用 packages（C7JsonTable / C7Dialog / C7Select / C7Switch / C7Copy）对齐新字段
- 增删改后失效客户端查询缓存，保证登录 Client Basic 读到最新配置

**Non-Goals:**

- 修改时重新生成密钥开关
- 密码验密后 reveal-secret
- grantTypes / scopes / redirectUris / requireAuthorizationConsent
- 导入导出与细粒度权限码（可后续挂）

## Decisions

1. **API 风格**  
   - 新增 / 修改均用 **POST**（`/add`、`/update`），删除同时提供 **GET 与 POST** `/remove`。  
   - 备选：REST PUT/DELETE — 否决，以对话约定为准。

2. **Secret 生命周期**  
   - 服务端生成随机明文 secret 入库（与种子 `quick-ui` / Basic 解密约定一致）。  
   - 分页 / 列表映射时清空 `clientSecret`；仅 `/add` 响应携带明文。  
   - 备选：BCrypt 入库 — 否决，当前运行时按明文比对。

3. **更新范围**  
   - `/update` 不改 `clientSecret`；`clientId` 不可变。  
   - 缓存：`@CacheEvict(cacheNames = CACHE_NAME, key = "#…clientId")`（或清全缓存名），挂在 add/update/remove。

4. **分页适配**  
   - 后端继续 `PageRequest`（`current`/`size`/`param`）。  
   - 前端 `listFunction` 将 C7JsonTable 的 `pageNum`/`pageSize` 映射为 `current`/`size`，响应保持 `{ data: { records, total } }`。

5. **删除**  
   - 优先逻辑删除（`del_flag`）；与 MyBatis-Plus `@TableLogic` 行为对齐（若实体未标注则补逻辑或走 removeById）。  
   - POST `/remove` 支持单 `clientId`；可扩展 `clientIds` 批量供表格多选。

6. **前端组件**  
   - 列表一体用 C7JsonTable；表单 / 凭证弹窗用 C7Dialog；布尔与状态下拉用 C7Switch / C7Select；凭证复制用 C7Copy。

## Risks / Trade-offs

- [明文 secret 泄露面] → 列表永不返回；UI 仅创建瞬间展示；运维靠安全传输与权限控制  
- [缓存未清导致 Basic 仍用旧配置] → 所有写路径强制 CacheEvict，任务验收含「改后立刻生效」  
- [旧前端书签/脚本仍打 `/system/oauth-clients`] → **BREAKING**：文档与实现同步切到 `sys/oauthclient`，不保留兼容层（本轮）  
- [逻辑删除与唯一 clientId] → 若逻辑删后同 id 无法再建，需在实现时确认 MP 逻辑删策略；冲突则文档化或物理删

## Migration Plan

1. 部署后端新接口后，前端同期发布（否则旧页仍打失效 API）  
2. 无需数据迁移（表结构已含新字段）  
3. 回滚：回退前后端版本即可；已创建客户端数据保留

## Open Questions

- 无阻塞项；批量删除 `clientIds` 是否首版必做：建议首版支持（C7JsonTable 多选删除更顺）
