-- Excel 导入任务与暂存行；导入导出中心菜单

CREATE TABLE IF NOT EXISTS sys_import_task (
    task_id           BIGINT        NOT NULL PRIMARY KEY COMMENT '任务主键',
    biz_type          VARCHAR(64)   NOT NULL DEFAULT '' COMMENT '业务编码，如 system:user',
    source_file_id    BIGINT        NOT NULL COMMENT '原始 Excel，关联 sys_file.file_id',
    error_file_id     BIGINT        NULL COMMENT '失败明细 xlsx，关联 sys_file.file_id',
    import_mode       VARCHAR(16)   NOT NULL DEFAULT 'sync' COMMENT 'sync::同步 async::异步',
    sync_max_rows     INT           NOT NULL DEFAULT 500 COMMENT '本次任务使用的同步行数上限快照',
    duplicate_strategy VARCHAR(16)  NOT NULL DEFAULT 'ignore' COMMENT 'ignore::跳过 overwrite::覆盖',
    status            VARCHAR(16)   NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING/RUNNING/SUCCESS/FAILED',
    total_rows        INT           NOT NULL DEFAULT 0 COMMENT '有效行总数',
    success_rows      INT           NOT NULL DEFAULT 0 COMMENT '成功行数',
    fail_rows         INT           NOT NULL DEFAULT 0 COMMENT '失败行数',
    processed_rows    INT           NOT NULL DEFAULT 0 COMMENT '已处理行数（异步进度）',
    error_message     VARCHAR(2000) NULL COMMENT '系统级失败原因',
    create_by         VARCHAR(64)   NOT NULL DEFAULT '' COMMENT '创建人',
    create_time       DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    finish_time       DATETIME      NULL COMMENT '完成时间',
    KEY idx_sys_import_task_create_by (create_by),
    KEY idx_sys_import_task_status (status),
    KEY idx_sys_import_task_create_time (create_time)
) COMMENT 'Excel 导入任务';

CREATE TABLE IF NOT EXISTS sys_import_staging_row (
    id               BIGINT        NOT NULL PRIMARY KEY COMMENT '主键',
    task_id          BIGINT        NOT NULL COMMENT '关联 sys_import_task.task_id',
    row_no           INT           NOT NULL COMMENT 'Excel 行号（含表头偏移）',
    row_json         TEXT          NOT NULL COMMENT '行数据 JSON',
    validate_status  VARCHAR(16)   NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING/OK/FAIL/SKIPPED',
    error_msg        VARCHAR(2000) NULL COMMENT '校验或落库失败原因',
    biz_ref          VARCHAR(128)  NULL COMMENT '落库后业务主键快照',
    KEY idx_sys_import_staging_task (task_id),
    KEY idx_sys_import_staging_task_status (task_id, validate_status)
) COMMENT 'Excel 导入异步暂存行';

INSERT IGNORE INTO sys_menu (menu_id, parent_id, menu_type, menu_name, order_num, path, component, query, route_name, is_frame, is_cache, visible, status, perms, icon, remark, del_flag, create_by, create_time)
VALUES (2268, 2000, 'C', '导入导出中心', 21, 'importExportCenter', 'system/importExportCenter/index', NULL, 'ImportExportCenter', '0', '0', '0', '0', 'system:ioCenter:list', 'upload', '导入任务与导出记录统一查看', '0', 'system', CURRENT_TIMESTAMP);

INSERT IGNORE INTO sys_menu (menu_id, parent_id, menu_type, menu_name, order_num, path, component, query, route_name, is_frame, is_cache, visible, status, perms, icon, remark, del_flag, create_by, create_time)
VALUES (2269, 2268, 'F', '导入任务查询', 1, '', NULL, NULL, NULL, '0', '0', '0', '0', 'system:ioCenter:list', '#', NULL, '0', 'system', CURRENT_TIMESTAMP);

INSERT IGNORE INTO sys_menu (menu_id, parent_id, menu_type, menu_name, order_num, path, component, query, route_name, is_frame, is_cache, visible, status, perms, icon, remark, del_flag, create_by, create_time)
VALUES (2270, 2268, 'F', '导入任务提交', 2, '', NULL, NULL, NULL, '0', '0', '0', '0', 'system:ioCenter:submit', '#', NULL, '0', 'system', CURRENT_TIMESTAMP);

INSERT IGNORE INTO sys_menu (menu_id, parent_id, menu_type, menu_name, order_num, path, component, query, route_name, is_frame, is_cache, visible, status, perms, icon, remark, del_flag, create_by, create_time)
VALUES (2271, 2268, 'F', '失败明细下载', 3, '', NULL, NULL, NULL, '0', '0', '0', '0', 'system:ioCenter:download', '#', NULL, '0', 'system', CURRENT_TIMESTAMP);

INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (1, 2268);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (1, 2269);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (1, 2270);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (1, 2271);
