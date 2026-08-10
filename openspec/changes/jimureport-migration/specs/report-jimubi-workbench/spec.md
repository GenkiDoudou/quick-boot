## ADDED Requirements

### Requirement: BI workbench menu entry
系统 SHALL 提供「数据可视化 / BI工作台」菜单（种子 `menu_id=3002`，权限 `report:jimubi:list`），以 InnerLink iframe 打开 JimuBI 列表页。

#### Scenario: Router meta.link for BI list
- **WHEN** 已授权管理员请求动态路由且 `qc.jimu.enabled=true`
- **THEN** BI 工作台路由 `component` 为 `InnerLink`，且 `meta.link` 等于 `qc.jimu.base-url` 与菜单 `query`（`/drag/list`）的拼接（或绝对 URL 直通）

### Requirement: JimuBI path exclusions and adapters
系统 SHALL 排除 `/drag/**`、`/jimubi/**` 等 JimuBI 路径的 Sa-Token 拦截，并启用 JimuBI 所需的 Drag 外部服务与 Redis 修补配置（可按 `qc.jimu.redis.enabled` 开关）。

#### Scenario: Drag paths excluded
- **WHEN** `qc.jimu.enabled=true`
- **THEN** Sa-Token 排除列表包含 `/drag/**` 与 `/jimubi/**`

#### Scenario: Drag redis util available when enabled
- **WHEN** `qc.jimu.enabled=true`
- **THEN** 应用提供修复版 Drag Redis 工具 Bean（避免无 Redis 时计数缺陷）；若 `qc.jimu.redis.enabled=true` 则提供适合 JimuBI 的 `RedisTemplate` 配置

### Requirement: Frontend iframe token for BI
前端 SHALL 在加载 BI InnerLink iframe 时于 URL 追加当前登录 `token` 查询参数（与报表工作台同一机制）。

#### Scenario: Iframe URL contains token
- **WHEN** 用户打开 BI 工作台标签页
- **THEN** iframe 请求 URL 包含非空的 `token` 参数
