-- 参数设置（menu_id=2040）补充按钮权限，并与超级管理员角色绑定

INSERT INTO sys_menu (menu_id, parent_id, menu_type, menu_name, order_num, path, component, query, route_name, is_frame, is_cache, visible, status, perms, icon, remark, del_flag, create_by, create_time)
VALUES (2041, 2040, 'F', '参数查询', 1, '', NULL, NULL, NULL, '0', '0', '0', '0', 'system:config:query', '#', NULL, '0', 'system', CURRENT_TIMESTAMP);

INSERT INTO sys_menu (menu_id, parent_id, menu_type, menu_name, order_num, path, component, query, route_name, is_frame, is_cache, visible, status, perms, icon, remark, del_flag, create_by, create_time)
VALUES (2042, 2040, 'F', '参数新增', 2, '', NULL, NULL, NULL, '0', '0', '0', '0', 'system:config:add', '#', NULL, '0', 'system', CURRENT_TIMESTAMP);

INSERT INTO sys_menu (menu_id, parent_id, menu_type, menu_name, order_num, path, component, query, route_name, is_frame, is_cache, visible, status, perms, icon, remark, del_flag, create_by, create_time)
VALUES (2043, 2040, 'F', '参数修改', 3, '', NULL, NULL, NULL, '0', '0', '0', '0', 'system:config:edit', '#', NULL, '0', 'system', CURRENT_TIMESTAMP);

INSERT INTO sys_menu (menu_id, parent_id, menu_type, menu_name, order_num, path, component, query, route_name, is_frame, is_cache, visible, status, perms, icon, remark, del_flag, create_by, create_time)
VALUES (2044, 2040, 'F', '参数删除', 4, '', NULL, NULL, NULL, '0', '0', '0', '0', 'system:config:remove', '#', NULL, '0', 'system', CURRENT_TIMESTAMP);

INSERT INTO sys_menu (menu_id, parent_id, menu_type, menu_name, order_num, path, component, query, route_name, is_frame, is_cache, visible, status, perms, icon, remark, del_flag, create_by, create_time)
VALUES (2045, 2040, 'F', '参数导出', 5, '', NULL, NULL, NULL, '0', '0', '0', '0', 'system:config:export', '#', NULL, '0', 'system', CURRENT_TIMESTAMP);

INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 2041);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 2042);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 2043);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 2044);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 2045);
