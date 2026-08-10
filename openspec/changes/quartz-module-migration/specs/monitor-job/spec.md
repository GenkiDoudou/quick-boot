## ADDED Requirements

### Requirement: Manage scheduled jobs
系统 SHALL 提供定时任务管理 API（路径前缀 `/monitor/job`），支持分页列表、详情、新增/修改、删除、改状态、立即执行与调用目标列表。

#### Scenario: List jobs
- **WHEN** 客户端以 GET 请求任务列表（含分页/筛选）
- **THEN** 系统返回 `R` 包装的分页任务数据

#### Scenario: Create job defaults to paused
- **WHEN** 管理员新增任务且未显式指定运行中状态
- **THEN** 任务以暂停状态（`status=1`）落库，且未开始按 Cron 调度，直至显式启用

#### Scenario: Change status enables scheduling
- **WHEN** 管理员将任务状态改为正常（`status=0`）
- **THEN** 系统在 Quartz 中注册/恢复对应触发器

#### Scenario: Run once
- **WHEN** 管理员对已存在任务发起立即执行
- **THEN** 系统触发一次该任务的执行（不永久改变其启用/暂停配置语义以外的调度定义）

### Requirement: Invoke target is Spring bean implementing ITask
系统 SHALL 仅允许将已存在的 Spring Bean 名称写入 `invoke_target`，且该 Bean 必须实现 `ITask`；保存/修改时 MUST 校验。

#### Scenario: Invalid invoke target rejected
- **WHEN** 保存任务时 `invoke_target` 对应 Bean 不存在或未实现 `ITask`
- **THEN** 系统拒绝保存并以业务错误码反馈（复用 `ErrorCodes.Job` 相关码）

#### Scenario: Demo task available
- **WHEN** 应用启动完成
- **THEN** 示例 Bean `QcDemoTask`（或约定 Bean 名）可作为可选调用目标被列出/选用

### Requirement: Quartz JDBC clustered job store
系统 SHALL 使用 Quartz JDBC JobStore，表前缀 `QRTZ_`，并启用集群模式配置（`isClustered=true`）。

#### Scenario: Scheduler uses JDBC tables
- **WHEN** 调度器启动且库中已有 `QRTZ_*` 表
- **THEN** Job/Trigger 元数据持久化到 JDBC JobStore，而非仅内存 Store

### Requirement: Concurrent and misfire semantics
系统 SHALL 按约定解释并发与错失策略字段：`concurrent` 为 `0` 允许 / `1` 禁止；`misfire_policy` 为 `0` 默认 / `1` 立即 / `2` 一次 / `3` 放弃。

#### Scenario: Disallow concurrent execution
- **WHEN** 任务 `concurrent=1` 且上一次执行尚未结束
- **THEN** Quartz 侧使用禁止并发的 Job 类型行为，避免重叠执行

### Requirement: Sync export jobs
系统 SHALL 提供任务列表同步 Excel 导出（`POST /monitor/job/export`），使用项目既有 `ExcelUtils`，不得依赖异步导出中心。

#### Scenario: Export downloads excel
- **WHEN** 管理员发起任务导出
- **THEN** 响应为可下载的 Excel 文件流

### Requirement: Job permissions
任务管理操作 SHALL 受权限控制（如 `monitor:job:list|query|add|edit|remove|export|changeStatus` 等，与菜单种子一致）。

#### Scenario: Unauthorized denied
- **WHEN** 无相应权限的用户调用受保护的任务管理接口
- **THEN** 系统拒绝该请求
