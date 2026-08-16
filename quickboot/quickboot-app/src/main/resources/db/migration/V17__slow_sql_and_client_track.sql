/*
 * 慢 SQL + 前端监控：sys_slow_sql、sys_client_track（合并 bak 终态列），监控菜单与超管授权。
 * 依赖：V8（监控目录 menu_id=2100）、V11（job 2130–2142）、V13（online 2150–2152）。
 * menu_id 使用 2160+，避开 2130–2152。
 * 注：V14–V16 已用于积木报表，故本脚本为 V17。
 */

-- ========== sys_slow_sql ==========

CREATE TABLE IF NOT EXISTS sys_slow_sql (
  slow_id              BIGINT        NOT NULL,
  sql_source           VARCHAR(20)   NOT NULL DEFAULT 'BUSINESS',
  sql_type             VARCHAR(20)   NOT NULL DEFAULT 'OTHER',
  mapper_id            VARCHAR(500)  NOT NULL DEFAULT '',
  sql_text             VARCHAR(4000) NOT NULL DEFAULT '',
  cost_time            BIGINT        NOT NULL DEFAULT 0,
  trace_id             VARCHAR(64)   NULL,
  client_operation_id  VARCHAR(64)   NULL,
  client_id            VARCHAR(64)   NULL,
  request_method       VARCHAR(20)   NOT NULL DEFAULT '',
  request_uri          VARCHAR(500)  NOT NULL DEFAULT '',
  oper_name            VARCHAR(64)   NOT NULL DEFAULT '',
  create_time          TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (slow_id)
);

CREATE INDEX idx_sys_slow_sql_create_time ON sys_slow_sql (create_time);
CREATE INDEX idx_sys_slow_sql_trace_id ON sys_slow_sql (trace_id);
CREATE INDEX idx_sys_slow_sql_sql_source ON sys_slow_sql (sql_source);
CREATE INDEX idx_sys_slow_sql_sql_type ON sys_slow_sql (sql_type);
CREATE INDEX idx_sys_slow_sql_cost_time ON sys_slow_sql (cost_time);
CREATE INDEX idx_sys_slow_sql_client_operation_id ON sys_slow_sql (client_operation_id);

-- ========== sys_client_track（终态合并列） ==========

CREATE TABLE IF NOT EXISTS sys_client_track (
  batch_id          BIGINT        NOT NULL,
  trace_id          VARCHAR(64)   NOT NULL DEFAULT '',
  operation_id      VARCHAR(64)   NOT NULL DEFAULT '',
  browser_visit_id  VARCHAR(64)   NOT NULL DEFAULT '',
  session_id        VARCHAR(64)   NOT NULL DEFAULT '',
  page_visit_id     VARCHAR(64)   NOT NULL DEFAULT '',
  trigger_action    VARCHAR(128)  NOT NULL DEFAULT '',
  user_id           BIGINT        NULL,
  user_name         VARCHAR(64)   NOT NULL DEFAULT '',
  reason            VARCHAR(32)   NOT NULL DEFAULT 'normal',
  page_path         VARCHAR(500)  NULL,
  ua                VARCHAR(500)  NULL,
  events_json       LONGTEXT      NOT NULL,
  client_ip         VARCHAR(128)  NULL,
  create_time       TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (batch_id)
);

CREATE INDEX idx_sys_client_track_trace_id ON sys_client_track (trace_id);
CREATE INDEX idx_sys_client_track_user_name ON sys_client_track (user_name);
CREATE INDEX idx_sys_client_track_create_time ON sys_client_track (create_time);
CREATE INDEX idx_sys_client_track_operation_id ON sys_client_track (operation_id);
CREATE INDEX idx_sys_client_track_trigger_action ON sys_client_track (trigger_action);
CREATE INDEX idx_sys_client_track_session_id ON sys_client_track (session_id);
CREATE INDEX idx_sys_client_track_page_visit_id ON sys_client_track (page_visit_id);
CREATE INDEX idx_sys_client_track_browser_visit_id ON sys_client_track (browser_visit_id);

-- ========== 监控菜单（parent_id=2100，menu_id 2160+） ==========

INSERT INTO sys_menu (menu_id, parent_id, menu_name, menu_type, path, component, route_name, perms, icon, order_num, status, del_flag, create_time)
SELECT 2160, 2100, '慢SQL日志', 'C', 'slowSql', 'monitor/slowSql/index', 'SysSlowSql', 'monitor:slowSql:query', 'time', 6, '0', '0', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2160);
INSERT INTO sys_menu (menu_id, parent_id, menu_name, menu_type, path, component, route_name, perms, icon, order_num, status, del_flag, create_time)
SELECT 2161, 2160, '慢SQL删除', 'F', NULL, NULL, NULL, 'monitor:slowSql:remove', NULL, 1, '0', '0', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2161);
INSERT INTO sys_menu (menu_id, parent_id, menu_name, menu_type, path, component, route_name, perms, icon, order_num, status, del_flag, create_time)
SELECT 2162, 2160, '慢SQL导出', 'F', NULL, NULL, NULL, 'monitor:slowSql:export', NULL, 2, '0', '0', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2162);

INSERT INTO sys_menu (menu_id, parent_id, menu_name, menu_type, path, component, route_name, perms, icon, order_num, status, del_flag, create_time)
SELECT 2163, 2100, '前端监控', 'C', 'clientTrack', 'monitor/clientTrack/index', 'SysClientTrack', 'monitor:clientTrack:list', 'bug', 7, '0', '0', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2163);
INSERT INTO sys_menu (menu_id, parent_id, menu_name, menu_type, path, component, route_name, perms, icon, order_num, status, del_flag, create_time)
SELECT 2164, 2163, '前端监控查询', 'F', NULL, NULL, NULL, 'monitor:clientTrack:list', NULL, 1, '0', '0', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2164);
INSERT INTO sys_menu (menu_id, parent_id, menu_name, menu_type, path, component, route_name, perms, icon, order_num, status, del_flag, create_time)
SELECT 2165, 2163, '前端监控删除', 'F', NULL, NULL, NULL, 'monitor:clientTrack:remove', NULL, 2, '0', '0', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2165);

INSERT INTO sys_menu (menu_id, parent_id, menu_name, menu_type, path, component, route_name, perms, icon, order_num, status, del_flag, create_time)
SELECT 2166, 2100, '事件链路', 'C', 'clientTrackEvents', 'monitor/clientTrack/events', 'SysClientTrackEvents', 'monitor:clientTrack:list', 'guide', 8, '0', '0', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2166);

INSERT INTO sys_menu (menu_id, parent_id, menu_name, menu_type, path, component, route_name, perms, icon, order_num, status, del_flag, create_time)
SELECT 2167, 2100, '行为轨迹', 'C', 'clientTrackTimeline', 'monitor/clientTrack/timeline', 'SysClientTrackTimeline', 'monitor:clientTrack:list', 'share', 9, '0', '0', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2167);

INSERT INTO sys_menu (menu_id, parent_id, menu_name, menu_type, path, component, route_name, perms, icon, order_num, status, del_flag, create_time)
SELECT 2168, 2100, '全链路监控', 'C', 'traceChain', 'monitor/traceChain/index', 'SysTraceChain', 'monitor:traceChain:query', 'connection', 10, '0', '0', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2168);

-- ========== 超管授权（role_id=1） ==========

INSERT INTO sys_role_menu (role_id, menu_id) SELECT 1, 2160 WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 1 AND menu_id = 2160);
INSERT INTO sys_role_menu (role_id, menu_id) SELECT 1, 2161 WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 1 AND menu_id = 2161);
INSERT INTO sys_role_menu (role_id, menu_id) SELECT 1, 2162 WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 1 AND menu_id = 2162);
INSERT INTO sys_role_menu (role_id, menu_id) SELECT 1, 2163 WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 1 AND menu_id = 2163);
INSERT INTO sys_role_menu (role_id, menu_id) SELECT 1, 2164 WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 1 AND menu_id = 2164);
INSERT INTO sys_role_menu (role_id, menu_id) SELECT 1, 2165 WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 1 AND menu_id = 2165);
INSERT INTO sys_role_menu (role_id, menu_id) SELECT 1, 2166 WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 1 AND menu_id = 2166);
INSERT INTO sys_role_menu (role_id, menu_id) SELECT 1, 2167 WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 1 AND menu_id = 2167);
INSERT INTO sys_role_menu (role_id, menu_id) SELECT 1, 2168 WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 1 AND menu_id = 2168);
