-- quick-h5 系统运维菜单（挂在 9001 系统管理下）
-- menu_id: 9005–9009；按钮 F 复用 PC system:* perms，不重复插入
-- 绑定 role_id=1

INSERT INTO sys_menu (menu_id, parent_id, menu_name, menu_type, path, component, route_name, perms, icon, order_num, visible, status, del_flag, create_time)
SELECT 9005, 9001, '参数设置', 'C', '/pages/system/config/index', NULL, 'H5Config', NULL, 'edit', 4, '0', '0', '0', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 9005);

INSERT INTO sys_menu (menu_id, parent_id, menu_name, menu_type, path, component, route_name, perms, icon, order_num, visible, status, del_flag, create_time)
SELECT 9006, 9001, '字典管理', 'C', '/pages/system/dict/type/index', NULL, 'H5Dict', NULL, 'dict', 5, '0', '0', '0', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 9006);

INSERT INTO sys_menu (menu_id, parent_id, menu_name, menu_type, path, component, route_name, perms, icon, order_num, visible, status, del_flag, create_time)
SELECT 9007, 9001, '客户端', 'C', '/pages/system/oauthClient/index', NULL, 'H5OauthClient', NULL, 'client', 6, '0', '0', '0', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 9007);

INSERT INTO sys_menu (menu_id, parent_id, menu_name, menu_type, path, component, route_name, perms, icon, order_num, visible, status, del_flag, create_time)
SELECT 9008, 9001, '文件分类', 'C', '/pages/system/fileClassify/index', NULL, 'H5FileClassify', NULL, 'list', 7, '0', '0', '0', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 9008);

INSERT INTO sys_menu (menu_id, parent_id, menu_name, menu_type, path, component, route_name, perms, icon, order_num, visible, status, del_flag, create_time)
SELECT 9009, 9001, '文件管理', 'C', '/pages/system/file/index', NULL, 'H5File', NULL, 'upload', 8, '0', '0', '0', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 9009);

INSERT INTO sys_role_menu (role_id, menu_id) SELECT 1, 9005 WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 1 AND menu_id = 9005);
INSERT INTO sys_role_menu (role_id, menu_id) SELECT 1, 9006 WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 1 AND menu_id = 9006);
INSERT INTO sys_role_menu (role_id, menu_id) SELECT 1, 9007 WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 1 AND menu_id = 9007);
INSERT INTO sys_role_menu (role_id, menu_id) SELECT 1, 9008 WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 1 AND menu_id = 9008);
INSERT INTO sys_role_menu (role_id, menu_id) SELECT 1, 9009 WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 1 AND menu_id = 9009);
