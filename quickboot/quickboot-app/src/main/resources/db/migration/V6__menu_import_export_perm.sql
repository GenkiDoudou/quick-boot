-- 菜单「导出」「导入」按钮权限；超级管理员默认授权

INSERT INTO sys_menu (menu_id, parent_id, menu_name, menu_type, path, component, route_name, perms, icon, order_num, status, del_flag, create_time)
SELECT 2025, 2020, '菜单导出', 'F', NULL, NULL, NULL, 'system:menu:export', NULL, 5, '0', '0', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2025);

INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, 2025 WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 1 AND menu_id = 2025);

INSERT INTO sys_menu (menu_id, parent_id, menu_name, menu_type, path, component, route_name, perms, icon, order_num, status, del_flag, create_time)
SELECT 2026, 2020, '菜单导入', 'F', NULL, NULL, NULL, 'system:menu:import', NULL, 6, '0', '0', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2026);

INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, 2026 WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 1 AND menu_id = 2026);
