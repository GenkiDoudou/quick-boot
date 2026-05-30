-- 操作日志：关联前端一次用户操作（X-Client-Operation-Id），与单次请求的 trace_id 分离
ALTER TABLE sys_oper_log
    ADD COLUMN client_operation_id VARCHAR(64) NULL COMMENT '前端操作 ID，来自请求头 X-Client-Operation-Id';

CREATE INDEX idx_sys_oper_log_client_operation_id ON sys_oper_log (client_operation_id);
