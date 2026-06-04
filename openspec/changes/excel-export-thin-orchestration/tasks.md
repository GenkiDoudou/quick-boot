# Tasks: excel-export-thin-orchestration

## 1. 后端 E1 — 同步直出

- [x] 1.1 `QcExportProperties` 增加 `syncWriteTask`（默认 false）
- [x] 1.2 新增 `ExportSubmitOutcome` 与 `submitForResponse` 编排分支（同步写 response / 异步 JSON）
- [x] 1.3 `ExportTaskController.submit` 改为 `void` + `HttpServletResponse` 双 Content-Type
- [x] 1.4 同步路径不再 `FileTemplate.upload`；默认不插入 `sys_export_task`

## 2. 前端 E2 — 薄客户端

- [x] 2.1 `submitExport` 使用 `responseType: 'blob'` 与 `returnBlobWithHeaders: true`
- [x] 2.2 `excelExport.js`：`blobValidate` 直返 Blob；JSON 解析 `mode=async`；移除同步 `downloadFile`

## 3. 业务页迁移（export-biz-type）

- [x] 3.3 用户/角色/参数/字典类型接入 `export-biz-type` + Handler
- [x] 3.4 定时任务/调度日志/慢 SQL 接入（tools 模块 Handler）
- [x] 3.5 字典数据：补全 `dict.data` 模块 + `system:dict:data` Handler + 前端 `export-biz-type`

## 4. 验证与文档

- [ ] 4.1 各列表页手工验证：≤500 行单请求下载；>500 行异步提示
- [x] 4.2 更新 `docs/docs/backend/modules/import-export-center.md` 同步路径与 bizType 表
