## Context

`quickboot2` 尚无定时任务表、Quartz 集成与管理端。需求见 `原始需求/系统管理/定时任务-需求文档.md`。工程内设计真源为 `docs/superpowers/specs/2026-05-16-job-design.md`（brainstorming 已确认：Bean 名 + `ITask`、JDBC 集群 JobStore、POST 化 API、独立日志路由、若依表语义 + `params` 扩展列）。可参考 `原始需求/old/quick-boot/quick-boot-system/.../quartz/` 迁移调度骨架。

## Goals / Non-Goals

**Goals:**

- 持久化若依语义 `sys_job` / `sys_job_log`（含扩展 `params`），Quartz `QRTZ_*` 表与集群 JobStore。
- `ITask` 执行契约：保存/修改校验 Bean 存在且类型正确；Cron 校验；新增默认 **暂停**。
- 管理 API：`GET` 列表/详情；`POST` 增改删、改状态、立即执行、导出、清空；权限与 OpenAPI 齐全。
- `quick-ui` 任务页 + 独立日志页 + Cron 可视化生成器；状态 switch 失败回滚。

**Non-Goals:**

- 若依 `bean.method('arg')` 反射调用、远程调度、多租户隔离。
- 日志异步批量写入、替代操作日志/链路追踪机制。

## Decisions

| 决策 | 选项 | 理由 |
|------|------|------|
| 执行模型 | **Bean 名 + `ITask`** | 已确认 1A；比反射串安全、与旧栈一致 |
| JobStore | **JDBC + 集群 `QRTZ_`** | 已确认 2B；多实例不重复触发 |
| HTTP 动词 | **POST 表达写操作** | 与 `logininfor`/`operlog` 及 `AGENTS.md` 一致 |
| 并发字段 | **`0` 允许 / `1` 禁止** | 若依语义；修正旧栈 `"Y"` 误判 |
| 模块放置 | **`quickboot-web`…`monitor.job`** + `quartz` 子包 | 与现有监控模块并列；调度配置同模块 |
| Cron UI | **Vue3 Cron 组件**（如 vcrontab） | 已确认 5A；满足需求「生成器回填」 |
| 示例任务 | **`@Component("qcDemoTask")`** | 联调与文档；Flyway 种子可选且默认暂停 |
| 立即执行权限 | 复用 **`monitor:job:changeStatus`** | 与若依习惯一致，减少权限碎片 |
| 导出上限 | 默认 **10000** 行 | 与 operlog 一致，实现阶段可配置键 |

## Risks / Trade-offs

| 风险 | 缓解 |
|------|------|
| Quartz 表与 Flyway 体积大 | 单迁移文件或拆分 `V19`/`V20`；使用官方 MySQL 脚本 |
| 集群配置错误导致双触发或不触发 | 文档注明 `isClustered`、数据源与 `QRTZ_` 前缀；联调 checklist |
| 任意 Bean 名误配 | 保存时强制 `instanceof ITask` |
| 写日志失败影响调度 | `AbstractQuartzJob.after` 内 `try/catch`，仅 error 日志 |
| `menu_id`/Flyway 版本冲突 | 实现前 glob 最大 `V*` 与菜单 id，顺延（建议 job 菜单 2240+） |

## Migration Plan

1. 部署前执行 Flyway：创建 `sys_job`、`sys_job_log`、`QRTZ_*`、字典、菜单。
2. 应用启动：`JobScheduleRunner` 加载 `status=0` 任务注册到 Scheduler。
3. 回滚：保留迁移文件；紧急可停应用并清理 `QRTZ_*`（生产以运维策略为准）；业务表可 `TRUNCATE`。

## Open Questions

- （无）Cron 组件具体 npm 包在实现任务 6.x 中选定；Flyway 版本号以仓库当时最大 `V*` +1 为准。
