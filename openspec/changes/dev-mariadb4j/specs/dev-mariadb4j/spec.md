## ADDED Requirements

### Requirement: Dev profile starts MariaDB Embedded before DataSource
在 `dev` profile 且 `qc.dev.embedded-mariadb.enabled=true`（缺省为 true）时，系统 MUST 在 Spring DataSource / Flyway 建立连接之前，于本机回环地址启动 mariadb4j 嵌入式 MariaDB，并创建配置的业务库。

#### Scenario: Default enabled on app startup
- **WHEN** 应用以 `dev` profile 启动且未关闭 `qc.dev.embedded-mariadb.enabled`
- **THEN** 进程内 MariaDB 在配置端口监听，且业务库已创建可供 JDBC 连接

#### Scenario: Can be disabled
- **WHEN** 设置 `qc.dev.embedded-mariadb.enabled=false`
- **THEN** 系统 MUST NOT 启动 mariadb4j，并依赖外部已提供的数据源配置

### Requirement: Persistent data directory and JDBC contract
系统 MUST 将嵌入式 MariaDB 数据目录落在可配置本地路径（缺省 `./data/mariadb`），重启后可保留数据；`application-dev.yml` MUST 使用 MariaDB JDBC URL（`jdbc:mariadb://`）与对应驱动连接该实例，且不再将 H2 作为 `dev` 默认数据源。

#### Scenario: Restart keeps data
- **WHEN** 使用同一 `data-dir` 与库名连续两次成功启动 `dev`
- **THEN** 第二次启动后既有业务表与 Flyway 历史仍然存在（非空库重跑全量业务种子覆盖场景除外）

#### Scenario: H2 is not the default datasource
- **WHEN** 读取 `dev` 默认 `spring.datasource.url`
- **THEN** URL MUST 指向嵌入式 MariaDB（或外部关闭嵌入后的等价 MariaDB/MySQL），MUST NOT 为 `jdbc:h2:`

### Requirement: Remove H2 console from dev surface
`dev` 路径 MUST 移除 H2 Console 依赖与暴露面（含 `spring.h2.console` 与 OAuth ignore 中的 `/h2-console/**`），避免开发者继续依赖 H2 控制台。

#### Scenario: No h2-console ignore needed
- **WHEN** 检查 `qc.oauth.ignore-url`（dev）
- **THEN** 列表 MUST NOT 包含 `/h2-console/**`

### Requirement: Flyway runs against embedded MariaDB
在嵌入式 MariaDB 就绪后，系统 MUST 继续使用现有 `classpath:db/migration` 通过 Flyway 迁移；迁移目标 MUST 为该 MariaDB 业务库。

#### Scenario: Fresh data dir migrates
- **WHEN** 使用空的 `data-dir` 首次启动 `dev`
- **THEN** Flyway MUST 成功应用迁移（或按项目既有 baseline 策略对齐），应用可完成启动
