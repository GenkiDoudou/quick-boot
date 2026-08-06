# 系统参数 / 操作日志 / 登录日志（对齐 bak 宽切面 + 本项目契约）

日期：2026-08-06  
状态：已定稿（待实现）  
来源：`bak` 宽切面与管理页 + 当前 `SysOauthClient` / `SysRole` 契约  
静态原型：`docs/demo/sys-config-operlog-logininfor-prototype.html`

## 背景与目标

将 bak 的**参数配置、操作日志、登录日志**迁入当前工程。管理 API 与前端按本仓库已落地的 OauthClient / Role 风格（`page/add/update/remove`、同步 Excel、显式字段赋值）；操作日志采集对齐 bak：**切全部 `@RestController`**，用 OpenAPI 注解丰富标题，导出等接口用 `@IgnoreLogger` 控制参数/结果是否入库。

### 已确认决策

| 项 | 选择 |
| --- | --- |
| 范围 | 配置 + 操作日志 + 登录日志，一次交付（完整可用） |
| 操作日志采集 | 宽切面：所有 `@RestController` public 方法 |
| 标题来源 | `@OperLogMeta` → `@Tag` + `@Operation.summary` → `Class.method` |
| 忽略粒度 | `@IgnoreLogger`：`ALL` / `PARAMS` / `RESULT`（导出等用 `RESULT`） |
| 导入导出 | 同步 Excel；不迁 bak 异步导入导出中心 |
| 配置 API 前缀 | `sys/config`（不用 bak `/system/config`） |
| 日志管理前缀 | `monitor/operlog`、`monitor/logininfor` |
| 方案组织 | 单 OpenSpec 变更；实现顺序 Config → OperLog → LoginInfor → 前端 |

### 非目标

- bak 异步导入/导出任务中心、`import-biz-type`
- 慢 SQL、在线用户、定时任务、客户端轨迹
- 原样保留 bak `/system/config/*` URL
- 操作日志「仅 `@Log` 注解才记录」模式（已否决）

## 架构

```text
quickboot-common
  OperLogPublishingAspect  → 切 RestController → OperLogCapturedEvent
  IgnoreLogger / OperLogMeta / OperLogProperties / 脱敏与序列化工具

quickboot-system
  OperLog 监听落库（默认异步）+ OperLogMetaResolver（OpenAPI）
  SysConfig / SysOperLog / SysLogininfor  管理 CRUD + 同步 Excel
  登录链路写入 SysLogininfor；unlock → LoginLockSupport

quick-ui
  system/config、monitor/operlog、monitor/logininfor
  C7JsonTable + columnType:slot；同步导入导出
```

实现顺序：`Config → OperLog（采集+管理）→ LoginInfor（写库+管理）→ 前端三页`

## 操作日志：采集与落库

### 采集（`quickboot-common`）

- `OperLogPublishingAspect`：`@within(RestController)` 且 `execution(public * *(..))`
- 配置前缀：`qc.monitor.operlog`
  - `capture-enabled`（默认 `true`）
  - `async-enabled`（默认 `true`）
  - `print`（默认 `false`，控制台摘要）
  - `export-max-rows`（默认 `10000`）
  - `ignore-url-patterns`：Ant 风格；默认含登录验证码、actuator、Swagger、`/monitor/operlog/**` 等，避免自递归与噪声
- `@IgnoreLogger`（方法或类）：
  - `ALL`：不发布事件（操作日志 Controller 类级使用）
  - `PARAMS`：不记录入参
  - `RESULT`：不记录出参（**导出/文件下载**等方法）
- 请求线程采集：URI、HTTP 方法、IP、登录用户 ID（Sa-Token）、耗时、参数/结果 JSON（截断约 3500 字符）、异常
- 发布 `OperLogCapturedEvent`；发布失败只打 warn，不影响业务

### 元数据

- 优先级：`@OperLogMeta` → 类 `@Tag.name` + 方法 `@Operation.summary` → `SimpleName.method`
- 业务类型：显式 `OperLogMeta.businessType`，否则按 HTTP + 路径/方法名推断（export/import/add/update/remove/clean…），与 bak `OperLogMetaResolver` / `OperLogBusinessType` 一致
- 操作者类别：默认后台用户（`1`）

### 落库（`quickboot-system`）

- 监听事件 → 组装 `sys_oper_log` → 默认异步写入
- 敏感字段脱敏（密码等）
- 未登录用户 ID 可空

### 管理 API：`monitor/operlog`

| 动作 | 方法 | 路径 |
| --- | --- | --- |
| 分页 | POST | `monitor/operlog/page` |
| 详情 | GET | `monitor/operlog/{operId}` |
| 批删 | POST | `monitor/operlog/remove` body=`List` |
| 清空 | POST | `monitor/operlog/clean` |
| 导出 | POST | `monitor/operlog/export`（方法上 `@IgnoreLogger(RESULT)`） |

无新增/修改接口。查询条件对齐 bak：URI、标题、操作人、业务类型、状态、traceId、clientOperationId、clientId、时间范围。

## 登录日志

### 写库

- 在现有登录成功/失败路径写入 `sys_logininfor`（用户名、IP、浏览器/OS 摘要、状态、消息、时间、clientId 等；字段对齐 bak）
- 失败含锁定相关文案时与 `LoginLockSupport` 行为一致

### 管理 API：`monitor/logininfor`

| 动作 | 方法 | 路径 |
| --- | --- | --- |
| 分页 | POST | `monitor/logininfor/page` |
| 批删 | POST | `monitor/logininfor/remove` |
| 清空 | POST | `monitor/logininfor/clean` |
| 导出 | POST | `monitor/logininfor/export`（`@IgnoreLogger(RESULT)`） |
| 解锁 | GET | `monitor/logininfor/unlock/{userName}` → 清 `LoginLockSupport` 锁定缓存 |

查询：IP、用户名、clientId、状态、登录时间范围。

## 参数配置

### 管理 API：`sys/config`

对齐 OauthClient 通用契约：

| 动作 | 方法 | 路径 |
| --- | --- | --- |
| 分页 | POST | `sys/config/page` |
| 详情 | GET | `sys/config/{configId}` |
| 新增 | POST | `sys/config/add` |
| 修改 | POST | `sys/config/update` |
| 单删 | GET | `sys/config/remove/{configId}` |
| 批删 | POST | `sys/config/remove` |
| 按键查值 | GET | `sys/config/configKey/{configKey}` |
| 刷新缓存 | POST | `sys/config/refreshCache` |
| 导出 | POST | `sys/config/export`（`@IgnoreLogger(RESULT)`） |
| 导入模板 | GET | `sys/config/import/template` |
| 导入 | POST | `sys/config/import` multipart + `updateSupport` |

### 业务规则

- `configKey` 唯一；键名模式对齐 bak（小写字母、数字、点号、连字符分段）
- 系统内置（`configType=1` / 与种子约定一致）**禁止删除**；编辑时键名与内置标记不可改（仅允许改值/备注，与 bak 一致）
- 缓存：`configKey → configValue`；增删改后刷新对应键；`refreshCache` 全量重载
- Service：显式字段赋值；导入失败明细与 OauthClient 一致（`writeErrorFile`）

## 数据模型（Flyway）

### `sys_config`

- `config_id` PK、`config_name`、`config_key`(UK)、`config_value`、`config_type`、`remark`、审计字段 / `del_flag`
- 种子：可迁 bak 常用项（初始密码、验证码开关等）中与本项目相关的子集

### `sys_oper_log`

- 对齐 bak：`oper_id`、`title`、`business_type`、`method`、`request_method`、`operator_type`、`oper_name`、`dept_name`、`oper_url`、`oper_ip`、`oper_location`、`oper_param`、`json_result`、`status`、`error_msg`、`oper_time`、`cost_time`、以及 `trace_id` / `client_operation_id` / `client_id`（若本项目已有追踪约定则写入）

### `sys_logininfor`

- 对齐 bak：`info_id`、`user_name`、`client_id`、`ipaddr`、`login_location`、`browser`、`os`、`status`、`msg`、`login_time`

### 字典种子（若尚无）

- `sys_oper_status`、`sys_oper_business_type`、`sys_oper_operator_type`、`sys_login_status`（供前端标签）

### 菜单权限

- 系统管理下：**参数设置** — `system:config:list|query|add|edit|remove|export|import`
- 系统监控下：**操作日志** — `monitor:operlog:list|query|remove|export`（清空共用 `remove`）
- 系统监控下：**登录日志** — `monitor:logininfor:list|query|remove|export|unlock`

（前端 `v-hasPermi` 与 Flyway 权限字同 PR 对齐；list 与 query 按现有菜单习惯可合并或并存。）

## 前端

| 页面 | 说明 |
| --- | --- |
| `views/system/config/index.vue` | C7JsonTable；刷新缓存；同步导入导出；操作列 `columnType:'slot'` |
| `views/monitor/operlog/index.vue` + 详情面板 | 无增改；删除/清空/导出/详情 |
| `views/monitor/logininfor/index.vue` | 删除/清空/导出/解锁选中 |
| `api/system/config.js`、`api/monitor/operlog.js`、`api/monitor/logininfor.js` | 对齐上表路径；blob 导出 |

参考 bak 页面结构与静态原型交互；不接异步导出中心。

## 实现顺序与验收

1. Flyway：三表 + 字典种子 + 菜单权限  
2. Config 后端 + 缓存 + 同步导入导出  
3. OperLog：common 宽切面 + system 落库 + 管理 API；关键 Controller 补 OpenAPI；导出加 `@IgnoreLogger(RESULT)`；operlog 自身 `@IgnoreLogger(ALL)`  
4. LoginInfor：登录写库 + 管理 API + unlock  
5. 前端三页  
6. `mvn compile`；手工：配置 CRUD/缓存/导入失败明细；调用带 `@Operation` 的接口后操作日志有标题；导出日志无大段 RESULT；登录失败可见日志且 unlock 生效  

## 风险

- [全量 RestController 噪声] → URI 黑名单 + 类/方法 `@IgnoreLogger`；operlog 自身 ALL  
- [异步落库丢失] → 监听器异常打日志；关键路径可配置切同步（`async-enabled`）  
- [userId / Sa-Token 类型] → 与现登录态一致，未登录写空  
- [权限字与 bak 不一致] → 以前端本项目约定为准，Flyway 同批写入  

## Open Questions

- 无（范围、采集方式、API 前缀、原型交互均已确认）
