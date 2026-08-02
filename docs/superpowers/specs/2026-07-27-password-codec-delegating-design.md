# PasswordCodec 委托扩展设计（方案 A）

> 日期：2026-07-27  
> 范围：`quickboot-common` → `io.github.genkidoudou.common.crypto`  
> 已拍板：内置算法 + **实例** `register` 扩展（方案 A）

## 1. 目标

- 开箱即用：`{bcrypt}` / `{sm3}` 内置。
- 可扩展：业务在拿到同一 `DelegatingPasswordCodec` 实例后 `register(id, codec)`。
- 存储形态与 Spring `DelegatingPasswordEncoder` 对齐：`{id}` + 算法负载。
- 无前缀历史哈希：按默认 bcrypt 做 `matches`（兼容旧存根）。

## 2. 非目标

- 不在本变更中接入 Spring Security `PasswordEncoder` 适配层。
- 不实现可逆加密的通用 decrypt（单向哈希仍 `UnsupportedOperationException`）。
- 不引入 SPI / 自动扫描注册（方案 C）；需要时可后补。

## 3. 类型职责

| 类型 | 职责 |
|------|------|
| `PasswordCodec` | `encrypt` / `matches` / `decrypt` 契约 |
| `AbstractValidatingPasswordCodec` | null/空串校验模板 |
| `BCryptPasswordCodec` | Hutool BCrypt；`encrypt` 产出 `$2a$...`；`decrypt` 不支持 |
| `Sm3PasswordCodec` | 已有 SM3 hex |
| `DelegatingPasswordCodec` | `{id}` 路由；持有可变算法表；`register` |
| `PasswordCodecFactories` | 仅工厂：创建带内置的委托实例；不持有全局可变表 |

## 4. API（方案 A）

### 4.1 创建（内置）

```java
PasswordCodec codec = PasswordCodecFactories.create();
// 等价：DelegatingPasswordCodec，idForEncode = "bcrypt"
// 内置：bcrypt → BCryptPasswordCodec，sm3 → Sm3PasswordCodec
// defaultPasswordEncoderForMatches = 同一 bcrypt 实例（无前缀时用）
```

`create(String encodingId)` / `create(String encodingId, Map<String, PasswordCodec> extras)`：在内置 Map 上 merge extras（同 id 以 extras 覆盖），再构造。

### 4.2 扩展（实例方法）

```java
public DelegatingPasswordCodec register(String id, PasswordCodec codec);
```

规则：

- `id` / `codec` 非 null；`id` 不含 `{` / `}`。
- 写入 `ConcurrentHashMap`（替换构造时的 `HashMap` 拷贝）。
- **允许覆盖**同 id（便于测试替换）；文档注明「覆盖内置需谨慎」。
- **不改变** `idForEncode` / `passwordEncoderForEncode`（新密文仍用创建时默认算法）。
- 删除错误的 `static addPasswordCodec`（`static` + `this` 非法且职责错误）。

### 4.3 编解码行为（保持）

- `encrypt` → `{idForEncode}` + delegate.encrypt(raw)
- `matches` → 解析 `{id}`；命中则剥前缀后委托；未命中前缀则 `defaultPasswordEncoderForMatches.matches(raw, 整串)`
- 构造结束时必须设置 `defaultPasswordEncoderForMatches`（Factories 内设为 bcrypt），避免 NPE

### 4.4 与 Spring

- 应用侧：注册一个 `DelegatingPasswordCodec`（或 `PasswordCodec`）Bean = `Factories.create()`。
- 扩展模块在 `@PostConstruct` / `InitializingBean` 中注入该 Bean 并 `register(...)`。
- `PasswordCodecFactories` **不再**依赖 `SpringUtil` 半成品逻辑；查找 Bean 留给业务配置。

## 5. BCrypt 实现

- 类名：`BCryptPasswordCodec`（与 `Sm3PasswordCodec` 对称；Factories 已引用此名）。
- 依赖：Hutool `BCrypt`（common 已有 hutool-all，零新增依赖）。
- `encrypt`：`BCrypt.hashpw(raw)`（或显式 strength，默认 Hutool/ jBCrypt 默认 cost）。
- `matches`：`BCrypt.checkpw(raw, encoded)`。
- `decrypt`：抛 `UnsupportedOperationException`。

## 6. 文件改动清单

1. 新增 `BCryptPasswordCodec.java`
2. 改 `DelegatingPasswordCodec`：`ConcurrentHashMap`、`register`、构造内初始化 default matches、删除 static add / 重复 create（create 只留 Factories）
3. 改 `PasswordCodecFactories`：完整 `create` / 可选 overload；去掉残缺 SpringUtil 代码
4. （可选）单测：encrypt 带 `{bcrypt}`、sm3 matches、register 后新 id matches、无前缀 bcrypt matches

## 7. 成功标准

- `create().encrypt("x")` 以 `{bcrypt}` 开头且可 `matches`
- `{sm3}` 历史串可 matches
- `register("noop", ...)` 后 `{noop}...` 可 matches
- 无编译错误；Factories 不再引用不存在的类型/半截语句

## 8. 开放点（实现时默认）

- BCrypt strength：跟 Hutool 默认，不在本版暴露构造参数（需要时可后加 `BCryptPasswordCodec(int strength)`）。
- `register` 是否禁止覆盖内置：默认允许覆盖。
