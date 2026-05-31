-- 前端监控批次：增加 browser_visit_id，串联「打开浏览器访问系统 → 登录 → 页面 → 操作」链路。
-- 依赖：V44（session_id / page_visit_id）。

ALTER TABLE sys_client_track
    ADD COLUMN browser_visit_id VARCHAR(64) NOT NULL DEFAULT '' COMMENT '浏览器访问 ID（打开浏览器访问本系统时生成，跨 tab 共用；登出不换，关浏览器超时后再访问换新）';

CREATE INDEX idx_sys_client_track_browser_visit_id ON sys_client_track (browser_visit_id);
