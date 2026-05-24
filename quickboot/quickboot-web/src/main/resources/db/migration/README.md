# Flyway 迁移脚本约定

## 方言

- **生产**：MySQL 8+
- **开发**：H2 `MODE=MySQL`（见 `application-dev.yml`）

脚本须 **MySQL 可执行**；开发 H2 在 MySQL 模式下尽量兼容。

## Druid Wall（生产）

`application.yml` / `application-prod.yml` 启用 Druid `wall` 时：

| 避免 | 推荐 |
|------|------|
| `CREATE INDEX IF NOT EXISTS` | `CREATE INDEX idx_... ON ...`（Flyway 版本只执行一次） |
| `ALTER TABLE ... ALTER COLUMN`（H2 写法） | `ALTER TABLE ... MODIFY COLUMN`（MySQL） |

`CREATE TABLE IF NOT EXISTS` 一般可过 Wall；索引上的 `IF NOT EXISTS` 会触发 **token IF** 解析错误。

开发 Profile 已 `wall.enabled: false`，生产已配置 `ddl-allow: true` 与 `multi-statement-allow: true`。

## 增量列

使用 `ALTER TABLE ... ADD COLUMN ...`（勿重复执行同一版本；Flyway 保证幂等）。

MySQL 可用 `COMMENT`、`AFTER col`；H2 MODE=MySQL 下 `AFTER` 通常可用。

## V7 失败后无法启动（Validate failed / sqlSessionTemplate）

现象：`Detected failed migration to version 7 (sys notice)`。

**开发（推荐）**：`application-dev.yml` 已设 `spring.flyway.repair-on-migrate: true`，修正脚本后**直接重启**即可。

**手工（MySQL / H2 控制台）**：

```sql
DELETE FROM flyway_schema_history WHERE version = '7' AND success = 0;
```

若表 `sys_notice` 已存在但缺索引，可先执行：

```sql
CREATE INDEX idx_sys_notice_create_time ON sys_notice (create_time);
```

再重启；或删失败记录后让 Flyway 重跑完整 V7。

完整说明见 `db/repair/fix_flyway_v7_failed.sql`。
