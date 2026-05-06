## Why

业务接口存在高频读场景，需要在不改动业务 key 设计的前提下降低数据库与下游压力；同一套代码需在单机开发环境与集群生产环境之间切换缓存实现，并希望按缓存分区动态指定 TTL，避免为每个缓存维护冗长的静态配置。

## What Changes

- 在 `quickboot-common` 提供统一缓存能力：`spring.cache.type=caffeine` 时使用本地 Caffeine，`spring.cache.type=redis` 时使用 Redis（依赖既有 Redis 连接基础设施）。
- 支持注解缓存名约定 `cacheName#ttlSeconds`：解析出逻辑分区名与过期秒数；解析失败或未写 `#ttl` 时使用默认 TTL（3600 秒）。
- Redis 模式下统一使用可读 JSON 序列化存储 value，不缓存 `null`，并保持事务感知缓存语义。
- Caffeine 模式下默认最大条目数 10000，并与动态 TTL 解析对齐。
- **BREAKING**：若现有代码依赖 Spring Boot 默认 Cache 自动配置且无自定义 Bean，引入本模块的 `@EnableCaching` + 自定义 `CacheManager` 后，缓存行为（序列化、TTL、是否缓存 null）以本模块为准；需在联调时核对既有 `@Cacheable` 用法。

## Capabilities

### New Capabilities

- `common-cache`：quickboot-common 侧缓存抽象能力，包括基于 `spring.cache.type` 的双实现切换、`cacheName#ttl` 动态 TTL、Redis/Caffeine 默认策略及验收约束对应的行为。

### Modified Capabilities

- （无）仓库 `openspec/specs/` 下尚无既有能力规范需增量修改。

## Impact

- **代码**：`quickboot-common` 新增 `cache` 包（配置类、`CacheManager` 定制实现、TTL 解析工具等）；消费模块需启用 Spring Cache（如启动类或配置类上的 `@EnableCaching`）。
- **依赖**：沿用已有 `spring-boot-starter-cache`、`caffeine`、`spring-boot-starter-data-redis`（Redis 模式）；无需新增外部依赖类型。
- **配置**：继续使用 `spring.cache.type`（`caffeine` / `redis`）；与 `quickboot-web` 等业务模块的 `application.yml` 对齐。
- **系统**：Redis 模式下占用 Redis 命名空间与 TTL 行为由本模块定义；业务侧仅决定 `@Cacheable` 的 `cacheNames`、`key` 与作用域。
