/*
 * 定时任务 / Quartz JDBC：sys_job、sys_job_log、QRTZ_* 表，字典种子，监控菜单与超管授权。
 * 范围：Quartz 模块 Flyway 初始化；QRTZ 布尔相关列 VARCHAR(12) 兼容 H2/MySQL StdJDBCDelegate。
 * 依赖：V7（sys_dict_*）、V8（监控目录 menu_id=2100）、V9/V10（is_default 0/1 约定）。
 *
 * 字典列语义（code_formater §4.3；DDL COMMENT 因 H2/MySQL 方言差异见实体 JavaDoc）：
 *   sys_job.job_group        → 任务分组(sys_job_group)
 *   sys_job.misfire_policy   → 错失策略(sys_job_misfire_policy)
 *   sys_job.concurrent       → 任务并发(sys_job_concurrent)，0=允许 1=禁止
 *   sys_job.status           → 任务状态(sys_job_status)，0=正常 1=暂停
 *   sys_job_log.status       → 调度日志状态(sys_job_log_status)，0=成功 1=失败
 */

-- ========== 定时任务业务表 ==========

CREATE TABLE IF NOT EXISTS sys_job (
  job_id          BIGINT       NOT NULL,
  job_name        VARCHAR(64)  NOT NULL,
  job_group       VARCHAR(64)  NOT NULL DEFAULT 'DEFAULT',
  invoke_target   VARCHAR(500) NOT NULL,
  cron_expression VARCHAR(255) NOT NULL,
  misfire_policy  VARCHAR(20)  NOT NULL DEFAULT '3',
  concurrent      CHAR(1)      NOT NULL DEFAULT '1',
  status          CHAR(1)      NOT NULL DEFAULT '1',
  params          VARCHAR(500),
  create_by       VARCHAR(64),
  create_time     TIMESTAMP    NULL,
  update_by       VARCHAR(64),
  update_time     TIMESTAMP    NULL,
  remark          VARCHAR(500),
  PRIMARY KEY (job_id)
);

CREATE INDEX idx_sys_job_name ON sys_job (job_name);
CREATE INDEX idx_sys_job_group ON sys_job (job_group);
CREATE INDEX idx_sys_job_status ON sys_job (status);

CREATE TABLE IF NOT EXISTS sys_job_log (
  job_log_id     BIGINT        NOT NULL,
  job_id         BIGINT,
  job_name       VARCHAR(64)   NOT NULL,
  job_group      VARCHAR(64)   NOT NULL,
  invoke_target  VARCHAR(500)  NOT NULL,
  job_message    VARCHAR(500),
  status         CHAR(1)       NOT NULL,
  exception_info VARCHAR(2000),
  create_time    TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (job_log_id)
);

CREATE INDEX idx_sys_job_log_create_time ON sys_job_log (create_time);
CREATE INDEX idx_sys_job_log_job_name ON sys_job_log (job_name);

-- ========== Quartz JDBC 表结构（bak V2__quartz.sql） ==========

CREATE TABLE IF NOT EXISTS QRTZ_JOB_DETAILS (
  sched_name        VARCHAR(120) NOT NULL,
  job_name          VARCHAR(200) NOT NULL,
  job_group         VARCHAR(200) NOT NULL,
  description       VARCHAR(250) NULL,
  job_class_name    VARCHAR(250) NOT NULL,
  is_durable        VARCHAR(12)  NOT NULL,
  is_nonconcurrent  VARCHAR(12)  NOT NULL,
  is_update_data    VARCHAR(12)  NOT NULL,
  requests_recovery VARCHAR(12)  NOT NULL,
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
  sched_name      VARCHAR(120) NOT NULL,
  trigger_name    VARCHAR(200) NOT NULL,
  trigger_group   VARCHAR(200) NOT NULL,
  repeat_count    BIGINT       NOT NULL,
  repeat_interval BIGINT       NOT NULL,
  times_triggered BIGINT       NOT NULL,
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
  is_nonconcurrent  VARCHAR(12)  NULL,
  requests_recovery VARCHAR(12)  NULL,
  PRIMARY KEY (sched_name, entry_id)
);

CREATE TABLE IF NOT EXISTS QRTZ_SCHEDULER_STATE (
  sched_name        VARCHAR(120) NOT NULL,
  instance_name     VARCHAR(200) NOT NULL,
  last_checkin_time BIGINT       NOT NULL,
  checkin_interval  BIGINT       NOT NULL,
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

-- Quartz 锁种子（sched_name 与 ScheduleConfig instanceName 一致）
INSERT INTO QRTZ_LOCKS (sched_name, lock_name)
SELECT 'QuickScheduler', 'STATE_ACCESS'
WHERE NOT EXISTS (SELECT 1 FROM QRTZ_LOCKS WHERE sched_name = 'QuickScheduler' AND lock_name = 'STATE_ACCESS');
INSERT INTO QRTZ_LOCKS (sched_name, lock_name)
SELECT 'QuickScheduler', 'TRIGGER_ACCESS'
WHERE NOT EXISTS (SELECT 1 FROM QRTZ_LOCKS WHERE sched_name = 'QuickScheduler' AND lock_name = 'TRIGGER_ACCESS');
INSERT INTO QRTZ_LOCKS (sched_name, lock_name)
SELECT 'QuickScheduler', 'JOB_ACCESS'
WHERE NOT EXISTS (SELECT 1 FROM QRTZ_LOCKS WHERE sched_name = 'QuickScheduler' AND lock_name = 'JOB_ACCESS');
INSERT INTO QRTZ_LOCKS (sched_name, lock_name)
SELECT 'QuickScheduler', 'CALENDAR_ACCESS'
WHERE NOT EXISTS (SELECT 1 FROM QRTZ_LOCKS WHERE sched_name = 'QuickScheduler' AND lock_name = 'CALENDAR_ACCESS');
INSERT INTO QRTZ_LOCKS (sched_name, lock_name)
SELECT 'QuickScheduler', 'MISFIRE_ACCESS'
WHERE NOT EXISTS (SELECT 1 FROM QRTZ_LOCKS WHERE sched_name = 'QuickScheduler' AND lock_name = 'MISFIRE_ACCESS');

-- ========== 字典类型 ==========

INSERT INTO sys_dict_type (dict_id, dict_name, dict_type, status, del_flag, remark, create_time)
SELECT 30, '任务分组', 'sys_job_group', '0', '0', '定时任务组', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE dict_type = 'sys_job_group');

INSERT INTO sys_dict_type (dict_id, dict_name, dict_type, status, del_flag, remark, create_time)
SELECT 31, '任务状态', 'sys_job_status', '0', '0', '定时任务启停', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE dict_type = 'sys_job_status');

INSERT INTO sys_dict_type (dict_id, dict_name, dict_type, status, del_flag, remark, create_time)
SELECT 32, '错失策略', 'sys_job_misfire_policy', '0', '0', 'Quartz misfire', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE dict_type = 'sys_job_misfire_policy');

INSERT INTO sys_dict_type (dict_id, dict_name, dict_type, status, del_flag, remark, create_time)
SELECT 33, '任务并发', 'sys_job_concurrent', '0', '0', '0允许 1禁止', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE dict_type = 'sys_job_concurrent');

INSERT INTO sys_dict_type (dict_id, dict_name, dict_type, status, del_flag, remark, create_time)
SELECT 34, '调度日志状态', 'sys_job_log_status', '0', '0', '执行成功/失败', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE dict_type = 'sys_job_log_status');

-- ========== 字典数据 ==========

INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, list_class, is_default, status, del_flag, create_time)
SELECT 300, 1, '默认', 'DEFAULT', 'sys_job_group', 'default', '1', '0', '0', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_code = 300);
INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, list_class, is_default, status, del_flag, create_time)
SELECT 301, 2, '系统', 'SYSTEM', 'sys_job_group', 'primary', '0', '0', '0', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_code = 301);

INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, list_class, is_default, status, del_flag, create_time)
SELECT 302, 1, '正常', '0', 'sys_job_status', 'success', '1', '0', '0', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_code = 302);
INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, list_class, is_default, status, del_flag, create_time)
SELECT 303, 2, '暂停', '1', 'sys_job_status', 'danger', '0', '0', '0', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_code = 303);

INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, list_class, is_default, status, del_flag, create_time)
SELECT 304, 1, '默认', '0', 'sys_job_misfire_policy', 'default', '1', '0', '0', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_code = 304);
INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, list_class, is_default, status, del_flag, create_time)
SELECT 305, 2, '立即执行', '1', 'sys_job_misfire_policy', 'primary', '0', '0', '0', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_code = 305);
INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, list_class, is_default, status, del_flag, create_time)
SELECT 306, 3, '执行一次', '2', 'sys_job_misfire_policy', 'warning', '0', '0', '0', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_code = 306);
INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, list_class, is_default, status, del_flag, create_time)
SELECT 307, 4, '放弃执行', '3', 'sys_job_misfire_policy', 'info', '0', '0', '0', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_code = 307);

INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, list_class, is_default, status, del_flag, create_time)
SELECT 308, 1, '允许', '0', 'sys_job_concurrent', 'success', '1', '0', '0', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_code = 308);
INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, list_class, is_default, status, del_flag, create_time)
SELECT 309, 2, '禁止', '1', 'sys_job_concurrent', 'danger', '0', '0', '0', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_code = 309);

INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, list_class, is_default, status, del_flag, create_time)
SELECT 310, 1, '成功', '0', 'sys_job_log_status', 'success', '1', '0', '0', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_code = 310);
INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, list_class, is_default, status, del_flag, create_time)
SELECT 311, 2, '失败', '1', 'sys_job_log_status', 'danger', '0', '0', '0', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_code = 311);

-- ========== 监控菜单（parent_id=2100，menu_id 2130+） ==========

INSERT INTO sys_menu (menu_id, parent_id, menu_name, menu_type, path, component, route_name, perms, icon, order_num, status, del_flag, create_time)
SELECT 2130, 2100, '定时任务', 'C', 'job', 'monitor/job/index', 'SysJob', 'monitor:job:list', 'time', 3, '0', '0', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2130);
INSERT INTO sys_menu (menu_id, parent_id, menu_name, menu_type, path, component, route_name, perms, icon, order_num, status, del_flag, create_time)
SELECT 2131, 2130, '定时任务新增', 'F', NULL, NULL, NULL, 'monitor:job:add', NULL, 1, '0', '0', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2131);
INSERT INTO sys_menu (menu_id, parent_id, menu_name, menu_type, path, component, route_name, perms, icon, order_num, status, del_flag, create_time)
SELECT 2132, 2130, '定时任务修改', 'F', NULL, NULL, NULL, 'monitor:job:edit', NULL, 2, '0', '0', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2132);
INSERT INTO sys_menu (menu_id, parent_id, menu_name, menu_type, path, component, route_name, perms, icon, order_num, status, del_flag, create_time)
SELECT 2133, 2130, '定时任务删除', 'F', NULL, NULL, NULL, 'monitor:job:remove', NULL, 3, '0', '0', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2133);
INSERT INTO sys_menu (menu_id, parent_id, menu_name, menu_type, path, component, route_name, perms, icon, order_num, status, del_flag, create_time)
SELECT 2134, 2130, '定时任务导出', 'F', NULL, NULL, NULL, 'monitor:job:export', NULL, 4, '0', '0', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2134);
INSERT INTO sys_menu (menu_id, parent_id, menu_name, menu_type, path, component, route_name, perms, icon, order_num, status, del_flag, create_time)
SELECT 2135, 2130, '定时任务查询', 'F', NULL, NULL, NULL, 'monitor:job:query', NULL, 5, '0', '0', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2135);
INSERT INTO sys_menu (menu_id, parent_id, menu_name, menu_type, path, component, route_name, perms, icon, order_num, status, del_flag, create_time)
SELECT 2136, 2130, '定时任务改状态', 'F', NULL, NULL, NULL, 'monitor:job:changeStatus', NULL, 6, '0', '0', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2136);

INSERT INTO sys_menu (menu_id, parent_id, menu_name, menu_type, path, component, route_name, perms, icon, order_num, status, del_flag, create_time)
SELECT 2140, 2100, '调度日志', 'C', 'job-log', 'monitor/job-log/index', 'SysJobLog', 'monitor:job:query', 'log', 4, '0', '0', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2140);
INSERT INTO sys_menu (menu_id, parent_id, menu_name, menu_type, path, component, route_name, perms, icon, order_num, status, del_flag, create_time)
SELECT 2141, 2140, '调度日志删除', 'F', NULL, NULL, NULL, 'monitor:job:remove', NULL, 1, '0', '0', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2141);
INSERT INTO sys_menu (menu_id, parent_id, menu_name, menu_type, path, component, route_name, perms, icon, order_num, status, del_flag, create_time)
SELECT 2142, 2140, '调度日志导出', 'F', NULL, NULL, NULL, 'monitor:job:export', NULL, 2, '0', '0', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2142);

-- ========== 超管授权（role_id=1） ==========

INSERT INTO sys_role_menu (role_id, menu_id) SELECT 1, 2130 WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 1 AND menu_id = 2130);
INSERT INTO sys_role_menu (role_id, menu_id) SELECT 1, 2131 WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 1 AND menu_id = 2131);
INSERT INTO sys_role_menu (role_id, menu_id) SELECT 1, 2132 WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 1 AND menu_id = 2132);
INSERT INTO sys_role_menu (role_id, menu_id) SELECT 1, 2133 WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 1 AND menu_id = 2133);
INSERT INTO sys_role_menu (role_id, menu_id) SELECT 1, 2134 WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 1 AND menu_id = 2134);
INSERT INTO sys_role_menu (role_id, menu_id) SELECT 1, 2135 WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 1 AND menu_id = 2135);
INSERT INTO sys_role_menu (role_id, menu_id) SELECT 1, 2136 WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 1 AND menu_id = 2136);
INSERT INTO sys_role_menu (role_id, menu_id) SELECT 1, 2140 WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 1 AND menu_id = 2140);
INSERT INTO sys_role_menu (role_id, menu_id) SELECT 1, 2141 WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 1 AND menu_id = 2141);
INSERT INTO sys_role_menu (role_id, menu_id) SELECT 1, 2142 WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 2142);
