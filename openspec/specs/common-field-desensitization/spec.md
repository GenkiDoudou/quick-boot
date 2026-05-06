# common-field-desensitization

## Purpose

`quickboot-common` 基于 Jackson **仅序列化写出** 阶段的 `String` 字段脱敏：`@Sensitive` 与多种内置类型、`CUSTOM` 策略；不改变内存中的 DTO/实体字段值。通过 Spring Boot `ObjectMapper` 自动注册模块以适配 Web JSON；与 `sensitive-word`、入参校验、日志脱敏等能力解耦。

## Requirements

### Requirement: @Sensitive 仅作用于 String 的 Jackson 序列化

系统 SHALL 提供字段级注解 `@Sensitive`，包含属性：`SensitiveType type`（默认 `CUSTOM`）、`String strategy`（形如 `prefix,suffix`，仅当 `type=CUSTOM` 时解析）。该注解 SHALL 仅对 **`java.lang.String`** 属性或其 Bean 访问器生效：对非 `String` 字段的误标注，实现 MAY 忽略且 MUST 在 JavaDoc 说明。脱敏 SHALL 发生在 **JSON 序列化写出阶段**，SHALL NOT 修改内存中 Java 对象字段的原始值。

#### Scenario: MOBILE 掩码

- **WHEN** 某 `String` 字段值为 `13812345678` 且标注 `@Sensitive(type=MOBILE)`
- **THEN** 序列化 JSON 中该字段值为 `138****5678`（前三、后四保留，中间段以等长 `*` 替换）

#### Scenario: null 与空串兜底

- **WHEN** 字段值为 `null` 或空字符串 `""`
- **THEN** 序列化输出与未加注解时一致（`null` 受全局 `JsonInclude` 策略影响；空串按普通字符串原样输出）

### Requirement: CUSTOM 与首尾保留

当 `type=CUSTOM` 且 `strategy` 为 `prefix,suffix`（非负整数），系统 SHALL 保留前 `prefix` 与后 `suffix` 个字符，中间用与中间段 **等长** 的 `*` 替换。若 `strategy` 解析失败、或为负、或 `prefix+suffix ≥ 原文长度`，系统 SHALL **原样输出**。

#### Scenario: CUSTOM 3,4

- **WHEN** 字段值为 `ABCDEFGHIJ` 且 `@Sensitive(type=CUSTOM, strategy="3,4")`
- **THEN** 输出为 `ABC***GHIJ`（中间 3 个 `*`）

### Requirement: 内置类型 NAME / ID_CARD / BANK_CARD / PASSWORD

系统 SHALL 实现以下内置算法（均仅对非空非 null 的 `String`；若规则要求的最小长度不满足，则 **原样输出**）：

- `NAME`：长度 `≤1` 原样；否则首字符保留，其余每位 `*`。  
- `ID_CARD`：至少 10 位时才脱敏：前 6、后 4 保留，中间 `*` 等长。  
- `BANK_CARD`：至少 8 位：前 4、后 4 保留，中间 `*` 等长。  
- `PASSWORD`：输出固定 `******`。

#### Scenario: NAME 单字

- **WHEN** 值为 `张` 且 `type=NAME`
- **THEN** 输出仍为 `张`

#### Scenario: PASSWORD 非空

- **WHEN** 值为 `secret` 且 `type=PASSWORD`
- **THEN** 输出为 `******`

### Requirement: 内置类型 EMAIL 与 ADDRESS

- `EMAIL`：须含字符 `@`；本地部分（`@` 之前）长度 `≤2` 时 **整串原样**；否则本地前 2 位保留，`@` 前其余位以等长 `*` 替换，`@` 及域名 **全文原样**。  
- `ADDRESS`：长度 `≤6` 原样；否则前 6 字符原样，第 7 位起每位以 `*` 替换（后缀段等长 `*`）。

#### Scenario: EMAIL 标准样例

- **WHEN** 值为 `abcde@example.com` 且 `type=EMAIL`
- **THEN** 输出为 `ab***@example.com`（本地段长度 5：前 2 保留，余下 3 位以等长 `*` 替换；域名全文保留）

#### Scenario: EMAIL 本地过短

- **WHEN** 值为 `a@b.com` 且 `type=EMAIL`
- **THEN** 输出原样 `a@b.com`

### Requirement: Spring Boot Web JSON 适配

系统 SHALL 通过 Spring Boot 自动配置或等效机制，使 Web 层默认用于 JSON 写的 `ObjectMapper` **注册本脱敏模块**，业务侧无需手写 `registerModule`（除非使用完全独立的 `ObjectMapper` 实例）。

#### Scenario: 控制器返回带注解 DTO

- **WHEN** Controller 返回包含 `@Sensitive` 标记 `String` 字段的 DTO
- **THEN** HTTP JSON 响应体中对应字段为掩码后的字符串，且服务端内存对象该字段值仍未被修改（可在单测通过对同一对象序列化前后引用相等或字段 getter 不变验证）

### Requirement: 模块边界

本能力 SHALL NOT 承担请求体入参校验脱敏、日志脱敏或与 `sensitive-word` 词库过滤合并；本能力 SHALL NOT 要求修改数据库层存储格式。

#### Scenario: 未标注字段不受影响

- **WHEN** DTO 字段无 `@Sensitive`
- **THEN** 序列化行为与引入本模块前一致
