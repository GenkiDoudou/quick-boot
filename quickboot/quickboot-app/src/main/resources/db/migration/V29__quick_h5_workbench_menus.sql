-- quick-h5 工作台菜单初始化（path 以 /pages/ 开头 = H5 入口约定）
-- menu_id: 9000 根目录 / 9001 系统管理 / 9002–9004 用户·部门·角色
-- 按钮 F 复用现有 system:user|dept|role:*，不在此重复插入
-- 绑定 role_id=1（管理员）

-- 根：移动端工作台（仅目录，不进 PC 侧栏有效叶子）
INSERT INTO sys_menu (menu_id, parent_id, menu_name, menu_type, path, component, route_name, perms, icon, order_num, visible, status, del_flag, create_time)
SELECT 9000, 0, '移动端工作台', 'M', 'h5-workbench', NULL, 'H5Workbench', NULL, 'phone', 90, '0', '0', '0', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 9000);

INSERT INTO sys_menu (menu_id, parent_id, menu_name, menu_type, path, component, route_name, perms, icon, order_num, visible, status, del_flag, create_time)
SELECT 9001, 9000, '系统管理', 'M', 'h5-system', NULL, 'H5System', NULL, 'system', 1, '0', '0', '0', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 9001);

INSERT INTO sys_menu (menu_id, parent_id, menu_name, menu_type, path, component, route_name, perms, icon, order_num, visible, status, del_flag, create_time)
SELECT 9002, 9001, '用户', 'C', '/pages/system/user/index', NULL, 'H5User', NULL, 'user', 1, '0', '0', '0', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 9002);

INSERT INTO sys_menu (menu_id, parent_id, menu_name, menu_type, path, component, route_name, perms, icon, order_num, visible, status, del_flag, create_time)
SELECT 9003, 9001, '部门', 'C', '/pages/system/dept/index', NULL, 'H5Dept', NULL, 'tree', 2, '0', '0', '0', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 9003);

INSERT INTO sys_menu (menu_id, parent_id, menu_name, menu_type, path, component, route_name, perms, icon, order_num, visible, status, del_flag, create_time)
SELECT 9004, 9001, '角色', 'C', '/pages/system/role/index', NULL, 'H5Role', NULL, 'peoples', 3, '0', '0', '0', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 9004);

INSERT INTO sys_role_menu (role_id, menu_id) SELECT 1, 9000 WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 1 AND menu_id = 9000);
INSERT INTO sys_role_menu (role_id, menu_id) SELECT 1, 9001 WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 1 AND menu_id = 9001);
INSERT INTO sys_role_menu (role_id, menu_id) SELECT 1, 9002 WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 1 AND menu_id = 9002);
INSERT INTO sys_role_menu (role_id, menu_id) SELECT 1, 9003 WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 1 AND menu_id = 9003);
INSERT INTO sys_role_menu (role_id, menu_id) SELECT 1, 9004 WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 1 AND menu_id = 9004);
