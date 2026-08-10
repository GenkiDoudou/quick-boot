## ADDED Requirements

### Requirement: Query job execution logs
系统 SHALL 提供调度日志管理 API（路径前缀 `/monitor/jobLog`），支持分页列表与详情查询。

#### Scenario: List job logs
- **WHEN** 客户端以 GET 请求调度日志列表（含筛选，可按任务名/组等）
- **THEN** 系统返回 `R` 包装的分页日志数据

#### Scenario: Get job log detail
- **WHEN** 客户端按日志 ID 查询详情
- **THEN** 系统返回该次执行的快照字段（含状态、消息、异常信息等）

### Requirement: Persist execution outcome
每次任务执行完成后，系统 SHALL 写入 `sys_job_log`；写日志失败 MUST NOT 导致调度线程外的业务失败被掩盖为成功的调度中断（写库异常仅记录 error 日志）。

#### Scenario: Successful run writes success log
- **WHEN** `ITask.execute` 正常返回
- **THEN** 产生状态为成功的调度日志记录

#### Scenario: Failed run writes failure log
- **WHEN** `ITask.execute` 抛出异常
- **THEN** 产生状态为失败的调度日志，并保存截断后的异常信息

### Requirement: Delete and clean job logs
系统 SHALL 支持调度日志批量删除与清空。

#### Scenario: Remove selected logs
- **WHEN** 管理员提交待删除的日志 ID 列表
- **THEN** 对应日志记录被删除

#### Scenario: Clean all logs
- **WHEN** 管理员发起清空调度日志
- **THEN** 日志表中记录被清空（或按实现约定的清空语义全部移除）

### Requirement: Sync export job logs
系统 SHALL 提供调度日志同步 Excel 导出，使用 `ExcelUtils`，不得依赖异步导出中心。

#### Scenario: Export job logs downloads excel
- **WHEN** 管理员发起调度日志导出
- **THEN** 响应为可下载的 Excel 文件流

### Requirement: Job log permissions
调度日志查询/删除/导出 SHALL 受权限控制（与 bak/菜单约定对齐，可复用 `monitor:job:query|remove|export`）。

#### Scenario: Unauthorized log export denied
- **WHEN** 无导出权限的用户调用日志导出接口
- **THEN** 系统拒绝该请求
