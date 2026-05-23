## Why

系统需要可审计的登录行为记录（查询、排序、导出、清理），并在多次密码失败后临时锁定账号、支持管理员解锁。当前 `quickboot` 无 `sys_logininfor` 与对应接口，无法满足《登录日志需求文档》与运维审计诉求。

## What Changes

- 新增数据库表 `sys_logininfor` 及 Flyway 迁移；登录成功/失败写入日志，写库失败不得阻断登录主流程。
- 新增监控接口：`GET /monitor/logininfor/list`、`POST /monitor/logininfor/remove`、`POST /monitor/logininfor/clean`、`GET /monitor/logininfor/unlock/{userName}`、`POST /monitor/logininfor/export`；权限点 `monitor:logininfor:*`（删除/清空语义使用 **POST**，与仓库接口约定一致，与需求文档字面 `DELETE` 不同）。
- 登录失败计数与锁定状态仅通过 Spring **`CacheManager`** 维护；**禁止**业务代码直接使用 `RedisTemplate` / `RedisConnectionFactory` 操作锁定数据。
- 在 `sys_config` 中种子化三条**系统内置**参数（`config_type=1`）：`qc.login.fail-lock-enabled`、`qc.login.max-retry`、`qc.login.lock-minutes`；遵循现有参数模块「内置不可删除、仅允许改键值」规则。
- `quick-ui`：登录日志列表页、API 模块、路由与菜单授权。

## Capabilities

### New Capabilities

- `monitor-login-infor`：涵盖登录访问日志持久化、监控侧 CRUD/导出/解锁、登录链路写日志、基于 `CacheManager` 的失败锁定与解锁、内置参数配置及前端列表与权限。

### Modified Capabilities

- （无）不修改既有 OpenSpec 主规格行为；本变更通过新能力规格描述对外契约。

## Impact

- **后端**：`quickboot-web` 新增 `monitor/logininfor` 相关分层；修改 `AuthController`、`AuthLoginService`；Flyway 迁移；可选字典种子；`sys_menu`/`sys_role_menu` 种子。
- **前端**：`quick-ui` 新增 `api/monitor/logininfor`、视图与路由。
- **依赖**：Spring Cache（`CacheManager`）、现有 `SysConfigService`、Sa-Token、Excel 导出工具链；与 `common-cache` 实现共存，不要求修改其公共 API。
