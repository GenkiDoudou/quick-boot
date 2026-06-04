## Why

当前 Excel 导入采用单次 HTTP 同步处理，前端 axios 全局超时仅 10s，大批量导入时页面易超时且无法查看进度；后端部分模块全量读入内存或整表大事务，进一步拉长请求时间。`原始需求/需求.md` 已要求区分实时/异步导入并建设导入导出中心，需在平台层统一编排、暂存与文件管理能力。

## What Changes

- 新增 **导入编排层（Import Orchestrator）**：混合阈值（默认同步 500 行）、异步任务、暂存表两阶段流水线（LOAD → PROCESS）。
- 新增数据库表 **`sys_import_task`**、**`sys_import_staging_row`** 及 Flyway 迁移、菜单权限种子。
- 原始 Excel 与失败明细 xlsx **统一经文件管理**（`import/source`、`import/error` classify）存储，任务表关联 `source_file_id` / `error_file_id`。
- 新增导入 API：`POST /import/submit`、`GET /import/task/{taskId}`、`GET /import/task/list`；失败文件下载复用 `GET /system/file/download/{fileId}`。
- 新增前端 **「导入导出中心」** 页面（导入任务 Tab + 导出记录 Tab）。
- 扩展 **`C7ExcelUpload`**：支持 `syncMaxRows`、`forceAsync`、异步轮询与跳转导入中心。
- P0 止血：导入专用 `importRequest` 超时（不提高全局 10s）；同步路径性能优化（流式读、拆大事务）。
- 业务模块（用户/角色/字典等）逐步接入 Orchestrator；旧 `importData` 委托或保留兼容。

## Capabilities

### New Capabilities

- `platform-import-orchestrator`：导入任务、暂存行、编排器、BizImportHandler 注册、异步执行与配置项 `qc.import.*`
- `import-export-center`：导入导出中心管理端（列表、详情、进度、失败文件下载入口）

### Modified Capabilities

- `common-file-storage`：新增 `import/source`、`import/error` 分类配置及导入场景上传约束说明
- `ui-c7-excel-upload`（变更 delta）：异步导入模式、`taskId` 轮询、结果展示与导入中心跳转（主规范见 `openspec/changes/ui-c7-excel-upload`，本变更以 delta 扩展）

## Impact

- **后端**：`quickboot-common`（配置、编排接口）、`quickboot-system` 或独立 import 模块（Controller/Service/Mapper/Handler）、`quickboot-tools`（Quartz 导入任务类型）、Flyway 迁移。
- **前端**：`quick-ui` 新增 `importRequest`、`views` 导入导出中心、`C7ExcelUpload` 扩展、各业务 `uploadFn` 适配。
- **配置**：`application.yml` 增加 `qc.import.*`；`qc.file.classifies` 增加 `import/source`、`import/error`。
- **依赖**：依赖已落地的 `system-file-management`（`sys_file`、文件上传下载）。
- **文档**：与 `sdd/Excel导入大批量超时处理设计.md` 对齐。
