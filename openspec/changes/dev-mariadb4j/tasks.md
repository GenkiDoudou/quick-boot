## 1. Dependencies

- [x] 1.1 在 `quickboot-app/pom.xml` 增加 `mariaDB4j` 与 MariaDB JDBC 驱动；移除 `h2`、`spring-boot-h2console`
- [x] 1.2 必要时在父 POM / dependencyManagement 锁定 mariadb4j 与驱动版本，确保可解析

## 2. Embedded MariaDB bootstrap

- [x] 2.1 新增 `DevMariaDb4jConfiguration`（对齐 Luban Redis）：`qc.dev.embedded-mariadb` 开关/端口/data-dir/库名/账号，启动并 `createDB`，`@AutoConfigureBefore` DataSource/Flyway，destroy 时 stop
- [x] 2.2 在 `application-dev.yml` 增加 `qc.dev.embedded-mariadb.*` 默认值，并将 `spring.datasource` 改为 `jdbc:mariadb://127.0.0.1:${port}/${database}` + MariaDB 驱动与账号

## 3. Dev surface cleanup

- [x] 3.1 删除 `spring.h2.console`；从 `qc.oauth.ignore-url` 去掉 `/h2-console/**`
- [x] 3.2 按需恢复 Druid Wall / merge-sql（真实 MariaDB）；确认 Quartz 对非 H2 URL 走集群路径可接受

## 4. Docs and verify

- [x] 4.1 更新 `quickboot/README.md` 与 `docs/docs/guide/installation.md` 中 H2/Console 说明为 MariaDB Embedded
- [x] 4.2 本地以 `dev` profile 启动：嵌入库就绪、Flyway 成功、应用可登录/冒烟；记录首次数据目录路径
