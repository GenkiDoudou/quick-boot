-- 登录日志、操作日志：记录发起请求的 OAuth 客户端 ID（X-Client-Id）

ALTER TABLE sys_logininfor
    ADD COLUMN client_id VARCHAR(64) NULL COMMENT 'OAuth 客户端 ID，来自请求头 X-Client-Id';

CREATE INDEX idx_sys_logininfor_client_id ON sys_logininfor (client_id);

ALTER TABLE sys_oper_log
    ADD COLUMN client_id VARCHAR(64) NULL COMMENT 'OAuth 客户端 ID，来自请求头 X-Client-Id';

CREATE INDEX idx_sys_oper_log_client_id ON sys_oper_log (client_id);
