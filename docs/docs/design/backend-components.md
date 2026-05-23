# 后端组件（设计）

## 分层

```text
quickboot-common   ← 自动配置 + 过滤器/拦截器/AOP
quickboot-web      ← 业务 Controller
```

## 组件清单

| 包 | 能力 |
|----|------|
| `api` | R、分页、错误码 |
| `exception` | 全局异常 |
| `security.firewall.*` | XSS/SQL/敏感词/幂等/CORS/头 |
| `excel` | EasyExcel |
| `monitor.operlog` | 操作日志 AOP |
| `file` | 本地/MinIO |
| `captcha` | 行为验证码 |
| `desensitization` | 序列化脱敏 |

## 使用文档

- [组件总览](../backend/components/index)
- [安全防护（实现）](../backend/modules/security-module)
- 侧栏「后端通用组件设计」为各子模块设计说明

配置入口：`qc.*`（`application.yml`）。
