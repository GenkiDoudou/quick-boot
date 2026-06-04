-- Excel 异步导出任务表；export-result 文件分类见 application.yml

CREATE TABLE IF NOT EXISTS sys_export_task (
    task_id           BIGINT        NOT NULL PRIMARY KEY COMMENT '任务主键',
    biz_type          VARCHAR(64)   NOT NULL DEFAULT '' COMMENT '业务编码，如 monitor:logininfor',
    query_json        TEXT          NOT NULL COMMENT '导出筛选条件 JSON',
    result_file_id    BIGINT        NULL COMMENT '结果 xlsx，关联 sys_file.file_id',
    export_mode       VARCHAR(16)   NOT NULL DEFAULT 'sync' COMMENT 'sync::同步 async::异步',
    sync_max_rows     INT           NOT NULL DEFAULT 500 COMMENT '本次任务同步行数上限快照',
    status            VARCHAR(16)   NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING/RUNNING/SUCCESS/FAILED',
    total_rows        INT           NOT NULL DEFAULT 0 COMMENT '导出行数',
    processed_rows    INT           NOT NULL DEFAULT 0 COMMENT '已处理行数（异步进度）',
    error_message     VARCHAR(2000) NULL COMMENT '系统级失败原因',
    create_by         VARCHAR(64)   NOT NULL DEFAULT '' COMMENT '创建人',
    create_time       DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    finish_time       DATETIME      NULL COMMENT '完成时间',
    KEY idx_sys_export_task_create_by (create_by),
    KEY idx_sys_export_task_status (status),
    KEY idx_sys_export_task_create_time (create_time)
) COMMENT 'Excel 导出任务';
