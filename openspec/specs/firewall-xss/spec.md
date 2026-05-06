# firewall-xss

## Purpose

`quickboot-common` 提供的 **安全防火墙 — XSS 启发式拦截**：对 **Query/Form**、**`application/json`** 递归字符串及 **`multipart/form-data`** 中**非文件**文本部件做内置危险片段 + **`customPatterns`** 正则检测；配置前缀 **`qc.security.firewall.xss`**；命中业务码 **30701**、**WARN** 日志、**`ServletUtils` + i18n**；**`FilterOrder` = `HIGHEST_PRECEDENCE + 3`**，早于 SQL（+4）与敏感词（+5)，并对 body 使用可重复读包装；**`customPatterns` 规格不设条数/长度上限**。

## Requirements

### Requirement: 配置前缀与开关

系统 MUST 提供安全防火墙能力 `firewall-xss`，并以 **`qc.security.firewall.xss`** 作为配置前缀。系统 MUST 支持 **`enabled`** 且默认值 MUST 为 **`false`**；当 **`enabled=false`** 时系统 MUST **不**对请求应用本能力的 XSS 检测与拦截逻辑。

#### Scenario: 默认关闭不拦截

- **WHEN** 未配置 `qc.security.firewall.xss.enabled` 或其值为 `false`
- **THEN** 任意请求 MUST NOT 因本能力而被拦截或改写 body

#### Scenario: 显式开启后生效

- **WHEN** `qc.security.firewall.xss.enabled=true`
- **THEN** 系统 MUST 对未命中 `ignoreUrls` 的请求按本 spec 执行检测并在命中时拦截

### Requirement: ignoreUrls

系统 MUST 支持 **`ignoreUrls[]`**（Ant 风格路径）。当请求路径匹配 **任一** 项时，本能力 MUST **完全跳过**（不对 Query、Form、JSON body、multipart 文本字段做本能力检测）。

#### Scenario: 命中忽略路径则跳过

- **WHEN** `ignoreUrls` 包含 `/richtext/**` 且请求路径为 `/richtext/save`
- **THEN** 本能力 MUST NOT 拦截该请求（即使参数中含疑似脚本片段）

### Requirement: 内置检测规则与 customPatterns

系统 MUST 实现 **内置默认规则集合**，覆盖至少下列危险形态（大小写不敏感语义）：包含 `<script` 的标记、`javascript:` 协议片段、`on` 前缀的事件处理器写法（如 `onclick=` 一类）、`iframe` / `object` / `embed` / `svg` 相关标记片段、`expression(`、`data:text/html`、`eval(`、`document.`、`window.`、`alert(` 等（与实现维护的完整列表一致并在 JavaDoc 可查阅）。

系统 MUST 支持 **`customPatterns[]`**（Java 正则字符串列表）；**任一** 内置规则 **或** **任一** 自定义正则 **匹配成功**即视为该字符串命中。系统 MUST **不**在规格层对 `customPatterns` 的 **条数**或**单条模式长度**设定上限。

#### Scenario: 内置规则命中

- **WHEN** `enabled=true` 且某 query 参数值包含 `<script`（任意大小写组合）
- **THEN** 系统 MUST 拦截并按命中响应条款返回

#### Scenario: 自定义正则命中

- **WHEN** `customPatterns` 含某正则且 JSON 某字符串值被该正则匹配
- **THEN** 系统 MUST 拦截

### Requirement: 检测范围 Query、Form、JSON 与 multipart 文本字段

当本能力生效且未命中 `ignoreUrls` 时，系统 MUST 对 **Query 与 x-www-form-urlencoded 等常规表单参数**的取值做检测（多值 MUST 逐一检测）。

当请求为 **`multipart/form-data`** 时，系统 MUST **跳过**被判定为 **文件上传 part** 的内容检测：**若某 part 的 `Content-Disposition` 包含非空的 `filename` 参数语义**，则该 part MUST **不**参与 XSS 规则匹配；**其余无文件名的文本 part** MUST 仍参与检测。

当请求的 **`Content-Type` 兼容 `application/json`** 且 body 非空时，系统 MUST 解析 JSON，对其中的 **字符串值**逐一检测；对 **对象与数组**MUST **递归**遍历；对 **非字符串标量** MUST **不参与**规则匹配。

当 **`Content-Type` 不兼容 `application/json`** 且非 multipart 表单已在前述条款覆盖时，系统 MUST **不**为本能力目的解析其他 raw body（与现有防火墙家族语义一致）。

#### Scenario: JSON 嵌套字符串命中

- **WHEN** `enabled=true` 且 body 为 JSON，深层某字符串值命中内置或自定义规则
- **THEN** 系统 MUST 拦截

#### Scenario: multipart 文件 part 跳过、文本字段仍检测

- **WHEN** `enabled=true` 且请求为 `multipart/form-data`，某 part 带 `filename="a.bin"`，另一 part 为无 `filename` 的文本字段且值命中规则
- **THEN** 系统 MUST 拦截（由文本字段触发）；文件 part 内容 MUST NOT 单独触发本能力匹配

#### Scenario: JSON 解析失败不按 XSS 拦截

- **WHEN** `Content-Type` 为 `application/json` 但 body 非合法 JSON
- **THEN** 本能力 MUST NOT 仅因「无法解析 JSON」而拦截；且 MUST 仍允许下游读取原始 body（与 design 一致）

### Requirement: 命中日志

当请求因本能力被拦截时，系统 MUST 输出 **WARN** 或以上级别日志，且 MUST 包含 **请求路径**、**客户端 IP**、**HTTP 方法**、**命中规则标识**（内置规则名或自定义模式索引/摘要）；并 SHOULD 包含 **参数名、part 名或 JSON 路径**（无需默认输出完整 body）。

#### Scenario: 拦截可审计

- **WHEN** 某请求因本能力被拦截
- **THEN** 日志 MUST 能区分命中原因（至少含规则标识与路径/参数上下文）

### Requirement: 命中响应与国际化

当请求因本能力被拦截时，系统 MUST 写出 **HTTP 200** + 与 **`R.error`** 一致的 JSON；业务 **`code`** MUST 为 **`30701`**（与 **`HttpCodes`** 常量一致）。

系统 MUST 以 **`String.valueOf(code)`** 为 i18n 键解析 **`msg`**；i18n **未命中**时 MUST 使用 **`forbiddenMessage`** 兜底；**`forbiddenMessage` 空白**时实现 MAY 使用默认文案（JavaDoc 说明）。

#### Scenario: i18n 命中

- **WHEN** 存在键 **`30701`** 的文案且请求被拦截
- **THEN** 响应 JSON 的 **`msg`** MUST 为 i18n 结果

#### Scenario: i18n 未命中时使用 forbiddenMessage

- **WHEN** 键 **`30701`** 不存在且 `forbiddenMessage="非法脚本"` 且请求被拦截
- **THEN** 响应 JSON 的 **`msg`** MUST 为 **`非法脚本`**

### Requirement: Spring Boot 集成与 Filter 顺序

能力 MUST 以 **quickboot-common** 自动配置注册 **独立** Servlet Filter，并绑定 **`@ConfigurationProperties`**。

本 Filter 的 **`FilterRegistrationBean` order** MUST 为 **`Ordered.HIGHEST_PRECEDENCE + 3`**，从而 **早于** SQL 注入防火墙（`+4`）与敏感词防火墙（`+5`），并 **晚于** CORS Filter（`HIGHEST_PRECEDENCE`）。未命中时系统 MUST 以 **可重复读 body** 的方式将请求传入下游 Filter。

#### Scenario: 关闭时不注册

- **WHEN** `enabled=false`
- **THEN** 容器 MUST NOT 注册执行本 spec 检测逻辑的 Filter（或等价 no-op 且不拦截）
