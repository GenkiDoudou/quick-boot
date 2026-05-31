-- 前端监控事件链路明细菜单（与「前端监控」批次页并列，挂载系统管理 parent_id=2000）

INSERT INTO sys_menu (menu_id, parent_id, menu_type, menu_name, order_num, path, component, query, route_name, is_frame, is_cache, visible, status, perms, icon, remark, del_flag, create_by, create_time)
VALUES (2256, 2000, 'C', '事件链路', 11, 'clientTrackEvents', 'monitor/clientTrack/events', NULL, 'SysClientTrackEvents', '0', '0', '0', '0', 'monitor:clientTrack:list', 'guide', '前端监控批次展开的事件明细', '0', 'system', CURRENT_TIMESTAMP);

INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 2256);
