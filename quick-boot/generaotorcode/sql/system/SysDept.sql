-- 菜单 SQL

    insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
    values('部门表', 1', '1', 'system', 'system/sysdept/index', 1, 0, 'C', '0', '0', 'system:sysdept:list', '#', 'admin', sysdate(), '', null, '操作日志记录菜单');

    -- 按钮父菜单ID
    SELECT @parentId := LAST_INSERT_ID();

    -- 按钮 SQL
    insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
    values('部门表查询', @parentId, '1',  '#', '', 1, 0, 'F', '0', '0', 'system:sysdept:query',        '#', 'admin', sysdate(), '', null, '');

    insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
    values('部门表新增', @parentId, '2',  '#', '', 1, 0, 'F', '0', '0', 'system:sysdept:add',          '#', 'admin', sysdate(), '', null, '');

    insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
    values('部门表修改', @parentId, '3',  '#', '', 1, 0, 'F', '0', '0', 'system:sysdept:edit',         '#', 'admin', sysdate(), '', null, '');

    insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
    values('部门表删除', @parentId, '4',  '#', '', 1, 0, 'F', '0', '0', 'system:sysdept:remove',       '#', 'admin', sysdate(), '', null, '');

