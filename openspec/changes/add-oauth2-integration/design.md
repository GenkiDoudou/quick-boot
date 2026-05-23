## Context

仓库现状：Sa-Token **1.38.0**（`quickboot/pom.xml`），`AuthController` + `StpUtil` 管理 `quick-ui` 会话；`SaTokenWebMvcConfig` 拦截 `/system/*`、`/monitor/*`。设计真源：`docs/superpowers/specs/2026-05-23-oauth2-integration-design.md`。

已确认决策：AS + Client 双角色；四种 Grant 均实现、**生产默认关** password/implicit；第三方仅 `openid`/`profile` 只读；实施时升级 Sa-Token **1.44+** 并全站回归。

## Goals / Non-Goals

**Goals:**

- 三轨认证：内部 Admin-Token、`/open-api/**` OAuth2 token、外部 IdP 短期 code（换本地会话）。
- AS：`/oauth2/*` + Redis 持久化 + `sys_oauth_client` DataLoader + 登录桥接（验证码、锁定、登录日志）。
- 开放 API：`GET /open-api/v1/userinfo`，按 scope 裁剪字段。
- Client：至少 1 个可配置 IdP（建议 Keycloak/GitHub 沙箱）完成联邦登录与绑定。
- 管理端 CRUD + 授权确认 Vue 页（`DESIGN.md`）。

**Non-Goals:**

- 向第三方开放 `/system/*`、`/monitor/*`；OIDC 完整 IdP（discovery/JWKS 仅预留）；替换主登录为纯 OAuth；本期改造防火墙 `X-Client-Id`。

## Decisions

| 决策 | 选择 | 理由 |
|------|------|------|
| 双角色 | AS + Client 同仓 | 设计已定稿；命名空间 `oauth2.server.*` vs `oauth2.client.*` 分离 |
| Grant 策略 | 四种实现 + prod 默认关 password/implicit | 满足 2-C；`sys_config` + per-client `grant_types` 双重控制 |
| Scope | 仅 `openid`、`profile` | 3-A；禁止 permissions/写 scope |
| Token 存储 | Redis 必选（prod） | 多实例 code/token；启动检查 Redis |
| 路径隔离 | `/open-api/**` vs `/system/**` | OAuth2 token 不得进管理 API；Admin-Token 不得冒充 scope |
| AS 登录桥接 | `AuthLoginService` + 验证码 + `LoginLockService` | 与主登录安全策略一致；日志标注 `OAuth2-AS` |
| Client 回调后 token | 与现有 `/login` 相同写 `Admin-Token` | 避免前端第二套 token 逻辑 |
| 模块布局 | `quickboot-web/.../web/auth/oauth2/{server,client,open}` | 见设计 §8 |
| secret 存储 | 项目 PasswordCodec/BCrypt 加密 | 日志与 operlog 脱敏 |

**备选（未采用）**：仅 AS 或仅 Client——不满足双角色需求；OAuth2 token 复用管理端拦截器——违反隔离。

## Risks / Trade-offs

| 风险 | 缓解 |
|------|------|
| Sa-Token 1.38→1.44+ 行为变化 | P0 清单：login/logout/getInfo/路由/在线用户/operlog/异常码 |
| 四种 Grant 扩大攻击面 | prod 全局关 password/implicit；redirect_uri 精确匹配 |
| Token 混用 | 路径前缀 + 集成测试断言 403 |
| AS/Client 概念混淆 | 表名 `sys_oauth_client` vs `sys_oauth_provider`；文档三分 |
| Redis 未配导致 code 丢失 | 启动校验；dev 可文档标注单实例限制 |

## Migration Plan

1. **P0**：升级依赖、接 Redis、跑回归；无业务表变更。
2. **P1–P2**：Flyway 建表 → 部署 AS 与 open-api → 配置 `qc.oauth2.server.*`。
3. **P3–P4**：前端授权页与管理 CRUD → 配置 IdP → 验证联邦登录。
4. **P5**：第三方对接文档、prod 清单。
5. **回滚**：关闭 `qc.oauth2.server.enabled` / `qc.oauth2.client.enabled`；Sa-Token 版本回退需与 P0 快照一致（运维记录版本号）。

## Open Questions

- （无）Flyway `menu_id` 与并行变更冲突时，实现阶段以仓库当前最大迁移序号顺延。
