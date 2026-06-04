-- 补齐 sys_slow_sql.sql_source：V51 曾失败重跑时可能已建表但无该列（与实体 SysSlowSql 对齐）

ALTER TABLE sys_slow_sql
    ADD COLUMN sql_source VARCHAR(20) NOT NULL DEFAULT 'BUSINESS' COMMENT '来源：BUSINESS 业务 / JIMU 积木 / SYSTEM 无 HTTP' AFTER slow_id;

CREATE INDEX idx_sys_slow_sql_sql_source ON sys_slow_sql (sql_source);
