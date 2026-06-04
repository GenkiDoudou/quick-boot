# platform-export-thin-orchestration

## ADDED Requirements

### Requirement: 同步导出单请求直出 Excel

当可导出行数不超过有效同步上限且未强制异步时，系统 MUST 在 `POST /export/submit` 的 HTTP 响应体中直接返回 Excel 二进制流，且 MUST NOT 要求客户端再请求 `GET /system/file/download/{fileId}` 获取同步结果。

#### Scenario: 小批量同步导出

- **WHEN** 已授权用户提交 `bizType` 与 `queryParams`，且 `countRows` ≤ 有效 `syncMaxRows`
- **THEN** 响应 `Content-Type` 为 Excel 相关类型，响应体为可下载的 xlsx 字节流
- **AND** 默认不插入带 `result_file_id` 的 `sys_export_task` 记录（`sync-write-task=false`）

### Requirement: 大批量导出异步任务

当可导出行数超过有效同步上限或 `mode=async` 时，系统 MUST 创建 `sys_export_task` 异步执行，并在提交响应中返回 JSON，包含 `mode=async` 与 `taskId`。

#### Scenario: 超阈值异步提交

- **WHEN** `countRows` 大于有效同步上限
- **THEN** HTTP 响应为 `application/json`，`data.mode` 为 `async`，`data.taskId` 非空
- **AND** 用户可在导入导出中心查看并下载完成后的结果文件

### Requirement: 导出请求携带筛选参数

`POST /export/submit` MUST 接受 `queryParams` 对象，编排层 MUST 将其 JSON 化后传给 `BizExportHandler.countRows` 与 `writeExcelBytes`，且与对应列表查询筛选语义一致。

#### Scenario: 带筛选条件导出

- **WHEN** 客户端在 `queryParams` 中传入与列表相同的筛选字段
- **THEN** 导出行集与相同筛选条件下的列表数据范围一致（受相同数据权限约束）

### Requirement: 前端编排导出薄客户端

使用 `export-biz-type` 的列表页 MUST 通过 `excelExport.runPlatformExport` 提交导出；同步成功时 MUST 仅发起一次 `POST /export/submit` 即得到 Blob；异步时 MUST 提示前往导入导出中心且 MUST NOT 将异步提示视为下载失败。

#### Scenario: C7JsonTable 平台导出

- **WHEN** 用户在有 `export-biz-type` 的 `C7JsonTable` 上点击导出
- **THEN** `exportDownloadFn` 将当前 `searchParam`（经规范化、去除分页字段）作为 `queryParams` 提交
- **AND** `C7ExcelDownload` 组件实现文件不变，仅接收 `downloadFn` 返回的 Blob 或 `{ data, headers }`
