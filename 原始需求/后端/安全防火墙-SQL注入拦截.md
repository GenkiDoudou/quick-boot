# 安全防火墙：SQL 注入拦截（firewall/sqlinjection）原始需求

## 背景
- 业务存在拼接查询/模糊查询等输入场景，需在入口对常见 SQL 注入 payload 做快速拦截。
- 需要可配置关键字列表，并支持忽略部分 URL。

## 目标
- 提供对请求参数与 JSON body 的 SQL 关键字检测过滤器。
- 命中时阻断请求并返回统一错误响应。

## 功能需求
- 启用条件：`qc.security.firewall.sql-injection.enabled=true`
- 检测范围：
  - Query/Form 参数
  - JSON body（递归遍历 Map/数组/字符串）
- 忽略 URL：`ignoreUrls[]`（Ant 风格）
- 关键字来源：
  - 配置 `keywords[]` 非空则使用配置
  - 否则使用内置默认关键字（select/union/drop/;/*... 等）
- 命中处理：
  - 记录告警日志：url/ip/参数名/命中关键字列表
  - 返回错误：`R.error(400, "请求参数包含非法字符")`

## 配置项
- 前缀：`qc.security.firewall.sql-injection`
  - `enabled`
  - `ignoreUrls[]`
  - `keywords[]`

## 验收标准
- 启用后 `?q=1%20union%20select...` 等典型输入会被拦截。
- JSON body 中嵌套字段命中关键字也会被拦截。

