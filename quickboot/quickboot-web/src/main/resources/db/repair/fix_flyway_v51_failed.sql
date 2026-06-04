-- =============================================================================
-- Flyway V51 失败后的手工修复（在已修正 V51 SQL 之后执行一次）
-- 适用：flyway_schema_history 中存在 version=51 且 success=0
-- 不放在 db/migration/，不会被 Flyway 自动执行
-- =============================================================================

-- 1) 删除失败记录（MySQL）
DELETE FROM flyway_schema_history WHERE version = '51' AND success = 0;

-- 2) 若第 1 步后仍报「contains a failed migration」，可再执行 Flyway repair 或：
-- DELETE FROM flyway_schema_history WHERE version = '51';

-- 3) 若 V51 在独立 CREATE INDEX 处失败过，可能留下重复索引（可忽略）或半成品表：
--    表 sys_slow_sql 已存在且无菜单时，可只补菜单（与 V51 中 INSERT IGNORE 一致）后标记成功；
--    推荐：执行步骤 1 后重启应用（--spring.profiles.active=dev），让 Flyway 重跑 V51。

-- 4) 若表已存在但无 sql_source 列（插入报 Unknown column 'sql_source'）：
--    重启应用执行 V52__sys_slow_sql_sql_source.sql，或手工：
--    ALTER TABLE sys_slow_sql ADD COLUMN sql_source VARCHAR(20) NOT NULL DEFAULT 'BUSINESS' AFTER slow_id;

-- 5) 可选：删除孤立重复索引（仅当确认索引名冲突且表已存在时手工执行）
-- ALTER TABLE sys_slow_sql DROP INDEX idx_sys_slow_sql_create_time;
