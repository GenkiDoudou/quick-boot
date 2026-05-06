## Why

系统存在用户密码哈希与客户端认证串对称加解密等多类场景，需要统一、可扩展的编码与校验入口，避免各处重复实现算法选择与密文格式解析。与防火墙安全能力一并演进，便于后续策略与集成对齐。

## What Changes

- 在通用模块中提供 **`PasswordCodec`**：支持 `{codecId}...` 前缀格式，委托不同算法进行加密与校验。
- 支持 **bcrypt**（不可逆，`matches`）与 **SM4**（可逆，密文为 **十六进制**；多 **keyId**，密钥在 Bean **初始化阶段** 通过 **`setProperties`** 注入）。
- **不提供** `upgradeEncoding`；本变更不引入 **Spring Security** 依赖，国密与 bcrypt 均通过 **Hutool** 能力实现。
- 提供 **Spring Boot 自动配置**，默认注册可被覆盖的 `PasswordCodec` Bean。
- 边界不变：不提供密码策略（复杂度、历史密码、轮换等）。

## Capabilities

### New Capabilities

- `firewall-password-codec`：统一密码/认证串编解码契约（前缀格式、bcrypt/SM4 行为、`setProperties` 与多 key、自动配置与验收要点）。

### Modified Capabilities

- （无）本变更为独立能力，不改变现有 `openspec/specs/` 中其它能力的 REQ 级别行为。

## Impact

- **代码**：预计在 `quickboot-common`（或现有防火墙/安全包路径下与项目分层一致处）新增 `PasswordCodec` 及 SM4/bcrypt 实现、Properties 解析、自动配置类与 `META-INF/spring/...imports`（若项目一贯如此）。
- **依赖**：使用已有/将显式加入的 **Hutool**（国密 SM4、BCrypt）；**不** 增加 `spring-security-*`。
- **配置**：SM4 多 key 通过 **Properties 键**（如 `sm4.key.<keyId>=...`）在装配 `PasswordCodec` 时写入；具体键名与值编码（hex 密钥等）在 design 中固定。
- **系统**：业务注入 `PasswordCodec` 完成加密与校验；与防火墙其它能力可并列部署。
