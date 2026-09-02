<#-- Flyway ADD-only 菜单/权限脚本模板：落盘后请改名为 Vxx__${tableName}_menu.sql 并调整 menu_id -->
<#if !table.parentMenuId??>
-- 未配置上级菜单（目录），请在「编辑生成配置」中选择上级菜单后重新预览/生成。
<#else>
-- ${tableComment!} 菜单（上级目录 menu_id = ${table.parentMenuId}）
-- 权限前缀: ${permissionPrefix}
-- 使用方式：复制到 quickboot-app/src/main/resources/db/migration/ 并改名为下一可用 V 版本（仅 ADD）。

SET @parentId := ${table.parentMenuId};
SET @menuId := 900001;

INSERT INTO sys_menu (menu_id, parent_id, menu_type, menu_name, order_num, path, component, query, route_name, is_frame, is_cache, visible, status, perms, icon, remark, del_flag, create_by, create_time)
SELECT @menuId, @parentId, 'C', '${functionName!tableComment!}', 1, '${businessName}', '${moduleName}/${businessName}/index', NULL, '${className}', '0', '0', '0', '0', '${permissionPrefix}:list', 'list', 'codegen', '0', 'system', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = @menuId AND del_flag = '0');

INSERT INTO sys_menu (menu_id, parent_id, menu_type, menu_name, order_num, path, component, query, route_name, is_frame, is_cache, visible, status, perms, icon, remark, del_flag, create_by, create_time)
SELECT @menuId + 1, @menuId, 'F', '${functionName!}新增', 1, '', NULL, NULL, NULL, '0', '0', '0', '0', '${permissionPrefix}:add', '#', NULL, '0', 'system', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = '${permissionPrefix}:add' AND del_flag = '0');

INSERT INTO sys_menu (menu_id, parent_id, menu_type, menu_name, order_num, path, component, query, route_name, is_frame, is_cache, visible, status, perms, icon, remark, del_flag, create_by, create_time)
SELECT @menuId + 2, @menuId, 'F', '${functionName!}修改', 2, '', NULL, NULL, NULL, '0', '0', '0', '0', '${permissionPrefix}:edit', '#', NULL, '0', 'system', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = '${permissionPrefix}:edit' AND del_flag = '0');

INSERT INTO sys_menu (menu_id, parent_id, menu_type, menu_name, order_num, path, component, query, route_name, is_frame, is_cache, visible, status, perms, icon, remark, del_flag, create_by, create_time)
SELECT @menuId + 3, @menuId, 'F', '${functionName!}删除', 3, '', NULL, NULL, NULL, '0', '0', '0', '0', '${permissionPrefix}:remove', '#', NULL, '0', 'system', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = '${permissionPrefix}:remove' AND del_flag = '0');

INSERT INTO sys_menu (menu_id, parent_id, menu_type, menu_name, order_num, path, component, query, route_name, is_frame, is_cache, visible, status, perms, icon, remark, del_flag, create_by, create_time)
SELECT @menuId + 4, @menuId, 'F', '${functionName!}查询', 4, '', NULL, NULL, NULL, '0', '0', '0', '0', '${permissionPrefix}:query', '#', NULL, '0', 'system', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = '${permissionPrefix}:query' AND del_flag = '0');

INSERT INTO sys_menu (menu_id, parent_id, menu_type, menu_name, order_num, path, component, query, route_name, is_frame, is_cache, visible, status, perms, icon, remark, del_flag, create_by, create_time)
SELECT @menuId + 5, @menuId, 'F', '${functionName!}导出', 5, '', NULL, NULL, NULL, '0', '0', '0', '0', '${permissionPrefix}:export', '#', NULL, '0', 'system', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = '${permissionPrefix}:export' AND del_flag = '0');

-- 管理员角色授权示例（按需取消注释）
-- INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, @menuId);
</#if>
