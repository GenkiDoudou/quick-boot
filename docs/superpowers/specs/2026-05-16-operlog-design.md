# 操作日志设计文档

## 1. 背景与目标

在 `quickboot` + `quick-ui` 中实现《操作日志需求文档》所述能力：审计后台用户操作，支持检索、排序、详情、单条/批量删除、清空、导出；采集侧采用 **宽切面 + Spring 事件 + `IgnoreLogger`**（与 `原始需求/old/quick-boot` 同构）；表字段语义 **对齐若依 `sys_oper_log`**，并扩展 **`trace_id`** 与统一响应/日志中的链路 id（`TraceIds` / MDC）一致，便于与文件日志关联追溯。

路径与权限以需求文档为真源：`/monitor/operlog/*`、`monitor:operlog:query|remove|export`。

## 2. 已确认决策摘要

| 项 | 决策 |
|----|------|
| API 前缀与权限 | `/monitor/operlog`，`monitor:operlog:query|remove|export` |
| HTTP 动词 | 与仓库约定一致：删除/清空用 **POST**；列表/详情 **GET**；导出 **POST**（与需求文档字面 `DELETE` 不一致，以本设计为准） |
| 批量删除 | **POST** + body 传 **`ids`** 数组（支持单条） |
| 采集策略 | **宽切面**（映射注解族切点）+ **发布应用事件** + **`@IgnoreLogger`**（`ALL` / `PARAMS` / `RESULT` 语义与旧栈一致） |
| 表字段语义 | 对齐若依 `sys_oper_log`；主键与列名在 Flyway 中与 **现有基线表主键策略** 一致（实现阶段对照 `V1__baseline` 等迁移定稿） |
| 操作状态 | **`status`**：`0` 正常、`1` 异常（与字典及需求一致），不使用旧 quick-boot 的 HTTP/业务码混用形态 |
| 模块/操作类型 | 切面无 RuoYi `@Log` 元数据时：**优先** 可选薄注解 **`@OperLogMeta`** 覆盖；否则用 **`@Tag` + `@Operation`** 拼出标题/展示用操作说明；再兜底「未分类」 |
| 脱敏 | 序列化为字符串后、**入库前** 调用项目统一脱敏能力（或小而专的 `OperLogSensitiveMasker`，规则与 `@Sensitive` 语义对齐） |
| 导出 | 同步 Excel；筛选与列表一致；**最大导出行数可配置**（默认 `10000`） |
| 写库失败 | **不得阻断**业务请求（与登录日志设计一致） |
| 链路 id | 表列 **`trace_id`**；事件 DTO 在切面 **`finally` 中写入 `TraceIds.current()`**；监听器落库以 DTO 为准；可空 |
| 管理端自记日志 | `/monitor/operlog/**` 建议使用 **`@IgnoreLogger(ALL)`**（或等价），避免列表/导出产生噪声或异常递归 |

## 3. 范围与非范围

### 3.1 范围

- Flyway：`sys_oper_log`（若依语义列 + **`trace_id`** + 必要索引）。
- 通用模块：**日志切面**、**事件载荷**、**监听器落库**、`IgnoreLogger`（或同名注解）、可选 **`@OperLogMeta`**。
- 监控模块：分页列表、详情、删除（含批量）、清空、导出。
- `quick-ui`：API、路由、列表页、详情、权限、菜单（与《登录日志》监控风格一致）。

### 3.2 非范围（本期不做）

- 异步导出、对象存储归档、采样率动态调参（可按二期扩展）。
- 替代 Micrometer/修改全局 tracing 传播机制（仅消费现有 `TraceIds`）。

## 4. 数据模型

### 4.1 `sys_oper_log`

对齐若依参考 DDL 语义，至少包含以下业务含义（物理列名以实现阶段 Flyway 为准，与若依保持一致便于对照）：

| 含义 | 说明 |
|------|------|
| 主键 | 与项目现有表主键生成策略一致 |
| `title` | 系统模块/标题 |
| `business_type` | 业务类型（字典） |
| `method` | 方法名称（如 `ClassName.methodName` 短格式，与旧栈展示习惯可一致） |
| `request_method` | HTTP 方法 |
| `operator_type` | 操作类别（字典，若依语义） |
| `oper_name` | 操作人员 |
| `dept_name` | 部门名称（可空） |
| `oper_url` | 请求 URI |
| `oper_ip` | 客户端 IP |
| `oper_location` | 操作地点（IP 解析，可空） |
| `oper_param` | 请求参数（JSON 字符串，截断上限在实现计划写死） |
| `json_result` | 返回或错误摘要（截断上限在实现计划写死） |
| `status` | `0` 正常 / `1` 异常 |
| `error_msg` | 异常信息（可空；与 `json_result` 分工：异常时优先填 `error_msg`，展示策略在 VO 层统一） |
| `oper_time` | 操作时间；列表默认按此倒序；索引 |
| `cost_time` | 耗时（毫秒） |
| **`trace_id`** | 链路 id；与 `TraceIds` / `R.traceId` 同源；**可空**；建议单列索引 |

**字典**：`business_type`、`operator_type`、`status` 等与 `sys_dict_data` 对齐的具体 `dict_type` 在实现阶段与现有字典种子统一命名。

## 5. 采集与落库链路

### 5.1 切面

- `@Around` 切点覆盖 **Controller 映射方法**（与 `原始需求/old/quick-boot` 的 `LoggingAspect` 意图一致）；`@Order` 与现有切面链在实现阶段测定，避免与权限、事务冲突。
- `finally` 中：根据 `@IgnoreLogger` 决定是否发布事件、是否剔除参数/返回值；组装 **事件 DTO**（含起止时间、签名、参数、返回值、异常、**`traceId` = `TraceIds.current()`**）。
- 发布 **同步应用事件**（首期）；监听器内写库，`try/catch` 吞异常并打 error 日志。

### 5.2 监听器

- 将 DTO 转为领域对象/实体：填若依列 + **`trace_id` 取自 DTO**（不用监听器线程盲读 MDC，避免未来异步化丢链路）。
- 操作人、部门：从当前登录上下文解析；匿名可空。
- **脱敏**：对 `oper_param`、`json_result`（及必要时的 `error_msg`）在持久化前处理。

### 5.3 排除路径

- 通过配置维护忽略前缀（如 actuator、swagger、静态资源、`/error` 等），与 `@IgnoreLogger` 互补。

## 6. 后端：操作日志管理模块

### 6.1 分层与包

- 建议包路径：`io.github.genkidoudou.web.monitor.operlog`（与 `logininfor` 监控模块并列）。
- Controller / Service / Mapper / Entity / QueryBo / Vo 分层与现有系统管理模块一致。

### 6.2 接口（对外契约）

| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| GET | `/monitor/operlog/list` | `monitor:operlog:query` | 分页 + 条件 + 白名单排序；默认 `oper_time` 降序 |
| GET | `/monitor/operlog/{operId}` | `monitor:operlog:query` | 详情（含 `trace_id`、请求/返回/异常等） |
| POST | `/monitor/operlog/remove` | `monitor:operlog:remove` | body：`ids`（`Long` 数组） |
| POST | `/monitor/operlog/clean` | `monitor:operlog:remove` | 清空 |
| POST | `/monitor/operlog/export` | `monitor:operlog:export` | body 与列表筛选同构；行数上限 |

**查询条件**：操作地址、系统模块、操作人员、业务类型、操作状态、操作时间区间；**首期支持 `trace_id` 精确匹配**（可选展示为截断 + 全量 tooltip 由前端定）。

**注解与校验**：`@Tag`、`@Operation`、`@Parameter`、`@SaCheckPermission`；Jakarta Validation；业务失败使用项目自定义异常，禁止 `IllegalArgumentException` 作为业务信号。

## 7. 前端（`quick-ui`）

- 新增 `api/monitor/operlog` 对接上述接口。
- 列表：需求中的列 + **`trace_id`**；支持列排序（操作人员、操作日期、耗时）；多选删除、清空、导出。
- 详情：请求地址、请求方式、操作方法、请求参数、返回参数、异常信息、**链路 id**。
- 权限：`v-hasPermi` 等与三点权限一致。
- 菜单：Flyway 增加「操作日志」及按钮项、`menu_id` 与现有迁移错开；超级管理员授权。

实现前须阅读 `DESIGN.md` 与 `sdd/前端代码规范.md`（见 `AGENTS.md`）。

## 8. 测试与验收

- 列表筛选、排序、详情字段与列表可追溯（含 **`trace_id` 与同一请求下应用日志 `%X{traceId}` 一致**）。
- 删除（单条/批量）、清空后列表为空并提示成功；导出非空且不超过行数上限。
- 写库异常不阻断任意被切面覆盖的业务接口。
- `/monitor/operlog` 自身接口不产生可观测的自循环日志风暴（`IgnoreLogger` 或路径排除生效）。

## 9. 后续步骤

在单独会话中按 **writing-plans** 产出 `docs/superpowers/plans/2026-05-16-operlog.md` 的实现任务清单后再编码。

---

**文档版本**：2026-05-16  
**关联需求**：`原始需求/系统管理/操作日志-需求文档.md`  
**关联设计**：`docs/superpowers/specs/2026-05-16-logininfor-design.md`（监控模块与 POST 删清约定）、`AGENTS.md`  
**参考实现**：`原始需求/old/quick-boot`（`LoggingAspect`、`OperateLoggerEventListener`、`@IgnoreLogger`）
