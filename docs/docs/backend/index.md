# 后端概述（quickboot）

`quickboot/` 是仓库的 Java 后端：Maven 多模块 + Spring Boot，对外提供管理端与移动端共用的 REST API、认证与系统域能力。

## 技术栈（现网）

- Java 17、Spring Boot 4.x
- MyBatis-Plus、Flyway、Druid
- Spring Security OAuth2 Authorization Server（登录 / 发牌 / 联邦）
- EasyExcel、Quartz、SpringDoc OpenAPI
- 开发默认：嵌入式 MariaDB（mariadb4j）+ 嵌入式 Redis；生产：外部 MariaDB/MySQL + Redis

## 默认本地

| 项 | 值 |
|----|-----|
| HTTP 端口 | **9993** |
| Issuer | `http://127.0.0.1:9993` |
| 嵌入式 DB | `127.0.0.1:3307` / 库 `quickboot` |
| 种子账号 | `admin` / `admin123` |

## 与另外两端

| 客户端 | 工程 | OAuth Client（开发默认） |
|--------|------|--------------------------|
| 管理端 | `quick-ui/` | `quick-ui` / `quick-ui-secret` |
| 移动端 | `quick-h5/` | `quick-h5` / `quick-h5-secret` |

更细的模块划分见 [模块结构](./structure)；启动步骤见 [快速上手](./getting-started)。
