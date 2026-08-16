## Why

本地 `dev` 目前用文件型 H2（`MODE=MySQL`）模拟生产库，但方言差异仍导致 Flyway DDL、Druid Wall、Quartz 集群判定、积木报表等能力与真实 MySQL/MariaDB 行为不一致，排障成本高。需要在不依赖本机安装外部数据库的前提下，用进程内 MariaDB Embedded（mariadb4j）替换 H2，使本地开发更接近生产。

## What Changes

- **BREAKING（仅本地 dev）**：`application-dev.yml` 数据源从 H2 文件库切换为 mariadb4j 嵌入式 MariaDB（数据目录落本地文件，重启可保留）。
- 新增与 Luban 嵌入式 Redis 同风格的 `DevMariaDb4jConfiguration`：启动前拉起 MariaDB，再交给 Spring DataSource / Flyway。
- `quickboot-app` 引入 `mariadb4j` + MariaDB/MySQL JDBC 驱动；移除或降级仅服务于 H2 Console 的依赖（dev 不再依赖 `/h2-console`）。
- 调整 Druid / Quartz / OAuth ignore-url / 文档中与 H2 绑定的说明，改为 MariaDB Embedded 约定。
- 范围限定：仅 `dev` profile；`test` / `prod` 不切换。

## Capabilities

### New Capabilities

- `dev-mariadb4j`: 开发环境进程内 MariaDB Embedded（mariadb4j）启停、数据目录、JDBC 连接与 Flyway 兼容约定。

### Modified Capabilities

- （无；不修改既有主 specs 业务能力需求。）

## Impact

- 后端：`quickboot-app`（依赖、`application-dev.yml`、嵌入式 DB 配置类）；可能触及 `ScheduleConfig` 的 H2 特例判定、README / `docs/docs/guide/installation.md`。
- 前端：无。
- 依赖：新增 `ch.vorburger.mariaDB4j:mariaDB4j`（及配套 MariaDB 二进制包）、JDBC 驱动；移除或可选保留 `h2` / `spring-boot-h2console`。
- 行为：首次切换需新建数据目录；旧 `./data/quickboot.mv.db` 不自动迁移；Flyway 在真实 MariaDB 上跑既有 `classpath:db/migration`。
