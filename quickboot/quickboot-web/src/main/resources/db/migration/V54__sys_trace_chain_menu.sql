-- 全链路监控（traceChain）：聚合页面跳转、行为明细、HTTP、操作日志、慢 SQL

CREATE INDEX idx_sys_slow_sql_client_operation_id ON sys_slow_sql (client_operation_id);

INSERT IGNORE INTO sys_menu (menu_id, parent_id, menu_type, menu_name, order_num, path, component, query, route_name, is_frame, is_cache, visible, status, perms, icon, remark, del_flag, create_by, create_time)
VALUES (2267, 2000, 'C', '全链路监控', 13, 'traceChain', 'monitor/traceChain/index', NULL, 'SysTraceChain', '0', '0', '0', '0', 'monitor:traceChain:query', 'connection', '按 operationId/traceId 聚合前后端链路（Network 视图）', '0', 'system', CURRENT_TIMESTAMP);

INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (1, 2267);
