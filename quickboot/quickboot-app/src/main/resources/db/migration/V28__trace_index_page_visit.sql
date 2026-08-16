-- 请求链路：页访问聚合所需字段
ALTER TABLE sys_trace_index
  ADD COLUMN session_id VARCHAR(64) NULL COMMENT '浏览器会话' AFTER uin,
  ADD COLUMN page_visit_id VARCHAR(64) NULL COMMENT '一次页面停留 ID' AFTER session_id;

CREATE INDEX idx_sys_trace_index_session ON sys_trace_index (session_id, started_at);
CREATE INDEX idx_sys_trace_index_page_visit ON sys_trace_index (page_visit_id, started_at);
