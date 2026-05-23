## Why

系统需要在线管理调度任务与执行日志（增删改查、启停、立即执行、Cron 配置、审计导出），当前 `quickboot` 无 `sys_job` / `sys_job_log`、无 Quartz 调度与管理端，无法满足《定时任务需求文档》与运维调度诉求。

## What Changes

- 新增 Flyway：`sys_job`、`sys_job_log`、`QRTZ_*` Quartz JDBC 表、相关字典与菜单授权。
- 引入 **Quartz**（JDBC JobStore + 集群）：启动加载任务、Cron 调度、立即执行、错失/并发策略；执行目标为 Spring **Bean 名** + **`ITask.execute(params)`**（非若依反射串）。
- 新增监控接口：`/monitor/job/*`（任务 CRUD、改状态、立即执行、导出）、`/monitor/jobLog/*`（日志列表/详情、删除、清空、导出）；权限 `monitor:job:*`；改删/改状态/执行/导出/清空使用 **POST**（与仓库约定一致，与需求字面 `PUT/DELETE` 不同）。
- `quick-ui`：定时任务列表/表单（含 Cron 生成器）、独立调度日志页、API 与菜单；状态开关失败时回滚。

## Capabilities

### New Capabilities

- `monitor-job`：涵盖任务与调度日志持久化、Quartz 调度引擎集成、`ITask` 执行契约、监控侧 REST、内置示例任务 Bean 及前端页面与权限。

### Modified Capabilities

- （无）不修改 `openspec/specs/` 下既有 capability 需求正文。

## Impact

- **后端**：`quickboot-web` 新增 `monitor/job`（及日志子包）；`spring-boot-starter-quartz`；Flyway `V19__sys_job.sql`（序号以实现时最大 `V*` 为准）；参考 `原始需求/old/quick-boot/.../quartz/` 迁移调度工具类。
- **前端**：`quick-ui` 新增 `api/monitor/job`、`api/monitor/jobLog`、`views/monitor/job`、`views/monitor/job-log`、Cron 组件依赖。
- **数据库**：业务表 + 11 张 Quartz 表；菜单 `menu_id` 与 V16/V17/V18 错开。
- **设计真源**：`docs/superpowers/specs/2026-05-16-job-design.md`
