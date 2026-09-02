-- 将 router/index.js dynamicRoutes 三条 hidden 路由写入 sys_menu（Flyway ADD only）

-- 字典数据页：/system/dict-data/index/:dictType
INSERT INTO sys_menu (menu_id, parent_id, menu_name, menu_type, path, component, route_name, perms, icon, order_num, visible, status, del_flag, create_time)
SELECT 2062, 2000, '字典数据路由', 'M', 'dict-data', 'ParentView', 'SysDictDataDir', 'system:dictData:list', NULL, 99, '1', '0', '0', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2062);

INSERT INTO sys_menu (menu_id, parent_id, menu_name, menu_type, path, component, route_name, perms, icon, order_num, visible, status, del_flag, remark, create_time)
SELECT 2063, 2062, '字典数据', 'C', 'index/:dictType', 'system/dict/data/index', 'SysDictData', 'system:dictData:list', NULL, 1, '1', '0', '0', 'activeMenu:/system/dict', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2063);

-- 用户分配角色：/system/user-auth/role/:userId
INSERT INTO sys_menu (menu_id, parent_id, menu_name, menu_type, path, component, route_name, perms, icon, order_num, visible, status, del_flag, create_time)
SELECT 2064, 2000, '分配角色路由', 'M', 'user-auth', 'ParentView', 'SysUserAuthDir', 'system:user:edit', NULL, 100, '1', '0', '0', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2064);

INSERT INTO sys_menu (menu_id, parent_id, menu_name, menu_type, path, component, route_name, perms, icon, order_num, visible, status, del_flag, remark, create_time)
SELECT 2065, 2064, '分配角色', 'C', 'role/:userId', 'system/user/auth-role', 'SysUserAuthRole', 'system:user:edit', NULL, 1, '1', '0', '0', 'activeMenu:/system/user', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2065);

-- 代码生成编辑：/tool/gen/edit
INSERT INTO sys_menu (menu_id, parent_id, menu_name, menu_type, path, component, route_name, perms, icon, order_num, visible, status, del_flag, remark, create_time)
SELECT 2308, 2300, '修改生成配置', 'C', 'gen/edit', 'tool/gen/edit', 'ToolGenEdit', 'tool:gen:edit', NULL, 99, '1', '0', '0', 'activeMenu:/tool/gen', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2308);

-- admin 角色绑定
INSERT INTO sys_role_menu (role_id, menu_id) SELECT 1, 2062 WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 1 AND menu_id = 2062);
INSERT INTO sys_role_menu (role_id, menu_id) SELECT 1, 2063 WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 1 AND menu_id = 2063);
INSERT INTO sys_role_menu (role_id, menu_id) SELECT 1, 2064 WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 1 AND menu_id = 2064);
INSERT INTO sys_role_menu (role_id, menu_id) SELECT 1, 2065 WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 1 AND menu_id = 2065);
INSERT INTO sys_role_menu (role_id, menu_id) SELECT 1, 2308 WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 1 AND menu_id = 2308);
