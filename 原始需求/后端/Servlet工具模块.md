# Servlet 工具模块（quickboot-common/common/utils）原始需求

## 背景
- 多个安全过滤器/拦截器需要在“未进入 Controller”时直接返回 JSON 错误响应。
- 需要统一响应格式，并支持 i18n 文案。

## 目标
- 提供 `ServletUtils.writeResponse(...)`：输出统一的 `R.error(code,message)` JSON。
- 供防火墙过滤器等复用，避免重复写 response。

## 功能需求
- 方法：`writeResponse(HttpServletResponse response, Integer code, Object... args)`
  - HTTP 状态：固定返回 200（由业务码区分错误类型）
  - Content-Type：`application/json;charset=UTF-8`
  - message：通过 `I18nUtil.getMessage(code,args)` 获取
  - body：`R.error(code,message)` 的 JSON

## 依赖与边界
- 依赖：`R`、`I18nUtil`、Jackson `ObjectMapper`。
- 边界：不负责异常抛出与统一异常映射，仅负责“写出响应”。

## 验收标准
- 在 Filter 中调用 `ServletUtils.writeResponse(resp, ErrorCode.XXX)` 能返回标准 JSON。
- i18n 有词条时返回对应语言的 message。

