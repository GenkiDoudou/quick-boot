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

## 校验和不一致（checksum mismatch，连带 sqlSessionTemplate 无法创建）

现象：`Migration checksum mismatch for migration version 7/11/19/22`，根因是 **Flyway 校验失败**，与 MyBatis 无关。

**开发（任选其一）**：

1. **推荐**：确认 `--spring.profiles.active=dev`（已配置 `validate-on-migrate: false` + `repair-on-migrate: true`），重启。
2. **清空本地 H2**（可丢掉开发数据）：停应用后删除 `quickboot-web/data/` 下 `qcc.*`，再启动。
3. **保留数据**：在 H2 控制台或 MySQL 执行 `DELETE FROM flyway_schema_history WHERE version IN ('7','11','19','22');` 后重启（仅当你明确要重跑这些版本时）。

生产环境 **禁止** 关闭 `validate-on-migrate`；应恢复脚本或走正式 `flyway repair` 流程。

## V33 积木脚本与 `${...}` 占位符

现象：`No value provided for placeholder: ${jm_expression.num}`。

原因：Flyway 默认把 `${}` 当占位符，而积木演示数据 JSON 里含 `${jm_expression.num}`。

处理：`application.yml` 已设 `spring.flyway.placeholder-replacement: false`；`V33__jimureport_init.sql` 仅保留 **积木核心表**（`jimu_*`、`onl_drag_*`，已去掉官方演示表 `huiyuan_*` / `rep_demo_*` / `test_*` 等），并去掉 H2 不支持的 MySQL 片段：`CHARACTER SET` / `COLLATE`、索引上的 `COMMENT`、列上的 `ON UPDATE CURRENT_TIMESTAMP`、`double(m,n)` 等。

若 V33 曾执行失败：删除 `quickboot-web/data/` 下 H2 文件后重启，或 `DELETE FROM flyway_schema_history WHERE version >= '33';`。

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
