-- 慢 SQL 记录（JDBC/Druid 统一采集：业务 MyBatis + 积木 MiniDao + SYSTEM 无 HTTP 上下文）
-- 若 V51 曾失败：见 db/repair/fix_flyway_v51_failed.sql，或 dev 下 repair-on-migrate 后重启

CREATE TABLE IF NOT EXISTS sys_slow_sql (
    slow_id              BIGINT         NOT NULL PRIMARY KEY,
    sql_source           VARCHAR(20)    NOT NULL DEFAULT 'BUSINESS',
    mapper_id            VARCHAR(500)   NOT NULL DEFAULT '',
    sql_text             VARCHAR(4000)  NOT NULL DEFAULT '',
    cost_time            BIGINT         NOT NULL DEFAULT 0,
    trace_id             VARCHAR(64)    NULL,
    client_operation_id  VARCHAR(64)    NULL,
    client_id            VARCHAR(64)    NULL,
    request_method       VARCHAR(20)    NOT NULL DEFAULT '',
    request_uri          VARCHAR(500)   NOT NULL DEFAULT '',
    oper_name            VARCHAR(64)    NOT NULL DEFAULT '',
    create_time          DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    KEY idx_sys_slow_sql_create_time (create_time),
    KEY idx_sys_slow_sql_trace_id (trace_id),
    KEY idx_sys_slow_sql_sql_source (sql_source),
    KEY idx_sys_slow_sql_cost_time (cost_time)
);

INSERT IGNORE INTO sys_menu (menu_id, parent_id, menu_type, menu_name, order_num, path, component, query, route_name, is_frame, is_cache, visible, status, perms, icon, remark, del_flag, create_by, create_time)
VALUES (2264, 2000, 'C', '慢SQL日志', 11, 'slowSql', 'monitor/slowSql/index', NULL, 'SysSlowSql', '0', '0', '0', '0', 'monitor:slowSql:query', 'time', 'JDBC 慢 SQL（含积木 traceId）', '0', 'system', CURRENT_TIMESTAMP);

INSERT IGNORE INTO sys_menu (menu_id, parent_id, menu_type, menu_name, order_num, path, component, query, route_name, is_frame, is_cache, visible, status, perms, icon, remark, del_flag, create_by, create_time)
VALUES (2265, 2264, 'F', '慢SQL删除', 1, '', NULL, NULL, NULL, '0', '0', '0', '0', 'monitor:slowSql:remove', '#', NULL, '0', 'system', CURRENT_TIMESTAMP);

INSERT IGNORE INTO sys_menu (menu_id, parent_id, menu_type, menu_name, order_num, path, component, query, route_name, is_frame, is_cache, visible, status, perms, icon, remark, del_flag, create_by, create_time)
VALUES (2266, 2264, 'F', '慢SQL导出', 2, '', NULL, NULL, NULL, '0', '0', '0', '0', 'monitor:slowSql:export', '#', NULL, '0', 'system', CURRENT_TIMESTAMP);

INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (1, 2264);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (1, 2265);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (1, 2266);
