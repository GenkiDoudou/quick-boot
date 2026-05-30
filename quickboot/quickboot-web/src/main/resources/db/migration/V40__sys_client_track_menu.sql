-- 前端行为监控菜单与按钮权限（挂载系统管理 parent_id=2000）

INSERT INTO sys_menu (menu_id, parent_id, menu_type, menu_name, order_num, path, component, query, route_name, is_frame, is_cache, visible, status, perms, icon, remark, del_flag, create_by, create_time)
VALUES (2253, 2000, 'C', '前端监控', 10, 'clientTrack', 'monitor/clientTrack/index', NULL, 'SysClientTrack', '0', '0', '0', '0', 'monitor:clientTrack:list', 'bug', 'quick-ui 用户行为监控批次查询', '0', 'system', CURRENT_TIMESTAMP);

INSERT INTO sys_menu (menu_id, parent_id, menu_type, menu_name, order_num, path, component, query, route_name, is_frame, is_cache, visible, status, perms, icon, remark, del_flag, create_by, create_time)
VALUES (2254, 2253, 'F', '前端监控查询', 1, '', NULL, NULL, NULL, '0', '0', '0', '0', 'monitor:clientTrack:list', '#', NULL, '0', 'system', CURRENT_TIMESTAMP);

INSERT INTO sys_menu (menu_id, parent_id, menu_type, menu_name, order_num, path, component, query, route_name, is_frame, is_cache, visible, status, perms, icon, remark, del_flag, create_by, create_time)
VALUES (2255, 2253, 'F', '前端监控删除', 2, '', NULL, NULL, NULL, '0', '0', '0', '0', 'monitor:clientTrack:remove', '#', NULL, '0', 'system', CURRENT_TIMESTAMP);

INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 2253);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 2254);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 2255);
