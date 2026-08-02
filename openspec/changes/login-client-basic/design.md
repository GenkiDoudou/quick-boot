## Context

当前 `/auth/login` 只收用户名密码，`AuthTokenService.issueUserToken` 写死 `quick-ui` 发牌。产品已确认：无用户 JWT 时用混淆 + HTTP Basic 校验 client；与用户密码校验分离；有合法用户 Bearer 则跳过 client 校验。产品设计见 `docs/superpowers/specs/2026-07-27-login-client-basic-design.md`。

## Goals / Non-Goals

**Goals:**

- 无 JWT 请求强制 Basic 客户端校验（解混淆 + secret matches）
- 登录发牌绑定已校验 RegisteredClient（须 password grant），去掉 hardcode
- 前端混淆凭证并在无 token 时自动注入 Basic

**Non-Goals:**

- BFF、PKCE、HMAC、client ticket、全站每次请求带 secret
- 将混淆当作抗抓包加密

## Decisions

1. **传输：S2 Basic + 混淆**  
   - `Authorization: Basic base64(obfId:obfSecret)`。  
   - 备选：自定义 X-Client-* 头 —— 不如标准 Basic。  
   - 备选：公钥加密 / HMAC —— 本期不做。

2. **校验时机：无用户 JWT 才验**  
   - Bearer 用户 JWT 有效则 skip；否则验 Basic。  
   - 备选：所有 API 都验 —— 业务请求反复暴露 secret。  
   - 备选：公开接口白名单 —— 维护成本高。

3. **Filter + request attribute**  
   - Filter 成功后放入 `RegisteredClient`；`/auth/login` 读取并发牌。  
   - Exclude：`/actuator/**`、`/h2-console/**`、静态资源。

4. **混淆算法**  
   - 前后端约定同一可逆变换（如固定盐 XOR + Base64 字符集）；文档标明非加密。

5. **Refresh**  
   - 无 JWT 时同样走 Basic；发牌 client 以 OAuth2Authorization 上记录为准，若与 Basic clientId 不一致则拒绝（防错绑）。

## Risks / Trade-offs

- [SPA secret 可抓包] → HTTPS + 文档诚实说明；后续可迁 PKCE/BFF  
- [Bearer 与 Basic 互斥] → 拦截器：有用户 token 用 Bearer，否则 Basic  
- [password grant 滥用] → 仅 grantTypes 含 password 的 client 可登录发牌（库内约束仍在）

## Migration Plan

1. 部署后端 Filter + 登录改造；前端同步发版（无 Basic 则登录失败）。  
2. 配置 `VITE_OAUTH_CLIENT_ID/SECRET`（开发默认 quick-ui / quick-ui-secret）。  
3. 回滚：恢复硬编码发牌并去掉 Filter（旧前端可临时兼容，不推荐长期）。

## Open Questions

- 无（决策已在产品设计确认）
