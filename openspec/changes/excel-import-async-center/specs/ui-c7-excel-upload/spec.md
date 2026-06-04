## ADDED Requirements

### Requirement: 导入专用 HTTP 超时

项目 MUST 提供 `importRequest`（或等价封装），默认超时 MUST 为 **120000ms**（可配置），且 MUST NOT 修改 `request.js` 全局 `timeout: 10000`。

所有 Excel 导入 `uploadFn` 实现 SHOULD 使用 `importRequest` 调用后端。

#### Scenario: 同步导入使用长超时

- **WHEN** 业务 `uploadFn` 通过 `importRequest` 调用 `POST /import/submit` 或兼容导入接口
- **THEN** 该请求 MUST 使用不少于 120s 的超时配置

### Requirement: 异步导入模式 props

`C7ExcelUpload` MUST 支持可选 props：

- `syncMaxRows`：传递给后端的整数，覆盖默认同步上限；
- `forceAsync`：为 true 时请求 MUST 带 `mode=async`。

`uploadFn` 的 resolve 值 MUST 允许扩展字段：`mode`（`sync`|`async`）、`taskId`（异步时必填）。

#### Scenario: 强制异步提交

- **WHEN** 用户设置 `forceAsync=true` 并确认导入
- **THEN** `uploadFn` MUST 收到后端响应且 `mode` 为 `async` 且包含 `taskId`

### Requirement: 异步结果展示与轮询

当 `uploadFn` 返回 `mode=async` 时，组件 MUST：

- 展示「已提交后台导入」类提示；
- 可选通过 prop `pollTaskFn(taskId)` 轮询直至完成，或 emit `async-submitted` 供业务跳转导入中心；
- MUST NOT 无限阻塞 `uploading` 直至后台任务结束（除非显式启用 `waitUntilComplete` 类 prop，默认 false）。

#### Scenario: 异步提交后释放 uploading

- **WHEN** 异步提交接口在 3s 内返回 `taskId`
- **THEN** 组件 MUST 在合理时间内将 `uploading` 置为 false

### Requirement: 同步结果字段兼容

同步模式下，组件 MUST 继续支持 `C7ExcelUploadResult` 的 `total`、`successCount`、`failCount`；失败文件优先使用 `errorFileId` 走文件管理下载，MAY 保留对 `errorFileUrl` / `errorKey` 的过渡兼容。

#### Scenario: 同步成功展示统计

- **WHEN** `mode=sync` 且 `failCount=0`
- **THEN** 组件 MUST emit `success` 并展示三项统计
