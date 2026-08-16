-- 监控态势总览菜单与权限（monitor-overview ①期）
-- menu_id: 2169 页面 / 2170 查询按钮；parent 2100 监控目录

INSERT INTO sys_menu (menu_id, parent_id, menu_name, menu_type, path, component, route_name, perms, icon, order_num, status, del_flag, create_time)
SELECT 2169, 2100, '态势总览', 'C', 'overview', 'monitor/overview/index', 'MonitorOverview', 'monitor:overview:query', 'dashboard', 0, '0', '0', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2169);

INSERT INTO sys_menu (menu_id, parent_id, menu_name, menu_type, path, component, route_name, perms, icon, order_num, status, del_flag, create_time)
SELECT 2170, 2169, '态势总览查询', 'F', NULL, NULL, NULL, 'monitor:overview:query', NULL, 1, '0', '0', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2170);

INSERT INTO sys_role_menu (role_id, menu_id) SELECT 1, 2169 WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 1 AND menu_id = 2169);
INSERT INTO sys_role_menu (role_id, menu_id) SELECT 1, 2170 WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 1 AND menu_id = 2170);
