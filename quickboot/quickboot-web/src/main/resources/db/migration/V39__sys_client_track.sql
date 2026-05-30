-- 前端用户行为监控批次表（quick-ui 全局插件上报，用于线上排障还原操作路径）
-- 说明：column `page` 在 MySQL 8 中为保留字，会导致 DDL 失败；使用 page_path。
-- 若 V39 曾失败，本脚本开头 DROP 可清理半成品表，配合 flyway repair-on-migrate 后重跑。

DROP TABLE IF EXISTS sys_client_track;

CREATE TABLE sys_client_track (
    batch_id     BIGINT        NOT NULL PRIMARY KEY COMMENT '批次主键',
    trace_id     VARCHAR(64)   NOT NULL DEFAULT '' COMMENT '前端会话 traceId，与 events 内 traceId 一致',
    user_id      BIGINT        NULL COMMENT '登录用户 ID，可空',
    user_name    VARCHAR(64)   NOT NULL DEFAULT '' COMMENT '登录用户名',
    reason       VARCHAR(32)   NOT NULL DEFAULT 'normal' COMMENT '上报触发原因：normal/error/leave/timer',
    page_path    VARCHAR(500)  NULL COMMENT '批次末次页面路径',
    ua           VARCHAR(500)  NULL COMMENT 'User-Agent 摘要',
    events_json  TEXT          NOT NULL COMMENT 'JSON 事件数组，禁止存输入框取值',
    client_ip    VARCHAR(128)  NULL COMMENT '客户端 IP',
    create_time  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '入库时间'
) COMMENT '前端用户行为监控批次';

CREATE INDEX idx_sys_client_track_trace_id ON sys_client_track (trace_id);
CREATE INDEX idx_sys_client_track_user_name ON sys_client_track (user_name);
CREATE INDEX idx_sys_client_track_create_time ON sys_client_track (create_time);
