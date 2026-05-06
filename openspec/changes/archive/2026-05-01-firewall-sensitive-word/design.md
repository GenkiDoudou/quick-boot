## Context

- 原始需求见 `原始需求/后端/安全防火墙-敏感词过滤.md`；仓库已声明 **`sensitive-word`**（houbb）与 `application.yml` 占位配置，`enabled` 命名将在本变更与 **BREAKING** 迁移中与 **`firewall.headers.enabled`** 对齐。
- **`common` 内 `SensitiveJackson*`** 属**响应脱敏**，与本能力（**请求侧**敏感词）分离，避免包内命名混淆。

## Goals / Non-Goals

**Goals:**

- **`qc.security.firewall.sensitive-word.enabled=true`** 时注册 Filter，**尽早**于链上执行（`FilterRegistrationBean` **`Ordered.HIGHEST_PRECEDENCE`** 或等价，确保在消费 body 的组件之前完成包装与处理）。
- **Query/Form**：通过 **包装 `HttpServletRequest`**，使 `getParameter` / `getParameterValues` 返回已按策略处理后的值（命中 `ignoreUrls` 则原样）。
- **JSON body**：**仅**当请求 **`Content-Type` 可判定为 `application/json`**（可含 `charset`）时读取、解析、递归处理并回灌 **`ServletInputStream`**；其它 body 类型 **不重写流**。
- **JSON 递归**：根对象视作 **Map**；对 **Map**、**Collection**（`List`/`Set` 等）递归；元素为 **String** 时过敏感词引擎；**非 Map/List 的标量**（`Number`、`Boolean`、`null` 等）**原样保留**。
- 词库：**启动期**用 **`ResourceLoader`** 解析 `classpath:` / `file:` 列表，支持 **`#` 行注释**与空行；黑名单在内置默认上追加；白名单放行；**不可热更**。
- 策略 **`REPLACE`**（houbb 替换语义，与需求「`*`」在文案层一致）与 **`THROW`**（`SensitiveWordException`，业务码 **30501**，携带命中词）；**Filter 内**写出 **`R` 形态 JSON**、**HTTP 200**，与 **`ServletUtils.writeResponse`**、**`I18nUtil`/traceId** 等现有约定对齐（Locale 已正确设置后方可取词条）。

**Non-Goals:**

- `multipart/form-data`、流式超大 body 专项治理、词库热加载、入库后审核。
- 修改 **`common-field-desensitization`** 行为。

## Decisions

1. **开关键名：`enabled`**  
   - 与 `qc.security.firewall.headers.enabled` 一致；**废弃/迁移** 旧键 `enable`（见 proposal **BREAKING**）。

2. **JSON 限定 `application/json`**  
   - 用 **`Content-Type` 解析**（忽略大小写、可带参数）；非 JSON 不读 body，避免误伤 `text/plain` / 自定义类型。

3. **递归模型：Map + Collection**  
   - Jackson **`JsonNode` 或 `Object` 树**（Map/List 结构）均可，规范上表述为 Map/List 语义；**不**将 JSON 数组根丢给「仅 Map」逻辑。

4. **引擎：`com.github.houbb:sensitive-word`**  
   - **REPLACE** / **contains** 等 API 由实现选型；**不**手写 AC 自动机。

5. **Filter 顺序：链首**  
   - **`Ordered.HIGHEST_PRECEDENCE`**（或明确常量少于此后的幂等、安全头 Filter）；文档注明与 **`ContentCachingRequestWrapper`**（或等价）配合，避免重复读 bug。

6. **词库生命周期**  
   - **Bean 初始化完成即构建只读** `WordAllow`/`WordDeny`（或库推荐 Facade），后续不变。

7. **THROW 响应**  
   - 业务码 **`30501`**；新增 **`HttpCodes`** 常量（任务）；**`messages*.properties`** 键 **`30501`**（与其它业务码一致）；**Filter** `catch SensitiveWordException` 后 **`ServletUtils.writeResponse`**（或项目内与 Filter 一致的写出工具），**不**依赖未参与的 DispatcherServlet 异常解析（除非设计显式统一为 HandlerExceptionResolver — 本设计采 **Filter 内终结** 以降低链路耦合）。

8. **`ignoreUrls`**  
   - **Ant** 风格，与 `firewall-security-headers` 的 `excludeUrls` 行为描述方式一致；命中则 **整请求** 跳过。

## Risks / Trade-offs

- **[Risk] 大 JSON body 内存** → **Mitigation**：与 Spring 默认 `character`/`InputStream` 读入一致；可在实现中加 **可选** `max-body-size`（本 design 不强制，Open Question）。
- **[Risk] 顺序错误导致 body 已读** → **Mitigation**：**HIGHEST_PRECEDENCE** + 集成测试校验「含敏感词 JSON 被拦截/替换」。
- **[Risk] REPLACE 与「全 `*`」字面不完全一致** → **Mitigation**：spec 写「以 houbb 替换规则为准」；验收以「明显掩蔽」为准。

## Migration Plan

- 将 `application.yml` 中 **`enable`** 改为 **`enabled`**；部署说明一条即可。
- **回滚**：`enabled=false` 或排除自动配置类。

## Open Questions

- 是否在配置中增加 **`max-json-body-bytes`**（默认沿用容器/框架不设限）可由实现阶段按性能测试再定。
