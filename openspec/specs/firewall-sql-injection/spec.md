# firewall-sql-injection

## Purpose

`quickboot-common` 提供的 **安全防火墙 — SQL 注入启发式拦截**：在 Servlet 链上对 **Query/Form** 与 **`application/json` body**（递归字符串值）做 **关键字子串、大小写不敏感** 检测（策略偏**宁可拦宽**）；配置前缀 **`qc.security.firewall.sql-injection`**；命中时业务码 **30601**、**WARN** 级审计日志、**`ServletUtils` + i18n**；**`FilterOrder` 早于** `SensitiveWordFirewallFilter`，并对 body 使用可重复读包装以协同下游。

## Requirements

### Requirement: 配置前缀与开关

系统 MUST 提供安全防火墙能力 `firewall-sql-injection`，并以 **`qc.security.firewall.sql-injection`** 作为配置前缀。系统 MUST 支持 **`enabled`** 且默认值 MUST 为 **`false`**；当 **`enabled=false`** 时系统 MUST **不**对请求应用本能力的 SQL 关键字检测与拦截逻辑。

#### Scenario: 默认关闭不拦截

- **WHEN** 未配置 `qc.security.firewall.sql-injection.enabled` 或其值为 `false`
- **THEN** 任意请求 MUST NOT 因本能力而被拦截或改写 body

#### Scenario: 显式开启后生效

- **WHEN** `qc.security.firewall.sql-injection.enabled=true`
- **THEN** 系统 MUST 按本 spec 对未命中 `ignoreUrls` 的请求执行检测并在命中时拦截

### Requirement: ignoreUrls

系统 MUST 支持 **`ignoreUrls[]`**（Ant 风格路径）。当请求路径匹配 **任一** 项时，本能力 MUST **完全跳过**（不对 Query/Form 与 JSON body 做本能力检测）。

#### Scenario: 命中忽略路径则跳过

- **WHEN** `ignoreUrls` 包含 `/actuator/**` 且请求路径为 `/actuator/health`
- **THEN** 本能力 MUST NOT 拦截该请求（即使参数中含关键字样片段）

### Requirement: 关键字来源与匹配语义

系统 MUST 支持 **`keywords[]`**（字符串列表）。当 **`keywords` 非空**时，系统 MUST **仅**使用该列表作为检测集合。当 **`keywords` 为空**（或未配置等价于空列表）时，系统 MUST 使用 **实现内置的默认关键字集合**（须覆盖常见 SQL 注入片段如 `union`、`select`、`drop`、注释与语句分隔相关片段等；精确列表由实现维护并在 JavaDoc 或配置元数据中说明）。

检测 MUST 对目标字符串做 **子串级**匹配（**宁可拦宽**）；匹配 MUST **大小写不敏感**。

#### Scenario: 配置关键字优先于内置

- **WHEN** `keywords=["xyz"]` 且请求 query 参数值为 `union select`
- **THEN** 若 `xyz` 未出现于该值中则 MUST NOT 因本能力拦截（即便存在 `union`）

#### Scenario: 空关键字使用内置集合

- **WHEN** `keywords` 为空且某 query 参数值包含内置集合中的典型片段（经大小写不敏感比较）
- **THEN** 系统 MUST 按命中处理条款拦截

### Requirement: 检测范围 Query、Form 与 JSON body

当本能力生效且未命中 `ignoreUrls` 时，系统 MUST 对 **所有 Query 与 Form 参数名值**参与检测（多值参数 MUST 逐一检测）。

当请求的 **`Content-Type` 兼容 `application/json`** 且 body 非空时，系统 MUST 解析 JSON，对其中的 **字符串值**逐一检测；对 **对象（Map）与数组（Collection）**MUST **递归**遍历；对 **非字符串标量**（如数字、布尔、`null`）MUST **不参与**关键字子串检测。

当 **`Content-Type` 不兼容 `application/json`** 时，系统 MUST **不**为本能力目的解析 raw body（Query/Form 仍检测）。

#### Scenario: Query 典型注入被拦截

- **WHEN** `enabled=true` 且 URL 含 query `q=1%20union%20select`（解码后含内置关键字语义）
- **THEN** 系统 MUST 拦截并返回统一错误响应

#### Scenario: JSON 嵌套字段命中

- **WHEN** `enabled=true` 且 body 为 JSON，深层字段某字符串值命中关键字策略
- **THEN** 系统 MUST 拦截

### Requirement: 命中日志

当请求因本能力被拦截时，系统 MUST 输出 **告警级别**日志（实现选定 **WARN** 或以上），且 MUST 包含 **请求路径**、**客户端 IP**、**HTTP 方法**、**命中关键字集合**；并 SHOULD 包含 **参数或 JSON 路径标识**（无需默认输出完整 body 原文）。

#### Scenario: 拦截时可审计

- **WHEN** 某请求因本能力被拦截
- **THEN** 日志中 MUST 能区分命中原因（至少含命中关键字集合与路径上下文）

### Requirement: 命中响应与国际化

当请求因本能力被拦截时，系统 MUST 写出 **HTTP 200** + 与 **`R.error`** 一致的 JSON 形态；JSON 中业务 **`code`** MUST 为 **`30601`**（与 **`HttpCodes`** 中对外常量一致）。

系统 MUST 使用 **`String.valueOf(code)`** 作为 i18n 词条键解析 **`msg`**；当 i18n **未命中**时，系统 MUST 使用配置项 **`forbiddenMessage`** 作为兜底文案；当 **`forbiddenMessage` 为空白**时，系统 MAY 使用实现定义的默认文案（须在 JavaDoc 说明）。

#### Scenario: i18n 命中

- **WHEN** 当前 Locale 下存在键 **`30601`** 的文案且请求被拦截
- **THEN** 响应 JSON 的 **`msg`** MUST 为 i18n 解析结果

#### Scenario: i18n 未命中时使用 forbiddenMessage

- **WHEN** 键 **`30601`** 未配置文案且 `forbiddenMessage="非法参数"` 且请求被拦截
- **THEN** 响应 JSON 的 **`msg`** MUST 为 **`非法参数`**

### Requirement: Spring Boot 集成与 Filter 顺序

能力 MUST 以 **quickboot-common** 自动配置注册 **独立** Servlet Filter，且 MUST 绑定 **`@ConfigurationProperties`**。

当本 Filter 与 **`SensitiveWordFirewallFilter` 同时启用**时，本 Filter MUST 在 Filter 链上 **早于** `SensitiveWordFirewallFilter` 执行（以保证对 **原始 body** 做检测）；具体 **`FilterRegistrationBean` order** MUST 与 **`design.md`** 中数值约定一致并可测试验证。

#### Scenario: 自动配置可关闭

- **WHEN** `enabled=false`
- **THEN** 容器 MUST NOT 注册执行本 spec 检测逻辑的 Filter（或等价 no-op 且不拦截）
