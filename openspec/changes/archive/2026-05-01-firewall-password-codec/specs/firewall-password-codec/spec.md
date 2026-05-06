## ADDED Requirements

### Requirement: 前缀格式与 codec 标识

系统 MUST 将编码后的秘密值表示为 **`{codecId}payload`**：`codecId` MUST 为 **bcrypt** 或 **`sm4:` + keyId**（`keyId` 为 **非空**、不含 **`}`** 的标识，用于解析 `setProperties` 中注册的 SM4 密钥）；`payload` MUST 为 **该算法定义的负载**（bcrypt 为经典 `$2a$/...` 串；SM4 为 **十六进制**）。

#### Scenario: bcrypt 输出带前缀

- **WHEN** 调用方使用 codecId **bcrypt** 对某明文执行加密
- **THEN** 返回值 MUST 以 `{bcrypt}` 开头，且后续负载 MUST 可被同一 `PasswordCodec.matches` 校验为真

#### Scenario: SM4 输出带 keyId 与 hex

- **WHEN** 调用方使用 codecId **sm4:***keyId* 对某明文执行加密，且该 *keyId* 已在初始化阶段通过 `setProperties` 注册
- **THEN** 返回值 MUST 以 `{sm4:keyId}` 开头（与所用 *keyId* 一致），且 `payload` MUST 仅含 **十六进制字符**（大小写实现可定，但 MUST 能被己方 `matches` 接受）

### Requirement: bcrypt 行为

对 **bcrypt**：系统 MUST 使用 **Hutool** 提供的 bcrypt 能力进行哈希与校验；MUST NOT 依赖 `spring-security-*`。系统 MUST NOT 对 bcrypt 串提供「解密」为明文的 API。

#### Scenario: bcrypt 可校验

- **WHEN** `prefixEncoded` 为 `{bcrypt}` 前缀的合法 bcrypt 串，且 `raw` 为正确明文
- **THEN** `matches(raw, prefixEncoded)` MUST 为真

### Requirement: SM4 行为与密钥初始化

对 **SM4**：系统 MUST 使用 **Hutool 国密** SM4 进行加密与解密；负载 MUST 为 **十六进制**。系统 MUST 在 `PasswordCodec` **首次用于编解码前** 支持通过 **`setProperties(Properties)`** 注入密钥：属性键 MUST 遵循 **设计文档** 中约定的前缀与子键（如 `sm4.keys.<keyId>` 与可选 `sm4.defaultKeyId`），密钥材料 MUST 为 **表示 16 字节密钥的 32 位十六进制字符串**。

系统 MUST 在 `keyId` 未注册或材料非法时于加密/校验/解密时 **失败**（抛受检或运行时异常，具体类型由实现选定并文档化），且 MUST NOT 静默回退至默认 key。

#### Scenario: SM4 加密后 matches 为真

- **WHEN** 已为 `clientA` 注册 SM4 key，且 `cipher = encrypt("secret", "sm4:clientA")`
- **THEN** `matches("secret", cipher)` MUST 为真

#### Scenario: SM4 可解密

- **WHEN** `cipher` 为合法的 `{sm4:clientA}` 前缀且对应已注册 key 的密文
- **THEN** `decrypt`（或与 spec 实现一致的等价 API）MUST 返回与加密前一致的明文字节/字符串（以实现 API 为准）

#### Scenario: 未注册 key 失败

- **WHEN** `encrypt` 或 `matches` 需要 `sm4:unknown` 但未通过 `setProperties` 注册 `unknown`
- **THEN** 操作 MUST 失败且 MUST NOT 成功产生可校验结果

### Requirement: matches 在无前缀时的默认算法

当 `prefixEncoded` **不包含** 形如 `{...}` 的前缀时，`matches(raw, prefixEncoded)` MUST 使用 **默认 codec**（实现 MUST 将默认设为 **bcrypt**）对 `prefixEncoded` 整串进行校验。

#### Scenario: 旧 bcrypt 存根无大括号

- **WHEN** `prefixEncoded` 为无大括号前缀的经典 bcrypt 串，`raw` 为对应明文
- **THEN** `matches(raw, prefixEncoded)` MUST 为真

### Requirement: 非目标能力

系统 MUST NOT 提供 **`upgradeEncoding`**。本能力 MUST NOT 将 **Spring Security** 作为必需依赖；SM4 与 bcrypt MUST 通过 **Hutool** 满足。

#### Scenario: 无 upgradeEncoding API

- **WHEN** 仅依赖本 spec 所描述的公开 API
- **THEN** MUST NOT 存在名为 `upgradeEncoding` 或由本模块文档定义为「编码升级策略」的公开方法

### Requirement: Spring Boot 自动配置

系统 MUST 提供 Spring Boot **自动配置类**，在应用未另行定义同类型 Bean 时，注册 **单例** `PasswordCodec`，并从 **环境配置** 构造 `Properties` 调用 **`setProperties`**，键与 `qc.security.firewall.password.codec`（或设计文档最终锁定的前缀）对齐。

#### Scenario: 可覆盖默认 Bean

- **WHEN** 应用中用 `@Bean` 定义了 `PasswordCodec`
- **THEN** 自动配置 MUST NOT 再注册第二个默认 `PasswordCodec`（遵循 Spring Boot `@ConditionalOnMissingBean` 语义）
