## Why

平台导出编排对同步小数据也走「任务表 + 文件中心 + 二次 HTTP 下载」，与列表页「点导出即下」习惯不一致，Network 多一跳、开发与接入成本高。`sdd/Excel导出编排简化设计.md` 已给出薄编排目标，需落地 E1（后端同步直出）与 E2（前端单请求 Blob）。

## What Changes

- **E1**：`POST /export/submit` 同步分支直接写 `HttpServletResponse` 输出流，默认不写 `sys_export_task`、不上传 `sys_file`。
- **E2**：`excelExport.js` 使用 `responseType: 'blob'` + `returnBlobWithHeaders`，去掉同步路径 `downloadFile(resultFileId)`；异步解析 JSON 后提示导入导出中心。
- 配置项 `qc.export.sync-write-task`（默认 `false`）。
- **不改动** `C7ExcelDownload` 组件；编排参数与 query 规范化仍在 `C7JsonTable`。

## Capabilities

### New Capabilities

- `platform-export-thin-orchestration`：导出提交双响应形态（同步 xlsx 流 / 异步 JSON）、编排分流不变

### Modified Capabilities

- （无 delta 到既有 spec 文件；与 `excel-import-async-center` 的导入导出中心共用 UI）

## Impact

- **后端**：`ExportTaskController`、`ExportOrchestratorServiceImpl`、`QcExportProperties`
- **前端**：`api/export/task.js`、`utils/excelExport.js`
- **文档**：`sdd/Excel导出编排简化设计.md` 状态可后续标为 E1/E2 已实现
