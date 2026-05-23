## ADDED Requirements

### Requirement: 定时任务持久化模型

系统 MUST 提供 `sys_job` 表，字段语义对齐若依参考，至少包含：`job_id`、`job_name`、`job_group`、`invoke_target`、`cron_expression`、`misfire_policy`、`concurrent`、`status`、`create_by`、`create_time`、`update_by`、`update_time`、`remark`；并 MUST 包含项目扩展列 **`params`**（可空）。主键 MUST 为 BIGINT，与现有系统表主键策略一致。

#### Scenario: 表存在且可写入

- **WHEN** Flyway 迁移已执行
- **THEN** 应用可向 `sys_job` 插入一行且 `job_name`、`invoke_target`、`cron_expression` 非空语义合法

---

### Requirement: 调度日志持久化模型

系统 MUST 提供 `sys_job_log` 表，至少包含：`job_log_id`、`job_id`（可空）、`job_name`、`job_group`、`invoke_target`、`job_message`、`status`、`exception_info`、`create_time`。`status` MUST 为 `0` 成功 / `1` 失败。`exception_info` 持久化前 MUST 截断至不超过 2000 字符。

#### Scenario: 执行后写入日志

- **WHEN** 某任务被 Quartz 触发或立即执行完成
- **THEN** `sys_job_log` 中 MUST 存在对应记录且 `create_time` 非空

---

### Requirement: Quartz JDBC 集群存储

系统 MUST 使用 Quartz JDBC JobStore，表前缀 `QRTZ_`，`org.quartz.jobStore.isClustered=true`，`instanceId=AUTO`。Flyway MUST 创建 Quartz 官方 MySQL 方言所需的 `QRTZ_*` 表。

#### Scenario: 集群表就绪

- **WHEN** Flyway 迁移已执行且应用启动
- **THEN** Scheduler 工厂 MUST 能初始化且不报 JobStore 缺表错误

---

### Requirement: ITask 执行契约

`invoke_target` MUST 表示 Spring 容器中 Bean 的名称。任务执行时系统 MUST 通过 `ApplicationContext.getBean(invokeTarget)` 取得 Bean，且该 Bean MUST 实现 `ITask` 接口（`void execute(String params)`）。保存或修改任务时，若 Bean 不存在或不是 `ITask` 实例，系统 MUST 拒绝并返回业务错误。

#### Scenario: 非法调用目标被拒绝

- **WHEN** 客户端提交 `invoke_target` 指向不存在或非 `ITask` 的 Bean
- **THEN** 保存或修改 MUST 失败且不得注册到 Scheduler

#### Scenario: 合法任务执行

- **WHEN** 已启用任务被触发且 Bean 实现 `ITask`
- **THEN** 系统 MUST 调用 `execute(params)`，其中 `params` 来自 `sys_job.params`（可空字符串）

---

### Requirement: Cron 与并发策略校验

系统 MUST 使用 `org.quartz.CronExpression`（或项目封装的 `CronUtils`）校验 `cron_expression`。`concurrent` MUST 为 `0`（允许并发，使用 `QuartzJobExecution`）或 `1`（禁止并发，使用带 `@DisallowConcurrentExecution` 的 Job 类）。`misfire_policy` MUST 支持 `0`/`1`/`2`/`3` 四种若依语义并映射到 Quartz CronScheduleBuilder。

#### Scenario: 非法 Cron 被拒绝

- **WHEN** 客户端提交无效的 `cron_expression`
- **THEN** 保存或修改 MUST 失败

---

### Requirement: 任务生命周期与 Scheduler 同步

新增任务时系统 MUST 默认将 `status` 设为 `1`（暂停）。删除任务时 MUST 先从 Scheduler 移除对应 Job，再删除库表记录（支持批量）。修改 `status` 为 `0` MUST 恢复/创建调度；为 `1` MUST 暂停调度。修改任务定义后 MUST 更新 Scheduler 中已注册 Job（若存在）。

#### Scenario: 新增默认暂停

- **WHEN** 客户端成功新增任务
- **THEN** 库中 `status` MUST 为 `1` 且不得在未启用前按 Cron 自动执行

#### Scenario: 删除同步 Scheduler

- **WHEN** 客户端删除一个已注册任务
- **THEN** Scheduler 中对应 JobKey MUST 不存在且库记录已删除

---

### Requirement: 立即执行

系统 MUST 提供 `POST /monitor/job/run`，body 含 `jobId`。调用后 MUST 通过 `scheduler.triggerJob` 触发一次执行，且 MUST 写入 `sys_job_log`。成功响应 MUST 包含明确中文成功提示（满足需求验收）。

#### Scenario: 立即执行成功反馈

- **WHEN** 用户对存在且可调度的任务调用立即执行且执行链路未抛未捕获异常
- **THEN** HTTP 响应 MUST 为成功且消息表明执行已触发或已完成

---

### Requirement: 应用启动加载任务

系统 MUST 在应用启动后（如 `CommandLineRunner`）从 `sys_job` 加载所有 `status=0` 的任务并注册到 Scheduler。

#### Scenario: 启动加载正常任务

- **WHEN** 库中存在 `status=0` 且 Cron 合法的任务且应用重启
- **THEN** 该任务 MUST 在 Scheduler 中可查询到且可按 Cron 触发

---

### Requirement: 调度日志写库失败隔离

`AbstractQuartzJob`（或等价）在 `after` 阶段写入 `sys_job_log` 时，任何持久化异常 MUST 被捕获并记录 error 日志，且 MUST NOT 向 Quartz 抛出导致调度器异常退出的未处理业务异常。

#### Scenario: 写日志失败不炸调度器

- **WHEN** 插入 `sys_job_log` 抛出运行时异常
- **THEN** Quartz Job 执行 MUST 仍以框架定义方式结束，且应用进程保持可用

---

### Requirement: 任务管理 REST 契约

系统 MUST 暴露：`GET /monitor/job/list`（分页，条件：任务名称、组名、状态）、`GET /monitor/job/{jobId}`（详情）、`POST /monitor/job`（新增）、`POST /monitor/job/edit`（修改）、`POST /monitor/job/remove`（body：`jobIds`）、`POST /monitor/job/changeStatus`（body：`jobId`、`status`）、`POST /monitor/job/run`（body：`jobId`）、`POST /monitor/job/export`（Excel，筛选与列表一致）。写操作 MUST NOT 使用 `PUT`/`DELETE` 表达。权限 MUST 分别为 `monitor:job:list`、`query`、`add`、`edit`、`remove`、`export`、`changeStatus`（`run` 使用 `changeStatus`）。

#### Scenario: 删除使用 POST

- **WHEN** 客户端批量删除任务
- **THEN** HTTP 方法 MUST 为 `POST` 且路径为 `/monitor/job/remove`

---

### Requirement: 调度日志管理 REST 契约

系统 MUST 暴露：`GET /monitor/jobLog/list`（分页，条件：任务名称、组名、执行状态、执行时间区间）、`GET /monitor/jobLog/{jobLogId}`（详情）、`POST /monitor/jobLog/remove`（body：`jobLogIds`）、`POST /monitor/jobLog/clean`（清空）、`POST /monitor/jobLog/export`（导出）。列表与详情权限 MUST 使用 `monitor:job:query`；删除/清空/导出 MUST 使用 `monitor:job:remove` 或 `monitor:job:export` 与需求一致。

#### Scenario: 日志路径驼峰

- **WHEN** 客户端访问调度日志列表
- **THEN** 路径前缀 MUST 为 `/monitor/jobLog`（非 `/monitor/job-log`）

---

### Requirement: 前端定时任务页

`quick-ui` MUST 提供定时任务列表页：查询（任务名称、组名、状态）、列展示（编号、名称、组、cron、调用目标、状态）、新增/修改/删除/导出、行内详情/修改/删除/立即执行/查看日志。状态列 MUST 使用 `el-switch`（`0` 正常 / `1` 暂停）；当 `changeStatus` 请求失败时 UI MUST 回滚 switch 至操作前值。表单 MUST 含 Cron 生成器弹窗回填 `cronExpression`，并对 `invokeTarget` 提示须为 `ITask` Bean 名。权限 MUST 使用 `v-hasPermi` 绑定 `monitor:job:*`。

#### Scenario: 状态开关失败回滚

- **WHEN** 用户切换状态且后端返回业务失败
- **THEN** switch 显示值 MUST 恢复为切换前的状态

#### Scenario: 跳转日志带筛选

- **WHEN** 用户从任务行点击「查看日志」
- **THEN** 路由 MUST 导航至独立日志页且 query 含 `jobName` 与 `jobGroup` 供默认筛选

---

### Requirement: 前端调度日志页

`quick-ui` MUST 提供独立路由的调度日志列表页：读取 `route.query` 预填任务名/组；支持详情、删除、清空、导出、返回任务页；列含日志编号、任务名称、组、调用目标、日志信息、执行状态、执行时间。

#### Scenario: 日志页读取 query

- **WHEN** 用户带 `?jobName=x&jobGroup=y` 打开日志页
- **THEN** 首次列表查询 MUST 包含对应筛选条件
