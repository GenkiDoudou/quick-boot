## Why

接口返回的手机号、证件号、银行卡、邮箱等隐私字段需要在不改变数据库与会话内存真实值的前提下，对 **JSON 输出**做统一掩码；若各团队手写格式化，易不一致且易漏。需要在 `quickboot-common` 提供 **字段注解 + Jackson 序列化**，与现有 Spring MVC JSON 链路对齐。

## What Changes

- 在 `quickboot-common` 提供 `@Sensitive`，属性包括：`type`（默认 `CUSTOM`）、`strategy`（仅 `CUSTOM` 生效，`"prefix,suffix"` 表示首尾保留位数）。
- 提供枚举式内置脱敏类型：`NAME`、`ID_CARD`、`MOBILE`、`BANK_CARD`、`EMAIL`、`ADDRESS`、`PASSWORD`、`CUSTOM`；均 **仅适用于 `java.lang.String` 字段/getter**，错误类型或未标注 `String` 由实现决定是否忽略或过编译期约束（见 `design.md`）。
- **兜底**：值为 `null`、空串、长度不足以按要求保留首尾、或 `CUSTOM` 非法策略时：**原样输出**（不脱敏）。
- **已拍板规则**：`NAME` 单字长度不足时不展开掩码规则，**保持原样**；`EMAIL` 形态为 **`ab***@example.com`**（本地段前 2 位保留 + `@` 前其余位用 `*` 填充 + 域名全文保留）。
- **`CUSTOM`**：首尾按 `strategy` 保留；中间段以与原文中间段 **等长** 的 `*` 替换；若 `prefix+suffix ≥ len` → 兜底原样输出。
- 注册 Jackson 扩展（模块/序列化器等）以使 Spring Boot Web 默认 `HttpMessageConverters` **无需业务手工拼装**即可享受脱敏。（具体注册方式见 `design.md`。）
- **BREAKING**：无；新增类型与 Bean。若与其它模块共用 `ObjectMapper` 序列化缓存体，可能出现「缓存里也存掩码字符串」的产物行为——见影响说明。

## Capabilities

### New Capabilities

- `common-field-desensitization`：`quickboot-common` 基于 Jackson 的字段级脱敏注解、内置算法、CUSTOM 语法、兜底策略及 Spring Boot JSON 注册的验收要求。

### Modified Capabilities

- （无）

## Impact

- **代码**：新增 `desensitization`（或以实现为准的包名）注解、枚举、算法工具、Jackson 绑定与 Spring Boot 自动配置导入。
- **依赖**：Jackson（项目已具备）。
- **系统**：不改变持久化明文；可能影响 **共用同一 `ObjectMapper` 的 JSON 写出**（如 Redis Cache value 序列化）——按需由业务选型或后续增强开关（非本提案正文范围，设计文档提示风险）。
- **团队**：业务在 DTO/`String` VO 字段上标注 `@Sensitive` 即可；需在模块文档中提示 **ObjectMapper 共享** 导致的缓存侧影响（若存在）。
