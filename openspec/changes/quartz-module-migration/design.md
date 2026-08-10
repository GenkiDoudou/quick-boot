## Context

权威产品设计见 `docs/superpowers/specs/2026-08-08-quartz-module-migration-design.md`。本文件为 OpenSpec 实现向设计。

现状：现网已完成 Modulith 分层（`common` / `core` / `module-system` / `app`），监控已有 operlog / logininfor；定时任务仍在 `bak/quickboot/quickboot-tools/.../monitor/job`，未迁入。`ErrorCodes.Job` 已预留。异步导出中心（`BizExportHandler`）现网不存在。

约束：对齐新域模板；包根 `io.github.genkidoudou.quartz`；不依赖 `module-system`；导出用同步 Excel；保留 bak 调度语义。

## Goals / Non-Goals

**Goals:**

1. 新建 `quickboot-module-quartz` 并注册到父反应器与 `app`。
2. 迁入任务/日志管理、Quartz JDBC 集群调度、`ITask` 执行模型与 `QcDemoTask`。
3. Flyway 建表、字典、菜单（挂监控 `2100`，`menu_id` 建议 2130+）与管理员授权。
4. 前端任务/日志页 + 同步导出；Modulith `verify()` 通过。

**Non-Goals:**

- 异步导出中心；若依反射调用串；远程调度；把 job 并入 `module-system`。
- 本期不强制 Job CRUD Facade（`api` 仅公开 `ITask`）。

## Decisions

### 1. 独立 Maven / Modulith 模块（非塞进 system）

- `quickboot-module-quartz` ↔ Application Module `quartz` 一对一。
- 备选：放进 `module-system` → 否决（违背新域模板、继续膨胀 system）。

### 2. 包与公开面

```text
io.github.genkidoudou.quartz/
  api/ITask (+ package-info @NamedInterface)
  internal/{controller,service,mapper,entity,dto,quartz,support,task,config}
```

- Controller 在 `internal`；`ApplicationModuleSourceFactory` 追加基包。
- 默认不依赖 `module-system`。

### 3. 从 bak 迁入并改导出

- 行为契约对齐 bak job 设计：Bean 名 + `ITask`；并发/错失/状态字典语义；新增默认暂停。
- **删除** `JobBizExportHandler` / `JobLogBizExportHandler`；`POST …/export` → `ExcelUtils.exportExcel`。
- HTTP 动词对齐现网 monitor：列表/详情 GET；改删/启停/执行/导出/清空 POST。

### 4. Flyway 与前端

- 单文件迁移（建议 `V11__sys_job_quartz.sql`，实现时按最新 `V*` 顺延）：`sys_job` / `sys_job_log` / `QRTZ_*` / 字典 / 菜单 / `sys_role_menu`。
- 前端从 bak 迁并对齐 operlog 交互；动态菜单为主；Cron 组件现网有则复用，缺则迁。

### 5. Quartz 依赖

- 使用 Spring Boot BOM 管理的 `spring-boot-starter-quartz`；JDBC JobStore + `isClustered=true` + `tablePrefix=QRTZ_`；布尔列沿用 bak `VARCHAR(12)` 以兼容 H2/MySQL。

## Risks / Trade-offs

- [Boot 4 × Quartz 版本] → 走 BOM；编译失败再钉版本。
- [Modulith 未注册基包] → 与脚手架同一变更改 SourceFactory。
- [菜单 ID 冲突] → 使用 2130+ + `WHERE NOT EXISTS`。
- [H2 JobStore] → 沿用 bak DDL/Delegate 兼容写法。

## Migration Plan

1. POM 脚手架 + Modulith `package-info` + app 依赖 + 基包。
2. Flyway。
3. 后端迁入、导出改造、Demo Task、单测。
4. `verify()` + 编译。
5. 前端 API/页面。
6. 冒烟：启停、立即执行、日志、导出。

回滚：删模块与依赖、Flyway 文件（已执行环境清表）、前端文件；以 Git 为准。

## Open Questions

- Flyway 最终序号、菜单精确 ID 列表：实现时核对现网后写入 tasks 勾选说明即可（建议段 2130–2149）。
- DTO 命名以现网 monitor 控制器为准微调，不扩大行为。
