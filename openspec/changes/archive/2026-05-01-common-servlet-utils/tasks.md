## 1. ServletUtils 实现（quickboot-common）

- [x] 1.1 新增 `ServletUtils`：`writeResponse(HttpServletResponse response, Integer code, Object... args)`；设置 HTTP **200**、`Content-Type: application/json;charset=UTF-8`、字符编码 UTF-8；词条键 **`String.valueOf(code)`**，调用 `I18nUtil.getMessage(key, argsArray)`（变参正确转为 `Object[]`）；组装 `R.error(code.intValue()` 或使用 `HttpCodes` 兜底 `null` code 的策略并 JavaDoc）；用 `ObjectMapper` 写入 `HttpServletResponse`（优先 `SpringUtil.getBean(ObjectMapper.class)`，缺失时按 design 声明回退）
- [x] 1.2 JavaDoc：`LocaleContextHolder`/Filter `@Order`、`response.isCommitted()` 行为（若实现静默跳过）、**code 同时为业务码与 i18n 键** 的约定

## 2. 测试

- [x] 2.1 单元测试：使用 Spring `MockHttpServletResponse`（或等价）断言状态码、`Content-Type`、UTF-8、JSON 含 `code`/`msg`；覆盖「有 i18n 词条 / 缺失词条兜底」「无占位 args」「带占位 args」三种之一组合
- [x] 2.2 （可选）在测试上下文注册最小 `MessageSource` + `LocaleContextHolder` 验证 Locale 文案切换

## 3. 文档

- [x] 3.1 在 `AGENTS.md` 的 quickboot-common 小节追加一句：`ServletUtils` 用途、词条键使用业务码字符串、Filter 内需保证 Locale 链路可用时机
