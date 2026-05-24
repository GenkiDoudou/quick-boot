-- =============================================================================
-- Flyway V7 失败后的手工修复（在「已修正 V7 SQL」之后执行一次）
-- 适用：flyway_schema_history 中存在 version=7 且 success=0 的记录
-- 不放在 db/migration/，不会被 Flyway 自动执行
-- =============================================================================

-- 1) 删除失败记录（MySQL / H2 均适用）
DELETE FROM flyway_schema_history WHERE version = '7' AND success = 0;

-- 2) 若 V7 在 CREATE INDEX 处失败：表可能已建、索引未建，补建索引（已存在则跳过或忽略报错）
-- CREATE INDEX idx_sys_notice_create_time ON sys_notice (create_time);

-- 3) 重启应用，Flyway 会重新执行 V7（CREATE TABLE IF NOT EXISTS + 索引 + 种子数据）
--    若第 2 步已建索引且不想重跑 INSERT，可改为手工执行 V7 剩余 INSERT 后执行：
-- INSERT INTO flyway_schema_history (installed_rank, version, description, type, script, checksum, installed_by, installed_on, execution_time, success)
-- 需与 Flyway 计算 checksum 一致，建议优先用「删失败记录 + 重启」或 flyway repair。
