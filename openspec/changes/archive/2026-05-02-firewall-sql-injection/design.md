## Context

- **现状**：`quickboot-common` 已有多项防火墙 Filter（CORS、敏感词、Method/Host 等）。敏感词 Filter（`SensitiveWordFirewallFilter`）对 JSON 请求采用 **`StreamUtils.copyToByteArray(request.getInputStream())`** 读满 body，再用 **`SensitiveWordHttpServletRequestWrapper`** 向下游提供可重复读的流；Query/Form 经包装器暴露。
- **约束**：统一响应须 **HTTP 200 + `R` JSON**；业务码与 **`HttpCodes`**、词条键 **`String.valueOf(code)`**、`ServletUtils.writeResponse`（含 **fallbackMessage**）对齐。
- **原始需求**：`原始需求/后端/安全防火墙-SQL注入拦截.md`。

## Goals / Non-Goals

**Goals:**

- 提供 **`qc.security.firewall.sql-injection`** 开关与 **`ignoreUrls`**、**`keywords`**，对 Query/Form 与 **JSON body 字符串**做关键字级检测（**宁可拦宽**：典型子串匹配，大小写不敏感建议默认）。
- 命中：**告警日志** + **`30601`**（常量名实现自定，与 `HttpCodes` 一致）+ i18n + 可选兜底文案。
- 与敏感词等能力 **共用「读 body → 缓存 → Wrapper」思路**，保持 **独立 Filter Bean**；**Filter Order** 可复现、可文档化。

**Non-Goals:**

- 不做完整 SQL 语法解析或参数化替代方案教学；不承诺零误杀。
- 不强制修改敏感词等既有 Filter 的 **对外规范条文**（行为不变，仅实现侧协调顺序与可选抽取）。

## Decisions

1. **业务码与文案**  
   - 新增 **`HttpCodes.SQL_INJECTION_DETECTED = 30601`**（命名以实现为准）。  
   - i18n 键 **`30601`**；配置项提供 **`forbiddenMessage`**（空白则实现定义最终兜底，与 method/host 命名一致便于记忆）。

2. **关键字与匹配语义**  
   - `keywords` 非空 → 仅用配置列表；为空 → **内置默认列表**（实现维护常量集，覆盖 `select`、`union`、`drop`、`--`、`;/*` 等常见片段，可文档化）。  
   - **匹配**：对参数值与 JSON 中每个字符串值做 **包含检测**（拦宽）；**大小写不敏感**（实现.normalize 后匹配）。

3. **JSON 与 Content-Type**  
   - 与敏感词一致：`Content-Type` **兼容** `application/json` 时解析；非法 JSON：可在实现中选择 **直接放行后续由业务报错** 或 **视为命中风险**——推荐 **放行链**（避免非 JSON 误伤），在 **Open Questions** 留痕若需改为拦截。

4. **共用 body 策略与 Filter 顺序**  
   - **原因**：必须在 **敏感词改写 body 之前**对 **原始用户输入**做 SQL 关键字检测（否则 REPLACE 可能掩盖恶意片段）。  
   - **顺序**（数值越小越早执行，与现有注册一致）：  
     - `FirewallCorsAutoConfiguration`：`Ordered.HIGHEST_PRECEDENCE`  
     - **SQL 注入**：`Ordered.HIGHEST_PRECEDENCE + 4`（新建）  
     - **敏感词**：现 `HIGHEST_PRECEDENCE + 5`  
     - **Method/Host**：现 `HIGHEST_PRECEDENCE + 10`  
   - **实现路径**：SQL 注入 Filter **首先** `copyToByteArray`；若未命中则构造 **与敏感词兼容的「仅缓存 body、不改写」包装**，或对 **`SensitiveWordHttpServletRequestWrapper` 抽取/下沉可读构造**（实现择优：最少重复代码、单测可覆盖）。敏感词 Filter **无需改 Order**，仍读包装后的流。

5. **日志**  
   - 级别建议 **WARN**；字段至少含：**请求路径、客户端 IP、HTTP method、命中的关键字集合**；参数侧含 **参数名或 JSON 路径**（实现可用简单路径如 `query.xxx` / `body.$.field`）。避免默认打印完整 body（体积与敏感信息）。

## Risks / Trade-offs

| 风险 | 缓解 |
|------|------|
| 合法内容含 `select` 等子串被误拦 | 已接受「拦宽」；可通过 `ignoreUrls` / 后续收紧匹配缓解 |
| 双 Filter 重复读/顺序错误导致流耗尽 | 固化 Order；单测覆盖「仅 SQL」「仅敏感词」「两者同时启用」 |
| 大 body 内存占用 | 与敏感词现状一致；超大请求可考虑后续限额（非本变更必须） |

## Migration Plan

- **部署**：默认 `enabled=false`，无行为变化；开启后可能影响少量边缘合法请求，需业务评估 `ignoreUrls`。  
- **回滚**：配置关关闭或移除 bean（自动配置条件关闭）；无库表迁移。

## Open Questions

- **畸形 JSON**：默认「不拦截 SQL 能力、交给下游」是否与安全审计预期一致；若需拦截可在 tasks 中加配置开关。  
- **内置关键字列表**：是否在文档/README 公开完整列表以便运维申诉。
