-- 字典管理菜单与按钮权限（与 DictTypeController / DictDataController @SaCheckPermission 对齐）

INSERT INTO sys_menu (menu_id, parent_id, menu_type, menu_name, order_num, path, component, query, route_name, is_frame, is_cache, visible, status, perms, icon, remark, del_flag, create_by, create_time)
VALUES (2060, 2000, 'C', '字典管理', 3, 'dict/type', 'system/dict/type/index', NULL, 'DictType', '0', '0', '0', '0', 'system:dict:list', 'dict', NULL, '0', 'system', CURRENT_TIMESTAMP);

INSERT INTO sys_menu (menu_id, parent_id, menu_type, menu_name, order_num, path, component, query, route_name, is_frame, is_cache, visible, status, perms, icon, remark, del_flag, create_by, create_time)
VALUES (2061, 2060, 'F', '字典查询', 1, '', NULL, NULL, NULL, '0', '0', '0', '0', 'system:dict:query', '#', NULL, '0', 'system', CURRENT_TIMESTAMP);

INSERT INTO sys_menu (menu_id, parent_id, menu_type, menu_name, order_num, path, component, query, route_name, is_frame, is_cache, visible, status, perms, icon, remark, del_flag, create_by, create_time)
VALUES (2062, 2060, 'F', '字典新增', 2, '', NULL, NULL, NULL, '0', '0', '0', '0', 'system:dict:add', '#', NULL, '0', 'system', CURRENT_TIMESTAMP);

INSERT INTO sys_menu (menu_id, parent_id, menu_type, menu_name, order_num, path, component, query, route_name, is_frame, is_cache, visible, status, perms, icon, remark, del_flag, create_by, create_time)
VALUES (2063, 2060, 'F', '字典修改', 3, '', NULL, NULL, NULL, '0', '0', '0', '0', 'system:dict:edit', '#', NULL, '0', 'system', CURRENT_TIMESTAMP);

INSERT INTO sys_menu (menu_id, parent_id, menu_type, menu_name, order_num, path, component, query, route_name, is_frame, is_cache, visible, status, perms, icon, remark, del_flag, create_by, create_time)
VALUES (2064, 2060, 'F', '字典删除', 4, '', NULL, NULL, NULL, '0', '0', '0', '0', 'system:dict:remove', '#', NULL, '0', 'system', CURRENT_TIMESTAMP);

INSERT INTO sys_menu (menu_id, parent_id, menu_type, menu_name, order_num, path, component, query, route_name, is_frame, is_cache, visible, status, perms, icon, remark, del_flag, create_by, create_time)
VALUES (2065, 2060, 'F', '字典导出', 5, '', NULL, NULL, NULL, '0', '0', '0', '0', 'system:dict:export', '#', NULL, '0', 'system', CURRENT_TIMESTAMP);

INSERT INTO sys_menu (menu_id, parent_id, menu_type, menu_name, order_num, path, component, query, route_name, is_frame, is_cache, visible, status, perms, icon, remark, del_flag, create_by, create_time)
VALUES (2066, 2060, 'F', '字典刷新', 6, '', NULL, NULL, NULL, '0', '0', '0', '0', 'system:dict:refresh', '#', NULL, '0', 'system', CURRENT_TIMESTAMP);

INSERT INTO sys_menu (menu_id, parent_id, menu_type, menu_name, order_num, path, component, query, route_name, is_frame, is_cache, visible, status, perms, icon, remark, del_flag, create_by, create_time)
VALUES (2067, 2060, 'F', '字典导入', 7, '', NULL, NULL, NULL, '0', '0', '0', '0', 'system:dict:import', '#', NULL, '0', 'system', CURRENT_TIMESTAMP);

INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 2060);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 2061);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 2062);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 2063);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 2064);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 2065);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 2066);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 2067);
