# cache

## 用途

`quickboot-common` 能力包 `io.github.genkidoudou.common.cache`。

源码目录：`quickboot/quickboot-common/src/main/java/io/github/genkidoudou/common/cache/`

## 公开类型 / API

| 类型 | 相对路径 | 说明 |
| --- | --- | --- |
| CacheAutoConfiguration | cache/CacheAutoConfiguration.java | /** * quickboot-common 缓存自动配置：按 &#123;@code spring.cache.type} 切换 Caffeine / Redis。 * &lt;p&gt; * 应用模块需自行启用 &#123;@link EnableCaching}（例 |
| CacheDefaults | cache/CacheDefaults.java | /** * quickboot-common 缓存模块默认常量（与原始需求 / OpenSpec common-cache 对齐）。 */ |
| CacheNameTtlParser | cache/CacheNameTtlParser.java | /** * 解析 &#123;@code cacheName#ttlSeconds}：后缀为正整数秒则生效，否则回退默认 TTL。 */ |
| DynamicTtlCaffeineCacheManager | cache/DynamicTtlCaffeineCacheManager.java | /** * 基于 Caffeine，按注解完整缓存名懒建实例；TTL 来自 &#123;@code cacheName#ttlSeconds}。 * &lt;p&gt; * 每个不同的注解字符串对应独立的 Caffeine 实例，单实例上限 &#123;@link Cac |
| DynamicTtlRedisCacheManager | cache/DynamicTtlRedisCacheManager.java | /** * 基于 RedisCacheManager，按注解完整缓存名懒建 &#123;@link RedisCache}；TTL 来自 &#123;@code cacheName#ttlSeconds}。 * &lt;p&gt; * Redis 分区名使用注解原始字符串 |


## 配置键

若存在 `*Properties`，以该类字段及 `application.yml` 中对应前缀为准（见上表类型）。

## 示例

业务模块通过依赖 `quickboot-common` 直接引用上表类型；具体用法见各业务 Service / Controller。
