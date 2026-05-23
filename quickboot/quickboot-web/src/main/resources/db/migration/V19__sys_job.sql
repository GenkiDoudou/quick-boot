-- 定时任务、调度日志、Quartz JDBC 表、字典与菜单（V19）

CREATE TABLE IF NOT EXISTS sys_job (
    job_id           BIGINT       NOT NULL PRIMARY KEY,
    job_name         VARCHAR(64)  NOT NULL,
    job_group        VARCHAR(64)  NOT NULL DEFAULT 'DEFAULT',
    invoke_target    VARCHAR(500) NOT NULL,
    cron_expression  VARCHAR(255) NOT NULL,
    misfire_policy   VARCHAR(20)  NOT NULL DEFAULT '3',
    concurrent       CHAR(1)      NOT NULL DEFAULT '1',
    status           CHAR(1)      NOT NULL DEFAULT '1',
    params           VARCHAR(500) NULL,
    create_by        VARCHAR(64)  NULL,
    create_time      DATETIME     NULL,
    update_by        VARCHAR(64)  NULL,
    update_time      DATETIME     NULL,
    remark           VARCHAR(500) NULL
);

CREATE INDEX IF NOT EXISTS idx_sys_job_name ON sys_job (job_name);
CREATE INDEX IF NOT EXISTS idx_sys_job_group ON sys_job (job_group);
CREATE INDEX IF NOT EXISTS idx_sys_job_status ON sys_job (status);

CREATE TABLE IF NOT EXISTS sys_job_log (
    job_log_id      BIGINT        NOT NULL PRIMARY KEY,
    job_id          BIGINT        NULL,
    job_name        VARCHAR(64)   NOT NULL,
    job_group       VARCHAR(64)   NOT NULL,
    invoke_target   VARCHAR(500)  NOT NULL,
    job_message     VARCHAR(500)  NULL,
    status          CHAR(1)       NOT NULL,
    exception_info  VARCHAR(2000) NULL,
    create_time     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_sys_job_log_create_time ON sys_job_log (create_time);
CREATE INDEX IF NOT EXISTS idx_sys_job_log_job_name ON sys_job_log (job_name);

-- Quartz JDBC（MySQL 方言，H2 MODE=MySQL 开发库兼容）
CREATE TABLE IF NOT EXISTS QRTZ_JOB_DETAILS (
    sched_name        VARCHAR(120) NOT NULL,
    job_name          VARCHAR(200) NOT NULL,
    job_group         VARCHAR(200) NOT NULL,
    description       VARCHAR(250) NULL,
    job_class_name    VARCHAR(250) NOT NULL,
    is_durable        VARCHAR(1)   NOT NULL,
    is_nonconcurrent  VARCHAR(1)   NOT NULL,
    is_update_data    VARCHAR(1)   NOT NULL,
    requests_recovery VARCHAR(1)   NOT NULL,
    job_data          BLOB         NULL,
    PRIMARY KEY (sched_name, job_name, job_group)
);

CREATE TABLE IF NOT EXISTS QRTZ_TRIGGERS (
    sched_name     VARCHAR(120) NOT NULL,
    trigger_name   VARCHAR(200) NOT NULL,
    trigger_group  VARCHAR(200) NOT NULL,
    job_name       VARCHAR(200) NOT NULL,
    job_group      VARCHAR(200) NOT NULL,
    description    VARCHAR(250) NULL,
    next_fire_time BIGINT       NULL,
    prev_fire_time BIGINT       NULL,
    priority       INTEGER      NULL,
    trigger_state  VARCHAR(16)  NOT NULL,
    trigger_type   VARCHAR(8)   NOT NULL,
    start_time     BIGINT       NOT NULL,
    end_time       BIGINT       NULL,
    calendar_name  VARCHAR(200) NULL,
    misfire_instr  SMALLINT     NULL,
    job_data       BLOB         NULL,
    PRIMARY KEY (sched_name, trigger_name, trigger_group)
);

CREATE TABLE IF NOT EXISTS QRTZ_SIMPLE_TRIGGERS (
    sched_name         VARCHAR(120) NOT NULL,
    trigger_name       VARCHAR(200) NOT NULL,
    trigger_group      VARCHAR(200) NOT NULL,
    repeat_count       BIGINT       NOT NULL,
    repeat_interval    BIGINT       NOT NULL,
    times_triggered    BIGINT       NOT NULL,
    PRIMARY KEY (sched_name, trigger_name, trigger_group)
);

CREATE TABLE IF NOT EXISTS QRTZ_CRON_TRIGGERS (
    sched_name      VARCHAR(120) NOT NULL,
    trigger_name    VARCHAR(200) NOT NULL,
    trigger_group   VARCHAR(200) NOT NULL,
    cron_expression VARCHAR(200) NOT NULL,
    time_zone_id    VARCHAR(80)  NULL,
    PRIMARY KEY (sched_name, trigger_name, trigger_group)
);

CREATE TABLE IF NOT EXISTS QRTZ_BLOB_TRIGGERS (
    sched_name    VARCHAR(120) NOT NULL,
    trigger_name  VARCHAR(200) NOT NULL,
    trigger_group VARCHAR(200) NOT NULL,
    blob_data     BLOB         NULL,
    PRIMARY KEY (sched_name, trigger_name, trigger_group)
);

CREATE TABLE IF NOT EXISTS QRTZ_CALENDARS (
    sched_name    VARCHAR(120) NOT NULL,
    calendar_name VARCHAR(200) NOT NULL,
    calendar      BLOB         NOT NULL,
    PRIMARY KEY (sched_name, calendar_name)
);

CREATE TABLE IF NOT EXISTS QRTZ_PAUSED_TRIGGER_GRPS (
    sched_name    VARCHAR(120) NOT NULL,
    trigger_group VARCHAR(200) NOT NULL,
    PRIMARY KEY (sched_name, trigger_group)
);

CREATE TABLE IF NOT EXISTS QRTZ_FIRED_TRIGGERS (
    sched_name        VARCHAR(120) NOT NULL,
    entry_id          VARCHAR(95)  NOT NULL,
    trigger_name      VARCHAR(200) NOT NULL,
    trigger_group     VARCHAR(200) NOT NULL,
    instance_name     VARCHAR(200) NOT NULL,
    fired_time        BIGINT       NOT NULL,
    sched_time        BIGINT       NOT NULL,
    priority          INTEGER      NOT NULL,
    state             VARCHAR(16)  NOT NULL,
    job_name          VARCHAR(200) NULL,
    job_group         VARCHAR(200) NULL,
    is_nonconcurrent  VARCHAR(1)   NULL,
    requests_recovery VARCHAR(1)   NULL,
    PRIMARY KEY (sched_name, entry_id)
);

CREATE TABLE IF NOT EXISTS QRTZ_SCHEDULER_STATE (
    sched_name       VARCHAR(120) NOT NULL,
    instance_name    VARCHAR(200) NOT NULL,
    last_checkin_time BIGINT      NOT NULL,
    checkin_interval BIGINT      NOT NULL,
    PRIMARY KEY (sched_name, instance_name)
);

CREATE TABLE IF NOT EXISTS QRTZ_LOCKS (
    sched_name VARCHAR(120) NOT NULL,
    lock_name  VARCHAR(40)  NOT NULL,
    PRIMARY KEY (sched_name, lock_name)
);

CREATE TABLE IF NOT EXISTS QRTZ_SIMPROP_TRIGGERS (
    sched_name    VARCHAR(120) NOT NULL,
    trigger_name  VARCHAR(200) NOT NULL,
    trigger_group VARCHAR(200) NOT NULL,
    str_prop_1    VARCHAR(512) NULL,
    str_prop_2    VARCHAR(512) NULL,
    str_prop_3    VARCHAR(512) NULL,
    int_prop_1    INT          NULL,
    int_prop_2    INT          NULL,
    long_prop_1   BIGINT       NULL,
    long_prop_2   BIGINT       NULL,
    dec_prop_1    NUMERIC(13,4) NULL,
    dec_prop_2    NUMERIC(13,4) NULL,
    bool_prop_1   VARCHAR(1)   NULL,
    bool_prop_2   VARCHAR(1)   NULL,
    PRIMARY KEY (sched_name, trigger_name, trigger_group)
);

-- dict_id / dict_code 使用 800012+ / 800080+，避免与 V7(800010/011)、V16(800060/061) 冲突
-- 失败重试时清理半成品（含历史错误脚本写入的 800007–800011）
DELETE FROM sys_dict_data WHERE dict_type IN ('sys_job_group', 'sys_job_status', 'sys_job_misfire_policy', 'sys_job_concurrent', 'sys_job_log_status');
DELETE FROM sys_dict_type WHERE dict_type IN ('sys_job_group', 'sys_job_status', 'sys_job_misfire_policy', 'sys_job_concurrent', 'sys_job_log_status');
DELETE FROM sys_dict_type WHERE dict_id BETWEEN 800007 AND 800011 AND dict_type LIKE 'sys_job%';

INSERT INTO sys_dict_type (dict_id, dict_name, dict_type, status, remark, del_flag, create_by, create_time)
VALUES (800012, '任务分组', 'sys_job_group', '0', 'Flyway：定时任务组', '0', 'system', CURRENT_TIMESTAMP);

INSERT INTO sys_dict_type (dict_id, dict_name, dict_type, status, remark, del_flag, create_by, create_time)
VALUES (800013, '任务状态', 'sys_job_status', '0', 'Flyway：定时任务启停', '0', 'system', CURRENT_TIMESTAMP);

INSERT INTO sys_dict_type (dict_id, dict_name, dict_type, status, remark, del_flag, create_by, create_time)
VALUES (800014, '错失策略', 'sys_job_misfire_policy', '0', 'Flyway：Quartz misfire', '0', 'system', CURRENT_TIMESTAMP);

INSERT INTO sys_dict_type (dict_id, dict_name, dict_type, status, remark, del_flag, create_by, create_time)
VALUES (800015, '任务并发', 'sys_job_concurrent', '0', 'Flyway：0允许1禁止', '0', 'system', CURRENT_TIMESTAMP);

INSERT INTO sys_dict_type (dict_id, dict_name, dict_type, status, remark, del_flag, create_by, create_time)
VALUES (800016, '调度日志状态', 'sys_job_log_status', '0', 'Flyway：执行成功失败', '0', 'system', CURRENT_TIMESTAMP);

INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, status, remark, del_flag, create_by, create_time)
VALUES (800080, 1, '默认', 'DEFAULT', 'sys_job_group', NULL, 'default', '0', NULL, '0', 'system', CURRENT_TIMESTAMP);

INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, status, remark, del_flag, create_by, create_time)
VALUES (800081, 2, '系统', 'SYSTEM', 'sys_job_group', NULL, 'primary', '0', NULL, '0', 'system', CURRENT_TIMESTAMP);

INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, status, remark, del_flag, create_by, create_time)
VALUES (800082, 1, '正常', '0', 'sys_job_status', NULL, 'success', '0', NULL, '0', 'system', CURRENT_TIMESTAMP);

INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, status, remark, del_flag, create_by, create_time)
VALUES (800083, 2, '暂停', '1', 'sys_job_status', NULL, 'danger', '0', NULL, '0', 'system', CURRENT_TIMESTAMP);

INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, status, remark, del_flag, create_by, create_time)
VALUES (800084, 1, '默认', '0', 'sys_job_misfire_policy', NULL, 'default', '0', NULL, '0', 'system', CURRENT_TIMESTAMP);

INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, status, remark, del_flag, create_by, create_time)
VALUES (800085, 2, '立即执行', '1', 'sys_job_misfire_policy', NULL, 'primary', '0', NULL, '0', 'system', CURRENT_TIMESTAMP);

INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, status, remark, del_flag, create_by, create_time)
VALUES (800086, 3, '执行一次', '2', 'sys_job_misfire_policy', NULL, 'warning', '0', NULL, '0', 'system', CURRENT_TIMESTAMP);

INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, status, remark, del_flag, create_by, create_time)
VALUES (800087, 4, '放弃执行', '3', 'sys_job_misfire_policy', NULL, 'info', '0', NULL, '0', 'system', CURRENT_TIMESTAMP);

INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, status, remark, del_flag, create_by, create_time)
VALUES (800088, 1, '允许', '0', 'sys_job_concurrent', NULL, 'success', '0', NULL, '0', 'system', CURRENT_TIMESTAMP);

INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, status, remark, del_flag, create_by, create_time)
VALUES (800089, 2, '禁止', '1', 'sys_job_concurrent', NULL, 'danger', '0', NULL, '0', 'system', CURRENT_TIMESTAMP);

INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, status, remark, del_flag, create_by, create_time)
VALUES (800090, 1, '成功', '0', 'sys_job_log_status', NULL, 'success', '0', NULL, '0', 'system', CURRENT_TIMESTAMP);

INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, status, remark, del_flag, create_by, create_time)
VALUES (800091, 2, '失败', '1', 'sys_job_log_status', NULL, 'danger', '0', NULL, '0', 'system', CURRENT_TIMESTAMP);

DELETE FROM sys_role_menu WHERE menu_id BETWEEN 2240 AND 2249;
DELETE FROM sys_menu WHERE menu_id BETWEEN 2240 AND 2249;

INSERT INTO sys_menu (menu_id, parent_id, menu_type, menu_name, order_num, path, component, query, route_name, is_frame, is_cache, visible, status, perms, icon, remark, del_flag, create_by, create_time)
VALUES (2240, 2000, 'C', '定时任务', 10, 'job', 'monitor/job/index', NULL, 'SysJob', '0', '0', '0', '0', 'monitor:job:list', 'time', NULL, '0', 'system', CURRENT_TIMESTAMP);

INSERT INTO sys_menu (menu_id, parent_id, menu_type, menu_name, order_num, path, component, query, route_name, is_frame, is_cache, visible, status, perms, icon, remark, del_flag, create_by, create_time)
VALUES (2241, 2240, 'F', '定时任务新增', 1, '', NULL, NULL, NULL, '0', '0', '0', '0', 'monitor:job:add', '#', NULL, '0', 'system', CURRENT_TIMESTAMP);

INSERT INTO sys_menu (menu_id, parent_id, menu_type, menu_name, order_num, path, component, query, route_name, is_frame, is_cache, visible, status, perms, icon, remark, del_flag, create_by, create_time)
VALUES (2242, 2240, 'F', '定时任务修改', 2, '', NULL, NULL, NULL, '0', '0', '0', '0', 'monitor:job:edit', '#', NULL, '0', 'system', CURRENT_TIMESTAMP);

INSERT INTO sys_menu (menu_id, parent_id, menu_type, menu_name, order_num, path, component, query, route_name, is_frame, is_cache, visible, status, perms, icon, remark, del_flag, create_by, create_time)
VALUES (2243, 2240, 'F', '定时任务删除', 3, '', NULL, NULL, NULL, '0', '0', '0', '0', 'monitor:job:remove', '#', NULL, '0', 'system', CURRENT_TIMESTAMP);

INSERT INTO sys_menu (menu_id, parent_id, menu_type, menu_name, order_num, path, component, query, route_name, is_frame, is_cache, visible, status, perms, icon, remark, del_flag, create_by, create_time)
VALUES (2244, 2240, 'F', '定时任务导出', 4, '', NULL, NULL, NULL, '0', '0', '0', '0', 'monitor:job:export', '#', NULL, '0', 'system', CURRENT_TIMESTAMP);

INSERT INTO sys_menu (menu_id, parent_id, menu_type, menu_name, order_num, path, component, query, route_name, is_frame, is_cache, visible, status, perms, icon, remark, del_flag, create_by, create_time)
VALUES (2245, 2240, 'F', '定时任务查询', 5, '', NULL, NULL, NULL, '0', '0', '0', '0', 'monitor:job:query', '#', NULL, '0', 'system', CURRENT_TIMESTAMP);

INSERT INTO sys_menu (menu_id, parent_id, menu_type, menu_name, order_num, path, component, query, route_name, is_frame, is_cache, visible, status, perms, icon, remark, del_flag, create_by, create_time)
VALUES (2246, 2240, 'F', '定时任务改状态', 6, '', NULL, NULL, NULL, '0', '0', '0', '0', 'monitor:job:changeStatus', '#', NULL, '0', 'system', CURRENT_TIMESTAMP);

INSERT INTO sys_menu (menu_id, parent_id, menu_type, menu_name, order_num, path, component, query, route_name, is_frame, is_cache, visible, status, perms, icon, remark, del_flag, create_by, create_time)
VALUES (2247, 2000, 'C', '调度日志', 11, 'job-log', 'monitor/job-log/index', NULL, 'SysJobLog', '0', '0', '0', '0', 'monitor:job:query', 'log', NULL, '0', 'system', CURRENT_TIMESTAMP);

INSERT INTO sys_menu (menu_id, parent_id, menu_type, menu_name, order_num, path, component, query, route_name, is_frame, is_cache, visible, status, perms, icon, remark, del_flag, create_by, create_time)
VALUES (2248, 2247, 'F', '调度日志删除', 1, '', NULL, NULL, NULL, '0', '0', '0', '0', 'monitor:job:remove', '#', NULL, '0', 'system', CURRENT_TIMESTAMP);

INSERT INTO sys_menu (menu_id, parent_id, menu_type, menu_name, order_num, path, component, query, route_name, is_frame, is_cache, visible, status, perms, icon, remark, del_flag, create_by, create_time)
VALUES (2249, 2247, 'F', '调度日志导出', 2, '', NULL, NULL, NULL, '0', '0', '0', '0', 'monitor:job:export', '#', NULL, '0', 'system', CURRENT_TIMESTAMP);

INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 2240);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 2241);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 2242);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 2243);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 2244);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 2245);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 2246);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 2247);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 2248);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 2249);
