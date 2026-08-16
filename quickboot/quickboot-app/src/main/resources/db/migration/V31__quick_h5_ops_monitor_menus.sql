-- quick-h5 监控运维菜单
-- 9010 系统监控目录；9011–9016 各 C 节点；F 复用 PC monitor:*；绑定 admin

INSERT INTO sys_menu (menu_id, parent_id, menu_name, menu_type, path, component, route_name, perms, icon, order_num, visible, status, del_flag, create_time)
SELECT 9010, 9000, '系统监控', 'M', 'h5-monitor', NULL, 'H5Monitor', NULL, 'monitor', 2, '0', '0', '0', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 9010);

INSERT INTO sys_menu (menu_id, parent_id, menu_name, menu_type, path, component, route_name, perms, icon, order_num, visible, status, del_flag, create_time)
SELECT 9011, 9010, '定时任务', 'C', '/pages/monitor/job/index', NULL, 'H5Job', NULL, 'time', 1, '0', '0', '0', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 9011);

INSERT INTO sys_menu (menu_id, parent_id, menu_name, menu_type, path, component, route_name, perms, icon, order_num, visible, status, del_flag, create_time)
SELECT 9012, 9010, '调度日志', 'C', '/pages/monitor/jobLog/index', NULL, 'H5JobLog', NULL, 'log', 2, '0', '0', '0', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 9012);

INSERT INTO sys_menu (menu_id, parent_id, menu_name, menu_type, path, component, route_name, perms, icon, order_num, visible, status, del_flag, create_time)
SELECT 9013, 9010, '登录日志', 'C', '/pages/monitor/logininfor/index', NULL, 'H5Logininfor', NULL, 'logininfor', 3, '0', '0', '0', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 9013);

INSERT INTO sys_menu (menu_id, parent_id, menu_name, menu_type, path, component, route_name, perms, icon, order_num, visible, status, del_flag, create_time)
SELECT 9014, 9010, '操作日志', 'C', '/pages/monitor/operlog/index', NULL, 'H5Operlog', NULL, 'form', 4, '0', '0', '0', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 9014);

INSERT INTO sys_menu (menu_id, parent_id, menu_name, menu_type, path, component, route_name, perms, icon, order_num, visible, status, del_flag, create_time)
SELECT 9015, 9010, '在线用户', 'C', '/pages/monitor/online/index', NULL, 'H5Online', NULL, 'online', 5, '0', '0', '0', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 9015);

INSERT INTO sys_menu (menu_id, parent_id, menu_name, menu_type, path, component, route_name, perms, icon, order_num, visible, status, del_flag, create_time)
SELECT 9016, 9010, '慢SQL日志', 'C', '/pages/monitor/slowSql/index', NULL, 'H5SlowSql', NULL, 'time', 6, '0', '0', '0', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 9016);

INSERT INTO sys_role_menu (role_id, menu_id) SELECT 1, 9010 WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 1 AND menu_id = 9010);
INSERT INTO sys_role_menu (role_id, menu_id) SELECT 1, 9011 WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 1 AND menu_id = 9011);
INSERT INTO sys_role_menu (role_id, menu_id) SELECT 1, 9012 WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 1 AND menu_id = 9012);
INSERT INTO sys_role_menu (role_id, menu_id) SELECT 1, 9013 WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 1 AND menu_id = 9013);
INSERT INTO sys_role_menu (role_id, menu_id) SELECT 1, 9014 WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 1 AND menu_id = 9014);
INSERT INTO sys_role_menu (role_id, menu_id) SELECT 1, 9015 WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 1 AND menu_id = 9015);
INSERT INTO sys_role_menu (role_id, menu_id) SELECT 1, 9016 WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 1 AND menu_id = 9016);
