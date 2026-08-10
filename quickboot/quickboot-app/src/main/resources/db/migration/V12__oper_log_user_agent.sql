-- 操作日志增加 User-Agent
ALTER TABLE sys_oper_log ADD COLUMN user_agent VARCHAR(512);
