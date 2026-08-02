## 1. Dependencies & Config

- [x] 1.1 选型并引入与 Spring Boot 4 可启动的 sa-token 依赖（含 Redis 集成如需要）
- [x] 1.2 增加 `sa-token` 与 `qc.login`（max-retry=5、lock-minutes=10）配置
- [x] 1.3 将 `/login` 加入 `qc.oauth.ignore-url`

## 2. Login Lockout

- [x] 2.1 实现 `LoginProperties` 与 Redis 失败计数 / 锁定助手（fail/lock key）
- [x] 2.2 覆盖：达阈值锁定、锁定中拒绝、成功清 key；验证码失败与停用不计入失败次数

## 3. Password Login Service

- [x] 3.1 实现 `LoginService`：验证码二次校验（可跳过）→ 锁定检查 → 用户/密码/`status` → `StpUtil.login` → 返回 `accessToken`
- [x] 3.2 精简 `LoginController#login` 调用 Service，统一 401/业务错误文案
- [x] 3.3 （可选）种子启用用户 SQL，密码为 `PasswordCodec`/`{bcrypt}` 密文

## 4. Frontend Alignment

- [x] 4.1 `quick-ui`：`POST /login`，body 使用 `uuid`（替换 `/auth/login` + `captchaId`）
- [x] 4.2 确认 user store 仍读取 `data.accessToken` 并写入本地 token

## 5. Verification

- [x] 5.1 手动验：验证码开/关、错误密码锁定、停用用户、成功发牌与 Bearer 请求
- [x] 5.2 确认空闲超过 `active-timeout` 后需重新登录（或文档说明如何观测）
