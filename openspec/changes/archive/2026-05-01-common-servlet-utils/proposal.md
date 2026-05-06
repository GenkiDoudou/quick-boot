## Why

安全防火墙、过滤器等常在未进入 Spring MVC `Controller` 前终止请求；若各处手写 `HttpServletResponse` 与 JSON，易出现响应体形状不一致、难以复用统一业务码与国际化文案。在已具备 `R<T>` 与 `I18nUtil` 的前提下，需要在 `quickboot-common` 补齐一次性的「写出标准错误 JSON」工具，降低重复与联调成本。

## What Changes

- 在 `quickboot-common` 提供工具类（如 `ServletUtils`）及 `writeResponse(HttpServletResponse response, Integer code, Object... args)`。
- **HTTP**：固定 **`200`**，与全局约定「业务成败看 `body.code`」一致；**Content-Type** 固定为 **`application/json;charset=UTF-8`**（并约束 UTF-8 编码写入）。
- **文案**：使用 `I18nUtil.getMessage(String, Object[])`；**词条键约定（已拍板选项 A）**：将 `Integer code` 转为 **`String.valueOf(code)`** 作为 `MessageSource` 的键，与响应体中的整数 **业务码** 同一取值。
- **Body**：序列化 `R.error(code, message)`（`message` 为 i18n 解析结果）。
- **BREAKING**：无（新增 API；不改变既有过滤器行为，除非你方主动改为调用新方法）。

## Capabilities

### New Capabilities

- `common-servlet-utils`：`quickboot-common` 内在 Filter/前置链路中写出统一 `R` 错误 JSON 的能力，及对 `ObjectMapper`、`I18nUtil`、`R` 的依赖与边界约束。

### Modified Capabilities

- （无）不修改 `openspec/specs/common-response-paging/spec.md` 中 `R` 的规范性要求本体；仅作为该能力的调用侧补充。

## Impact

- **代码**：新增 `ServletUtils`（包名以实现阶段为准，建议在 `common` 下设 `web`/`servlet` 类工具包），可能包含对 Spring 容器中 `ObjectMapper` 的获取逻辑（见 `design.md`）。
- **依赖**：沿用已有 **`jakarta.servlet-api`**、`R`、`I18nUtil`、`jackson-databind`/`ObjectMapper`；不新增硬性外部系统依赖。
- **配置**：`messages*.properties` 需按需维护 **数字字符串形式**词条键（例如 `40301=...`），与业务码分段规划一致。
- **运维/网关**：与现有「HTTP 200 + JSON」策略一致。
