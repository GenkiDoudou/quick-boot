# 定时任务设计文档

## 1. 背景与目标

在 `quickboot` + `quick-ui` 中实现《定时任务需求文档》所述能力：管理调度任务与执行日志，支持任务增删改查、状态切换、立即执行、Cron 表达式生成器回填、日志审计与导出。

- **调度引擎**：Quartz + **JDBC JobStore** + **集群**（`QRTZ_` 表前缀）。
- **执行模型**：`invoke_target` 为 Spring **Bean 名称**；目标 Bean 须实现 **`ITask.execute(String params)`**（延续 `原始需求/old/quick-boot`，非若依反射串）。
- **路径与权限**以需求文档为真源：`/monitor/job*`、`/monitor/jobLog*`；`monitor:job:*` 及日志侧 `monitor:job:query|remove|export`。

## 2. 已确认决策摘要

| 项 | 决策 |
|----|------|
| 调用目标 | Bean 名 + `ITask`；保存/修改时校验 Bean 存在且类型正确 |
| 任务参数 | 独立表字段 **`params`**（扩展列，与若依核心列并存） |
| Quartz | JDBC `LocalDataSourceJobStore`，`isClustered=true`，`tablePrefix=QRTZ_` |
| 表/字典 | 对齐若依 `sys_job` / `sys_job_log` 语义；主键 `job_id` / `job_log_id` |
| HTTP 动词 | 与 `logininfor` / `operlog` 一致：列表/详情 **GET**；改删/改状态/执行/导出/清空 **POST**（与需求字面 `PUT/DELETE` 不一致，以本设计为准） |
| 并发策略 | `concurrent`：**`0` 允许** / **`1` 禁止**（修正旧栈 `"Y"` 误判） |
| 错失策略 | `misfire_policy`：`0` 默认 / `1` 立即 / `2` 一次 / `3` 放弃 |
| 任务状态 | `status`：`0` 正常 / `1` 暂停；**新增默认暂停**（`1`），注册前须显式启用 |
| Cron UI | 引入 Vue3 兼容 Cron 组件（如 vcrontab 类库）弹窗回填 |
| 日志页 | 独立路由 `/monitor/job-log`，支持 query 预填 `jobName` / `jobGroup` |
| 写日志失败 | 不得影响 Quartz 调度线程外的业务；写库异常仅记录 error 日志 |

## 3. 范围与非范围

### 3.1 范围

- Flyway：`sys_job`、`sys_job_log`、`QRTZ_*` 建表、字典种子、菜单与管理员角色授权（建议单文件如 `V19__sys_job.sql`，序号以实现时仓库最新迁移为准）。
- 后端：`io.github.genkidoudou.web.monitor.job` 包；Quartz `ScheduleConfig`、`ScheduleUtils`、`AbstractQuartzJob`、`ITask`、启动加载 `CommandLineRunner`；可选示例 Bean `qcDemoTask`。
- 前端：任务列表/表单、日志列表/详情、API、菜单权限、Cron 生成器。

### 3.2 非范围（本期不做）

- 若依式 `bean.method('arg')` 反射调用与白名单解析。
- 远程/RMI/HTTP 调度、多租户调度隔离、日志异步批量落库。
- 替代全局链路追踪或操作日志采集机制。

## 4. 数据模型

### 4.1 `sys_job`

| 字段 | 说明 |
|------|------|
| `job_id` | 主键（BIGINT，与项目现有分配策略一致） |
| `job_name` | 任务名称 |
| `job_group` | 任务组名（字典 `sys_job_group`） |
| `invoke_target` | 调用目标字符串（**Spring Bean 名**） |
| `cron_expression` | Cron 表达式 |
| `misfire_policy` | 错失执行策略（字典 `sys_job_misfire_policy`） |
| `concurrent` | 是否并发：`0` 允许 / `1` 禁止 |
| `status` | `0` 正常 / `1` 暂停（字典 `sys_job_status`） |
| `params` | **项目扩展**：执行参数，可空 |
| `create_by` / `create_time` / `update_by` / `update_time` / `remark` | 审计字段 |

建议索引：`job_name`、`job_group`、`status`。

### 4.2 `sys_job_log`

| 字段 | 说明 |
|------|------|
| `job_log_id` | 主键 |
| `job_id` | 关联任务 ID（任务删除后仍保留日志，可空） |
| `job_name` / `job_group` / `invoke_target` | 执行快照 |
| `job_message` | 日志信息（成功简述；失败可为固定文案） |
| `status` | `0` 成功 / `1` 失败（复用 `sys_common_status`） |
| `exception_info` | 异常信息（截断上限 **2000** 字符） |
| `create_time` | 执行时间（列表「执行时间」列） |

可选扩展列：`params` 快照，便于审计（实现计划可选）。

### 4.3 Quartz 表（`QRTZ_`）

- Flyway 引入 Quartz 官方 JDBC 建表脚本（MySQL 方言），与 `ScheduleConfig` 中 `tablePrefix=QRTZ_` 一致。
- 集群：`org.quartz.jobStore.isClustered=true`，`instanceId=AUTO`，`clusterCheckinInterval=15000`。

### 4.4 字典与菜单

**字典类型（示例）**

| dict_type | 用途 |
|-----------|------|
| `sys_job_group` | 任务组（DEFAULT 等） |
| `sys_job_status` | 任务状态 正常/暂停 |
| `sys_job_misfire_policy` | 错失策略 |
| `sys_common_status` | 日志执行成功/失败（复用） |

**菜单（挂载监控父菜单 `parent_id=2000`，`menu_id` 与 V16/V17/V18 错开）**

- 目录/菜单「定时任务」：`path=job`，`component=monitor/job/index`，`perms=monitor:job:list`。
- 按钮：`add` / `edit` / `remove` / `export` / `query` / `changeStatus`（与需求一致）。
- 「调度日志」：`path=job-log`，`component=monitor/job-log/index`，列表权限 `monitor:job:query`；按钮 `remove`、`export`。
- 超级管理员 `role_id=1` 授权上述菜单。

## 5. 调度与执行

### 5.1 组件职责

| 组件 | 职责 |
|------|------|
| `ScheduleConfig` | 注册 `SchedulerFactoryBean`、数据源 JobStore、集群与线程池参数 |
| `ScheduleUtils` | 创建/更新/暂停/删除 Job；按 `concurrent` 选择 Job 实现类 |
| `QuartzJobExecution` | 允许并发执行 |
| `QuartzDisallowConcurrentExecution` | 禁止并发（`@DisallowConcurrentExecution`） |
| `AbstractQuartzJob` | 统一 before/after、写 `sys_job_log` |
| `JobScheduleRunner` | 应用启动后加载 `status=0` 的任务到 Scheduler |
| `ITask` | `void execute(String params)` 业务契约 |

### 5.2 执行流程

1. Quartz 触发 → `AbstractQuartzJob.execute`。
2. 从 `JobDataMap` 还原任务快照（含 `params`）。
3. `ApplicationContext.getBean(invokeTarget)` → 强转 `ITask` → `execute(params)`。
4. `after`：写 `sys_job_log`（成功 `status=0`；异常 `status=1` + `exception_info`）。
5. 写库 `try/catch`：失败仅打日志，不向 Quartz 抛业务异常。

### 5.3 任务生命周期

| 操作 | 行为 |
|------|------|
| 新增 | 校验 Cron、Bean/`ITask`；默认 `status=1`；落库后 **不** 自动注册（或注册为暂停，与若依一致） |
| 修改 | 校验通过后更新库表；若原任务已注册则 `updateScheduleJob` |
| 删除 | 先 `scheduler.deleteJob`，再删库表；支持批量 |
| 改状态 | `0`→恢复调度 / `1`→暂停；库表与 Scheduler 同步 |
| 立即执行 | `triggerJob(jobKey, dataMap)`；仍写调度日志 |

### 5.4 校验规则

- `CronUtils.isValid(cronExpression)`（基于 `org.quartz.CronExpression`）。
- `invoke_target` 非空；Bean 必须存在且 `instanceof ITask`。
- 禁止将非 `ITask` 的通用 Service 作为目标（防止误调用）。

### 5.5 示例任务

- `@Component("qcDemoTask")` 实现 `ITask`，`execute` 内打 info 日志。
- Flyway **可选**插入一条演示任务（默认暂停），便于联调；生产环境可仅保留 Bean 文档说明。

## 6. 后端接口契约

包路径：`io.github.genkidoudou.web.monitor.job`（任务）、`...joblog` 或同包 `log` 子包（日志），实现阶段二选一保持清晰。

注解：`@Tag`、`@Operation`、`@Parameter`、`@SaCheckPermission`；Jakarta Validation；业务失败使用 `WarningException` 等，**禁止** `IllegalArgumentException` 作为业务信号。

依赖：`quickboot-web` 增加 `spring-boot-starter-quartz`。

### 6.1 任务 `/monitor/job`

| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| GET | `/list` | `monitor:job:list` | 分页；条件：任务名称、组名、状态 |
| GET | `/{jobId}` | `monitor:job:query` | 详情 |
| POST | `/` | `monitor:job:add` | 新增 |
| POST | `/edit` | `monitor:job:edit` | 修改 |
| POST | `/remove` | `monitor:job:remove` | body：`jobIds`（`Long[]`） |
| POST | `/changeStatus` | `monitor:job:changeStatus` | body：`jobId`、`status` |
| POST | `/run` | `monitor:job:changeStatus` | body：`jobId`；返回明确成功文案（满足「立即执行成功反馈」） |
| POST | `/export` | `monitor:job:export` | Excel；筛选与列表一致；行数上限默认 **10000** |

### 6.2 日志 `/monitor/jobLog`

| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| GET | `/list` | `monitor:job:query` | 分页；任务名、组名、状态、执行时间区间 |
| GET | `/{jobLogId}` | `monitor:job:query` | 详情 |
| POST | `/remove` | `monitor:job:remove` | body：`jobLogIds` |
| POST | `/clean` | `monitor:job:remove` | 清空 |
| POST | `/export` | `monitor:job:export` | 导出 |

**说明**：需求文档日志路径写作 `jobLog`（驼峰），与若依 `/monitor/jobLog` 一致；前端 API 模块与之对齐。

## 7. 前端（`quick-ui`）

实现前须阅读 `DESIGN.md`、`sdd/前端代码规范.md`（见 `AGENTS.md`）。

### 7.1 任务页 `views/monitor/job/index.vue`

- `C7JsonTable`：查询（任务名称、组名、状态）；列（编号、名称、组、cron、调用目标、状态）。
- 工具栏：新增、修改、删除、导出；跳转「调度日志」。
- 行操作：详情、修改、删除、**立即执行**、**查看日志**（带 `jobName`、`jobGroup` query）。
- 状态列：`el-switch`，`active-value="0"`、`inactive-value="1"`；`changeStatus` 失败时 **回滚** switch（验收硬要求）。
- 表单弹窗：必填 `jobName`、`invokeTarget`、`cronExpression`；`jobGroup`、`misfirePolicy`、`concurrent`、`status`、`params`、`remark`；`invokeTarget` 旁 tooltip 说明须为 `ITask` Bean 名。

### 7.2 Cron 生成器

- 依赖 Vue3 + Element Plus 兼容的 Cron 组件（实现阶段选定具体包，如 `@vcrontab/vue3` 或社区等价物）。
- 弹窗选择后回填 `cronExpression`；关闭不污染原值。

### 7.3 日志页 `views/monitor/job-log/index.vue`

- 独立路由；`onMounted` 读取 `route.query.jobName`、`jobGroup` 作为默认筛选。
- 列：日志编号、任务名称、组、调用目标、日志信息、执行状态、执行时间。
- 操作：详情、删除、清空、导出、返回任务页。

### 7.4 API 与权限

- `api/monitor/job.js`、`api/monitor/jobLog.js`。
- `v-hasPermi` 与菜单 `perms` 一致。

## 8. 测试与验收

| 验收项（需求） | 验证方式 |
|----------------|----------|
| 状态切换失败回滚开关 | 模拟 `changeStatus` 失败，断言 switch 恢复原值 |
| 立即执行成功反馈 | `POST /run` 成功 + 前端 `ElMessage.success` |
| 任务页进日志带筛选 | 跳转 query 生效且列表首查命中 |
| Cron 非法 | 前后端拒绝保存 |
| 调度写日志 | Cron 触发与立即执行均产生 `sys_job_log` 记录 |
| Bean 校验 | 非 `ITask` Bean 保存失败并提示 |

建议联调顺序：Flyway → 启动无报错 → 新增 demo 任务 → 启用 → 立即执行 → 查日志 → 导出。

## 9. 后续步骤

在单独会话中按 **writing-plans** 产出 `docs/superpowers/plans/2026-05-16-job.md` 的实现任务清单后再编码。可选同步创建 OpenSpec change（如 `add-monitor-job`）供 `/opsx:apply` 跟踪。

---

**文档版本**：2026-05-16  
**关联需求**：`原始需求/系统管理/定时任务-需求文档.md`  
**关联设计**：`docs/superpowers/specs/2026-05-16-logininfor-design.md`（监控模块与 POST 约定）、`docs/superpowers/specs/2026-05-16-operlog-design.md`（列表/导出风格）  
**参考实现**：`原始需求/old/quick-boot/quick-boot-system/.../quartz/`（`ScheduleConfig`、`AbstractQuartzJob`、`ITask`）
