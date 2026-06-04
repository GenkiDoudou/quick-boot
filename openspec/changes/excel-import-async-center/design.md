## Context

- 现状：各业务 `POST .../importData` 同步处理；前端 `C7ExcelUpload` + 全局 axios 10s 超时；用户导入全量 `doReadSync` + 内存 `errorKey`；角色导入整方法 `@Transactional`。
- 已确认决策（2026-06-03，详见 `sdd/Excel导入大批量超时处理设计.md` §4.3）：
  - 同步默认上限 **500 行**，`qc.import.sync.max-rows` 可配，请求 **`syncMaxRows`** 可覆盖（硬顶 `sync.max-rows-cap` 如 2000）。
  - 异步 **必须** `sys_import_staging_row`，两阶段 LOAD → PROCESS。
  - 文件 **必须** 走 `SysFileService` / `FileAccessService`（`import/source`、`import/error`）。
  - UI **一期** 合并「导入导出中心」。
- 约束：Spring Boot 3、EasyExcel、Flyway、Sa-Token 权限、POST 语义优先、驼峰 API。

## Goals / Non-Goals

**Goals:**

- ≤500 行（可配置）同步导入不触发默认 10s 超时（配合 `importRequest` 与同步优化）。
- > 阈值或 `mode=async` 时 3s 内返回 `taskId`，后台完成解析、暂存、校验落库、失败 xlsx 入文件管理。
- 导入导出中心可查看任务进度、统计、下载失败文件。
- `BizImportHandler` 按 `bizType` 注册，用户/角色/字典等可迁移。

**Non-Goals:**

- 不做 Excel 框架替换；不在编排层实现业务批量写库策略。
- 不做分片上传、断点续传、任务取消（二期可选）。
- 同步路径默认不写 staging（可配置开启审计）。

## Decisions

### 决策 1：混合阈值 + 请求覆盖

- **选择**：有效上限 = `request.syncMaxRows ?? qc.import.sync.max-rows`（默认 500）；`mode=async` 强制异步。
- **理由**：兼顾运维配置与单次业务场景（如仅导入 800 行仍想同步需显式提高 `syncMaxRows`）。
- **备选**：仅配置文件、不可请求覆盖——灵活性不足。

### 决策 2：异步固定 staging 两阶段

- **选择**：LOAD（EasyExcel → 批量 insert `sys_import_staging_row`）→ PROCESS（按 `bizType` Handler 读 staging 校验落库）。
- **理由**：对齐 `原始需求`「先落表再按业务编码校验」；可审计、可断点续处理进度。
- **备选**：流式直写业务表——无法复核行级状态，不符合已确认决策。

### 决策 3：文件仅存 fileId

- **选择**：任务表 `source_file_id`、`error_file_id` 关联 `sys_file`；下载走文件管理 API。
- **理由**：多节点一致、统一清理与权限；废弃用户导入 JVM `errorKey`。
- **备选**：任务表存相对路径——与文件管理重复、权限分散。

### 决策 4：执行器复用 Quartz

- **选择**：导入任务类型 `IMPORT_TASK`，单任务顺序处理行，全局 `qc.import.async.max-concurrent` 限流。
- **理由**：项目已有 job 模块；长任务可观测。
- **备选**：纯 `@Async`——与现有运维习惯不一致。

### 决策 5：导入导出中心一期合并

- **选择**：单菜单「导入导出中心」，Tab 分导入任务与导出记录。
- **理由**：用户已确认与导出中心合并；减少菜单碎片。

### 决策 6：旧 API 兼容

- **选择**：保留 `POST /system/user/importData` 等，内部委托 Orchestrator；超阈值返回 `IMPORT_ASYNC_REQUIRED` + `taskId` 或自动转异步（实现时二选一并写入 spec）。
- **理由**：降低前端业务页一次性改造成本。

## Risks / Trade-offs

- **[staging 表数据量大]** → 批量 insert（`staging.batch-size`）、任务完成后按 `task_id` 清理或归档。
- **[同步仍超 nginx 超时]** → 文档约定 `proxy_read_timeout`；主推异步。
- **[Handler 迁移遗漏]** → 注册表启动校验；未注册 `bizType` 任务置 `FAILED`。
- **[重复提交]** → `uploading` 禁用 + 可选 `fileMd5+bizType+userId` 去重。

## Migration Plan

1. P0：`importRequest` + 配置项 + 同步行数校验（可先不启用异步）。
2. P1：同步性能（用户监听器、角色拆事务）。
3. P2：Flyway 表 + classify + Orchestrator 骨架 + 文件关联。
4. P3：异步 LOAD/PROCESS + Quartz + 失败 xlsx。
5. P4：导入导出中心 + `C7ExcelUpload` 扩展。
6. P5：用户/角色/字典 Handler 迁移；废弃 `importError` 内存缓存。

回滚：功能开关 `qc.import.enabled=false` 时旧 `importData` 直连原 Service（需实现保留分支）。

## Open Questions

- 旧接口超阈值时 **自动转异步** 还是返回错误码由前端二次提交？（建议：**自动转异步** 并返回 `taskId`，减少用户操作。）
