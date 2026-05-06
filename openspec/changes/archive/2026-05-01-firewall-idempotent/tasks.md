## 1. 配置与公共契约

- [x] 1.1 新增 `qc.security.firewall.idempotent` 的 `@ConfigurationProperties` 类：`enabled` 默认 `false`、`interceptMethods`、`excludeUrls`、`expireTime`/`timeUnit`、`keyPrefix`、`defaultMessage`、`tokenHeader`、`cacheType`（`auto|redis|caffeine`）
- [x] 1.2 定义 `IdempotentException`（或项目约定基类子类），**固定业务码 `30201`**，可带 `defaultMessage`
- [x] 1.3 在全局异常处理中映射：`IdempotentException` → **HTTP 200** + `R` 且 **`code=30201`**（与现有 `ServletUtils`/`R` 体系一致处复用）

## 2. 幂等存储

- [x] 2.1 定义 `IdempotentStore` 接口：`boolean setIfAbsent(String key, Duration ttl)` 与 `void delete(String key)`
- [x] 2.2 实现 **Redis**：基于 `StringRedisTemplate` 或 `RedisConnection` 的 **SET NX EX**（或等价原子 API）
- [x] 2.3 实现 **Caffeine**（或进程内缓存 + 并发安全）：满足 NX 语义与 TTL；文档注明**多实例不互斥**
- [x] 2.4 实现 **`cache-type` 选择器**：`auto` 时存在 `RedisConnectionFactory` → Redis，否则 Caffeine；`redis`/`caffeine` 强制与 fail-fast 行为按 design 选定并实现单测

## 3. 拦截与注解

- [x] 3.1 定义 `@Idempotent`：`expireTime`、`timeUnit`、`prefix`、`deleteAfterExecution`（及默认）
- [x] 3.2 实现 **Mvc `HandlerInterceptor`**（或等价）：解析 `HandlerMethod`；判断是否 `@Idempotent` 或命中 `interceptMethods`；`AntPathMatcher` 处理 `excludeUrls`；**头空则直接放行**
- [x] 3.3 实现 **键拼接**：`keyPrefix` + 注解 `prefix()` + trim 后的 token；不对 token/用户头做额外逻辑
- [x] 3.4 实现 **before**：`setIfAbsent` 失败则抛 `IdempotentException`；成功则将 key 记入 request 属性供后置清理
- [x] 3.5 实现 **afterCompletion**（或环绕）：业务异常 / `deleteAfterExecution` / 正常结束等路径下 **`delete`** 规则与 spec 一致
- [x] 3.6 `WebMvcConfigurer#addInterceptors` 注册拦截器并设定 **`@Order`**（与鉴权链协调）

## 4. 自动配置

- [x] 4.1 `@AutoConfiguration` + `@EnableConfigurationProperties`；`@ConditionalOnProperty(prefix=..., name=enabled, havingValue=true)`（或与现有 firewall 配置风格一致）
- [x] 4.2 注册 `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`

## 5. 测试与文档

- [x] 5.1 单元/集成测试：`excludeUrls`、缺 token 不占位、重复第二次 30201 映射、`auto` 选 Redis/Caffeine、业务异常删键、`deleteAfterExecution`
- [x] 5.2 （可选）`messages*.properties` 增加 `30201` 词条；`application.yml` 注释示例块
- [ ] 5.3 `mvn -pl quickboot-common test`（JDK 17+）通过 — 当前 CI/本机需 JDK 17；沙箱默认 JDK 8 无法编译验证
