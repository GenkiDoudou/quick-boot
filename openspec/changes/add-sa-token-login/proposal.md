## Why

`LoginController#login` 仅有验证码二次校验草稿，无法完成账号密码登录发牌；Spring Security 已移除，需用 sa-token 承接会话。现在补齐验证码二次校验、用户状态、失败锁定与前端路径字段对齐，才能打通管理端登录闭环。

## What Changes

- 补全 `POST /login`：验证码二次校验（可关闭跳过）、密码校验、用户 `status` 判断、sa-token 发牌与活性续期
- Redis 登录失败锁定：默认连续 5 次失败锁定 10 分钟（`qc.login.*` 可配）
- 引入 sa-token（优先 Redis 持久化）与相关配置；`/login` 加入 Client Basic `ignore-url`
- **BREAKING（前端）**：登录改为 `POST /login`，验证码字段改为 `uuid`（废弃 `/auth/login` + `captchaId`）
- 不引入 refreshToken；不做社交/短信/扫码；不把失败写成用户停用

## Capabilities

### New Capabilities

- `password-login`: 账号密码登录编排（验证码二次校验、状态、锁定、sa-token 发牌与返回契约）
- `login-lockout`: 基于 Redis 的连续失败计数与临时锁定

### Modified Capabilities

- （无）当前 `openspec/specs/` 无既有登录会话能力需改需求级 delta

## Impact

- 后端：`quickboot-system`（LoginController / LoginService）、依赖与 `application*.yml`（sa-token、`qc.login`）、oauth `ignore-url`
- 前端：`quick-ui` 的 `login.js` / `login.vue` / user store
- 依赖：sa-token（需验证 Spring Boot 4 兼容坐标）、现有 Redis、天爱 captcha、`PasswordCodec`
- 文档：与历史 `/auth/login` 说明冲突处后续更正
