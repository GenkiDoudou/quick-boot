-- 前端监控批次：增加 session_id / page_visit_id，串联「登录会话 → 页面访问 → 按钮操作」链路。
-- 依赖：V42（operation_id）、V43（trigger_action）。

ALTER TABLE sys_client_track
    ADD COLUMN session_id VARCHAR(64) NOT NULL DEFAULT '' COMMENT '登录会话 ID（同一次登录内多批次共用，前端 sessionStorage）';

ALTER TABLE sys_client_track
    ADD COLUMN page_visit_id VARCHAR(64) NOT NULL DEFAULT '' COMMENT '页面访问 ID（同页 route_enter 至离开共用，按钮操作批继承）';

CREATE INDEX idx_sys_client_track_session_id ON sys_client_track (session_id);

CREATE INDEX idx_sys_client_track_page_visit_id ON sys_client_track (page_visit_id);
