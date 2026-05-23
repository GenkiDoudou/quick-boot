## 1. 数据库与实体

- [x] 1.1 新增 Flyway：`sys_oper_log`（若依语义列 + `trace_id` + 索引），版本号 `Vn` 取当前仓库 `db/migration` 最大序号 +1
- [x] 1.2 新增 Entity、Mapper（MyBatis-Plus），主键 BIGINT 与现有表一致，public API JavaDoc 中文

## 2. 切面、事件与 IgnoreLogger

- [x] 2.1 新增 `@IgnoreLogger` 与事件载荷（含 `traceId`、`signature`、`args`、`result`、`throwable`、时间戳）
- [x] 2.2 新增 `OperLogPublishingAspect`：`@Around` 切点等价覆盖 Web 映射方法；`finally` 中写入 `traceId = TraceIds.current()` 并发布事件
- [x] 2.3 注册切面 Bean（自动配置或现有配置类）；新增 `OperLogProperties`（`ignore-url-patterns` 默认含 actuator、swagger、静态资源、`/error` 等）
- [x] 2.4 单元测试：MDC 有 `traceId` 时载荷非空；`@IgnoreLogger(ALL)` 不发布事件

## 3. 监听器、脱敏与元数据解析

- [x] 3.1 新增 `OperLogPersistListener`：同步监听，组装实体，`try/catch` 写库失败仅打日志
- [x] 3.2 新增 `OperLogAssembler`：`status` 0/1、方法名短格式、异常 `error_msg`、截断长度常量
- [x] 3.3 新增 `OperLogSensitiveMasker`（或等价）对接 `common-field-desensitization` 规则
- [x] 3.4 新增可选 `@OperLogMeta` 与 `@Tag`/`@Operation` 解析兜底标题与业务类型

## 4. 管理端 API

- [x] 4.1 新增 `io.github.genkidoudou.web.monitor.operlog` 包下 Controller/Service/BO/VO：`GET /list`、`GET /{operId}`、`POST /remove`、`POST /clean`、`POST /export`
- [x] 4.2 `application.yml`：`qc.monitor.operlog.export-max-rows`（默认 10000）；导出超限抛项目业务异常
- [x] 4.3 `OperLogController` 加 `@IgnoreLogger(ALL)`；OpenAPI 与 `@SaCheckPermission`、`@Valid` 齐全

## 5. 菜单与字典

- [x] 5.1 Flyway 插入「操作日志」菜单、按钮权限、`sys_role_menu`；`menu_id` 与现有迁移错开
- [x] 5.2 若 `business_type`/`operator_type`/`status` 字典缺失则种子补齐并与前端字典类型一致

## 6. 前端

- [x] 6.1 新增 `quick-ui/src/api/monitor/operlog.ts` 与列表/详情页面、路由
- [x] 6.2 权限指令绑定 `monitor:operlog:*`；列表含 `trace_id` 列；`pnpm build:prod` 通过

## 7. 验证

- [x] 7.1 手工或自动化：业务请求写库且 `trace_id` 与 `R`/日志一致；`/monitor/operlog/list` 不自增噪声日志
- [ ] 7.2 `mvn -pl quickboot-web -am test`（或 CI 等价）通过（本环境 Maven 报 JDK 目标版本异常，未在此执行）
