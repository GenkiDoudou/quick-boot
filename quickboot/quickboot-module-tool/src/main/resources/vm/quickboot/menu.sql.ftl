<#-- 菜单 SQL：需已选择上级目录（parent_menu_id）；menu_id 请按环境调整为未占用 ID -->
<#if !table.parentMenuId??>
-- 未配置上级菜单（目录），请在「编辑生成配置」中选择上级菜单后重新预览/生成。
<#else>
-- ${tableComment!} 菜单（上级目录 menu_id = ${table.parentMenuId}）
-- 权限前缀: ${permissionPrefix}
-- 请将 @menuId 起连续 ID 替换为库内未占用值后再执行

SET @parentId := ${table.parentMenuId};
SET @menuId := 900001;

INSERT INTO sys_menu (menu_id, parent_id, menu_type, menu_name, order_num, path, component, query, route_name, is_frame, is_cache, visible, status, perms, icon, remark, del_flag, create_by, create_time)
VALUES (@menuId, @parentId, 'C', '${functionName!tableComment!}', 1, '${businessName}', '${moduleName}/${businessName}/index', NULL, '${className}', '0', '0', '0', '0', '${permissionPrefix}:list', 'list', NULL, '0', 'system', CURRENT_TIMESTAMP);

INSERT INTO sys_menu (menu_id, parent_id, menu_type, menu_name, order_num, path, component, query, route_name, is_frame, is_cache, visible, status, perms, icon, remark, del_flag, create_by, create_time)
VALUES (@menuId + 1, @menuId, 'F', '${functionName!}新增', 1, '', NULL, NULL, NULL, '0', '0', '0', '0', '${permissionPrefix}:add', '#', NULL, '0', 'system', CURRENT_TIMESTAMP);

INSERT INTO sys_menu (menu_id, parent_id, menu_type, menu_name, order_num, path, component, query, route_name, is_frame, is_cache, visible, status, perms, icon, remark, del_flag, create_by, create_time)
VALUES (@menuId + 2, @menuId, 'F', '${functionName!}修改', 2, '', NULL, NULL, NULL, '0', '0', '0', '0', '${permissionPrefix}:edit', '#', NULL, '0', 'system', CURRENT_TIMESTAMP);

INSERT INTO sys_menu (menu_id, parent_id, menu_type, menu_name, order_num, path, component, query, route_name, is_frame, is_cache, visible, status, perms, icon, remark, del_flag, create_by, create_time)
VALUES (@menuId + 3, @menuId, 'F', '${functionName!}删除', 3, '', NULL, NULL, NULL, '0', '0', '0', '0', '${permissionPrefix}:remove', '#', NULL, '0', 'system', CURRENT_TIMESTAMP);

INSERT INTO sys_menu (menu_id, parent_id, menu_type, menu_name, order_num, path, component, query, route_name, is_frame, is_cache, visible, status, perms, icon, remark, del_flag, create_by, create_time)
VALUES (@menuId + 4, @menuId, 'F', '${functionName!}查询', 4, '', NULL, NULL, NULL, '0', '0', '0', '0', '${permissionPrefix}:query', '#', NULL, '0', 'system', CURRENT_TIMESTAMP);

INSERT INTO sys_menu (menu_id, parent_id, menu_type, menu_name, order_num, path, component, query, route_name, is_frame, is_cache, visible, status, perms, icon, remark, del_flag, create_by, create_time)
VALUES (@menuId + 5, @menuId, 'F', '${functionName!}导出', 5, '', NULL, NULL, NULL, '0', '0', '0', '0', '${permissionPrefix}:export', '#', NULL, '0', 'system', CURRENT_TIMESTAMP);

-- 管理员角色授权示例（按需调整 role_id）
-- INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, @menuId);
</#if>
