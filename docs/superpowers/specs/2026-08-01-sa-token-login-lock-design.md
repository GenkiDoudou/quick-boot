# 账号密码登录（sa-token + 验证码二次校验 + 失败锁定）设计

**日期：** 2026-08-01  
**状态：** 已批准（对话确认）  
**范围：** 补全 `POST /login`；接入 sa-token；天爱验证码二次校验；用户状态校验；登录失败 Redis 锁定；前端字段/路径对齐。

## 背景

`LoginController#login` 仅有验证码二次校验草稿，未完成密码校验、状态判断与发牌。工程已移除 Spring Security，计划以 **sa-token** 作为会话方案。前端当前调用 `/auth/login` + `captchaId`，与后端约定不一致，本轮以后端为准并改前端。

## 已确认决策

| 项 | 选择 |
|----|------|
| Token | 单 accessToken + sa-token **活性续期**（不做 refreshToken） |
| 锁定 | Redis：连续失败 **5** 次锁定 **10** 分钟（可配置） |
| 路径/字段 | `POST /login`，验证码字段 **`uuid`**；前端对齐 |
| 验证码关闭 | `ImageCaptchaApplication` 不可用时 **跳过**二次校验 |

## 目标

1. 验证码二次验证（服务启用时）
2. 用户存在性 / 密码 / `status` 判断
3. sa-token 登录发牌；请求携带 token 时按 `active-timeout` 续期
4. 可配置的失败次数锁定

## 非目标（本轮不做）

- accessToken + refreshToken 双票
- 失败达阈值后改用户 `status=停用`
- 社交 / 短信 / 扫码登录
- 完整权限菜单与 `LoginUserService` 全量改造（可最小接通 token 校验）

## 方案

采用 **LoginService 编排 + LoginProperties + Redis 锁定 + sa-token 配置**（不把逻辑堆在 Controller，不新建独立 Auth 业务模块）。

### 登录流程

```
POST /login { username, password, uuid? }
  │
  ├─ captcha Bean 可用？
  │    ├─ 是且 uuid 空 → 401 请先完成验证码
  │    ├─ 是且 secondaryVerification(uuid)=false → 401 验证码已失效
  │    └─ 否 → 跳过验证码
  │
  ├─ Redis 存在 login:lock:{username}？ → 401 账号已锁定（可带剩余秒数）
  │
  ├─ 查 SysUser
  │    ├─ 不存在或密码不匹配 → 失败计数 +1；达 max-retry 写 lock TTL；统一文案「用户名或密码错误」
  │    └─ status != 启用(0) → 401 账号已停用（不计入失败锁定，或计入：本设计为「不计入」，避免误伤）
  │
  └─ 成功：清 fail/lock；StpUtil.login(userId)；返回 { accessToken, tokenName? }
```

密码校验使用现有 `PasswordCodec.matches(raw, encoded)`。

用户「不存在」与「密码错误」对外同一文案，避免枚举账号。

### 锁定键

| Key | 含义 | TTL |
|-----|------|-----|
| `login:fail:{username}` | 连续失败次数 | 与 lock-minutes 对齐或略长（实现时：锁定时删除 fail，或 fail 与 lock 同窗口） |
| `login:lock:{username}` | 锁定标记 | `lock-minutes`（默认 10 分钟） |

成功登录删除上述 key。

### 配置

```yaml
qc:
  login:
    max-retry: 5
    lock-minutes: 10
  oauth:
    ignore-url:
      - /login
      - /api/captcha/**
      # …既有项

sa-token:
  token-name: Authorization
  token-prefix: Bearer
  timeout: 604800        # 7 天绝对超时
  active-timeout: 1800   # 30 分钟无请求则失效；有请求则续期
  is-concurrent: true
  is-share: false
  is-read-header: true
```

依赖：引入与 Spring Boot 4 兼容的 sa-token starter（实现阶段核对坐标；优先官方 Boot3 starter 或社区 Boot4 适配，必要时 `sa-token-spring-boot-starter` + 手动配置）。持久化优先 **Redis**（与现有 Luban/Lettuce 一致）。

### API 契约

**请求** `POST /login`

```json
{ "username": "admin", "password": "...", "uuid": "<captchaId after TAC validate>" }
```

**成功**（HTTP 200，业务码 200）

```json
{
  "code": 200,
  "data": {
    "accessToken": "<sa-token value>",
    "tokenName": "Authorization"
  }
}
```

前端继续读 `data.accessToken`；请求头使用 `Authorization: Bearer <token>`（与现有拦截器一致）。

**失败**：HTTP 401（或沿用项目 WarningException → 401），`msg` 为可读中文。

### 前端对齐

- `src/api/login.js`：`url: '/login'`；body 字段 `uuid`（由原 `captchaId` 映射）
- `login.vue` / user store：字段名改为 `uuid`，或提交前映射即可

### 模块落点

| 组件 | 位置（建议） |
|------|----------------|
| `LoginController` | `quickboot-system`（变薄，调 Service） |
| `LoginService` / `LoginProperties` / 锁定助手 | `quickboot-system` 或 `quickboot-auth`（若 auth 模块已适合放登录编排则优先 auth；否则 system） |
| sa-token 自动配置 / 拦截放行 | `quickboot-web` 或 common 自动配置 |
| 种子用户（可选） | `db/data-*.sql` + BCrypt 密文 |

`/login` 必须在 Client Basic `ignore-url` 中，或登录接口允许「无用户 token 时用 Client Basic」——与现网 oauth 过滤器行为对齐：**登录接口加入 ignore-url**，避免未登录无法打登录。

## 错误与安全

- 验证码二次校验失败：不暴露内部异常细节
- 锁定中仍校验验证码（可选优化：锁定时仍要求验证码，防撞库）；本设计 **锁定优先于密码校验**，仍建议在锁定前完成验证码（流程顺序已含）
- 日志：用户名可打 debug；禁止打明文密码

## 验收标准

1. 验证码开启：无 `uuid` / 无效 `uuid` 无法登录；有效 `uuid` 一次性消费
2. 验证码关闭：可不带 `uuid` 登录
3. 错误密码 5 次后锁定 10 分钟；期间即使密码正确也拒绝；到期或成功登录后清除
4. `status=1` 用户无法登录
5. 成功返回 `accessToken`；后续请求 Bearer 可通过；空闲超过 `active-timeout` 失效，活跃请求续期
6. 前端改用 `/login` + `uuid` 后能完成登录跳转

## 实现备注

- Spring Boot **4.0.0**：选型 sa-token 坐标时需验证能启动；若官方仅 Boot3 starter，评估兼容或降级手动注册过滤器
- `SecondaryVerificationApplication` 强转：仅当 captcha 启用且 Bean 实际为该类型时调用；否则跳过或明确报错
- 与历史 `/auth/login` 文档：本轮废弃该路径，README/注释随后更正（非阻塞）
