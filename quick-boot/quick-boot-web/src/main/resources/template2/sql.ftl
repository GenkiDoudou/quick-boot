-- 菜单 SQL

<#if  tableEntity.verifyPermission == 'N' >
    INSERT INTO sys_menu ( menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, button_perms, icon, create_by, create_time, update_by, update_time, remark, api_perms)
    VALUES ( '${tableComment!}', ${tableEntity.parentId}, 1, '${classNameLower}', '${moduleName}/${classNameLower}/index', null, '', 1, 0, 'C', '0', '0', '${moduleName}:${classNameLower}:list', '#', '1', sysdate(), '1', null, '', '');
<#else>
    INSERT INTO sys_menu ( menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, button_perms, icon, create_by, create_time, update_by, update_time, remark, api_perms)
    VALUES ( '${tableComment!}', ${tableEntity.parentId}, 1, '${classNameLower}', '${moduleName}/${classNameLower}/index', null, '', 1, 0, 'C', '0', '0', '${moduleName}:${classNameLower}:list', '#', '1', sysdate(), '1', null, '', '${moduleName}:${classNameLower}:list');

    -- 按钮父菜单ID
    SELECT @parentId := LAST_INSERT_ID();


    -- 按钮 SQL
    INSERT INTO sys_menu ( menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, button_perms, icon, create_by, create_time, update_by, update_time, remark, api_perms)
    VALUES ( '${tableComment!}',  @parentId, 4, '#', '', null, '', 1, 0, 'F', '0', '0', '${moduleName}:${classNameLower}:remove', '#', '1', sysdate(), '1', null, '', '${moduleName}:${classNameLower}:remove');
    INSERT INTO sys_menu ( menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, button_perms, icon, create_by, create_time, update_by, update_time, remark, api_perms)
    VALUES ( '${tableComment!}',  @parentId, 3, '#', '', null, '', 1, 0, 'F', '0', '0', '${moduleName}:${classNameLower}:edit', '#', '1', sysdate(), '1', null, '', '${moduleName}:${classNameLower}:edit');
    INSERT INTO sys_menu ( menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, button_perms, icon, create_by, create_time, update_by, update_time, remark, api_perms)
    VALUES ( '${tableComment!}',  @parentId, 2, '#', '', null, '', 1, 0, 'F', '0', '0', '${moduleName}:${classNameLower}:add', '#', '1', sysdate(), '1', null, '', '${moduleName}:${classNameLower}:add');
    INSERT INTO sys_menu ( menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, button_perms, icon, create_by, create_time, update_by, update_time, remark, api_perms)
    VALUES ( '${tableComment!}',  @parentId, 1, '#', '', null, '', 1, 0, 'F', '0', '0', '${moduleName}:${classNameLower}:query', '#', '1', sysdate(), '1', null, '', '${moduleName}:${classNameLower}:query');





</#if>
