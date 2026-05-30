-- 前端监控批次：主关联键 operation_id；trace_id 列保留作过渡（可存首个 API 的 serverTraceId）
ALTER TABLE sys_client_track
    ADD COLUMN operation_id VARCHAR(64) NOT NULL DEFAULT '' COMMENT '前端一次用户操作 ID，与 oper_log.client_operation_id 联查';

CREATE INDEX idx_sys_client_track_operation_id ON sys_client_track (operation_id);

ALTER TABLE sys_client_track
    MODIFY COLUMN trace_id VARCHAR(64) NOT NULL DEFAULT '' COMMENT '过渡：批次内首个 API 的 serverTraceId，非会话 front_trace_id';
