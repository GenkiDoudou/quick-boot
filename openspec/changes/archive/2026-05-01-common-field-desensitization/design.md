## Context

原始需求：`原始需求/后端/字段脱敏模块.md`。工程为 Spring Boot 3，`spring.jackson` 已全局配置（如 `non_null`）；`quickboot-common` 已采用 `META-INF/spring/...AutoConfiguration.imports` 模式注册自动配置先例。需在 **不写业务样板代码的前提下**，让 MVC JSON 写出对标记字段脱敏。**不覆盖**日志脱敏、入参校验、敏感词黑名单（`qc.security.firewall.sensitive-word`）等非本模块范畴。

已确认：**EMAIL** 形如 `ab***@example.com`；**NAME** 单字不脱敏保留原样；**仅处理 `String`**。

## Goals / Non-Goals

**Goals:**

- `@Sensitive(type, strategy)` 控制 String 属性的 Jackson **序列化输出**掩码逻辑；运行时 **不修改** JavaBean/DTO 内存字段值。
- 内置：`NAME`、`ID_CARD`、`MOBILE`、`BANK_CARD`、`EMAIL`、`ADDRESS`、`PASSWORD`、`CUSTOM`；行为与提案/验收样例对齐。
- 统一兜底：`null`、空字符串、长度不足、`CUSTOM` 非法 → **原样输出**。
- `CUSTOM`：`strategy` 为 `prefix,suffix` 非负整数；中间段用 **与原文中间段等长** 的 `*`；若 `prefix+suffix ≥ length` 则原样输出。
- `ADDRESS`：前 6 字符原样；若长度 `>6`，第 7 个字符起每位替换为 `*`（中间段等长 `*`，与 `CUSTOM` 形态一致）。
- `PASSWORD`：固定 **`******`**，但当空串/`null` 仍走兜底原样（通常为不输出或由 `non_null` 省略）。

**Non-Goals:**

- List/Map/嵌套递归自动扫描全局对象图（首期仅作用于 **直接标注的 String Bean 属性/访问器**，见风险）。
- 基于角色开关（ADMIN 明文）或非 Jackson 通路（Protobuf、Jdbc、日志）脱敏。
- 修改 OpenAPI/schema 语义（可后续）。

## Decisions

1. **挂载 Jackson 的机制**  
   - **首选**：`SimpleModule` + `BeanSerializerModifier`（或对 `AnnotatedString`/`String` Getter 注册的 `JsonSerializer`）在 **`ObjectMapper` Bean 组装后**追加模块；经由 `Jackson2ObjectMapperBuilderCustomizer` **或** `ObjectMapper` `@Bean` 包装（与 Boot 默认 `JacksonAutoConfiguration` 兼容）。也可采用 **复合元注解 `@JacksonAnnotationsInside` + `@JsonSerialize`** — 任选其一在主实现中敲定并在 tasks 中单测锁住。  
   - **备选**：要求业务在每个字段手动 `@JsonSerialize(using=...)` — 侵入大，不推荐。

2. **`@Sensitive` 与类型**  
   - **首期**：只对 **`String`** 生效；若非 `String` 误标注，实现侧 **静默忽略**或在编译期无法用注解约束时于 JavaDoc **明确忽略**。

3. **内置规则精确定义**（与 spec 一致）  
   - `NAME`：长度 `≤1` 原样；否则首字符 + 后续每位 `*`。  
   - `ID_CARD`：至少需 `6+4` 位才脱敏，否则原样；否则前 6 + 中间 `*` + 后 4。  
   - `MOBILE`：至少 `3+4`；前 3 + `*` + 后 4。  
   - `BANK_CARD`：至少 `4+4`；前 4 + `*` + 后 4。  
   - `EMAIL`：须含 `@`；本地段长度 `≤2` 时按「长度不足原样」整体原样；否则本地前 2 + 中间等长 `*` + `@` + 域名不变。  
   - `PASSWORD`：非空非 null 时输出 `******`；空串/null 原样。

4. **与其它 `ObjectMapper` 实例**  
   - **记录风险**：Redis 等若复用同一套 `ObjectMapper` 可能把掩码写入缓存。建议文档提示；是否提供 `@ConditionalOnProperty` 开关留作 **Open Question**（默认 ON 以匹配需求「默认适配 Web」）。

## Risks / Trade-offs

- **[Risk]** 仅 Web `ObjectMapper` 注册模块时，单元测试手动 `new ObjectMapper()` 可能 **不脱敏** → **Mitigation**：提供测试用 `ObjectMapper` 工厂或文档示例。  
- **[Risk]** 嵌套 DTO 内层 String 未标注则 **不脱敏** → **Mitigation**：文档说明；后续可加 `@SensitiveNested` 类级扫描（非本变更）。  
- **[Trade-off]** `non_null` 下 null 不出现在 JSON，与「null 原样」不冲突。

## Migration Plan

1. 引入模块后选 1–2 个 VO 试点加注解。  
2. 回归关键接口 JSON 样例与前端展示。  
3. 回滚：移除自动配置注册或依赖升级撤包。

## Open Questions

- 是否提供全局 `quickboot.desensitization.enabled` 开关：默认 **需要** 还是 **不需要**（需求写无配置项—倾向 **不做开关**，必要时后续 change）。
