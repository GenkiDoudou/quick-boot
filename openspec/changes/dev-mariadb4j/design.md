## Context

当前 `dev` profile（`application-dev.yml`）使用文件型 H2：

- URL：`jdbc:h2:file:./data/quickboot;MODE=MySQL;...`
- 依赖：`h2` + `spring-boot-h2console`
- Flyway：`classpath:db/migration` + `flyway-mysql`（H2 走 flyway-core 内置）
- Druid：为兼容 H2 关闭 Wall / merge-sql
- Quartz：`ScheduleConfig` 对 `jdbc:h2:` 关闭集群
- 同类先例：`DevLubanEmbeddedRedisConfiguration`（`qc.dev.embedded-redis`）进程内起 Redis

用户已确认：仅 `dev`；采用 mariadb4j；数据目录落本地文件。

## Goals / Non-Goals

**Goals:**

- `dev` 启动时自动拉起 MariaDB Embedded，应用以真实 MariaDB JDBC 连接（无需本机安装 DB）。
- 数据目录持久化，重启可保留；与现有 Flyway 迁移兼容。
- 去掉对 H2 Console / H2 方言特例的依赖（dev 路径）。
- 配置风格对齐嵌入式 Redis（可开关、可配端口/数据目录）。

**Non-Goals:**

- 不切换 `test` / `prod` profile。
- 不自动迁移旧 H2 文件（`./data/quickboot.mv.db`）数据。
- 不引入 Docker Compose / Testcontainers 作为本 change 的默认方案。
- 不改业务 API / 菜单 / 前端。

## Decisions

1. **嵌入方案：mariadb4j（`ch.vorburger.mariaDB4j:mariaDB4j`）**  
   - 理由：用户选定；进程内、可指定 `baseDir`/`dataDir`、端口可配，贴近「免安装」。  
   - 备选：本机 MariaDB（非 Embedded）、Testcontainers（需 Docker）——本版不做。

2. **配置类模式：对齐 `DevLubanEmbeddedRedisConfiguration`**  
   - 新增 `DevMariaDb4jConfiguration`（`@AutoConfiguration`）。  
   - `@ConditionalOnProperty(prefix = "qc.dev.embedded-mariadb", name = "enabled", havingValue = "true", matchIfMissing = true)`。  
   - `@AutoConfigureBefore` DataSource / Flyway 自动配置，确保监听就绪后再建连接。  
   - Bean `destroyMethod` 停止 DB。  
   - 属性建议：`enabled`、`port`（默认 `3307`，避开常见本机 3306）、`data-dir`（默认 `./data/mariadb`）、`database`（默认 `quickboot`）、`username`/`password`（默认 `root` / 空或固定开发口令）。

3. **JDBC：MariaDB 驱动 + `jdbc:mariadb://`**  
   - `application-dev.yml` 指向 `127.0.0.1:${port}/${database}`。  
   - 使用 `org.mariadb.jdbc.Driver`（或项目已有 mysql-connector-j；优先 mariadb URL 以便积木 `JimuPrimaryDataSourceSynchronizer` 识别 `jdbc:mariadb:`）。  
   - Druid：`db-type: mysql`；**开启** Wall / 按需 `merge-sql`（真实 MariaDB 下可恢复更接近生产的过滤）。

4. **移除 / 降级 H2（仅 app 模块 runtime 用途）**  
   - 从 `quickboot-app` 去掉 `h2`、`spring-boot-h2console`（若测试模块仍需 H2 再另议；本版不引入 test 范围 H2）。  
   - 删除 `spring.h2.console`；`qc.oauth.ignore-url` 去掉 `/h2-console/**`。  
   - `ScheduleConfig.isH2` 对 MariaDB URL 自然走集群路径；若本地单实例对集群锁敏感，可后续再加开关——本版先按真实 MySQL 行为开启集群。

5. **Flyway**  
   - 保持 `enabled` + `locations: classpath:db/migration` + `flyway-mysql`。  
   - 新库首次迁移全量执行；`baseline-on-migrate` 可保留兼容旧库场景，但对全新 data-dir 无实质影响。  
   - 既有 H2 专属方言若在迁移脚本中存在，实施时以启动失败为准修复（优先修 SQL 以兼容 MariaDB，而非再加 H2 兼容层）。

6. **文档**  
   - 更新 `quickboot/README.md`、`docs/docs/guide/installation.md` 中 H2 / Console 说明为 MariaDB Embedded 约定与数据目录路径。

## Risks / Trade-offs

- [首次启动下载/解压 MariaDB 二进制较慢] → 文档注明；固定版本依赖；`baseDir` 可缓存。  
- [Windows 杀毒/路径锁导致启停失败] → 数据目录放仓库外或 `./data/mariadb`；失败日志明确。  
- [端口占用] → 默认 3307 + 可配置；启动前可检测。  
- [旧 H2 数据丢失观感] → **BREAKING** 声明；不自动迁移；用户可保留旧文件作备份。  
- [Quartz 集群在单机 Embedded 上的锁开销] → 与生产更一致；若本地卡顿再开非集群开关（另 change）。  
- [mariadb4j 与 Boot 4 / Java 17 兼容性] → 实施时锁定已知可用版本；验证 `mvn -pl quickboot-app spring-boot:run -Dspring-boot.run.profiles=dev`。

## Migration Plan

1. 加依赖与配置类；改 `application-dev.yml`。  
2. 删除 H2 Console 相关依赖与 ignore-url。  
3. 清理本地：停止旧进程；可选删除或归档 `./data/quickboot.mv.db`；启动后确认 `./data/mariadb` 与 Flyway 成功。  
4. 回滚：恢复 H2 依赖与 yml；切回 profile 配置即可（旧 H2 文件若未删可继续用）。

## Open Questions

- （无阻塞项）开发默认口令是否用空密码 `root`/空，还是固定 `root`/`root`——实施默认：`root` / 空（与常见 mariadb4j 示例一致），可在 yml 覆盖。
