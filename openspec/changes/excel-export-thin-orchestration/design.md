# Excel 导出薄编排（E1/E2）设计

## Context

现网 `ExportOrchestratorServiceImpl.submit` 同步/异步均 `writeExcelBytes` → `FileTemplate.upload` → 返回 `resultFileId`；前端 `excelExport.js` 再 `downloadFile`。登录/操作日志已接 `export-biz-type`。

## Goals / Non-Goals

**Goals**

- 同步（≤ `qc.export.sync-max-rows`）：单次 HTTP，响应体为 xlsx。
- 异步（> 阈值）：JSON `R.ok({ mode, taskId, totalRows })`，引导导入导出中心。
- 请求携带 `queryParams`（与列表筛选一致，不含分页）。

**Non-Goals（本 change）**

- Handler 流式分页写（E6）、多 Sheet/模板契约扩展（E3）、旧 `POST /xxx/export` 委托（E3）。

## Decisions

1. **Controller 返回 `void`**，根据 `ExportSubmitOutcome` 写流或 JSON；业务失败仍抛 `WarningException` 走全局异常 → JSON。
2. **同步默认不写任务表**：`qc.export.sync-write-task=false`。
3. **前端** `submitExport` 设 `responseType: 'blob'`、`returnBlobWithHeaders: true`；`excelExport.js` 内 `blobValidate` + JSON 解析异步分支。

## API

`POST /export/submit`（JSON body，权限 `system:ioCenter:submit`）

| 分支 | Content-Type | Body |
|------|--------------|------|
| 同步成功 | `application/vnd.openxmlformats-officedocument.spreadsheetml.sheet` | 二进制 |
| 异步接受 | `application/json` | `R.ok(ExportSubmitResultVo)`，`resultFileId` 为空 |
| 失败 | `application/json` | `R.error` |

## Risks

- 同一 URL 返回 Blob/JSON：前端必须 `blob` + `blobValidate`（与 `downloadRequest` 一致）。

## 参考

`sdd/Excel导出编排简化设计.md` §5.2、§6.1、§11 E1/E2
