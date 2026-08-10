## ADDED Requirements

### Requirement: Slow SQL capture and persist
系统 SHALL 在启用 `qc.monitor.slow-sql.capture-enabled` 时，于 JDBC/Druid 层采集超过阈值的 SQL，并写入 `sys_slow_sql`（可异步）；MUST 忽略命中 `ignore-sql-contains` 的语句以避免自写入递归。

#### Scenario: Slow query is persisted
- **WHEN** 业务 SQL 执行耗时超过配置阈值且采集开关开启
- **THEN** `sys_slow_sql` 新增一条记录，包含 sql 文本摘要、耗时、来源/类型及可选请求关联字段

#### Scenario: Capture disabled
- **WHEN** `qc.monitor.slow-sql.capture-enabled` 为 false
- **THEN** 系统不因该开关关闭而新增慢 SQL 落库记录

### Requirement: Slow SQL management API
系统 SHALL 在 `/monitor/slowSql` 提供列表、详情、同步导出、批量删除与清空，并按权限字校验（`monitor:slowSql:query|export|remove`）。

#### Scenario: List and detail with permission
- **WHEN** 持有 `monitor:slowSql:query` 的用户请求列表或详情
- **THEN** 返回分页或单条慢 SQL 数据

#### Scenario: Export with row limit
- **WHEN** 持有导出权限的用户导出且结果不超过 `export-max-rows`
- **THEN** 系统以 Excel 同步下载；超过上限 MUST 拒绝并提示缩小筛选

#### Scenario: Remove and clean
- **WHEN** 持有 `monitor:slowSql:remove` 的用户批量删除或清空
- **THEN** 对应记录从 `sys_slow_sql` 移除

### Requirement: Slow SQL admin UI
系统 SHALL 提供慢 SQL 管理前端页与 API 封装，对齐现网监控页交互与权限指令。

#### Scenario: Admin can open slow SQL page
- **WHEN** 管理员打开慢 SQL 菜单页
- **THEN** 可查询列表并执行详情/导出/删除/清空（受权限控制）
