# 安全防火墙：XSS 拦截（firewall/xss）原始需求

## 背景
- 用户输入（query/body）可能包含脚本注入风险，需要在进入业务逻辑前阻断。
- 需要支持忽略某些 URL（如富文本提交接口）以及自定义检测规则。

## 目标
- 提供请求参数与 JSON body 的 XSS 检测过滤器。
- 检测命中时返回统一错误响应并记录告警日志。

## 功能需求
- 启用条件：`qc.security.firewall.xss.enabled=true`
- 检测范围：
  - Query/Form 参数
  - `Content-Type: application/json` 的 body（递归遍历 Map/数组/字符串）
- 忽略 URL：`ignoreUrls[]`（Ant 风格）
- 检测规则：
  - 内置默认模式：`<script>`、`javascript:`、`on*=`、`iframe/object/embed/svg`、`expression(`、`data:text/html`、`eval(`、`document.`、`window.`、`alert(`
  - 自定义正则：`customPatterns[]`
- 命中处理：
  - 记录日志：url/ip/命中参数/命中规则
  - 返回错误：`R.error(400, "请求参数包含非法脚本")`

## 配置项
- 前缀：`qc.security.firewall.xss`
  - `enabled`（默认 `false`）
  - `ignoreUrls[]`
  - `customPatterns[]`

## 验收标准
- 启用后携带明显脚本的参数会被拦截，接口返回统一 JSON 错误。
- 命中 `ignoreUrls` 的请求不做检测。

