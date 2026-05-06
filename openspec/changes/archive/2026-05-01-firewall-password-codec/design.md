## Context

- 原始需求见 `原始需求/后端/安全防火墙-密码加密.md`：统一 `{codecId}...` 前缀、bcrypt + SM4、委托式 API、自动配置。
- 干系人已确认：类型名 **`PasswordCodec`**；**不依赖 Spring Security**；国密与 bcrypt 走 **Hutool**；SM4 密文 **十六进制**；支持 **多 key**，在 Bean **初始化阶段** 通过 **`setProperties(Properties)`** 注入；**不提供** `upgradeEncoding`。
- 主规格 `openspec/specs/` 下尚无本能力条目；实施后归档将合并 `firewall-password-codec` delta。

## Goals / Non-Goals

**Goals:**

- 提供可注入的 `PasswordCodec`，支持 `encrypt`、`matches`；SM4 分支提供 **`decrypt(prefixEncoded)`**（或与之一致的公开方法），供服务端解析密文；bcrypt **无** 解密。
- 前缀格式可解析 **算法** 与 **SM4 的 keyId**（形如 `{bcrypt}...`、`{sm4:keyId}...`）。
- Spring Boot 自动配置注册默认 Bean；业务可用自定义 Bean **覆盖**。
- 密钥材料仅经 **`setProperties`** 在生命周期早期加载；运行期视为只读映射。

**Non-Goals:**

- 密码策略（复杂度、历史密码、轮换、升级编码策略）。
- 引入 `spring-security-*` 依赖。
- 密钥轮换、热更新 `setProperties`（默认一次初始化）。

## Decisions

1. **类型命名：`PasswordCodec`**  
   - **理由**：与 Spring `DelegatingPasswordEncoder` 区分，避免类名/Bean 名混淆。  
   - **备选**：包装 Spring 类 — **否决**（干系人要求不依赖 Spring Security）。

2. **bcrypt：Hutool `BCrypt`**  
   - **理由**：与 SM4 同属 Hutool，减少依赖面。  
   - **备选**：`org.mindrot:jbcrypt` — 仅当 Hutool 行为不满足时再评估。

3. **SM4：Hutool 国密实现；对外负载为 hex**  
   - **理由**：对齐干系人选择；便于日志与传输。  
   - **备选**：Base64 — **否决**。

4. **多 key：前缀携带 keyId — `{sm4:<keyId>}`**  
   - **`setProperties` 约定**（实现与文档保持一致，示例前缀 `qc.security.firewall.password.codec.`）：  
     - `sm4.keys.<keyId>=<密钥材料>`：密钥材料为 **32 个十六进制字符**（表示 16 字节 SM4 密钥）。  
     - 可选：`sm4.defaultKeyId=<keyId>` — 当 `encrypt(raw, "sm4")` 或需默认 SM4 key 时使用。  
   - **装配**：`@Configuration` 从 `Environment` 绑定为 `Properties`（或程序化构建 `Properties`）后调用 `passwordCodec.setProperties(...)`，**早于**其它 Bean 对 `PasswordCodec` 的调用。  
   - **备选**：仅 `{sm4}` + 每次调用传入 key — **否决**（已通过 setProperties 多 key）。

5. **无 prefix 的 `matches` 语义**  
   - 与原始需求一致：若 stored 串 **无** `{...}` 前缀，则 MUST 使用 **默认 codec**（实现默认 **bcrypt**）进行校验。`encrypt` 对外仍应生成带前缀的串以便区分算法。

6. **`upgradeEncoding`**  
   - **不适用**：本设计不包含该 API。

7. **算法细节（IV/模式）**  
   - **决策**：以 **Hutool SM4 封装默认用法**为准；若实际 API 产生 IV+密文，实现 MUST 将 **可对端解析的单一 hex 串** 写在 `{sm4:keyId}` 之后（拼接规则在实现 JavaDoc 中写清，并保持向后兼容）。  
   - **备选**：在 spec 中强行规定 ECB — **推迟**：除非安全评审要求，否则跟随 Hutool 默认以降低错配风险。

8. **模块位置**  
   - **决策**：与现有防火墙能力一致，落在 **`quickboot-common`**（或与 `firewall-security-headers` 同包风格下的 `io.github.genkidoudou.common.security.firewall.password` 类包，具体以仓库现有 `firewall` 代码位置为准；若尚无则新建与同批防火墙包并列）。  
   - 自动配置：使用 **`META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`** 注册（与 AGENTS.md 所述 common 模式一致）。

## Risks / Trade-offs

- **[Risk] SM4 模式/IV 与旧数据不兼容** → **Mitigation**：实现文档注明 Hutool 版本与 hex 布局；集成测试覆盖「加密再解密/再 matches」闭环。  
- **[Risk] Properties 密钥以 hex 明文出现在配置文件** → **Mitigation**：与现有 `jasypt` / 环境变量注入方式一致，由运维约束；本模块不负责加解密配置本身。  
- **[Risk] 默认无 prefix 的 matches 误用** → **Mitigation**：JavaDoc 明确「仅兼容旧 bcrypt 存根」；新数据一律带前缀。

## Migration Plan

- **新能力**：无存量 `PasswordCodec` 的迁移。  
- **业务侧**：新产生的密码/认证串应使用 `encrypt` 生成带前缀的串；若历史仅为 bcrypt 哈希且无 `{bcrypt}`，依赖「无前缀走默认 bcrypt」规则直至数据修复。  
- **回滚**：关闭自动配置或排除 `AutoConfiguration` 类；移除 Bean 定义即可。

## Open Questions

- SM4 是否需在 spec 层写死 **GCM/CBC/ECB**（当前设计委托 Hutool 默认与实现说明）。若安全审计要求，可在实现前收紧为明确模式。  
- `qc.security.firewall.password.codec` 下键名是否在已有 `application.yml` 示例中补充 — 实现任务中可增加 `application-dev` 片段或文档站说明。
