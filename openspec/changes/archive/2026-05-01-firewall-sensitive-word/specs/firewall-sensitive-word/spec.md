## ADDED Requirements

### Requirement: 开关与忽略 URL

系统 MUST 通过 **`qc.security.firewall.sensitive-word.enabled`** 控制敏感词过滤器是否生效；`enabled` 为 **`false`** 或未配置为启用语义时，MUST **不**对请求参数或请求体应用敏感词处理。系统 MUST 支持 **`ignoreUrls`**（**Ant** 风格路径）；当请求路径匹配 **任一** `ignoreUrls` 项时，本能力 MUST **完全跳过**（参数与 JSON body 均不处理）。

#### Scenario: 关闭时不生效

- **WHEN** `qc.security.firewall.sensitive-word.enabled` 为 `false` 或未配置为真
- **THEN** 对请求 **MUST NOT** 改写 `getParameter`/`getParameterValues` 的返回值，**MUST NOT** 改写 `application/json` 请求体

#### Scenario: 命中 ignoreUrls 跳过

- **WHEN** `enabled=true` 且 `ignoreUrls` 含 `/login`，请求路径为 `/login`
- **THEN** 本能力 MUST NOT 对参数或 JSON body 做敏感词处理

### Requirement: 词库加载与合并规则

系统 MUST 在 **应用启动阶段**（Bean 就绪前或就绪时单次）加载词库并构造 **只读**敏感词引擎。词库来源 MUST 支持配置中的 **`whiteList`**、**`blackList`** 路径列表；路径 MUST 支持 **`classpath:`** 与 **`file:`** 前缀，并由 Spring **`ResourceLoader`** 解析。资源文件 MUST 忽略 **空行** 与以 **`#`** 开头的 **注释行**。黑名单 MUST 在 **`sensitive-word` 内置默认词库** 基础上 **追加** 自定义词；白名单 MUST 对命中词 **放行**（即白名单优先于黑名单/默认库的阻断或替换语义，以 houbb 配置能力为准实现）。

#### Scenario: 注释与空行被跳过

- **WHEN** 某黑名单资源文件含 `# comment` 与空行及有效词行
- **THEN** 仅有效词行 MUST 参与引擎构建

#### Scenario: 启动后词库不变

- **WHEN** 应用已启动且引擎已构建
- **THEN** 修改磁盘上词库文件 **MUST NOT** 自动反映到引擎（除非重启或另行定义之能力）

### Requirement: Query 与 Form 参数过滤

当本能力生效且未命中 `ignoreUrls` 时，系统 MUST 通过 **包装 `HttpServletRequest`**，使 **`getParameter`** 与 **`getParameterValues`** 的返回值对调用方表现为已按 **`strategy`** 处理（**`REPLACE`** 或 **`THROW`** 语义与 JSON 字符串一致）。

#### Scenario: REPLACE 策略下参数被掩码

- **WHEN** `strategy=REPLACE` 且某 query 参数值含敏感词
- **THEN** `getParameter` MUST 返回掩码后的字符串（houbb 替换规则）

### Requirement: JSON body 范围与递归

当本能力生效且未命中 `ignoreUrls`，且请求的 **`Content-Type` 表示 `application/json`**（可含 `charset` 等参数，类型名大小写不敏感）时，系统 MUST 读取请求体、解析为 **JSON 结构**，对其中的 **String** 值按 **`strategy`** 处理；对 **Map** 与 **Collection** MUST **递归**遍历元素；对 **非 Map/List 的标量**（如 `Number`、`Boolean`、`null`）MUST **原样保留**；处理完成后 MUST 使下游读取到的 body 为 **重写后的** JSON 字节流（与 UTF-8 编码处理在实现 JavaDoc 中说明）。

当 **`Content-Type` 非 `application/json`** 时，系统 MUST **不**为敏感词目的重写 body 流（Query/Form 仍按前述条款处理）。

#### Scenario: 嵌套对象与数组内字符串被处理

- **WHEN** body 为 `application/json` 且含嵌套对象与数组，数组内含字符串字段含敏感词
- **THEN** 该字符串 MUST 按 `strategy` 被替换或触发 `THROW` 语义

#### Scenario: 非 JSON Content-Type 不改 body

- **WHEN** `Content-Type` 为 `text/plain` 且 body 含敏感词明文
- **THEN** 系统 MUST NOT 为本能力重写 body 流

### Requirement: 策略 REPLACE 与 THROW

系统 MUST 支持配置 **`strategy`** 取 **`REPLACE`** 或 **`THROW`**（大小写不敏感之接受方式由实现文档化）。**`REPLACE`** MUST 使用 **`sensitive-word`** 库的替换能力（与需求文档「`*`」在库行为下对齐）。**`THROW`** MUST 在检测到敏感词时抛出 **`SensitiveWordException`**（或项目统一命名之等价类型），且 MUST 携带 **业务码 `30501`** 与 **命中词**信息。

#### Scenario: THROW 时需失败且可定位词

- **WHEN** `strategy=THROW` 且输入含敏感词
- **THEN** 处理 MUST 失败，且异常 MUST 携带业务码 `30501` 与命中词（或等价访问器）

### Requirement: Filter 错误响应形态

当 **`THROW`** 路径在 Filter 内抛出 `SensitiveWordException` 时，Filter MUST 捕获并写出 **HTTP 200** + **`R` 形态 JSON**，`code` MUST 为 **`30501`**，且 MUST 填充 **`traceId`**（若项目约定 MDC 键一致）并与 **`I18nUtil`/MessageSource** 词条键 **`30501`** 对齐（于 **Locale 已写入 `LocaleContextHolder`** 之后写出）。

#### Scenario: 不进入业务 Controller

- **WHEN** `strategy=THROW` 且参数命中敏感词
- **THEN** Filter MUST 在写出错误 JSON 后结束请求，业务 Controller MUST NOT 收到该请求的正常处理路径（除非实现显式 forward，本 spec **禁止**）

### Requirement: Filter 注册顺序

敏感词 Filter MUST 以 **尽可能靠前** 的 **`FilterRegistrationBean` order** 注册（**`Ordered.HIGHEST_PRECEDENCE`** 或文档说明的等价最早顺序），以保证在其它可能 **读取请求体** 的组件之前完成请求包装与（若适用）body 重写。

#### Scenario: order 早于典型业务 Filter

- **WHEN** 查看 `FilterRegistrationBean` 的 order 与同类防火墙 Filter
- **THEN** 敏感词 Filter 的 order MUST 不晚于（数值上不大于或等于「最早」约定）其它非基础设施 Filter 的默认注册（除非设计文档说明的例外）

### Requirement: Spring Boot 自动配置

系统 MUST 提供 Spring Boot **自动配置**，在 **`qc.security.firewall.sensitive-word.enabled=true`** 且为 **Servlet Web** 应用时注册 Filter 与配置属性 Bean；依赖 **`sensitive-word`**、**`ResourceLoader`**、**Jackson**；并在 **`META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`** 注册。

#### Scenario: 关闭时不注册 Filter

- **WHEN** `enabled=false`
- **THEN** MUST NOT 注册敏感词 Filter Bean（或等价为 no-op，以不会改写请求为准）
