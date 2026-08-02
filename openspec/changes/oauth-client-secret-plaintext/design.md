## Context

客户端管理（`/system/oauth-clients` + `system/oauthClient/index`）已可用，但 `client_secret` 经全局 `BCryptPasswordEncoder` 哈希后无法回显。产品已确认：库内明文保存客户端密钥，查看前校验当前登录用户密码；用户密码仍 BCrypt。详细产品设计见 `docs/superpowers/specs/2026-07-26-oauth-client-secret-plaintext-design.md`。

## Goals / Non-Goals

**Goals:**

- 新建/更新/种子写入的客户端密钥为明文，且 AS 客户端认证仍可用
- 提供密码二次确认后的 reveal API；列表/普通详情不返回 secret
- 管理端「查看」与表单字段小眼睛说明

**Non-Goals:**

- AES 等可逆加密、reveal 审计日志、改表结构
- 列表默认带回明文 secret
- 改变用户密码哈希策略

## Decisions

1. **双模式 PasswordEncoder（单 Bean）**  
   - 选择：`encode`→BCrypt；`matches`→`$2a$`/`$2b$`/`$2y$` 走 BCrypt，否则明文 equals。  
   - 备选：DelegatingPasswordEncoder + `{noop}` 前缀 —— 库内带前缀、展示需剥离。  
   - 备选：客户端专用 NoOp Bean —— SAS 接线改动更大。  
   - 理由：满足「真明文」且用户密码路径不变。

2. **写入路径不再 encode 客户端密钥**  
   - `OAuthClientService` / Seeder 直接存明文；种子约定：`quick-ui-secret`、`demo-app-secret`、`job-runner-secret`。  
   - 已存在且为 BCrypt 形态的种子客户端：启动时更新为约定明文（脚手架可接受）。

3. **Reveal 与列表分离**  
   - `POST /system/oauth-clients/{clientId}/reveal-secret` + `{ "password" }`。  
   - 从 JWT/`SecurityContext` 取当前用户 → `AuthUserLookup` + `matches` 校验密码 → 返回 `{ clientId, clientSecret }`。  
   - 备选：详情 query `?reveal=true` —— 易误缓存/误打日志，不采用。

4. **前端**  
   - 「查看」：密码弹窗 → reveal → 展示可复制。  
   - 表单：label 前小眼睛 + `el-tooltip`（字段说明按设计文档表）。

## Risks / Trade-offs

- [库内明文 secret] → 依赖库访问控制与 reveal 二次密码；文档标明脚手架权衡  
- [旧 BCrypt 客户端导致认证失败] → 种子迁移写回明文；开发可清库重启  
- [双模式 matches 误判] → 仅识别标准 BCrypt 前缀；用户哈希与客户端明文路径分离  

## Migration Plan

1. 部署含双模式 Encoder 与明文写入的版本  
2. 启动种子将三客户端 secret 对齐明文（或清 H2/开发库）  
3. 冒烟：`/auth/login`、reveal 正/误密码、新建客户端查库明文  
4. 回滚：恢复 BCrypt 写入会导致已明文 secret 无法 matches（需重新 seed）；脚手架环境优先清库

## Open Questions

- 无（决策已在设计文档确认）
