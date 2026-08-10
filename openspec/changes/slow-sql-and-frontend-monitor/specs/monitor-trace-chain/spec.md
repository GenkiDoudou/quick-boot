## ADDED Requirements

### Requirement: Trace chain graph aggregation
系统 SHALL 在 `GET /monitor/traceChain/graph` 按查询条件聚合前端批次、操作日志与慢 SQL，返回全链路图数据；权限字为 `monitor:traceChain:query`。

#### Scenario: Graph with matching data
- **WHEN** 持有权限的用户按 operationId/时间等条件查询且存在匹配监控数据
- **THEN** 系统返回包含页面跳转、行为明细与后端资源节点的图数据

#### Scenario: No matching data
- **WHEN** 查询条件下无匹配监控批次
- **THEN** 系统以业务可理解方式拒绝或提示未找到数据（不得返回空成功误导）

### Requirement: Cross-module operlog via system API
全链路读取操作日志时，`module-monitor` MUST 通过 `system.api` 只读 Facade 查询，MUST NOT 直接依赖 system `internal` 实体或 Mapper。

#### Scenario: Operlog correlation available
- **WHEN** 前端操作已上报且后端操作日志写入了相同 `client_operation_id`
- **THEN** 全链路图中可关联到对应操作日志节点

### Requirement: Operlog client headers capture
操作日志采集 SHALL 从请求头写入 `clientOperationId`（`X-Client-Operation-Id`）与 `clientId`（`X-Client-Id`），MUST NOT 在存在有效请求头时写死为空串。

#### Scenario: Header propagated to operlog
- **WHEN** 带有 `X-Client-Operation-Id` 的已登录请求触发操作日志采集
- **THEN** 落库的 `sys_oper_log.client_operation_id` 等于该头值

### Requirement: Trace chain admin UI
系统 SHALL 提供全链路监控前端页与 API 封装。

#### Scenario: Admin opens trace chain page
- **WHEN** 管理员打开全链路监控菜单并查询
- **THEN** 页面展示聚合图（受 `monitor:traceChain:query` 控制）
