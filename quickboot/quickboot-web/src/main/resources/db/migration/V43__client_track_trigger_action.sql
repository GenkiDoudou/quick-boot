/*
 * 前端监控批次增加触发操作摘要（如 user-edit、修改用户），便于列表筛选与展示。
 * 依赖：V42 sys_client_track.operation_id 已存在。
 */
ALTER TABLE sys_client_track
    ADD COLUMN trigger_action VARCHAR(128) NOT NULL DEFAULT '' COMMENT '触发操作标识：data-track 或 beginOperation 原因，如 user-edit:1';

CREATE INDEX idx_sys_client_track_trigger_action ON sys_client_track (trigger_action);
