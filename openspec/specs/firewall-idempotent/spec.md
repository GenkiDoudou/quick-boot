# firewall-idempotent

## Purpose

`quickboot-common` 提供的 **接口幂等护栏（TOKEN-only）**：在 `qc.security.firewall.idempotent.enabled=true` 时，对携带非空幂等头的请求做原子占位与 TTL；重复请求经 **`IdempotentException`/`30201`** 转为 **HTTP 200 + `R`**；支持 **Redis / Caffeine** 与 **`auto`** 回落；**不**对缺 token 报错、**不**做用户标识拼键。

## Requirements

### Requirement: 启用开关与默认关闭

系统 MUST 通过 `qc.security.firewall.idempotent.enabled` 控制幂等护栏；该属性 MUST 默认 **`false`**。当为 **`false`** 时，系统 MUST NOT 对任何请求执行幂等占位或重复检测逻辑。

#### Scenario: 默认不注册拦截逻辑

- **WHEN** 未配置 `enabled` 或配置为 `false`
- **THEN** 应用 MUST NOT 因本能力拒绝请求，且 MUST NOT 写入幂等存储

### Requirement: 仅 TOKEN 键与缺省跳过

系统 MUST 仅从配置的头（`tokenHeader`，默认 `X-Idempotent-Token`）读取幂等 token。系统 MUST 仅当头**存在**且 trim 后**非空**时构造存储键并尝试占位；当头缺失或为空时，系统 MUST **跳过**本请求的全部幂等逻辑（等同未启用幂等护栏），且 MUST NOT 返回「缺 token」类错误。

#### Scenario: 无 token 不占位

- **WHEN** `enabled=true` 且请求未携带幂等头或值为空白
- **THEN** 业务处理正常执行，且不写入幂等存储

#### Scenario: 有 token 才占位

- **WHEN** `enabled=true` 且头值为非空字符串 `T`
- **THEN** 系统 MUST 使用由 `keyPrefix`、注解可选 `prefix()` 及 `T` 构成的键尝试占位（在命中拦截规则前提下）

### Requirement: 注解与全局 HTTP 方法拦截

系统 MUST 支持：① Controller 方法标注 `@Idempotent` 时，在满足 token 规则时参与幂等；② 配置 `interceptMethods[]`（如 `POST,PUT,DELETE`）时，**匹配的 HTTP 方法**在无注解情况下同样参与幂等（仍须 token 非空）。系统 MUST 支持 `excludeUrls[]`（Ant）：请求路径命中则**不参与**幂等。

#### Scenario: 仅注解命中

- **WHEN** 某方法带 `@Idempotent` 且 token 非空且路径未排除
- **THEN** 系统 MUST 执行占位逻辑

#### Scenario: 全局方法命中

- **WHEN** 方法无注解但请求方法在 `interceptMethods` 内且 token 非空且路径未排除
- **THEN** 系统 MUST 执行占位逻辑

#### Scenario: 排除路径跳过

- **WHEN** `excludeUrls` 匹配当前请求路径
- **THEN** 系统 MUST NOT 执行占位逻辑

### Requirement: 原子占位与 TTL

系统 MUST 提供幂等存储抽象，至少包含 **`setIfAbsent(key, ttl)`**（或语义等价的原子 NX + TTL）与 **`delete(key)`**。过期时间 MUST 优先取 `@Idempotent` 的 `expireTime/timeUnit`，否则取全局 `expireTime/timeUnit`。

#### Scenario: 重复请求在窗口内被拒绝

- **WHEN** 首次请求已占位成功且在 TTL 内第二次使用相同键尝试占位
- **THEN** 第二次 MUST 失败并触发重复语义（见「重复响应」条）

### Requirement: 存储 Redis 与 Caffeine 及 auto 回落

系统 MUST 支持 `cache-type`（或等价配置键）：**`redis`**、**`caffeine`**、**`auto`**。当为 **`auto`** 时：若 Spring 容器存在可用的 **`RedisConnectionFactory`**（或本实现文档中锁定的 Redis 基础设施 Bean），系统 MUST 使用 **Redis** 实现存储；否则 MUST 使用 **Caffeine**（进程内）实现。当显式 `redis` 但 Bean 缺失时，行为 MUST 为 **启动失败** 或 **按实现选定的显式 fail-fast**（须在 tasks 中固定一种并单测）。

#### Scenario: 有 Redis 时 auto 选 Redis

- **WHEN** `cache-type=auto` 且存在 `RedisConnectionFactory` Bean
- **THEN** 占位使用 Redis 后端

#### Scenario: 无 Redis 时 auto 选 Caffeine

- **WHEN** `cache-type=auto` 且不存在 `RedisConnectionFactory` Bean
- **THEN** 占位使用 Caffeine（进程内）后端

### Requirement: 重复响应 HTTP 200 与业务码 30201

当判定为幂等窗口内重复请求时，系统 MUST 通过 **`IdempotentException`**（或等价机制）表达 **`30201`**；经全局异常处理映射后，对外响应 MUST 为 **HTTP 200**，且响应体 MUST 携带统一 **`R`** 契约下的 **业务码 `30201`**（及可选 `msg`，可与 `defaultMessage`/i18n 对齐）。

#### Scenario: 重复时非 409

- **WHEN** 重复请求被拦截
- **THEN** HTTP 状态码 MUST 为 **200**，且业务错误由 **`R.code`**（或项目与同 spec 一致的字段）**`30201`** 表示

### Requirement: 业务异常释放占位

若占位成功后目标处理逻辑 **抛出异常**，系统 MUST **`delete`** 对应键，以便客户端使用**同一 token** 重试。若 `@Idempotent(deleteAfterExecution=true)`，系统 MUST 在方法**正常返回后** **`delete`** 键。

#### Scenario: 失败可重试

- **WHEN** 占位成功但业务方法抛异常
- **THEN** 存储中对应键 MUST 被删除（或等价不再阻档下一次同键占位）

### Requirement: 不校验 token 语义

系统 MUST **不对** token 格式、颁发、预注册做服务端校验；任意非空字符串均可。系统 MUST **不对** `X-User-Id`、session、IP 等与键拼接。

#### Scenario: 任意非空 token

- **WHEN** 头值为合法非空字符串
- **THEN** 仅按字符串参与键构造，无额外校验步骤
