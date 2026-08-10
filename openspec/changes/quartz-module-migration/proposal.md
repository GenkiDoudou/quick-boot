## Why

监控侧缺少可管理的 Quartz 定时任务与调度日志：bak 已有完整实现（`quickboot-tools` / `monitor.job`），但现网尚未迁入，且需按 Modulith 新域模板落地独立模块，而非塞进 `module-system`。

## What Changes

- 新建 Maven / Modulith 模块 **`quickboot-module-quartz`**（包根 `io.github.genkidoudou.quartz`，`api` / `internal`）。
- 迁入 bak 调度能力并适配现网：`sys_job` / `sys_job_log` 管理 API、Quartz JDBC JobStore 集群、`ITask` + Bean 名调用、`QcDemoTask`。
- Flyway：业务表、`QRTZ_*`、字典、监控菜单与管理员授权。
- 前端：定时任务 / 调度日志页面与 API；Cron 组件复用或迁入。
- 导出改为同步 `ExcelUtils`（对齐 operlog）；**不迁**异步导出中心。
- **BREAKING（相对 bak）**：包名改为 `io.github.genkidoudou.quartz.*`；去掉 `BizExportHandler`；模块独立，不依赖 `module-system`。

权威产品设计：`docs/superpowers/specs/2026-08-08-quartz-module-migration-design.md`。

## Capabilities

### New Capabilities

- `monitor-job`: 定时任务 CRUD、启停、立即执行、调用目标列表、同步导出；Quartz 调度与 `ITask` 执行模型。
- `monitor-job-log`: 调度日志查询、详情、删除、清空、同步导出。
- `maven-module-quartz`: `quickboot-module-quartz` 脚手架、Modulith 边界、`app` 依赖与基包注册。

### Modified Capabilities

- （无既有主 specs 能力需改写；Modulith 基包列表实现时扩展，不另立 delta。）

## Impact

- 后端：新建 `quickboot-module-quartz`；父 POM / `quickboot-app` 依赖；`ApplicationModuleSourceFactory` 追加 `io.github.genkidoudou.quartz`；Flyway 新迁移；依赖 `spring-boot-starter-quartz`。
- 前端：`quick-ui` 增加 `monitor/job`、`monitor/job-log` 及 API；可能迁入/复用 `Crontab`、`JobFormDialog`。
- 库表：`sys_job`、`sys_job_log`、全套 `QRTZ_*`；字典与菜单（建议 `menu_id` 2130+，挂在监控 `2100` 下）。
- 错误码：复用已有 `ErrorCodes.Job`（20020–20023）。
