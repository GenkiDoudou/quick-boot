## Why

重复提交与重试会导致写操作重复执行；需要在 **`quickboot-common`** 提供可开关的 **HTTP 幂等护栏**：仅以客户端传入的 **幂等 Token 请求头** 作为键，在时间窗口内拒绝相同 token 的重复请求，并与统一响应 **`R`（HTTP 200 + 业务码）** 对齐。

## What Changes

- 新增 **`@Idempotent`** 注解（仅配置 TTL、`prefix`、`deleteAfterExecution` 等，**无**多策略枚举）。
- 新增 **`qc.security.firewall.idempotent`** 配置：`enabled` **默认 `false`**、`interceptMethods[]`、`excludeUrls[]`、全局 TTL、`keyPrefix`、`tokenHeader`（默认 `X-Idempotent-Token`）、`defaultMessage`、`cacheType`（`auto|redis|caffeine`）。
- **仅 TOKEN 键**：键 = 前缀 + 可选注解分段 + 头值；**头缺失或空白则跳过幂等**（不报错）。
- **存储**：`setIfAbsent` + TTL；**Redis 与 Caffeine` 双实现**；`auto` 时 **存在可用 `RedisConnectionFactory` Bean 则 Redis，否则 Caffeine**。
- **重复请求**：抛出 **`IdempotentException`（业务码 `30201`）**，由全局异常处理转为 **`HTTP 200` + `R.code=30201`**。
- **业务失败**：目标方法抛异常时 **删除占位**，允许同 token 重试；`deleteAfterExecution=true` 时成功返回后删键。
- **不在范围**：参数/hash、`URL_USER`、自定义 `IdempotentKeyGenerator`、缺 token 错误响应、409/429 HTTP 语义。

## Capabilities

### New Capabilities

- `firewall-idempotent`：TOKEN-only 接口幂等、配置、存储回落、与 `R`/30201 的契约及验收场景。

### Modified Capabilities

- （无）主 specs 中尚无本能力；若全局异常尚未识别 `30201`，属实现阶段与 **异常模块** 的集成点，不在此变更中修改其他 capability 的需求条文。

## Impact

- **代码**：`quickboot-common` 新增配置属性、幂等存储抽象、Redis/Caffeine 实现、AOP 或 **HandlerInterceptor**（与 Spring MVC 协同）、异常类；注册 `AutoConfiguration` 与 `AutoConfiguration.imports`。
- **配置**：`application.yml` 中与 `qc.security.firewall.idempotent` 对齐；现有片段仅 `key-prefix` 时需与全套键名统一（kebab-case）。
- **运行**：Redis 多实例一致；Caffeine 仅进程内，**多节点不互斥**——文档与 spec 需明示局限。
- **API 行为**：带 token 且重复的写请求在幂等窗口内得到 **200 + 30201**；不带 token **行为与未启用幂等前一致**（对同一路径无新增错误形态）。
