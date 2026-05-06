# common-response-paging

## Purpose

`quickboot-common` 提供的统一 HTTP JSON 契约：`R<T>` 响应体、`PageRequest` / `PageInfo` 分页入参与出参、HTTP 语义向业务码常量；约定业务成败由响应体 `code` 表达并与 HTTP 层边界划分（不包含全局异常映射）。

## Requirements

### Requirement: 统一响应体 R

系统 SHALL 在 `quickboot-common` 提供泛型响应类型 `R<T>`，序列化为 JSON 时包含字段：`code`（整数业务码）、`msg`（字符串提示）、`data`（可为 null 的业务载荷）、`traceId`（字符串，取自 MDC 或链路上下文；若当前上下文无追踪信息则可为 null 或由实现约定空字符串）、`timestamp`（长整型毫秒时间戳）。系统 SHALL 提供工厂方法：`ok()`、`ok(String msg)`、`ok(T data)`、`ok(String msg, T data)`，以及 `error()`、`error(String msg)`、`error(int code, String msg)`、`error(int code, String msg, T data)`。系统 SHALL 提供 `boolean isSuccess()` 当且仅当 `code == 200`，并提供 `boolean isError()` 表示 `code != 200`。

#### Scenario: 成功返回携带数据与时间戳

- **WHEN** 调用 `R.ok("操作成功", someDto)` 并序列化为 JSON
- **THEN** `code` 为 200，`msg` 为「操作成功」，`data` 为 `someDto` 的结构化表示，`timestamp` 为非空的毫秒值

#### Scenario: 业务失败返回非 200 码

- **WHEN** 调用 `R.error(400, "参数无效")`
- **THEN** `code` 为 400，`isSuccess()` 为 false，`isError()` 为 true

### Requirement: 分页请求 PageRequest

系统 SHALL 提供 `PageRequest<T>`，字段包括：`current`（页码，默认 1）、`size`（页大小，默认 10，`size` 最小值必须为 1）、`param`（查询条件载荷，泛型 `T`）。系统 SHALL 提供 `long getOffset()`（或等价返回类型），其值为 `(current - 1) * size`。`PageRequest` SHALL 作为 Controller/API 契约使用；持久层分页 MUST NOT 将该类型强加为 Mapper 唯一入参形态（由业务在 Service 内转换为 MyBatis-Plus `Page`）。

#### Scenario: 默认分页与偏移

- **WHEN** 新建 `PageRequest` 未显式设置 `current` 与 `size`
- **THEN** `current` 为 1，`size` 为 10，`getOffset()` 为 0

#### Scenario: 非首页偏移

- **WHEN** `current=3` 且 `size=10`
- **THEN** `getOffset()` 为 20

### Requirement: 分页响应 PageInfo

系统 SHALL 提供 `PageInfo<T>`，字段包括：`current`、`size`、`records`（当前页列表）、`total`（总条数）、`pages`（总页数）、`ext`（可选扩展 Map）。系统 SHALL 按 `pages = (total + size - 1) / size` 计算总页数，且在 `size >= 1` 前提下不发生除零。

#### Scenario: 总页数计算

- **WHEN** `total=23`、`size=10`
- **THEN** `pages` 为 3

#### Scenario: 扩展字段可选

- **WHEN** 构造 `PageInfo` 时不设置 `ext`
- **THEN** 序列化行为符合项目 Jackson 全局策略（通常为省略 null 或可空 Map，由实现与配置一致）

### Requirement: HTTP 语义业务码常量

系统 SHALL 提供集中定义的常用 HTTP 语义向整数常量（至少包含：200、400、401、403、404、500、503），供 `R` 与上层模块引用，避免魔术数字散落。

#### Scenario: 常量可被 R 使用

- **WHEN** 使用常量类中的 `HTTP_OK`（或等价命名，如 `HttpCodes.OK`）作为 `R.ok` 的默认 `code` 来源
- **THEN** 该常量值为 200 且与 `isSuccess()` 判定一致

### Requirement: 与异常与HTTP层的边界

本能力 SHALL NOT 要求在本模块内实现全局异常处理器或业务错误码表；本能力 SHALL NOT 将 HTTP 状态码用于表达业务成败（业务成败由响应体 `code` 表达，HTTP SHALL 维持 200 的成功响应语义用于「请求已送达并由应用生成 body」的场景，与项目约定对齐）。

#### Scenario: 范围限定

- **WHEN** 开发者仅引入本模块的类型与常量
- **THEN** 不自动改变未显式使用该类型的 Controller 的返回行为
