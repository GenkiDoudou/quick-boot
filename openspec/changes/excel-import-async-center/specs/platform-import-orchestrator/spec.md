## ADDED Requirements

### Requirement: 导入配置项

系统 SHALL 支持配置前缀 `qc.import`，至少包含：`sync.max-rows`（默认 **500**）、`sync.max-rows-cap`（请求覆盖硬顶，建议 **2000**）、`sync.timeout-seconds`（默认 **120**）、`async.max-concurrent`（默认 **3**）、`staging.batch-size`（默认 **200**）、`file.retention-days`（默认 **7**）。

#### Scenario: 默认同步上限为 500

- **WHEN** 未配置 `qc.import.sync.max-rows`
- **THEN** 编排器 MUST 以 **500** 作为同步与异步分流的有效默认上限

### Requirement: 同步与异步分流

系统 SHALL 在导入提交时计算有效同步上限：`effectiveMaxRows = request.syncMaxRows ?? qc.import.sync.max-rows`，且 `request.syncMaxRows` MUST NOT 大于 `qc.import.sync.max-rows-cap`。

当 `request.mode` 为 `async` 时，系统 MUST 走异步路径。否则当解析后有效行数大于 `effectiveMaxRows` 时，系统 MUST 走异步路径；否则 MUST 走同步路径。

#### Scenario: 行数未超上限走同步

- **WHEN** 有效行数为 400 且 `mode` 非 `async` 且 `effectiveMaxRows` 为 500
- **THEN** 系统 MUST 在同一 HTTP 请求内完成导入并返回统计结果

#### Scenario: 行数超限走异步

- **WHEN** 有效行数为 600 且 `mode` 非 `sync` 强制
- **THEN** 系统 MUST 创建异步任务并在响应中返回 `taskId` 与 `mode: async`，且 MUST NOT 阻塞至全部行处理完成

#### Scenario: 请求覆盖 syncMaxRows

- **WHEN** 配置 `sync.max-rows=500` 且请求 `syncMaxRows=800` 且有效行数为 700
- **THEN** 系统 MUST 按 **800** 作为本次上限并走同步路径

### Requirement: 导入任务主表

系统 SHALL 持久化导入任务至 `sys_import_task`，至少包含：`task_id`、`biz_type`、`source_file_id`、`error_file_id`（可空）、`import_mode`（`sync`/`async`）、`sync_max_rows`（本次快照）、`duplicate_strategy`、`status`、`total_rows`、`success_rows`、`fail_rows`、`processed_rows`、`error_message`、`create_by`、`create_time`、`finish_time`。

任务状态 MUST 支持：`PENDING`、`RUNNING`、`SUCCESS`、`FAILED`（仅系统级失败）。存在业务行失败时，任务状态 MUST 为 `SUCCESS` 且 `fail_rows > 0`。

#### Scenario: 异步任务创建后为 PENDING

- **WHEN** 用户提交异步导入
- **THEN** 系统 MUST 插入任务记录且 `status` 为 `PENDING`，并关联已上传的 `source_file_id`

### Requirement: 异步暂存行表

异步导入 MUST 使用 `sys_import_staging_row`，字段至少包含：`task_id`、`row_no`、`row_json`、`validate_status`（`PENDING`/`OK`/`FAIL`/`SKIPPED`）、`error_msg`、`biz_ref`（可选）。

异步执行 MUST 分两阶段：**LOAD**（解析 Excel 批量写入 staging，`validate_status=PENDING`）与 **PROCESS**（按 `biz_type` 调用 Handler 更新行状态并写业务表）。

#### Scenario: LOAD 完成后 total_rows 正确

- **WHEN** 异步任务进入 LOAD 且 Excel 含 1000 行有效数据
- **THEN** 系统 MUST 在 staging 表写入 1000 行且任务 `total_rows` 为 1000

### Requirement: 文件管理与任务关联

提交导入时，系统 MUST 通过文件管理能力将原始 Excel 上传至 classify **`import/source`** 并登记 `sys_file`，任务表 MUST 保存 `source_file_id`。

任务完成后若存在失败行，系统 MUST 生成失败明细 xlsx，上传至 classify **`import/error`** 并更新任务 `error_file_id`。

#### Scenario: 失败明细通过 fileId 下载

- **WHEN** 任务完成且 `fail_rows > 0`
- **THEN** 任务 MUST 具有非空 `error_file_id`，且该 id MUST 对应 `sys_file` 中可下载记录

### Requirement: BizImportHandler 注册

系统 SHALL 提供 `BizImportHandler` 扩展点，每个实现 MUST 声明唯一 `bizType()`（如 `system:user`）及行模型 `rowClass()`。

未注册 `biz_type` 的任务在 PROCESS 阶段 MUST 置为 `FAILED` 并写入明确 `error_message`。

#### Scenario: 已注册 Handler 处理行

- **WHEN** `biz_type` 为 `system:user` 且已注册对应 Handler
- **THEN** PROCESS 阶段 MUST 调用该 Handler 完成校验与落库，并更新 staging 行 `validate_status`

### Requirement: 导入提交与查询 API

系统 SHALL 提供：

- `POST /import/submit`：`multipart`，字段 `file`、`bizType`、`updateSupport`（布尔），可选 `mode`、`syncMaxRows`；响应含 `mode`、`taskId`（异步时）、统计字段（同步时）。
- `GET /import/task/{taskId}`：返回状态、进度（`processed_rows`/`total_rows`）、统计、`errorFileId`。
- `GET /import/task/list`：分页列表，默认仅当前用户创建的任务（管理员策略可在实现中扩展）。

失败文件下载 MUST 复用 `GET /system/file/download/{fileId}`，权限 MUST 校验任务归属或导入权限。

#### Scenario: 异步提交快速返回

- **WHEN** 用户提交 5000 行异步导入
- **THEN** HTTP 响应 MUST 在 **3 秒内** 返回且包含 `taskId`

### Requirement: 同步路径性能约束

同步路径 MUST 使用 EasyExcel 监听器流式读取，MUST NOT 使用全量 `doReadSync` 加载超大 List（用户模块迁移后）。

同步导入专用 HTTP 客户端超时 MUST 独立于全局 10s（前端 `importRequest`，见 `ui-c7-excel-upload` delta）。

#### Scenario: 500 行同步不触发默认 10s 前端超时

- **WHEN** 前端使用 `importRequest` 且后端在合理环境完成 500 行用户导入
- **THEN** 前端 MUST NOT 因默认 10000ms 全局超时失败
