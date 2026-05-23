# 后端组件总览

可复用能力集中在 **`quickboot-common`**，通过 Spring Boot 自动配置注入。业务模块（`quickboot-web`）应优先引用，避免重复实现。

## 分类

| 分类 | 文档入口 |
|------|----------|
| API 与异常 | [异常处理模块](./通用组件/异常处理模块使用文档) |
| 安全防火墙 | [安全防护](../modules/security-module) |
| 操作日志 | `monitor.operlog`（AOP） |
| Excel | `excel` 包 + `ExcelUtils` |
| 脱敏 | [字段脱敏模块](./通用组件/字段脱敏模块使用文档) |
| 文件 | [文件上传模块](./通用组件/文件上传模块使用文档) |
| 国际化 | [国际化模块](./通用组件/国际化模块使用文档) |
| 验证码 | `captcha` 包（Tianai） |

## 使用方式

1. 在 `application.yml` 打开对应 `qc.security.firewall.*` 或 `qc.monitor.*` 开关  
2. Controller 按需加注解（如 `@IgnoreLogger`、幂等 Token 头）  
3. 业务 Vo 字段加 `@Sensitive` 控制序列化脱敏  

## 与前端对应

| 后端 | 前端 |
|------|------|
| 字典 API | `useDict`、`C7DictTag` |
| EasyExcel 导入导出 | `C7ExcelUpload`、`C7ExcelDownload` |
| 统一 `R` | `utils/request.js` |
| Client HMAC | `utils/clientSign.js` |

侧栏「通用组件」各篇为分模块说明，可按需展开阅读。
