# 功能模块总览

本文按 **Controller 包** 索引 quickboot-web 已实现能力。接口路径以 Swagger 为准。

## 认证与会话（`web.auth`）

| 类 | 路径前缀 | 能力 |
|----|----------|------|
| `AuthController` | `/` | 登录、登出、`getInfo`、`getRouters`、验证码相关 |
| `QrcodeImageController` | — | 二维码图片 |

## OAuth2（`web.auth.oauth2`）

| 类 | 能力 |
|----|------|
| `SaOAuth2ServerController` | 授权服务器 `/oauth2/*`（Sa-Token） |
| `OAuth2ClientController` | 第三方 IdP：`/oauth2/client/authorize`、`callback/{provider}` |
| `OAuth2LoginProvidersController` | 登录页可用 IdP 列表 `/oauth/login/providers` |
| `OpenApiUserinfoController` | Open API `/open-api/v1/userinfo` |
| `SysOauthClientController` | 管理端 OAuth 客户端 CRUD |
| `SysOauthProviderController` | 管理端外部 IdP 配置 CRUD |

对接说明见 [OAuth2 集成](./oauth2)。

## 系统管理（`web.system`）

| 模块 | Controller | 说明 |
|------|------------|------|
| 用户 | `SysUserController` | 用户 CRUD、导入导出、角色分配 |
| 角色 | `SysRoleController` | 角色、菜单权限、**数据权限** |
| 菜单 | `SysMenuController` | 菜单树、路由元数据 |
| 部门 | `SysDeptController` | 部门树 |
| 字典类型 | `DictTypeController` | 字典类型 |
| 字典数据 | `DictDataController` | 字典项 |
| 参数配置 | `SysConfigController` | 系统参数键值 |
| 通知公告 | `SysNoticeController` | 公告（富文本 XSS 过滤） |

## 监控与调度（`web.monitor`）

| 模块 | Controller | 说明 |
|------|------------|------|
| 操作日志 | `SysOperLogController` | AOP 采集、查询、导出 |
| 登录日志 | `SysLogininforController` | 登录成功/失败记录 |
| 在线用户 | `SysUserOnlineController` | 会话列表、强退 |
| 定时任务 | `SysJobController` | Quartz 任务 CRUD、启停、立即执行 |
| 任务日志 | `SysJobLogController` | 执行历史 |

## 开发工具（`web.tool`）

| 模块 | Controller | 说明 |
|------|------------|------|
| 代码生成 | `GenController` | 表导入、配置、预览、生成 ZIP；MyBatis-Plus Generator + FreeMarker |

配置项：`quickboot.gen.*`（作者、包名、模块名等）。

---

## quickboot-common 能力索引

与业务模块正交的横切能力（实现于 common，由 web 引用）：

| 能力 | 包/类（示意） |
|------|----------------|
| 统一响应与分页 | `api.R`、`PageInfo` |
| 异常与错误码 | `exception.*` |
| 国际化 | `i18n.I18nUtil` |
| 缓存 | `cache` 动态 TTL |
| Excel | `excel.EasyExcelSupport` 等 |
| 字段脱敏 | `desensitization` |
| 验证码 | `captcha` |
| 文件存储 | `file.FileTemplate` |
| 操作日志 | `monitor.operlog` |
| Web 防火墙 | `security.firewall.*` |

详细使用文档规划见 [能力文档大纲](../../guide/capabilities-outline) 第三节「通用组件」。

---

## 数据库迁移索引

| 版本 | 文件 | 用途 |
|------|------|------|
| V1 | baseline | Flyway 基线 |
| V2–V7 | sys_dept … sys_notice | 部门、字典、配置、菜单、公告 |
| V8–V15 | 各类 menu/seed | 用户/配置/角色等菜单与演示数据 |
| V16–V17 | oper_log, logininfor | 操作日志、登录日志 |
| V18 | gen_table | 代码生成元数据 |
| V19–V22 | sys_job, quartz | 定时任务与 Quartz 修正 |
| V23–V24 | dict_menu, online_menu | 字典、在线用户菜单 |
| V25–V31 | oauth2_* | OAuth2 表、菜单、quick-ui 客户端、API 路径授权、签名校验 |

脚本路径：`quickboot-web/src/main/resources/db/migration/`。

---

## 模块文档

| 文档 | 说明 |
|------|------|
| [用户管理](./user-management) | 用户 CRUD、导入导出 |
| [权限管理](./permission-management) | 菜单、角色、数据权限 |
| [系统配置](./system-management) | 部门、字典、参数、公告 |
| [监控审计](./monitor-audit) | 操作/登录日志、在线用户 |
| [定时任务](./job-scheduler) | Quartz |
| [代码生成](./codegen) | 表导入与 ZIP 生成 |
| [安全防护](./security-module) | Web 防火墙 |
| [客户端管理](./client-management) | OAuth Client + HMAC |
| [OAuth2 集成](./oauth2) | AS / 联邦 / Open API |

## 相关文档

- [后端概述](../index)
- [接口规范](../api/index)
