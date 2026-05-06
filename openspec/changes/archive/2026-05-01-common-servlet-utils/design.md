## Context

原始需求见 `原始需求/后端/Servlet工具模块.md`。仓库已具备 `io.github.genkidoudou.common.api.R`、`io.github.genkidoudou.common.i18n.I18nUtil` 与 Jakarta Servlet 依赖；网关/业务侧已约定 **HTTP 200 + JSON `code`**。需新增 `ServletUtils.writeResponse`，供 Filter 等在不进入 `Controller` 时输出与 `R.error` 一致的 JSON，并通过 `MessageSource` 取多语言 `msg`。

**已拍板**：`Integer code` 同时为 **响应体业务码** 与 **i18n 词条键的字符串形式**（`String.valueOf(code)`）。

## Goals / Non-Goals

**Goals:**

- 实现 `writeResponse(HttpServletResponse response, Integer code, Object... args)`：HTTP **200**、`Content-Type: application/json;charset=UTF-8`、`UTF-8` 写出。
- `msg`：`I18nUtil.getMessage(String.valueOf(code), toArray(args))`（将变参安全转为 `Object[]`）。
- body：`R.error(code, msg)` 经 `ObjectMapper` 序列化为 JSON（含 `timestamp`/`traceId` 等与 `R` 工厂一致的行为）。
- JavaDoc 写明：调用方需关注 **Filter 顺序**（在 `LocaleContextHolder` 可用的前提下调用，见风险）。

**Non-Goals:**

- 不实现 `@ControllerAdvice`、统一异常映射或业务错误码枚举。
- 不处理「响应已提交」之外的高级恢复策略（可做最小检测 + 文档约束）。
- 不替代 Spring Security 的入口点/拒绝访问机制（可与之并存）。

## Decisions

1. **ObjectMapper 来源**  
   - **首选**：通过 Spring 容器获取已配置的 `ObjectMapper`（如 Hutool `SpringUtil.getBean(ObjectMapper.class)`），与全局 Jackson 模块/日期格式保持一致。  
   - **备选**：取不到 Bean 时使用 `new ObjectMapper()` 并仅在 JavaDoc 标明「与全局行为可能不一致」（实现阶段二选一写清，避免静默分叉）。  
   - **取舍**：优先 Spring Bean，减少与 `application.yml` 中 Jackson 配置漂移。

2. **code 为 null**  
   - **做法**：防御性处理——若 `code == null`，使用 `HttpCodes.INTERNAL_ERROR`（500）或以 `0` 兜底并记录；**推荐**与 `HttpCodes.INTERNAL_ERROR` 对齐且 i18n 键同步为 `"500"` 或单独约定（实现与 spec 一致即可）。  
   - **备选**：直接 `NullPointerException` — 对 Filter 不友好。

3. **字符集与 Content-Type**  
   - **做法**：`setStatus(200)`、`setContentType("application/json;charset=UTF-8")`、`setCharacterEncoding("UTF-8")`，再 `getWriter()` 写 JSON。

4. **与 `I18nUtil` 的衔接**  
   - **做法**：变参 `args` 映射为 `Object[]`（无参时传 `null`）调用 `getMessage(String, Object[])`；不得新增 `I18nUtil` 的 `Object...` 重载以避免历史擦除问题。

## Risks / Trade-offs

- **[Risk]** Filter 早于 `LocaleResolver` 设置 `LocaleContextHolder` → `msg` 始终默认语言。  
  → **Mitigation**：文档约定 Filter `@Order`；或后续增强为从 `request.getLocale()` 显式解析（非本变更必选）。
- **[Risk]** Spring 未启动完成时调用 → 无 `MessageSource`/`ObjectMapper` Bean。  
  → **Mitigation**：`I18nUtil` 已有兜底行为；`ObjectMapper` 取 Bean 失败时的回退策略在设计 1 中说明。
- **[Risk]** `MessageSource` 键为纯数字字符串可能与团队「点分键」风格混用。  
  → **Mitigation**：在 `messages` 文件中统一业务码键规范；与产品线约定码段。

## Migration Plan

1. 合并后在本地编写单测或 Mock `HttpServletResponse` 验证 JSON 形态。  
2. 选取一个防火墙 Filter 将手写响应替换为 `ServletUtils.writeResponse`（按需）。  
3. 回滚：删除工具类调用，恢复各 Filter 原实现。

## Open Questions

- `code` 为 `null` 时最终兜底整数与 i18n 键是否统一为 `500`：实现阶段在代码与单测中敲定。  
- 是否在工具类内对 `response.isCommitted()` 做短路静默返回：若做，需单测或文档说明避免「以为已写出」。
