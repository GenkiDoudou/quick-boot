/*
 * sys_slow_sql.sql_text：VARCHAR(4000) → TEXT。
 * 避免截断后缀或超长 SQL 触发 Data too long；应用侧仍按 qc.monitor.slow-sql.max-sql-length 截断。
 */
ALTER TABLE sys_slow_sql
  MODIFY COLUMN sql_text TEXT NOT NULL;
