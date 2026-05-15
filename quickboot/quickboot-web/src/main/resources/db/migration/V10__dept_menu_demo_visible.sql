-- 系统管理下补充「部门管理」菜单及按钮；组件演示下子菜单全部侧栏显示（visible=0）。

UPDATE sys_menu SET visible = '0' WHERE parent_id = 2100 AND menu_type = 'C';

INSERT INTO sys_menu (menu_id, parent_id, menu_type, menu_name, order_num, path, component, query, route_name, is_frame, is_cache, visible, status, perms, icon, remark, del_flag, create_by, create_time)
VALUES (2050, 2000, 'C', '部门管理', 2, 'dept', 'system/dept/index', NULL, 'Dept', '0', '0', '0', '0', 'system:dept:list', 'tree', NULL, '0', 'system', CURRENT_TIMESTAMP);

INSERT INTO sys_menu (menu_id, parent_id, menu_type, menu_name, order_num, path, component, query, route_name, is_frame, is_cache, visible, status, perms, icon, remark, del_flag, create_by, create_time)
VALUES (2051, 2050, 'F', '部门查询', 1, '', NULL, NULL, NULL, '0', '0', '0', '0', 'system:dept:query', '#', NULL, '0', 'system', CURRENT_TIMESTAMP);

INSERT INTO sys_menu (menu_id, parent_id, menu_type, menu_name, order_num, path, component, query, route_name, is_frame, is_cache, visible, status, perms, icon, remark, del_flag, create_by, create_time)
VALUES (2052, 2050, 'F', '部门新增', 2, '', NULL, NULL, NULL, '0', '0', '0', '0', 'system:dept:add', '#', NULL, '0', 'system', CURRENT_TIMESTAMP);

INSERT INTO sys_menu (menu_id, parent_id, menu_type, menu_name, order_num, path, component, query, route_name, is_frame, is_cache, visible, status, perms, icon, remark, del_flag, create_by, create_time)
VALUES (2053, 2050, 'F', '部门修改', 3, '', NULL, NULL, NULL, '0', '0', '0', '0', 'system:dept:edit', '#', NULL, '0', 'system', CURRENT_TIMESTAMP);

INSERT INTO sys_menu (menu_id, parent_id, menu_type, menu_name, order_num, path, component, query, route_name, is_frame, is_cache, visible, status, perms, icon, remark, del_flag, create_by, create_time)
VALUES (2054, 2050, 'F', '部门删除', 4, '', NULL, NULL, NULL, '0', '0', '0', '0', 'system:dept:remove', '#', NULL, '0', 'system', CURRENT_TIMESTAMP);

INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 2050);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 2051);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 2052);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 2053);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 2054);
