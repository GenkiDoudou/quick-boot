-- 菜单 SQL

<#if  tableEntity.verifyPermission == 'N' >
    insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, button_perms,api_perms, icon, create_by, create_time, update_by, update_time, remark)
    values('${tableComment!}', ${tableEntity.parentId}, '1', '${tableEntity.moduleName}', '${moduleName}/${classNameLower}/index', 1, 0, 'C', '0', '0', '', '','#', 'admin', sysdate(), '', null, '操作日志记录菜单');

<#else>
    insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, button_perms,api_perms, icon, create_by, create_time, update_by, update_time, remark)
    values('${tableComment!}', ${tableEntity.parentId}, '1', '${tableEntity.moduleName}', '${moduleName}/${classNameLower}/index', 1, 0, 'C', '0', '0', '${moduleName}:${classNameLower}:list','${moduleName}:${classNameLower}:list', '#', 'admin', sysdate(), '', null, '操作日志记录菜单');

    -- 按钮父菜单ID
    SELECT @parentId := LAST_INSERT_ID();

    -- 按钮 SQL
    insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, button_perms,api_perms, icon, create_by, create_time, update_by, update_time, remark)
    values('${tableComment!}查询', @parentId, '1',  '#', '', 1, 0, 'F', '0', '0', '${moduleName}:${classNameLower}:query', '${moduleName}:${classNameLower}:query',        '#', 'admin', sysdate(), '', null, '');

    insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, button_perms,api_perms, icon, create_by, create_time, update_by, update_time, remark)
    values('${tableComment!}新增', @parentId, '2',  '#', '', 1, 0, 'F', '0', '0', '${moduleName}:${classNameLower}:add', '${moduleName}:${classNameLower}:add',          '#', 'admin', sysdate(), '', null, '');

    insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status,  button_perms,api_perms, icon, create_by, create_time, update_by, update_time, remark)
    values('${tableComment!}修改', @parentId, '3',  '#', '', 1, 0, 'F', '0', '0', '${moduleName}:${classNameLower}:edit','${moduleName}:${classNameLower}:edit',         '#', 'admin', sysdate(), '', null, '');

    insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status,  button_perms,api_perms, icon, create_by, create_time, update_by, update_time, remark)
    values('${tableComment!}删除', @parentId, '4',  '#', '', 1, 0, 'F', '0', '0', '${moduleName}:${classNameLower}:remove','${moduleName}:${classNameLower}:remove',       '#', 'admin', sysdate(), '', null, '');

</#if>
