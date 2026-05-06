## Context

原始需求见 `原始需求/后端/缓存模块.md`。工程为 Spring Boot 3 多模块，`quickboot-common` 已声明 `spring-boot-starter-cache`、`caffeine`、`spring-boot-starter-data-redis`；`quickboot-web` 的 `application.yml` 已设置 `spring.cache.type: caffeine`。当前需在 common 层补齐可切换的 `CacheManager` 与 `cacheName#ttlSeconds` 解析，使业务可通过标准 `@Cacheable` 等注解使用。

## Goals / Non-Goals

**Goals:**

- 随 `spring.cache.type` 在 Caffeine 与 Redis 之间选择单一生效的 `CacheManager` Bean。
- 从注解传入的 `cacheNames` 字符串解析逻辑名与 TTL；非法或未指定时使用默认 TTL（3600 秒）。
- Redis：String key、`GenericJackson2JsonRedisSerializer` value、`cache-null-values=false`、事务感知包装。
- Caffeine：默认最大条目 10000，按解析 TTL 构建缓存实例。
- 行为可被验收用例覆盖（含 `a#1` 短 TTL 与 Redis JSON/null）。

**Non-Goals:**

- 规定业务缓存 key 拼接规则与命名空间前缀策略（业务自定）。
- 引入多级缓存、缓存穿透布隆过滤器、集群本地缓存同步等非需求范围能力。
- 替换 Spring Cache 注解模型（仍以 `@Cacheable` / `@CacheEvict` 为主）。

## Decisions

1. **TTL 与分区名的注入点：自定义 `CacheManager`（推荐）**  
   - **做法**：在 `getCache(String name)`（或等价扩展点）中解析 `name`，对外注册的 Spring `Cache` 使用剥离 `#ttl` 后的逻辑名作为 Redis/Caffeine 的「分区」，内部为该 `(逻辑名, ttl)` 维护一致的序列化与过期策略。  
   - **备选**：自定义 `CacheResolver` — 更细粒度但侵入注解较少场景时需确保与默认 `@Cacheable` 缓存解析链一致；若团队更偏向单一入口，`CacheManager` 更简单。  
   - **取舍**：优先 `CacheManager` 统一入口，减少与 Spring Boot 自动配置的分叉点。

2. **Redis 动态 TTL**  
   - **做法**：基于 `RedisCacheManager`/`RedisCacheConfiguration`，按「解析后的 TTL」生成或使用对应的 cache configuration（可为每种 TTL 懒建 configuration，或自定义 `RedisCacheWriter` 在 put 时带 TTL）。具体实现以实现阶段可读性与 Spring Data Redis 版本 API 为准。  
   - **备选**：固定少量 TTL 档位映射 — 简化实现但不满足任意整数秒需求。

3. **Caffeine 动态 TTL**  
   - **做法**：按 `(逻辑名, ttl)` 或完整注解名懒创建 `CaffeineCache` 实例，`expireAfterWrite(ttl)`；全局 `maximumSize(10000)`（可按逻辑名分缓存实例时每实例上限或总量策略在实现中择一并文档化）。  
   - **备选**：单一全局缓存不按 TTL 分区 — 无法满足同一逻辑名下多种 TTL。

4. **`null` 不入缓存**  
   - **做法**：Redis 侧 `disableCachingNullValues()`；Caffeine 侧在自定义封装或 `Cache` 包装中跳过 null put（与 Spring `@Cacheable` 默认「可能缓存 null」区分时优先满足需求文档）。

5. **启用方式**  
   - **做法**：`quickboot-common` 提供 `@Configuration` + `@ConditionalOnProperty`/`CacheType` 条件 Bean；应用模块保留 `@EnableCaching`。避免在未引入 Redis 的测试中误装配 Redis `CacheManager`（可用条件注解约束）。

## Risks / Trade-offs

- **[Risk]** `@CacheEvict` / `@CachePut` 使用的 `cacheNames` 必须与 `@Cacheable` 完全一致（含或不含 `#ttl`），否则无法命中同一物理分区。  
  → **Mitigation**：文档与代码注释强调同一业务常量字符串复用；可选后续增强解析规范化（非本变更必需）。
- **[Risk]** Redis JSON 与现有全局 `ObjectMapper` 类型信息、多态序列化不一致导致反序列化失败。  
  → **Mitigation**：使用与 Spring Boot 一致的 Jackson builder/`RedisSerializer` 配置；集成测试覆盖典型 DTO。
- **[Risk]** 高频 distinct TTL 导致 Redis 侧 configuration 或内部缓存实例数量膨胀。  
  → **Mitigation**：文档说明业务宜收敛 TTL 档位；实现可考虑 LRU 淘汰惰性创建的 configuration（按需）。
- **[Trade-off]** 注解中的 `#` 与 SpEL 无冲突，但若未来扩展其他后缀语法需再约定分隔符。

## Migration Plan

1. 合并代码后在本地 `spring.cache.type=caffeine` 运行现有用例与冒烟接口。  
2. 在具备 Redis 的环境切换 `spring.cache.type=redis`，清空相关 key 后验证 TTL 与 JSON value。  
3. 回滚：移除自定义 `CacheManager` Bean 或还原依赖版本，恢复 Spring Boot 默认 cache 自动配置（若有）。

## Open Questions

- `quickboot-common` 当前工作区是否缺少 `src`：若为准交付状态，是否需在实现任务中「恢复/重建」整个 common Java 源码树（超出纯缓存文件的评审范围需在 tasks 中单列）。
- Caffeine「10000」是指每个命名缓存实例还是全局共享池：实现中选择其一并在 spec 或文档中对齐一句。
