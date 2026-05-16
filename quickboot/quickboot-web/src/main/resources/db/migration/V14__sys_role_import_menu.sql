-- 角色管理：导入按钮权限，并授予超级管理员角色

INSERT INTO sys_menu (menu_id, parent_id, menu_type, menu_name, order_num, path, component, query, route_name, is_frame, is_cache, visible, status, perms, icon, remark, del_flag, create_by, create_time)
VALUES (2200, 2010, 'F', '角色导入', 6, '', NULL, NULL, NULL, '0', '0', '0', '0', 'system:role:import', '#', NULL, '0', 'system', CURRENT_TIMESTAMP);

INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 2200);
