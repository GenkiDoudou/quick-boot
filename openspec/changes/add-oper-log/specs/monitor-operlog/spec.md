## ADDED Requirements

### Requirement: 操作日志持久化模型

系统 MUST 提供 `sys_oper_log` 表，字段语义对齐若依参考（含 `title`、`business_type`、`method`、`request_method`、`operator_type`、`oper_name`、`dept_name`、`oper_url`、`oper_ip`、`oper_location`、`oper_param`、`json_result`、`status`、`error_msg`、`oper_time`、`cost_time`），并 MUST 包含可空的 **`trace_id`**（与 `TraceIds`/MDC 同源字符串）；主键类型与现有系统表 BIGINT 策略一致。

#### Scenario: 表存在且可写入

- **WHEN** Flyway 迁移已执行
- **THEN** 应用可向 `sys_oper_log` 插入一行且 `oper_time`、`cost_time`、`status` 非空语义合法

---

### Requirement: 宽切面采集与事件载荷

系统 MUST 通过 AOP 环绕命中 Web 映射注解的 Controller 方法；在 `finally` 中组装事件载荷，包含起止时间、方法签名、参数、返回值、异常、`traceId`（在切面线程从 `TraceIds.current()` 读取并写入载荷）。系统 MUST 支持 `@IgnoreLogger`：`ALL` 不发布事件，`PARAMS` 不记录参数，`RESULT` 不记录返回值。系统 MUST 支持可配置的 URL 前缀列表，匹配请求 URI 前缀时不发布事件。

#### Scenario: 正常请求产生事件

- **WHEN** 某已映射 Controller 方法成功返回且无 `ALL` 忽略
- **THEN** 至少发布一条应用事件且载荷含 URI 与耗时

#### Scenario: IgnoreLogger 阻断

- **WHEN** 方法标注 `@IgnoreLogger(ALL)`
- **THEN** 不得发布操作日志事件

#### Scenario: traceId 进载荷

- **WHEN** 当前线程 MDC 已设置 `traceId`
- **THEN** 事件载荷中的 `traceId` MUST 与该值一致（空白则可为空）

---

### Requirement: 同步落库与失败隔离

系统 MUST 使用同步 `@EventListener`（或等价）将事件转换为实体并脱敏后插入 `sys_oper_log`。持久化过程中的任何异常 MUST 被捕获并记录日志，且 MUST NOT 向业务 Controller 调用链抛出以阻断原请求。

#### Scenario: 写库失败不阻断业务

- **WHEN** 插入 `sys_oper_log` 抛出运行时异常
- **THEN** 原 HTTP 业务响应仍按未开启审计写库时的成功路径返回（或按原业务逻辑失败），不因写日志失败而改变

---

### Requirement: 脱敏与截断

系统 MUST 在持久化前对 `oper_param`、`json_result`（及实现约定内的 `error_msg`）做敏感信息处理：复用或对齐 `common-field-desensitization` 规则；并对字符串长度施加实现约定上限。

#### Scenario: 参数含敏感键

- **WHEN** 请求参数 JSON 含配置的敏感键名（如 password）
- **THEN** 入库字符串 MUST 不包含明文敏感值

---

### Requirement: 管理端 REST 契约

系统 MUST 暴露以下端点（均在统一 API 前缀之下，与现有监控模块一致）：`GET /monitor/operlog/list`（分页与筛选）、`GET /monitor/operlog/{operId}`（详情）、`POST /monitor/operlog/remove`（body 含 `ids` 数组）、`POST /monitor/operlog/clean`（清空）、`POST /monitor/operlog/export`（Excel，筛选条件与列表一致）。删除与清空 MUST NOT 使用 `DELETE` 动词表达。系统 MUST 使用 `monitor:operlog:query`、`monitor:operlog:remove`、`monitor:operlog:export` 进行权限校验（与需求文档一致）。

#### Scenario: 删除使用 POST

- **WHEN** 客户端调用批量删除
- **THEN** HTTP 方法 MUST 为 `POST` 且路径为 `/monitor/operlog/remove`

#### Scenario: 导出超限拒绝

- **WHEN** 导出结果行数超过配置上限
- **THEN** 系统 MUST 返回业务错误且不生成完整超大文件

---

### Requirement: 管理端不产生自循环日志

`OperLogController` 及其映射方法 MUST 通过 `@IgnoreLogger(ALL)` 或等价机制避免为「查询/删除/导出操作日志」本身写入 `sys_oper_log`，除非后续单独变更需求。

#### Scenario: 查询列表不产生新行

- **WHEN** 管理员仅调用 `GET /monitor/operlog/list`
- **THEN** 不得仅因该调用新增一条 `sys_oper_log` 记录

---

### Requirement: 前端操作日志页

`quick-ui` MUST 提供操作日志列表页：支持需求文档中的查询条件、默认按操作时间倒序、列排序（操作人员、操作日期、耗时）、详情查看、单条/批量删除、清空、导出；MUST 展示 `trace_id` 列或等价可追溯展示；MUST 使用 `v-hasPermi`（或项目等价）绑定 `monitor:operlog:*`。

#### Scenario: 无权限隐藏按钮

- **WHEN** 当前用户缺少 `monitor:operlog:remove`
- **THEN** 删除与清空入口 MUST 不可见或不可用
