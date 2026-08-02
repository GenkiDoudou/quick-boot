## Context

`LoginController` 位于 `quickboot-system`，目前仅草稿调用天爱 `SecondaryVerificationApplication`。工程已移除 Spring Security，会话拟用 sa-token；密码哈希走既有 `PasswordCodec`；验证码走 `qc.captcha`；Client Basic 过滤器保护 API，登录路径须加入 `ignore-url`。前端仍打 `/auth/login` + `captchaId`，本变更以后端 `POST /login` + `uuid` 为准并改前端。详细产品决策见 `docs/superpowers/specs/2026-08-01-sa-token-login-lock-design.md`。

## Goals / Non-Goals

**Goals:**

- 可工作的账号密码登录：验证码二次校验（可关）、状态校验、Redis 失败锁定、sa-token 发牌与活性续期
- 清晰分层：Controller 薄、LoginService 编排、LoginProperties 配置
- 前后端路径/字段对齐；`/login` 可匿名（相对用户 token）访问

**Non-Goals:**

- refreshToken 双票体系
- 失败达阈值改用户 `status=停用`
- 社交 / 短信 / 扫码
- 完整 RBAC / 菜单权限改造（仅最小接通 token）

## Decisions

1. **编排落点：`quickboot-system` 的 LoginService**  
   - 备选：扩 `quickboot-auth` 为完整登录模块 → 本轮范围过大。  
   - 理由：Controller 已在 system，用户查询已在 `ISysUserService`。

2. **Token：单 token + `active-timeout` 续期**  
   - 备选：access + refresh → 前端与安全复杂度更高。  
   - 理由：管理端已批准；与现有单 Cookie/Header token 一致。

3. **锁定：Redis `login:fail:{user}` / `login:lock:{user}`**  
   - 备选：写库停用 → 需管理员解禁，误伤大。  
   - 默认 `max-retry=5`，`lock-minutes=10`。停用账号拒绝登录且**不计入**失败锁定。

4. **验证码：Bean 可用则强制 `uuid` 二次校验；不可用则跳过**  
   - 备选：关闭验证码也禁止登录 → 不利本地调试。

5. **sa-token + Redis；Boot 4 坐标实现时验证**  
   - 备选：仅内存 → 多实例/重启丢会话。  
   - Header：`Authorization: Bearer <token>`，响应用 `accessToken` 字段兼容前端。

6. **错误：统一 WarningException / HTTP 401**  
   - 「用户不存在」与「密码错误」同一文案，防枚举。

## Risks / Trade-offs

- [Boot 4 与 sa-token starter 不兼容] → 实现阶段试官方 Boot3 starter / 手动注册；不通过则换兼容版本或薄封装  
- [Client Basic 仍拦 `/login`] → `ignore-url` 显式加入 `/login`  
- [验证码关闭后撞库] → 依赖锁定与后续限流；文档标明生产建议开启验证码  
- [前端 BREAKING] → 同步改 `login.js` / store / vue，避免半旧半新

## Migration Plan

1. 合入后端依赖与 `/login` 实现，配置 `qc.login` + `sa-token` + `ignore-url`
2. 部署/本地重启后用 curl 验锁定与发牌
3. 前端切 `/login` + `uuid` 后联调
4. 回滚：去掉登录编排与依赖，前端可临时回旧路径（旧路径本轮不再维护）

## Open Questions

- sa-token 在 Spring Boot 4.0.0 上的最终 Maven 坐标（实现时锁定）
- 是否提供种子管理员账号 SQL（联调可选，非阻塞）
