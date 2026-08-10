# 项目介绍

QuickBoot（本仓库 `quickboot2`）是一套 **企业后台全栈解决方案**：Java 后端 + Vue 3 管理端 + VitePress 文档站，强调公共能力下沉、规范驱动协作与统一的前端列表/表单体验。

## 仓库结构

| 目录 | 说明 |
|------|------|
| `quickboot/` | Spring Boot 3 后端（Maven 多模块） |
| `quick-ui/` | Vue 3 + Vite + Element Plus 管理端 |
| `docs/` | VitePress 文档站点（当前站点） |
| `openspec/` | OpenSpec 变更与规范 |

## 技术栈一览

### 后端（quickboot）

- Java 17、Spring Boot 3.5.x
- MyBatis-Plus、Flyway、Druid
- **Sa-Token**（会话 + OAuth2 授权服务器/客户端）
- EasyExcel、Quartz、SpringDoc OpenAPI
- 缓存：Caffeine / Redis 可切换；配置加密：Jasypt

### 前端（quick-ui）

- Vue 3.5、Vite 5、Pinia、Vue Router 4
- Element Plus 2.x、Axios、Vitest
- **C7 组件库**（`src/packages`）：JSON 驱动表格/表单、Excel、字典标签等
- 包管理：**pnpm 9**

## quickboot 核心能力摘要

### 模块划分

- **quickboot-common**：统一响应 `R`、异常、校验分组、i18n、缓存、Excel、脱敏、验证码、文件存储、操作日志 AOP、Web 防火墙（XSS/SQL 注入/敏感词/幂等/CORS/安全头等）
- **quickboot-core**：项目间共享（如 `BaseEntity`）
- **quickboot-module-system**：系统域业务（用户/角色/菜单/部门/字典/配置/日志/OAuth 等）
- **quickboot-app**：启动组装、Flyway、Modulith 校验（不写业务 Controller）

### 业务域

| 域 | 能力 |
|----|------|
| **认证** | 登录/登出、`getInfo`、`getRouters`、验证码、二维码 |
| **系统管理** | 用户、角色、菜单、部门、字典、参数、公告 |
| **OAuth2** | 授权服务器（AS）、第三方 IdP 联邦登录、Open API、客户端/提供方管理、Client HMAC 签名校验 |
| **监控** | 操作日志、登录日志、在线用户、定时任务及任务日志 |
| **工具** | 代码生成（表导入、预览、ZIP 下载） |

默认端口 **9992**；开发 Profile 使用 **H2 文件库**，生产使用 **MySQL + Redis Token**。

详见 [后端概述](../backend/index)、[功能模块总览](../backend/modules/index)。

## quick-ui 核心能力摘要

### 架构特点

- **动态路由**：登录后调用 `/getRouters`，按菜单权限 `addRoute`
- **权限**：`v-hasPermi` / `v-hasRole`、全局 `$auth`
- **请求**：Bearer Token + **Client HMAC 签名**（与 `sys_oauth_client` 配套，见 OAuth2 文档）
- **列表范式**：业务列表优先 **C7JsonTable** + 列配置，样式对齐 `views/system/config/index.vue`

### 业务页面

| 模块 | 页面 |
|------|------|
| 系统 | 用户、角色、部门、菜单、字典、参数、公告、OAuth 客户端/提供方 |
| 监控 | 在线用户、操作日志、登录日志、定时任务、调度日志 |
| 工具 | 代码生成 |
| OAuth | 授权确认页 `oauth/authorize`；登录页第三方 IdP 入口 |
| 开发 | `dev/*` 下 C7 组件 E2E 演示页 |

详见 [前端概述](../frontend/index)、[业务页面总览](../frontend/modules/index)。

## 文档与规范

- [能力文档大纲](./capabilities-outline)：全站目录与填充进度
- [快速上手](./quick-start)：本地运行三步
- 编码规范：仓库根 `code_formater.md`；协作流程：`AGENTS.md`；视觉：`DESIGN.md`
- 变更流程：`openspec/changes/<change-id>/`

## 下一步

- 本地运行 → [快速上手](./quick-start)
- 后端细节 → [后端概述](../backend/index)
- 前端细节 → [前端概述](../frontend/index)
- OAuth2 对接 → [OAuth2 集成](../backend/modules/oauth2)
