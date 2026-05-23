# 工具类总览

QuickBoot **未使用传统 JWT 工具类**，会话由 **Sa-Token** 管理。下列为 `quickboot-common` 中实际存在的常用工具（按包）：

| 类 | 包 | 用途 |
|----|-----|------|
| `ServletUtils` | `common.servlet` | 获取 Request/Response、参数、渲染 JSON |
| `ValidatorUtils` | `common.validation` | 手动触发 Jakarta Validation |
| `ExcelUtils` | `common.excel` | EasyExcel 读写封装 |
| `I18nUtil` | `common.i18n` | 国际化消息 |
| `BeanUtil` | Hutool | Bo/Entity/Vo 属性拷贝（项目约定） |

## 缓存

Spring `CacheManager` 支持 **Caffeine**（默认）与 **Redis**。缓存名格式：

```text
cacheName#ttlSeconds
```

示例：`clientSignNonce#300`。

## 加密与密码

- 用户密码：**BCrypt**（`PasswordCodec`）
- 配置/密钥字段：**SM4**（环境变量 `QC_SM4_KEY_HEX`）
- Client 签名：**HMAC-SHA256**（`ClientSignService`）

侧栏中的「JWT 工具」为历史规划项，请以 **Sa-Token + OAuth2** 为准。

## 相关文档

- [接口规范](../api/index)
- [安全防护模块](../modules/security-module)
