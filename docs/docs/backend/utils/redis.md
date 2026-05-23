# Redis 与缓存

QuickBoot 通过 Spring Cache 使用 Redis（生产 Profile），非独立「RedisUtil」工具类。

## 启用

`application-prod.yml`：

- `spring.cache.type=redis`
- `qc.oauth2.token-store=redis`
- `spring.data.redis.host/port`

## 典型用途

| 场景 | 缓存名示例 |
|------|------------|
| Client 签名 nonce | `clientSignNonce#300` |
| Sa-Token 会话 | Sa-Token Redis 集成 |
| 验证码 | Tianai `captcha` 存储 |
| 业务缓存 | `cacheName#ttl` 动态 TTL |

详见 [工具类总览](./index)。
