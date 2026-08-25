# 项目介绍

QuickBoot 是一套 **企业后台全栈解决方案**：Java 后端 + Vue 3 管理端 + uni-app 移动端 + VitePress 文档站，强调公共能力下沉、规范驱动协作与统一的前端列表/表单体验。

## 仓库结构

| 目录 | 说明 |
|------|------|
| `quickboot/` | Spring Boot 4 后端（Maven 多模块） |
| `quick-ui/` | Vue 3 + Vite + Element Plus 管理端 |
| `quick-h5/` | uni-app 移动端（H5 / 微信小程序） |
| `docs/` | VitePress 文档站点（当前站点） |
| `deploy/` | Jenkins / Nginx / systemd 部署约定 |
| `openspec/` | OpenSpec 变更与规范 |

## 技术栈一览

### 后端（quickboot）

- Java 17、Spring Boot 4.x
- MyBatis-Plus、Flyway、Druid
- Spring Security OAuth2 Authorization Server
- EasyExcel、Quartz、SpringDoc OpenAPI
- 开发：嵌入式 MariaDB + Redis；生产：外部 MariaDB/MySQL + Redis；配置加密：Jasypt（按需）

### 管理端（quick-ui）

- Vue 3.5、Vite 5、Pinia、Vue Router 4
- Element Plus、Axios、Vitest
- **C7**（`src/packages`）：JSON 驱动表格/表单等
- 包管理：**pnpm 9**

### 移动端（quick-h5）

- uni-app Vue3 + uView Pro
- 独立 OAuth Client：`quick-h5`

## 默认本地联调

- 后端端口 **9993**
- 管理端种子账号：`admin` / `admin123`
- 文档站 `base`：`/docs`（生产挂 Nginx `/docs/`）

## 在线演示

对外演示环境见 **[在线演示](./demo)**（文档 / 后台 / H5）。

## 文档入口

| 分区 | 入口 |
|------|------|
| 指南 | [项目介绍](./introduction)、[快速上手](./quick-start)、[在线演示](./demo) |
| 后端 | [概述](/docs/backend/)、[能力包](/docs/backend/components/) |
| 管理端 | [概述](/docs/frontend/)、[C7](/docs/frontend/components/) |
| 移动端 | [概述](/docs/h5/)、[Qb](/docs/h5/components/) |

编码规范：仓库根 `code_formater.md`；协作：`AGENTS.md`。

## 下一步

- 本地运行 → [快速上手](./quick-start)
- 后端细节 → [后端概述](/docs/backend/)
- 管理端细节 → [管理端概述](/docs/frontend/)
- 移动端细节 → [移动端概述](/docs/h5/)
