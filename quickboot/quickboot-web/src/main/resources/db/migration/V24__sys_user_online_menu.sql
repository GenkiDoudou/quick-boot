-- 在线用户菜单与按钮权限

INSERT INTO sys_menu (menu_id, parent_id, menu_type, menu_name, order_num, path, component, query, route_name, is_frame, is_cache, visible, status, perms, icon, remark, del_flag, create_by, create_time)
VALUES (2250, 2000, 'C', '在线用户', 7, 'online', 'monitor/online/index', NULL, 'SysUserOnline', '0', '0', '0', '0', 'monitor:online:list', 'people', NULL, '0', 'system', CURRENT_TIMESTAMP);

INSERT INTO sys_menu (menu_id, parent_id, menu_type, menu_name, order_num, path, component, query, route_name, is_frame, is_cache, visible, status, perms, icon, remark, del_flag, create_by, create_time)
VALUES (2251, 2250, 'F', '在线查询', 1, '', NULL, NULL, NULL, '0', '0', '0', '0', 'monitor:online:list', '#', NULL, '0', 'system', CURRENT_TIMESTAMP);

INSERT INTO sys_menu (menu_id, parent_id, menu_type, menu_name, order_num, path, component, query, route_name, is_frame, is_cache, visible, status, perms, icon, remark, del_flag, create_by, create_time)
VALUES (2252, 2250, 'F', '强退用户', 2, '', NULL, NULL, NULL, '0', '0', '0', '0', 'monitor:online:forceLogout', '#', NULL, '0', 'system', CURRENT_TIMESTAMP);

INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 2250);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 2251);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 2252);
