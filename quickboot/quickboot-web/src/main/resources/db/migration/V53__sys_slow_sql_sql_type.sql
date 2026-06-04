-- 慢 SQL 操作类型（SELECT/INSERT/UPDATE/DELETE 等），支持列表筛选

ALTER TABLE sys_slow_sql
    ADD COLUMN sql_type VARCHAR(20) NOT NULL DEFAULT 'OTHER' COMMENT 'SQL 操作类型：SELECT/INSERT/UPDATE/DELETE 等' AFTER sql_source;

CREATE INDEX idx_sys_slow_sql_sql_type ON sys_slow_sql (sql_type);
