## 1. 依赖与数据库

- [x] 1.1 `quickboot-web/pom.xml` 增加 `spring-boot-starter-quartz`
- [x] 1.2 新增 Flyway `V19__sys_job.sql`（版本号以仓库当前最大 `V*` +1 为准）：`sys_job`、`sys_job_log`、`QRTZ_*` 建表
- [x] 1.3 同文件或后续迁移：字典 `sys_job_group`、`sys_job_status`、`sys_job_misfire_policy`；复用/确认 `sys_common_status` 用于日志状态
- [x] 1.4 Flyway：菜单「定时任务」「调度日志」及按钮、`sys_role_menu`（`menu_id` 建议 2240+，与 V16/V17/V18 错开）

## 2. Quartz 调度内核

- [x] 2.1 新增 `ITask` 接口与 `qcDemoTask` 示例 Bean（`@Component("qcDemoTask")`）
- [x] 2.2 迁移/实现：`ScheduleConfig`、`ScheduleUtils`、`CronUtils`、`AbstractQuartzJob`、`QuartzJobExecution`、`QuartzDisallowConcurrentExecution`（`concurrent` 使用 `0`/`1`）
- [x] 2.3 实现 `JobScheduleRunner`：启动加载 `status=0` 任务
- [x] 2.4 单元测试：`CronUtils` 校验；`ScheduleUtils.getQuartzJobClass` 对 `0`/`1` 分支

## 3. 任务领域与 API

- [x] 3.1 新增 `SysJob` 实体、Mapper、QueryBo、Vo、SaveBo；主键 `job_id` BIGINT
- [x] 3.2 实现 `SysJobService`：分页、详情、新增（默认暂停）、修改、批量删除、改状态、立即执行、导出；保存时校验 Cron 与 `ITask` Bean
- [x] 3.3 新增 `SysJobController`：`GET /list`、`GET /{jobId}`、`POST /`、`POST /edit`、`POST /remove`、`POST /changeStatus`、`POST /run`、`POST /export`；OpenAPI + `@SaCheckPermission` + `@Valid`
- [x] 3.4 导出默认最大行数 10000（常量或配置项）

## 4. 调度日志领域与 API

- [x] 4.1 新增 `SysJobLog` 实体、Mapper、QueryBo、Vo
- [x] 4.2 实现 `SysJobLogService`：分页、详情、批量删除、清空、导出；`addJobLog` 供 `AbstractQuartzJob` 调用
- [x] 4.3 新增 `SysJobLogController`：`GET /monitor/jobLog/list`、`GET /{jobLogId}`、`POST /remove`、`POST /clean`、`POST /export`

## 5. 集成验证（后端）

- [x] 5.1 `mvn -pl quickboot-web -am compile`（或 `test`）通过（需在 JDK 17 环境执行）
- [x] 5.2 启动应用：Flyway 成功、Scheduler 无 JobStore 报错、加载暂停/正常任务行为符合设计（本地 dev 验证）

## 6. 前端

- [x] 6.1 安装并接入 Vue3 Cron 组件（如 vcrontab 类库）；封装 Cron 弹窗组件
- [x] 6.2 新增 `api/monitor/job.js`、`api/monitor/jobLog.js`
- [x] 6.3 新增 `views/monitor/job/index.vue`（含表单弹窗、状态 switch 失败回滚、立即执行、跳转日志）
- [x] 6.4 新增 `views/monitor/job-log/index.vue`（query 预填、返回任务页）
- [x] 6.5 注册路由/菜单 component 路径与 Flyway 一致；`v-hasPermi` 绑定权限
- [x] 6.6 `pnpm -C quick-ui build:prod` 通过

## 7. 手工验收

- [x] 7.1 新增任务（`qcDemoTask`）→ 启用 → 立即执行 → 日志页有成功记录
- [x] 7.2 非法 Cron / 非 `ITask` Bean 保存失败；状态 switch 失败回滚
- [x] 7.3 从任务页「查看日志」带 `jobName`/`jobGroup` 筛选生效；导出非空
