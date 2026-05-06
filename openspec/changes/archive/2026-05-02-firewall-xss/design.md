## Context

- 仓库已有 **CORS**（`HIGHEST_PRECEDENCE`）、**SQL 注入**（`+4`）、**敏感词**（`+5`）、**Method/Host**（`+10`）等 Filter；SQL 使用 **读满 body + `CachedBodyHttpServletRequestWrapper`** 再交给下游。
- 原始需求文档列出内置危险片段与 **`customPatterns[]`**；产品明确：**规格不对 custom 条数/长度设上限**。
- 需在 **`multipart/form-data`** 下 **跳过文件上传 part**，其余文本 part 仍检测。

## Goals / Non-Goals

**Goals:**

- **`qc.security.firewall.xss`** + 独立 Filter；**`30701`** + i18n + `forbiddenMessage`；内置规则 + `customPatterns`（**全部编译为 `Pattern`，大小写不敏感建议默认**）。
- Query/Form（含 multipart **非文件** 字段）、JSON 字符串递归检测；**文件 part 不读内容做 XSS 规则匹配**（避免对大二进制做无意义扫描）。
- **早于** SQL 注入与敏感词执行（见 Order），对 **原始** 输入检测；未命中则 **缓存 body** 向下游传递。

**Non-Goals:**

- 不做浏览器 DOM 上下文 XSS 终结方案；不替代输出编码 / CSP。
- 规格层 **不**规定 `customPatterns` 数量与单模式长度上限（见 Risks）。

## Decisions

1. **业务码与文案**  
   - `HttpCodes` 新增 **`XSS_SCRIPT_DETECTED = 30701`**（命名以实现为准）。  
   - `ServletUtils.writeResponse(..., forbiddenMessage)`；`messages*.properties` 键 **`30701`**。

2. **内置规则形态**  
   - 以 **`java.util.regex.Pattern`** 实现（或等价），**`CASE_INSENSITIVE | DOTALL`** 等按规则需要选用；覆盖原始需求中的典型模式：`<script`、`javascript:`、`on\w+\s*=`、`<iframe`/`<object`/`<embed`/`<svg`、`expression(`、`data:text/html`、`eval(`、`document.`、`window.`、`alert(` 等（精确列表在实现常量中维护并在 JavaDoc 列示）。  
   - **`customPatterns[]`**：按字符串编译为 `Pattern`；**编译失败**时：实现可选择 **启动失败** 或 **跳过该条并 WARN**——推荐 **启动失败**（fail-fast）并在 JavaDoc 说明。

3. **`multipart/form-data` 文件 part 判定**  
   - 若某 part 的 **`Content-Disposition`** 含 **`filename=`**（非空文件名语义），则视为 **文件 part**：**不对该 part 的 body 做 XSS 检测**。  
   - **无 `filename` 的普通表单字段**（含文本）**仍检测**。  
   - **Query 字符串**始终按现有语义检测（与是否 multipart 无关）。

4. **JSON body**  
   - 与 SQL 防火墙对齐：**`Content-Type` 兼容 `application/json`** 时解析；**解析失败**则 **不按 XSS 规则拦截**（仅 TRACE/DEBUG 记录），仍 **缓存原始 body** 向下游传递。

5. **Filter `FilterRegistrationBean` order**  
   - **`Ordered.HIGHEST_PRECEDENCE + 3`**：**早于** SQL 注入（`+4`）与敏感词（`+5`），**晚于** CORS（`HIGHEST_PRECEDENCE`）。  
   - 理由：与 SQL 相同，需在敏感词改写前检查 **原始** 输入；XSS 与 SQL 顺序取 **XSS 略早**（可先拦明显脚本再耗 SQL 关键字）。

6. **`customPatterns` 无上限**  
   - 规格不限制条数与长度；实现 **MAY** 内部做防护（例如单次请求超时、Pattern 编译缓存），属实现细节，**不改变**「配置可任意扩展」的产品表述。

## Risks / Trade-offs

| 风险 | 缓解 |
|------|------|
| 自定义正则 ReDoS / 大 body CPU | 运维把控规则；实现可文档化建议；可选内部防护但不写进规格上限 |
| 合法文本误杀（含 `javascript:` 子串等） | `ignoreUrls`、文件 part 跳过；业务侧拆分接口 |
| `filename=` 判定边界 | JavaDoc 写明与 RFC 2183 / Spring `Multipart` 解析一致性假设 |

## Migration Plan

- 默认 **`enabled=false`**，零行为变化；开启后评估富文本与上传接口的 **`ignoreUrls`**。  
- 回滚：关配置或下线自动配置。

## Open Questions

- **空 filename**：若 `filename=""` 是否仍视为文件 part——实现建议按「有 filename 参数即文件 part」与常见容器行为对齐。
