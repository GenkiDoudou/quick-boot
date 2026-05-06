## 1. 常量、i18n 与配置

- [x] 1.1 在 `HttpCodes` 中新增 **30701** 常量（如 `XSS_SCRIPT_DETECTED`）及 JavaDoc
- [x] 1.2 在 `i18n/messages*.properties` 与测试资源中增加键 **`30701`**
- [x] 1.3 新增 `XssFirewallProperties`（`qc.security.firewall.xss`）：`enabled`、`ignoreUrls`、`customPatterns`、`forbiddenMessage`；默认值与 spec 一致

## 2. 规则引擎

- [x] 2.1 实现内置 **默认 `Pattern` 集合**（含 spec 列出的危险形态），JavaDoc 列完整清单与 flags
- [x] 2.2 将 `customPatterns` 编译为 `Pattern` 列表；**非法正则**按 design 选择 fail-fast 并文档化
- [x] 2.3 提供对单字符串的匹配 API，返回**首个命中**的内置规则名或 custom 下标（供日志）

## 3. Filter 与 body

- [x] 3.1 实现 `XssFirewallFilter`（`OncePerRequestFilter`）：`ignoreUrls`、Query/Form 全量值检测
- [x] 3.2 **`multipart/form-data`**：使用 `getParts()`（或项目约定等价方式）遍历；**带 `filename` 的 part 跳过内容扫描**；无 `filename` 的文本 part 读入为字符串后检测（大 part 的内存策略在 JavaDoc 说明）
- [x] 3.3 JSON：与 SQL 防火墙一致 `Content-Type` 判断、树递归、**解析失败不拦截**、仍缓存 body
- [x] 3.4 未命中时复用/对齐 **`CachedBodyHttpServletRequestWrapper`**（与 `sqlinjection` 包内类共享或抽取公共类），`doFilter` 传递包装请求

## 4. 命中、注册与自动配置

- [x] 4.1 命中：`ServletUtils.writeResponse(..., 30701, forbiddenMessage)`；**WARN** 日志含 path/ip/method/规则标识/参数或路径上下文
- [x] 4.2 `FilterRegistrationBean`：**`Ordered.HIGHEST_PRECEDENCE + 3`**；注释写明与 CORS、SQL（+4）、敏感词（+5）、Method/Host（+10）关系
- [x] 4.3 `XssFirewallAutoConfiguration` + `@ConditionalOnProperty` + Servlet Web；**更新** `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`（可与 SQL 自动配置条目相邻便于阅读）

## 5. 测试

- [x] 5.1 `enabled=false` 不注册 Filter（或等价）
- [x] 5.2 `ignoreUrls` 命中跳过
- [x] 5.3 query / JSON 命中内置规则 → **`code=30701`**
- [x] 5.4 `customPatterns` 命中
- [x] 5.5 multipart：带 `filename` 的 part 不触发拦截；同请求另有一文本字段命中 → 拦截
- [x] 5.6 与 SQL（+4）、敏感词（+5）同时启用时 **order** 校验及链式 body 可读（Mock 或 `WebApplicationContextRunner`）
