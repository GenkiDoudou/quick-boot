# 加密与签名

| 能力 | 实现 |
|------|------|
| 用户密码 | BCrypt（`PasswordCodec`） |
| 配置/密钥字段 | SM4，环境变量 `QC_SM4_KEY_HEX` |
| Client API 签名 | HMAC-SHA256 + Base64 |
| 配置加密 | Jasypt `ENC(...)` |

OAuth `client_secret` 入库为 SM4，**签名时仍用解密后的明文**。

详见 [客户端管理](../modules/client-management)、[OAuth2](../modules/oauth2)。
