## ADDED Requirements

### Requirement: 按 spring.cache.type 切换缓存实现

系统 MUST 根据 `spring.cache.type` 选择单一生效的 Spring `CacheManager`：`caffeine` 时使用基于 Caffeine 的实现，`redis` 时使用基于 Redis 的实现并依赖可用的 Redis 连接工厂。

#### Scenario: 本地模式使用 Caffeine

- **WHEN** 配置 `spring.cache.type=caffeine` 且应用启用 Spring Cache
- **THEN** 缓存读写通过 Caffeine 本地实现完成，不强制要求 Redis 可用

#### Scenario: 集群模式使用 Redis

- **WHEN** 配置 `spring.cache.type=redis` 且 Redis 基础设施可用
- **THEN** 缓存读写通过 Redis 完成，且 key 使用字符串序列化

### Requirement: cacheName#ttlSeconds 动态 TTL

系统 MUST 将 `@Cacheable`（及同类注解）中的 `cacheNames` 元素解析为可选 TTL：`cacheName#ttl` 中 `ttl` 为正整数秒时表示过期时间；`cacheName` 为剥离后缀后的逻辑分区名。若字符串不包含合法 `#ttl` 后缀或解析失败，系统 MUST 使用默认 TTL 3600 秒。

#### Scenario: 指定 1 秒 TTL

- **WHEN** 使用 `cacheNames="a#1"` 写入缓存
- **THEN** 约 1 秒后该缓存项失效（再次读取未命中缓存层）

#### Scenario: 未指定后缀时使用默认 TTL

- **WHEN** 使用 `cacheNames="userCache"`（无 `#` TTL 后缀）写入缓存
- **THEN** 该项过期时间按默认 3600 秒策略生效

### Requirement: Caffeine 默认容量

在 Caffeine 模式下，系统 MUST 为缓存容量设置默认上限 10000 条目（实现可对「每分区」或「聚合策略」取其一，但须在模块文档中与行为一致）。

#### Scenario: 容量约束生效

- **WHEN** 使用 Caffeine 模式持续写入大量 distinct 缓存条目
- **THEN** 缓存实现施加不少于文档所述的容量控制策略，避免因无限增长耗尽内存

### Requirement: Redis 序列化与 null 策略

在 Redis 模式下，系统 MUST 使用 `StringRedisSerializer` 序列化 cache key，使用 `GenericJackson2JsonRedisSerializer`（或等价可读 JSON）序列化 value；系统 MUST NOT 将 `null` 结果写入缓存。

#### Scenario: Value 为可读 JSON

- **WHEN** 缓存非 null 的 Java 对象至 Redis
- **THEN** Redis 中存储的 value 载荷为人类可读的 JSON 结构（非 JDK 原生二进制序列化）

#### Scenario: null 不写入

- **WHEN** 被缓存方法返回 `null`
- **THEN** Redis 中不存在对应该调用的缓存条目

### Requirement: Redis 事务感知

在 Redis 模式下，系统 MUST 以事务感知方式暴露缓存（例如通过 Spring 提供的 `TransactionAwareCacheDecorator` 或等价机制），使缓存在参与 Spring 事务时与提交/回滚语义一致。

#### Scenario: 事务回滚不遗留错误缓存

- **WHEN** 在事务方法上使用 `@Cacheable` 且事务最终回滚
- **THEN** 不因该事务路径留下错误提交的缓存副作用（与 Spring 事务感知缓存语义一致）
