-- 用户管理菜单与超级管理员授权（主键 203x，避免与 V6 201x、V7 202x 冲突）

INSERT INTO sys_menu (menu_id, parent_id, menu_type, menu_name, order_num, path, component, query, route_name, is_frame, is_cache, visible, status, perms, icon, remark, del_flag, create_by, create_time)
VALUES (2030, 2000, 'C', '用户管理', 5, 'user', 'system/user/index', NULL, 'SysUser', '0', '0', '0', '0', 'system:user:list', 'user', NULL, '0', 'system', CURRENT_TIMESTAMP);

INSERT INTO sys_menu (menu_id, parent_id, menu_type, menu_name, order_num, path, component, query, route_name, is_frame, is_cache, visible, status, perms, icon, remark, del_flag, create_by, create_time)
VALUES (2031, 2030, 'F', '用户新增', 1, '', NULL, NULL, NULL, '0', '0', '0', '0', 'system:user:add', '#', NULL, '0', 'system', CURRENT_TIMESTAMP);

INSERT INTO sys_menu (menu_id, parent_id, menu_type, menu_name, order_num, path, component, query, route_name, is_frame, is_cache, visible, status, perms, icon, remark, del_flag, create_by, create_time)
VALUES (2032, 2030, 'F', '用户修改', 2, '', NULL, NULL, NULL, '0', '0', '0', '0', 'system:user:edit', '#', NULL, '0', 'system', CURRENT_TIMESTAMP);

INSERT INTO sys_menu (menu_id, parent_id, menu_type, menu_name, order_num, path, component, query, route_name, is_frame, is_cache, visible, status, perms, icon, remark, del_flag, create_by, create_time)
VALUES (2033, 2030, 'F', '用户删除', 3, '', NULL, NULL, NULL, '0', '0', '0', '0', 'system:user:remove', '#', NULL, '0', 'system', CURRENT_TIMESTAMP);

INSERT INTO sys_menu (menu_id, parent_id, menu_type, menu_name, order_num, path, component, query, route_name, is_frame, is_cache, visible, status, perms, icon, remark, del_flag, create_by, create_time)
VALUES (2034, 2030, 'F', '用户导出', 4, '', NULL, NULL, NULL, '0', '0', '0', '0', 'system:user:export', '#', NULL, '0', 'system', CURRENT_TIMESTAMP);

INSERT INTO sys_menu (menu_id, parent_id, menu_type, menu_name, order_num, path, component, query, route_name, is_frame, is_cache, visible, status, perms, icon, remark, del_flag, create_by, create_time)
VALUES (2035, 2030, 'F', '用户导入', 5, '', NULL, NULL, NULL, '0', '0', '0', '0', 'system:user:import', '#', NULL, '0', 'system', CURRENT_TIMESTAMP);

INSERT INTO sys_menu (menu_id, parent_id, menu_type, menu_name, order_num, path, component, query, route_name, is_frame, is_cache, visible, status, perms, icon, remark, del_flag, create_by, create_time)
VALUES (2036, 2030, 'F', '用户重置密码', 6, '', NULL, NULL, NULL, '0', '0', '0', '0', 'system:user:resetPwd', '#', NULL, '0', 'system', CURRENT_TIMESTAMP);

INSERT INTO sys_menu (menu_id, parent_id, menu_type, menu_name, order_num, path, component, query, route_name, is_frame, is_cache, visible, status, perms, icon, remark, del_flag, create_by, create_time)
VALUES (2039, 2030, 'C', '分配角色', 99, 'auth-role', 'system/user/auth-role', NULL, 'SysUserAuthRole', '0', '0', '1', '0', 'system:user:edit', '#', NULL, '0', 'system', CURRENT_TIMESTAMP);

INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 2030);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 2031);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 2032);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 2033);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 2034);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 2035);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 2036);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 2039);
