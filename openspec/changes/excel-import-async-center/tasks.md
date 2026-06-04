## 1. 配置与 P0 止血



- [x] 1.1 在 `application.yml` 增加 `qc.import.*` 默认项（`sync.max-rows=500` 等）及 `QcImportProperties`

- [x] 1.2 在 `qc.file.classifies` 增加 `import-source`、`import-error` 示例配置

- [x] 1.3 前端新增 `importRequest`（默认 120s），文档说明 nginx `proxy_read_timeout` 对齐要求



## 2. 数据库与权限



- [x] 2.1 Flyway：创建 `sys_import_task`、`sys_import_staging_row`（含索引与列注释）

- [x] 2.2 Flyway：导入导出中心菜单、按钮权限种子数据

- [x] 2.3 实体、Mapper、基础 CRUD（任务与 staging 行）



## 3. 编排器核心（platform-import-orchestrator）



- [x] 3.1 定义 `BizImportHandler` 接口与 Spring 注册表（按 `bizType`）

- [x] 3.2 实现 `ImportOrchestrator`：文件上传至 `import-source`、创建任务、分流逻辑（含 `syncMaxRows`/`mode`）

- [x] 3.3 实现同步路径：流式 EasyExcel + Handler 回调 + 统一 `ExcelImportResult` 响应

- [x] 3.4 实现异步 LOAD：解析写入 `sys_import_staging_row`（批量大小可配置）

- [x] 3.5 实现异步 PROCESS：按 staging 调用 Handler、更新进度与统计

- [x] 3.6 失败明细 xlsx 生成并上传 `import-error`，回写 `error_file_id`

- [x] 3.7 `ImportController`：`POST /import/submit`、`GET /import/task/{id}`、`GET /import/task/list`

- [x] 3.8 Spring `@Async` + `importTaskExecutor` 与 `Semaphore` 并发上限（等价 IMPORT_TASK）



## 4. 同步性能优化（业务迁移前）



- [x] 4.1 用户导入：`doReadSync` 改为监听器；移除或废弃内存 `importErrorCache`，改为 `error_file_id`（编排启用后走 Handler；旧 `importData` 全量读仍保留）

- [x] 4.2 角色导入：去掉方法级整表 `@Transactional`，改为行级/分批事务

- [x] 4.3 用户 Handler：预加载 `roleByKey`（`loadActiveRolesByKeyForImport`）



## 5. 业务 Handler 接入



- [x] 5.1 实现 `system:user` Handler（从 `SysUserServiceImpl#importData` 抽取）

- [x] 5.2 实现 `system:role`、`system:dict:type` 等 Handler（按优先级）

- [x] 5.3 旧业务 `POST .../import` 委托 Orchestrator；超阈值自动异步并返回 `taskId`（用户、角色、字典类型已接入）



## 6. 导入导出中心（前端）



- [x] 6.1 API：`api/import/task.js`（submit、get、list）

- [x] 6.2 页面 `views/system/importExportCenter/index.vue`（导入 Tab + 导出 Tab，遵循 DESIGN.md）

- [x] 6.3 导入详情：轮询进度、下载失败明细（`fileId`）

- [x] 6.4 导出 Tab：`sys_export_task` 列表与下载；登录/操作日志经 `exportBizType` 走 `/export/submit` 异步



## 7. C7ExcelUpload 扩展



- [x] 7.1 Props：`syncMaxRows`、`forceAsync`；emit `async-submitted`

- [x] 7.2 异步 UX：提交成功提示（用户页提示前往导入导出中心）

- [x] 7.3 业务 API 使用 `importRequest`；`C7JsonTable.importUploadFn` 统一调用 `utils/excelImport` 映射异步/失败明细（页面仅保留简单 `importFunction`）



## 8. 验证与文档



- [x] 8.1 后端：500 行同步、5000 行异步（3s 返回 taskId）集成测试或手工用例

- [x] 8.2 前端：`pnpm build:prod`；导入中心列表与下载失败文件

- [x] 8.3 更新 `sdd/Excel导入大批量超时处理设计.md` 与 VitePress 导入相关文档（若需）


