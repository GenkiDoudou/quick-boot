# 导入导出中心

## 概述

解决 Excel **大批量导入/导出** 时 HTTP 同步超时问题：按行数与配置在 **同步** 与 **异步任务** 间切换；异步任务在「导入导出中心」统一查看进度、下载失败明细或导出结果。

| 项 | 值 |
|----|-----|
| 导入 API | `ImportTaskController`，`/import/*` |
| 导出 API | `ExportTaskController`，`/export/*` |
| 前端中心页 | `quick-ui/src/views/system/importExportCenter/index.vue` |
| 菜单 | 系统监控 → 导入导出中心（`menu_id=2268`，`perms=system:ioCenter:list` / `system:ioCenter:submit`） |
| 表 | `sys_import_task`、`sys_import_staging_row`（V55）；`sys_export_task`（V56） |
| 设计详述 | `sdd/Excel导入大批量超时处理设计.md`（导入）、`sdd/Excel导出编排简化设计.md`（导出；E1/E2 薄编排已落地） |

## 配置

```yaml
qc:
  import:
    enabled: true
    sync-max-rows: 500          # 不超过则尝试同步
    sync-max-rows-cap: 2000
    sync-timeout-seconds: 120
    async-max-concurrent: 3
    staging-batch-size: 200
    file-retention-days: 7
    source-classify: import-source
    error-classify: import-error
  export:
    enabled: true
    sync-max-rows: 500
    sync-max-rows-cap: 5000
    sync-timeout-seconds: 120
    async-max-concurrent: 3
    async-max-rows: 50000         # 单次异步导出行数上限
    result-classify: export-result
    sync-write-task: false       # 同步不写 sys_export_task 审计
```

关闭 `qc.import.enabled` / `qc.export.enabled` 后，各业务 Controller 回退为原有同步导入/导出实现。

## 导入 API

| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| POST | `/import/submit` | `system:ioCenter:submit` | multipart：`file`、`bizType`、`updateSupport`、可选 `mode`（`sync`/`async`）、`syncMaxRows` |
| GET | `/import/task/{taskId}` | `system:ioCenter:list` | 任务详情 |
| GET | `/import/task/list` | `system:ioCenter:list` | 分页列表 |

### 已注册 `bizType`（导入 Handler）

| bizType | 说明 |
|---------|------|
| `system:user` | 用户导入 |
| `system:role` | 角色导入 |
| `system:dict:type` | 字典类型导入 |

字典**数据**页若仍走业务 Controller 直连接口，则 **未** 接入平台编排（以代码注册为准）。

## 导出 API

| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| POST | `/export/submit` | `system:ioCenter:submit` | 请求体：`bizType`、`queryParams`（列表筛选，不含分页）；**同步**响应为 xlsx 流，**异步**为 JSON `R.ok({ mode, taskId, totalRows })` |
| GET | `/export/task/{taskId}` | `system:ioCenter:list` | 任务详情 |
| GET | `/export/task/list` | `system:ioCenter:list` | 分页列表 |

### 已注册 `bizType`（导出 Handler）

| bizType | 说明 |
|---------|------|
| `system:user` | 用户管理 |
| `system:role` | 角色管理 |
| `system:config` | 参数设置 |
| `system:dict:type` | 字典类型 |
| `system:dict:data` | 字典数据（按 dictType） |
| `monitor:logininfor` | 登录日志 |
| `monitor:operlog` | 操作日志 |
| `monitor:job` | 定时任务 |
| `monitor:jobLog` | 调度日志 |
| `monitor:slowSql` | 慢 SQL |

## 前端接入（列表页）

### 导入

1. 页面提供 `importFunction(file, strategy)`，内部调用 `excelImport`（`utils/excelImport.js`）或业务 API 包装。
2. `C7JsonTable` 配置 `:import-function="importFunction"`；组件内 **统一映射** 编排结果（同步统计 / 异步 `taskId` / 失败 `fileId` 下载提示）。
3. 异步完成提示用户前往 **导入导出中心** 查看。

用户、角色、字典类型等页已按此模式接入。

### 导出

1. 推荐配置 **`export-biz-type`**（如 `monitor:logininfor`），由 `C7JsonTable` 调用 `excelExport`（`utils/excelExport.js`）。
2. 可选 **`export-query-normalizer`**：将列表 `searchParam` 转为后端所需结构；组件内会剔除 `pageNum`/`pageSize` 等分页字段。
3. 行数 ≤ `qc.export.sync-max-rows`：**一次** `POST /export/submit` 即下载 xlsx（无 `resultFileId` 二次请求）。
4. 行数超过阈值：自动异步，提示前往导入导出中心；完成后经 `result_file_id` 在中心下载（`export-result` 分类）。

仍使用仅 `exportFunction` 的页面须保证返回 **Blob**；表单 POST 导出须与 `clientSign.js` / `tansParams` 编码一致，否则易出现签名校验失败（如 `30002`）。

### 导入导出中心页

- **导入 Tab** / **导出 Tab**：分别拉取 `/import/task/list`、`/export/task/list`。
- 表格列使用 `columnType: 'slot'` + `slotName` 渲染状态、操作（下载源文件/失败明细/导出结果）。
- API：`quick-ui/src/api/import/task.js`、`api/export/task.js`。

## 任务与文件

| 表 | 关键字段 |
|----|----------|
| `sys_import_task` | `source_file_id`、`error_file_id`、`status`、`biz_type`、统计字段 |
| `sys_export_task` | `result_file_id`、`status`、`biz_type`、`query_json` |

文件 ID 均关联 [文件管理](./file-management) 的 `sys_file`。

## 相关文档

- [文件管理](./file-management)
- [监控审计](./monitor-audit)
- [C7ExcelUpload 组件](../../frontend/components/通用组件/c7-excel-upload)
- [C7ExcelDownload 组件](../../frontend/components/通用组件/c7-excel-download)
