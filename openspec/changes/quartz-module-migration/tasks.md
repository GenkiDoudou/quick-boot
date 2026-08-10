## 1. Maven / Modulith 脚手架

- [x] 1.1 新建 `quickboot/quickboot-module-quartz`（POM：依赖 `quickboot-core`、`spring-boot-starter-quartz`；不依赖 `module-system`）
- [x] 1.2 父 POM 注册 `<module>quickboot-module-quartz</module>`；`quickboot-app` 增加对该模块的依赖
- [x] 1.3 声明 `io.github.genkidoudou.quartz` / `api` 的 `package-info`（`@ApplicationModule`、`@NamedInterface("api")`）；`ApplicationModuleSourceFactory` 追加基包
- [x] 1.4 确认 app 组件扫描 / MapperScan 覆盖 `io.github.genkidoudou.quartz`（按现网惯例调整）

## 2. 数据与权限

- [x] 2.1 Flyway（下一可用版本，建议 `V11__sys_job_quartz.sql`）：`sys_job`、`sys_job_log`、全套 `QRTZ_*`（自 bak 抽取，布尔列 `VARCHAR(12)`）
- [x] 2.2 同迁移：字典种子（`sys_job_group` / `sys_job_status` / `sys_job_misfire_policy` / `sys_job_concurrent` 等）
- [x] 2.3 同迁移：监控下菜单+按钮（`menu_id` 建议 2130+，parent `2100`）与 `role_id=1` 授权；权限字对齐 `monitor:job:*`

## 3. 后端调度与管理 API

- [x] 3.1 迁入并改包：`api.ITask`；`internal` 下 entity/mapper/dto/service/quartz/support/config；删除异步 `*BizExportHandler`
- [x] 3.2 `ScheduleConfig` + JobStore 集群配置；启动加载/对账逻辑与 bak 行为一致（按需保留 Reconciler/Cleanup）
- [x] 3.3 `SysJobController` / `SysJobLogController`：路径 `/monitor/job*`、`/monitor/jobLog*`；动词对齐设计（列表详情 GET，写操作 POST）；`R<T>` + springdoc
- [x] 3.4 导出改为 `ExcelUtils.exportExcel`；导出方法加 `@IgnoreLogger(RESULT)`（若现网 operlog 切面已启用）
- [x] 3.5 迁入 `QcDemoTask`；`JobInvokeTargetRegistry` 可列出合法 `ITask` Bean
- [x] 3.6 迁入/改写 `CronUtilsTest`、`ScheduleUtilsTest`（优先放在 module-quartz）

## 4. 前端

- [x] 4.1 `quick-ui/src/api/monitor/job.js`、`jobLog.js`（同步导出下载，无异步导出中心）
- [x] 4.2 `views/monitor/job/index.vue`、`job-log/index.vue`；对齐现网 monitor 页交互
- [x] 4.3 复用或迁入 `Crontab` / `JobFormDialog`（现网已有则复用）

## 5. 验证

- [x] 5.1 `mvn -pl quickboot-module-quartz,quickboot-app -am test`（或等价）编译 + Modulith `verify()` 通过
- [ ] 5.2 冒烟：菜单可见；对 Demo 任务配置 Cron、启用、立即执行；调度日志有记录；任务/日志 Excel 可下载
