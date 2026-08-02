-- OAuth 客户端「导出」按钮权限；超级管理员默认授权（Flyway 只执行一次）

INSERT INTO sys_menu (menu_id, parent_id, menu_name, menu_type, path, component, route_name, perms, icon, order_num, status, del_flag, create_time)
SELECT 2007, 2001, '客户端导出', 'F', NULL, NULL, NULL, 'system:oauthClient:export', NULL, 6, '0', '0', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2007);

INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, 2007 WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 1 AND menu_id = 2007);
