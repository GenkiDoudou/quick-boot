## Why

用户经 Query/Form 与 JSON 提交的字符串可能含脚本注入片段，需在进入业务前做 **启发式阻断** 并留审计日志；需支持 **忽略 URL**、**自定义正则**，且 **不扫描 multipart 中的文件上传部件**。原始说明见 `原始需求/后端/安全防火墙-XSS拦截.md`。

## What Changes

- 在 **quickboot-common** 增加 **`qc.security.firewall.xss`**：`enabled`（默认 `false`）、`ignoreUrls`、`customPatterns[]`；内置默认检测规则（脚本/event/DOM 危险片段等，与原始需求列表对齐）。
- **独立 Servlet Filter**：检测 Query/Form 文本值、**`application/json` body** 递归字符串；**`multipart/form-data` 中判定为「文件 part」的字段跳过本能力检测**，同一请求内 **非文件** 的文本 part **仍检测**。
- 命中：**WARN 级日志**（url、ip、命中上下文、规则标识）；**HTTP 200 + `R` JSON**，业务码 **`30701`**（防火墙下一档）、**i18n** 键 `String.valueOf(code)`、可选 **`forbiddenMessage`**，经 **`ServletUtils.writeResponse`**（**不使用** `R.error(400, 固定中文)` 作为对外契约）。
- **`customPatterns`**：规格层 **不**规定条数与单条长度上限（接受误杀与 ReDoS 风险由运维与实现权衡，见 design）。
- 与 **SQL 注入、敏感词** 等共用 **读 body → 可重复读 Wrapper** 思路；**Filter Order** 早于敏感词及 SQL 注入链上约定位置（见 design）。

## Capabilities

### New Capabilities

- `firewall-xss`：XSS 启发式防火墙的配置、检测范围（含 multipart 文件 part 跳过）、内置与自定义规则组合语义、命中日志与 **30701** 响应、Spring Boot 自动配置与 **FilterRegistrationBean` order**。

### Modified Capabilities

- （无）不修改其他 capability 的规范层条文；与既有防火墙的协作顺序写在 **design.md**。

## Impact

- **代码**：`quickboot-common`（Properties、Filter、AutoConfiguration、`AutoConfiguration.imports`）、`HttpCodes`、`i18n`、`CachedBody` 类是否复用现有 SQL 包内 Wrapper 或抽取公共组件（实现择优）。
- **行为**：启用后可能误拦含「像脚本」子串的合法文本；富文本接口依赖 **`ignoreUrls`** 或文件上传走 **文件 part 跳过**。
- **依赖**：Jackson、正则（JDK `Pattern`）；不强制新增三方 HTML 消毒库。
