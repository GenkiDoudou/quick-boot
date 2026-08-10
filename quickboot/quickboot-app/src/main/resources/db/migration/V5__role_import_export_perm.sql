-- 角色「导出」「导入」按钮权限；超级管理员默认授权

INSERT INTO sys_menu (menu_id, parent_id, menu_name, menu_type, path, component, route_name, perms, icon, order_num, status, del_flag, create_time)
SELECT 2017, 2010, '角色导出', 'F', NULL, NULL, NULL, 'system:role:export', NULL, 7, '0', '0', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2017);

INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, 2017 WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 1 AND menu_id = 2017);

INSERT INTO sys_menu (menu_id, parent_id, menu_name, menu_type, path, component, route_name, perms, icon, order_num, status, del_flag, create_time)
SELECT 2018, 2010, '角色导入', 'F', NULL, NULL, NULL, 'system:role:import', NULL, 8, '0', '0', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2018);

INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, 2018 WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 1 AND menu_id = 2018);
