# 定时任务（Quartz）模块迁移设计

日期：2026-08-08  
状态：已定稿（待实现计划）  
来源：`bak/quickboot/quickboot-tools/.../monitor/job` + bak 设计 `2026-05-16-job-design.md`  
对齐：`2026-08-08-spring-modulith-maven-layering-design.md` 新域模板

## 1. 背景与目标

将 `bak` 中已实现的 Quartz 定时任务能力迁入现网 quickboot，并落成独立业务 Maven / Modulith 模块，补齐监控侧「定时任务 / 调度日志」前后端能力。

**目标**

1. 新建 `quickboot-module-quartz`，包根 `io.github.genkidoudou.quartz`，按 `api` / `internal` 边界落地。
2. 保留 bak 调度语义：Spring Bean 名 + `ITask`、JDBC JobStore 集群、`sys_job` / `sys_job_log` / `QRTZ_*`。
3. 管理 API、同步 Excel 导出、前端页面与菜单权限可用。
4. 迁入示例任务 `QcDemoTask`。

**非目标**

- 异步导出中心（`BizExportHandler` / exporttask）。
- 若依式 `bean.method('arg')` 反射调用。
- 远程 / RMI / HTTP 调度、多租户调度隔离。
- 将 job 塞入 `module-system`。
- 合并 `common` / `core`。

## 2. 已确认决策

| 项 | 选择 |
|----|------|
| Maven 模块 | `quickboot-module-quartz` |
| 范围 | 后端 + 前端 + Flyway |
| 改造深度 | 按 Modulith / `code_formater` 适配，非原样搬包 |
| 包根 | `io.github.genkidoudou.quartz` |
| 导出 | 同步 `ExcelUtils.exportExcel`（对齐 operlog/config） |
| 示例任务 | 迁入 `QcDemoTask` |
| 流程 | 设计文档 → 实现计划 → 编码 |

## 3. 架构

```text
quickboot-app
    ├── quickboot-module-system
    └── quickboot-module-quartz   ← 新建
              └── quickboot-core → quickboot-common
```

依赖方向：`app → module-* → core → common`。

| 项 | 约定 |
|----|------|
| Artifact / 目录 | `quickboot-module-quartz`；父 POM `<modules>` 注册；`quickboot-app` 增加依赖 |
| Modulith | `@ApplicationModule(displayName = "quartz")`；开放 `api` |
| 基包注册 | `ApplicationModuleSourceFactory#getModuleBasePackages` 追加 `io.github.genkidoudou.quartz` |
| 跨模块 | 默认 **不依赖** `module-system`；他域若需实现任务，只依赖 `quartz.api`（至少 `ITask`） |
| Controller | 留在 `internal`；`app` 不写业务 REST |
| 依赖 | `spring-boot-starter-quartz`；`quickboot-core`（传递 common） |

## 4. 包结构与迁入映射

```text
io.github.genkidoudou.quartz/
  package-info.java                 @ApplicationModule
  api/
    package-info.java               @NamedInterface("api")
    ITask.java                      执行契约（他域可实现）
  internal/
    controller/                     SysJobController、SysJobLogController
    service/ (+ impl)
    mapper/
    entity/                         SysJob、SysJobLog（原 domain）
    dto/                            *Bo / *Vo / ExcelRow
    quartz/                         ScheduleConfig、ScheduleUtils、AbstractQuartzJob…
    support/                        JobInvokeTargetRegistry
    task/                           QcDemoTask
    config/                         JobMonitorProperties
```

**从 bak 迁入（行为保留，包名改写）**

- 控制器、Service、Mapper、实体、DTO、Quartz 运行时、Registry、Demo Task、相关单测。
- HTTP：`/monitor/job*`、`/monitor/jobLog*`；权限 `monitor:job:*`（与 bak 设计一致）。
- 列表/详情 **GET**；改删 / 改状态 / 立即执行 / 导出 / 清空 **POST**。

**删除或不迁**

- `JobBizExportHandler`、`JobLogBizExportHandler` 及对 `BizExportHandler` 的依赖。

**改造**

- 导出：`POST …/export` → Service 查询列表 → `ExcelUtils.exportExcel`（可复用既有 ExcelRow / Vo）。
- 命名与分层对齐 `code_formater` 与现网 system monitor 控制器风格（`R<T>`、springdoc、`BaseBaseMapper` 等）。
- 实体布尔/是否类字段保持 bak 的 `String` + `0`/`1`（及 Quartz 表 `VARCHAR(12)`）约定，符合现网规范。

**公开 API（首批最小）**

- `ITask`：他域 Bean 实现后即可被 `invoke_target`（Bean 名）调度。
- 本期不强制抽出 Job CRUD Facade；管理能力仅本模块 HTTP 使用。

## 5. 调度语义（行为契约）

延续 bak `2026-05-16-job-design.md`，本期不变更语义：

| 项 | 约定 |
|----|------|
| 调用目标 | Spring **Bean 名称**；目标须实现 `ITask.execute(String params)` |
| 参数 | 表字段 `params`（可空） |
| Quartz | JDBC `LocalDataSourceJobStore`，`isClustered=true`，`tablePrefix=QRTZ_` |
| 并发 `concurrent` | `0` 允许 / `1` 禁止 |
| 错失 `misfire_policy` | `0` 默认 / `1` 立即 / `2` 一次 / `3` 放弃 |
| 状态 `status` | `0` 正常 / `1` 暂停；**新增默认暂停** |
| 写日志失败 | 不得拖垮调度线程外业务；写库异常仅打 error 日志 |
| 错误码 | 复用已有 `ErrorCodes.Job`（`20020`–`20023`） |

## 6. 数据与 Flyway

建议迁移文件：`V11__sys_job_quartz.sql`（若实现时已有更高版本号则顺延）。内容从 bak 抽取并按现网迁移风格改写：

1. **`sys_job` / `sys_job_log`**：来自 bak `V1__business.sql`；列注释含字典类型（`code_formater` §4.3）。
2. **`QRTZ_*`**：来自 bak `V2__quartz.sql`（含布尔列 `VARCHAR(12)`，兼容 H2 / MySQL）。
3. **字典**：`sys_job_group`、`sys_job_status`、`sys_job_misfire_policy`、`sys_job_concurrent` 等（及日志状态若独立）。
4. **菜单**：挂在现网监控目录 `menu_id = 2100` 下；建议空闲段 **`2130+`**（现网已用至约 `2124`，避免复用 bak 的 `2240+` 除非确认空闲）。
   - 定时任务页 + 按钮权限：`monitor:job:list|add|edit|remove|export|query|changeStatus` 等
   - 调度日志页 + 删除/导出（权限对齐 bak：日志侧复用 `monitor:job:query|remove|export`）
5. **角色授权**：`role_id = 1` 写入对应 `sys_role_menu`（与 V8 monitor 菜单同一模式）。

主键与审计字段对齐 `BaseEntity` / 现网 `ASSIGN_ID` 惯例。

## 7. 前端

从 bak 迁入并对齐现网 `monitor/operlog`、`monitor/logininfor` 交互：

| 路径 | 说明 |
|------|------|
| `quick-ui/src/api/monitor/job.js` | 任务 API |
| `quick-ui/src/api/monitor/jobLog.js` | 日志 API |
| `views/monitor/job/index.vue` | 任务列表 / 启停 / 立即执行 |
| `views/monitor/job-log/index.vue` | 调度日志 |
| `components/Crontab`（及 bak `JobFormDialog` 若仍依赖） | Cron 生成；现网已有则复用，缺则迁入 |

- 路由：以动态菜单为主（Flyway 菜单 `component` 指向上述页面）；仅在现网同类页有静态路由先例时再补。
- 导出：同步文件下载，去掉异步导出任务中心调用。

## 8. 测试与成功标准

**测试**

- 迁入 / 改写 `CronUtilsTest`、`ScheduleUtilsTest` 至 `quickboot-module-quartz`（优先）或 app。
- `ApplicationModules.verify()` 在注册 quartz 基包后通过。

**成功标准**

1. `mvn -pl quickboot-app -am test`（或等价）可通过 Modulith verify，且 quartz 模块编译通过。
2. 启动后 Flyway 创建表与菜单；管理员可见「定时任务」「调度日志」。
3. 可对 `QcDemoTask` 配置 Cron、启用、立即执行，并在调度日志看到记录。
4. 任务/日志同步导出可下载 Excel。
5. 无对 `module-system.internal` 或异步导出中心的依赖。

## 9. 风险与回滚

| 风险 | 缓解 |
|------|------|
| Quartz 与 Spring Boot 4 版本 | 使用 Boot BOM 管理的 `spring-boot-starter-quartz`；编译期钉版本 |
| H2 / MySQL JobStore 差异 | 沿用 bak `VARCHAR(12)` 布尔列与 StdJDBCDelegate 兼容写法 |
| Modulith 未注册基包导致 verify 失败 | 同步改 `ApplicationModuleSourceFactory` |
| 菜单 ID 冲突 | 使用 `2130+` 并 `WHERE NOT EXISTS` |

回滚：删除模块目录与父 POM/`app` 依赖、删除 Flyway 文件（已执行环境需人工清表）、移除前端页面与 API；以 Git 为准。

## 10. 实现顺序（计划阶段细化）

1. 脚手架：POM、Modulith `package-info`、app 依赖与基包注册。
2. Flyway `V11`（或下一可用版本）。
3. 后端迁入 + 导出改造 + Demo Task。
4. 单测与 `verify()`。
5. 前端 API / 页面 / Cron 组件对齐。
6. 冒烟：启停、立即执行、日志、导出。

## 11. 开放问题（实现计划阶段关闭）

- Flyway 最终文件名序号（以仓库当时最新 `V*` 为准）。
- 菜单精确 `menu_id` 列表（建议 2130–2149 段，实现时核对）。
- DTO 后缀若与现网 `*Vo` 请求体习惯不完全一致，实现时以同域 monitor 控制器为准微调，不扩大行为。
