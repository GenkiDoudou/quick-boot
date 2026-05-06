## 1. 基础设施与模块对齐

- [x] 1.1 确认 `quickboot-common` 源码目录完整；若缺失则恢复最小 Java 包结构与现有 `pom.xml` 对齐
- [x] 1.2 添加缓存自动配置包（如 `io.github.genkidoudou.common.cache`）：`@Configuration`、条件注解与 `@EnableCaching` 消费说明（由 web 或 starter 启用）

## 2. 动态 TTL 解析

- [x] 2.1 实现 `cacheName#ttlSeconds` 解析工具：合法正整数秒返回 TTL，否则回退默认 3600；剥离后缀得到逻辑分区名
- [x] 2.2 单元测试覆盖：`userCache#3600`、`a#1`、无后缀、非法 `#` 后缀、`#0`/负数

## 3. Caffeine CacheManager

- [x] 3.1 实现条件于 `spring.cache.type=caffeine` 的 `CacheManager`：`getCache` 懒建、`expireAfterWrite` 对齐解析 TTL、`maximumSize(10000)` 策略与设计选定一致并注释
- [x] 3.2 集成或切片测试：`cacheNames="a#1"` 约 1 秒后 miss；无后缀走默认 TTL

## 4. Redis CacheManager

- [x] 4.1 条件于 `spring.cache.type=redis`：基于 `RedisConnectionFactory` 构建 `RedisCacheManager`，key `StringRedisSerializer`，value `GenericJackson2JsonRedisSerializer`，`disableCachingNullValues()`
- [x] 4.2 使用事务感知包装（如 `TransactionAwareCacheManagerProxy`）
- [x] 4.3 实现动态 TTL（与设计决策一致）：`a#1` 等场景 TTL 与 Redis `TTL` 一致；集成测试校验 JSON value 与 null 不写入

## 5. 文档与验收

- [x] 5.1 在模块 README 或 AGENTS 指向的文档位置简要说明：`spring.cache.type`、`cacheNames` 约定、`@CacheEvict` 名称一致性注意点
- [x] 5.2 对照 `openspec/changes/common-cache-module/specs/common-cache/spec.md` 做一次自查勾选（或补充自动化测试对应场景）

### 规范自查（common-cache）

- 切换实现：`QuickbootCacheAutoConfiguration` + `spring.cache.type`（测试：`QuickbootCacheAutoConfigurationTest`）。
- 动态 TTL / 默认 TTL：`CacheNameTtlParserTest`、`DynamicTtlCaffeineCacheManagerTest`、`DynamicTtlRedisCacheManagerIT.shortTtlExpires`。
- Caffeine 容量：`DynamicTtlCaffeineCacheManager` 使用 `maximumSize(10000)`。
- Redis 序列化 / null / 事务感知：`DynamicTtlRedisCacheManager` + `TransactionAwareCacheManagerProxy`；`DynamicTtlRedisCacheManagerIT`（Docker）覆盖 Jackson Map 往返与 null 禁止写入；未覆盖「事务回滚不遗留缓存」的端到端场景（依赖应用事务测试）。
