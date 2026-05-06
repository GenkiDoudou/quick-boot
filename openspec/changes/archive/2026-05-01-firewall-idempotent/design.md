## Context

- **原始需求**：`原始需求/后端/安全防火墙-接口幂等.md` 已定案：默认关闭、仅 TOKEN、Redis/Caffeine 自动回落、重复返回 HTTP 200 + 业务码 30201、缺 token 跳过且不报错。
- **现状**：仓库内暂无 `Idempotent`/`30201` 实现；`application.yml` 有部分 `qc.security.firewall.idempotent` 占位。
- **约束**：Spring Boot 3、JDK 17+；与 **`R`、全局异常处理、可选 i18n（`30201` 词条）** 对齐。

## Goals / Non-Goals

**Goals:**

- `enabled=true` 时，对 **命中范围** 且 **幂等头非空** 的请求，在业务执行前 **原子占位**；占位失败则 **`IdempotentException`（30201）**，经异常处理得到 **HTTP 200 + `R.code=30201`**。
- 支持 **`@Idempotent`** 与 **`interceptMethods[]` + 路径排除** 两条触发路径，共享同一套键与存储。
- **`cache-type=auto`**：存在可用 **`RedisConnectionFactory`**（或设计阶段锁定的等价 Bean）→ Redis；否则 Caffeine。
- 业务方法 **抛异常** → **释放**占位（`delete`）；**`deleteAfterExecution=true`** → 方法正常返回后 **delete**。

**Non-Goals:**

- 多策略键、参数 hash、`URL_USER`、自定义 Generator、缺 token 报错、HTTP 409/429 作为重复语义。

## Decisions

| 决策 | 选择 | 理由 | 备选 |
|------|------|------|------|
| 切点 | **`HandlerInterceptor` + AOP 环绕** 二选一或组合：优先 **单一 `IdempotentGuard` 服务** 被 **拦截器**（检测 `HandlerMethod` 上注解 + HTTP 方法与全局 `interceptMethods`）调用，避免两套逻辑 | MVC 里拿 `Method` 与 `HttpServletRequest` 一致；Filter 层难绑定 Controller 方法 | 纯 AOP 难以表达「仅配置了全局 POST 但未标注解」与路径排除的统一 |
| Redis 原子性 | **`SET key value NX EX seconds`**（或等价 `setIfAbsent` + TTL API） | 满足并发占位 | Lua 脚本（过度） |
| Caffeine 原子性 | 进程内 **`ConcurrentHashMap` + 过期** 或 Caffeine **`Cache`**：写入用 **单 key 同步/原子替换** 模拟 NX；TTL 用 `expireAfterWrite` | 无 Redis 时单 JVM 可用 | 要求跨进程强一致则必须 Redis |
| 键格式 | `keyPrefix` + 注解 `prefix()`（可选）+ **头值 trim** 后字符串；**不对** token 做额外归一 | 与需求一致 | hash token（不必要） |
| 排除路径 | **`AntPathMatcher`** 与防火墙其它子模块一致 | 配置习惯统一 | 正则 |
| 异常映射 | **`IdempotentException` 携带 code=30201**；由 **`@ControllerAdvice`** 或既有全局处理转为 **`R`**，**HTTP 200** | 已定案 | - |

## Risks / Trade-offs

- **[Risk] Caffeine 多实例穿透** → **缓解**：文档与 spec 明示；生产多节点推荐 Redis + `auto`。
- **[Risk] 拦截器顺序早于/晚于鉴权** → **缓解**：`@Order` 文档化；通常幂等检查在鉴权之后（需读取头，不依赖用户键）。
- **[Risk] 长事务占用键** → **缓解**：合理 TTL；`deleteAfterExecution` 缩短占用。
- **[Risk] 30201 未注册 i18n** → **缓解**：`defaultMessage` 回退。

## Migration Plan

1. 合并后默认 `enabled=false`，行为不变。
2. 开启后按环境配置 TTL、`cache-type`、排除路径。
3. 回滚：`enabled=false` 或移除自动配置。

## Open Questions

- 与现有 **Sa-Token / 安全过滤器链** 的 **相对顺序** 在具体工程中的 `Interceptor` 注册方式（实现阶段用 `WebMvcConfigurer` 固定）。
