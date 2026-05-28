# Flyway 迁移脚本约定



## 方言



- **开发 / 生产**：同一远程 MySQL（`application-dev.yml` 与 `application-prod.yml` 中 `spring.datasource` 一致，库名 `qc2`）



脚本按 **MySQL** 编写；dev / prod 共用 `db/migration`。



## Druid Wall



`application.yml` / `application-dev.yml` / `application-prod.yml` 启用 Druid `wall` 时：



| 避免 | 推荐 |

|------|------|

| `CREATE INDEX IF NOT EXISTS` | `CREATE INDEX idx_... ON ...`（Flyway 版本只执行一次） |

| `ALTER TABLE ... ALTER COLUMN`（H2 写法） | `ALTER TABLE ... MODIFY COLUMN`（MySQL） |



`CREATE TABLE IF NOT EXISTS` 一般可过 Wall；索引上的 `IF NOT EXISTS` 会触发 **token IF** 解析错误。



dev 已启用 `wall`（`ddl-allow` / `multi-statement-allow` / 积木 `where 1=1` 放行），与 prod 行为一致。



## 增量列



使用 `ALTER TABLE ... ADD COLUMN ...`（勿重复执行同一版本；Flyway 保证幂等）。



## 校验和不一致（checksum mismatch）



现象：`Migration checksum mismatch for migration version 7/11/19/22`。



**开发**：确认 `--spring.profiles.active=dev`（`validate-on-migrate: false` + `repair-on-migrate: true`），或连 MySQL 执行 `flyway repair` / 删除对应 `flyway_schema_history` 行后重启。



**生产**：禁止长期关闭 `validate-on-migrate`；应恢复脚本或走正式 `flyway repair`。



## V33 / V36 积木初始化



| 脚本 | 内容 |

|------|------|

| `V33__jimureport_init.sql` | 官方 **44 张表** DDL |

| `V36__jimureport_demo_data.sql` | 官方演示 INSERT（约 9MB，可选） |



启动后 `JimuPrimaryDataSourceSynchronizer` 会把积木数据源 JDBC 同步为当前 `spring.datasource`（覆盖官方 `jimureport` 演示地址）。



**不需要官方样例**：可删除 `V36__jimureport_demo_data.sql`。



### `${...}` 占位符



`application.yml` 已设 `spring.flyway.placeholder-replacement: false`（演示 JSON 中含 `${jm_expression.num}` 等）。



## V7 迁移失败



`application-dev.yml` 已设 `spring.flyway.repair-on-migrate: true`。手工见 `db/repair/fix_flyway_v7_failed.sql`。


