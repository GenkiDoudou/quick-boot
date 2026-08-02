## Context

活代码几乎无登录；详细产品/技术设计见 `docs/superpowers/specs/2026-07-26-spring-security-oauth2-as-design.md`。本 design 将该方案落到 OpenSpec 实现约束：Boot 4、Spring Security、同进程 AS + RS、不用 Sa-Token/JustAuth。利益相关方为管理端 `quick-ui`、未来外部 OAuth 客户端、以及后续业务 API。

## Goals / Non-Goals

**Goals:**

- AS 作为唯一 JWT 签发点（password 扩展、authorization_code、refresh、可选 client_credentials）
- 管理端密码登录门面 + 用户 JWT；社交登录绑定/建号后发同种用户 JWT
- 外部 App 仅授权码；RegisteredClient 可管理；用户/客户端 Token 可区分（`token_kind`）
- 业务 API 作为 Resource Server 验 Bearer JWT
- 按 P0 → P1 → P2 分期可验收交付

**Non-Goals:**

- JustAuth、Sa-Token、完整菜单权限迁回、多 Issuer/多租户 AS
- 向非第一方 client 开放 password grant
- 本期强制完成全部社交平台（首期 ≥1 个 IdP 即可）

## Decisions

1. **一体 AS（同应用）**  
   - 选择：`quickboot-web` 同进程部署 AS + RS + oauth2Login。  
   - 备选：独立认证服务 —— 运维重，当前团队/仓库不适合。  
   - 理由：与已定「一体发牌」一致，联调成本低。

2. **管理端 password 扩展 grant + `/auth/login` 门面**  
   - 选择：SAS 扩展 `grant_type=password`；对外推荐 JSON 门面内部走同一 TokenGenerator。  
   - 备选：仅 authorization_code+PKCE 给 SPA —— UX 绕；自签 JWT 不经 AS —— 违背唯一发牌。  
   - 理由：贴合后台习惯且发牌仍收敛到 AS。

3. **社交发牌：直接调同一 OAuth2TokenGenerator**  
   - 选择：绑定完成后服务端签发用户 JWT，不强制再走浏览器 password。  
   - 备选：社交后模拟 password —— 多余且难存密。  
   - 理由：密钥与 claims 一致，实现简单。

4. **模块 `quickboot-auth` + system 端口倒转**  
   - 选择：Security/AS 在 auth；用户/绑定在 system；auth 依赖用户查询接口。  
   - 备选：全部塞进 system —— 与 Security 配置耦合难测。  

5. **Token 区分用 `token_kind` claim**  
   - 用户：`token_kind=user`，`sub=userId`；客户端：`token_kind=client`，`sub=client_id`。  
   - `/auth/me` 仅接受 user token。

6. **外部 client 禁止 password**  
   - 管理 API / 种子数据校验 grant 列表。

7. **分期**  
   - P0：AS 骨架 + password + `/auth/login` + RS + `quick-ui`  
   - P1：社交 + bind 表  
   - P2：客户端 CRUD + 授权码联调 + 可选 client_credentials  

## Risks / Trade-offs

- [password 属 OAuth2.1 弃用] → 仅第一方 client；文档标明风险；可后续改纯 code 流而不改 JWT 形态  
- [SAS 扩展 grant 升级 fragile] → 集中在 auth 模块；加集成测试锁行为  
- [双过滤器链 matcher 易配错] → 明确 `/oauth2/**` vs 应用链；冒烟清单覆盖登录与 API  
- [社交 IdP 差异] → 首期只承诺 1 个；其余配置追加  
- [与 quick-ui 旧 API 不兼容] → **BREAKING** 相对 bak 路径；P0 起改登录对接  

## Migration Plan

1. 合并依赖与空模块骨架，不破坏现有 H2/Luban 启动  
2. P0 上线后管理端改用新登录；旧 Sa-Token 路径不恢复  
3. P1 打开社交开关（yml secret）  
4. P2 注册外部 demo client 联调  
5. 回滚：关闭 Security 强制鉴权开关或回退版本；JWT 无服务端会话，主要风险在客户端存 token  

## Open Questions

- Access/Refresh 具体存 JDBC vs Redis：默认 P0 用 SAS JDBC，可后续加 Redis  
- 社交落地页传 token：默认 JSON/`/auth/social/complete`，避免 query 长期 token（与详细方案一致）  
- 管理权限模型：客户端 CRUD 首期可仅 admin 用户或暂放行开发环境（实现时选一种并写进 tasks）  
