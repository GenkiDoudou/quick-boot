# OAuth 客户端密钥明文存储与查看

日期：2026-07-26  
状态：已确认待实施  
关联：`docs/superpowers/specs/2026-07-26-oauth-client-mp-menu-design.md`

## 1. 目标

1. `oauth2_registered_client.client_secret` **明文存储**，管理端可在校验管理员密码后查看 Client ID / Secret。  
2. 客户端管理表单各字段 label 旁增加说明（小眼睛 + tooltip）。  
3. **用户登录密码仍 BCrypt**，不改为明文。

## 2. 已确认决策

| 项 | 选择 |
|----|------|
| 客户端密钥存储 | 库内真明文（不 BCrypt、不加 `{noop}` 前缀） |
| PasswordEncoder | 双模式：`encode`→BCrypt；`matches`→BCrypt 形态走哈希，否则明文 equals |
| 列表/普通详情 | 不返回 `clientSecret` |
| 查看密钥 | `POST .../reveal-secret`，Body 校验**当前登录用户**密码 |
| 二次确认 | 需要（方案 B） |
| 表单提示 | label 前小眼睛 + `el-tooltip` |

## 3. 后端

### 3.1 PasswordEncoder

替换 `AuthBeansConfiguration#passwordEncoder`：

- `encode(raw)`：`BCryptPasswordEncoder.encode`（用户密码）
- `matches(raw, encoded)`：
  - `encoded` 以 `$2a$` / `$2b$` / `$2y$` 开头 → BCrypt matches  
  - 否则 → `raw` 与 `encoded` 明文相等（客户端密钥）

### 3.2 写入路径

- `OAuthClientService#toRegisteredClient`：有 secret 时直接 `builder.clientSecret(plain)`，**禁止** encode。  
- `Oauth2RegisteredClientSeeder`：种子 secret 写明文（`quick-ui-secret` / `demo-app-secret` / `job-runner-secret`）。  
- 若库中已有客户端且 secret 为 BCrypt 形态：种子/启动迁移将其更新为上述约定明文（脚手架可接受）。

### 3.3 Reveal API

`POST /system/oauth-clients/{clientId}/reveal-secret`

请求：

```json
{ "password": "管理员当前密码" }
```

流程：

1. 从 SecurityContext / JWT 取当前用户标识  
2. `AuthUserLookup` 取账号，`passwordEncoder.matches(password, passwordHash)`  
3. 失败 → 业务错误（如 400，文案「密码错误」）  
4. 成功 → 查 `RegisteredClient`，返回：

```json
{ "clientId": "...", "clientSecret": "..." }
```

客户端不存在 → 404。

## 4. 前端

- 操作列「查看」→ 先密码弹窗 → 调 reveal → 展示 Client ID + Secret（可复制）。  
- 表单字段提示文案：

| 字段 | 说明要点 |
|------|----------|
| Client ID | OAuth2 客户端唯一标识，创建后不可改 |
| Client Secret | 客户端密钥，库内明文；创建必填，修改留空则不改 |
| 授权类型 | 如 authorization_code / refresh_token；password 仅 quick-ui |
| Redirect URIs | 授权码回调地址 |
| Scopes | 授权范围 |
| 需要授权同意 | 是否展示用户同意页；quick-ui 强制否 |

## 5. 非目标

- AES 等可逆加密、审计日志、改表结构、列表默认带回明文 secret。

## 6. 验收

1. 新建客户端后，库中 `client_secret` 为明文。  
2. `/auth/login`（quick-ui + 明文 secret）仍可发牌。  
3. 错误管理员密码无法 reveal；正确密码返回明文 secret。  
4. 表单各字段有小眼睛说明。  
