## Why

业务存在拼接查询、模糊查询等入口，需在 Servlet 链上对常见 SQL 注入 payload 做**快速、可配置**的启发式拦截，并与现有统一响应（`R` + 业务码 + i18n）、防火墙家族配置风格对齐。原始说明见 `原始需求/后端/安全防火墙-SQL注入拦截.md`。

## What Changes

- 在 **quickboot-common** 增加 **`qc.security.firewall.sql-injection`** 配置（`enabled`、`ignoreUrls`、`keywords`）与自动配置注册的 **独立 Servlet Filter**。
- 对 **Query/Form** 与 **`application/json` body**（递归遍历 Map/列表/字符串）做关键字检测：`keywords` 非空则用配置，否则用**内置默认关键字列表**（策略偏**宁可拦宽**：子串级命中即拦截，接受误杀风险）。
- 命中时：**结构化告警日志**（url、ip、参数路径或名、命中关键字集合等，具体字段以 design 为准）、通过 **`ServletUtils.writeResponse`** 返回统一 JSON；业务码 **`HttpCodes` 新增常量（建议 30601）**，文案走 **i18n**（词条键 `String.valueOf(code)`），可选配置兜底话术时走 **`fallbackMessage`**（与 method/host 体系一致）。
- 与 **敏感词等需读 body 的防火墙** **共用 body 可重复读取策略**（例如统一 Request 包装或共享组件），但 **Filter 各自独立、顺序在 design 中固化**。

## Capabilities

### New Capabilities

- `firewall-sql-injection`：SQL 注入关键字防火墙的配置前缀、检测范围、忽略 URL、关键字来源与默认值、命中日志与统一错误响应（30601 + i18n）、与现有防火墙的协作边界（独立 Filter、顺序、共用 body 策略）。

### Modified Capabilities

- （无）本变更不修改其他 capability 的 **规范层**行为；与敏感词等 Filter 的集成通过实现侧共用 body 策略与顺序约定落在 **design.md**，不强制改写既有 `firewall-sensitive-word` 等主 spec 条文。

## Impact

- **代码**：`quickboot-common`（新 Properties、Filter、AutoConfiguration、`META-INF/.../AutoConfiguration.imports` 如需）、`HttpCodes`、i18n `messages*.properties`。
- **行为**：启用后对命中请求提前终止并返回 200 + `R` JSON；可能增加误拦合法输入（已接受的「拦宽」取舍）。
- **依赖**：优先复用现有 Jackson / `ServletUtils` / 与敏感词 Filter 相同的 body 读取路径；不引入新的重型 SQL 解析库（除非 design 阶段另有论证）。
