-- 前端监控行为轨迹可视化菜单（与批次页、事件链路并列，挂载系统管理 parent_id=2000）

INSERT INTO sys_menu (menu_id, parent_id, menu_type, menu_name, order_num, path, component, query, route_name, is_frame, is_cache, visible, status, perms, icon, remark, del_flag, create_by, create_time)
VALUES (2257, 2000, 'C', '行为轨迹', 12, 'clientTrackTimeline', 'monitor/clientTrack/timeline', NULL, 'SysClientTrackTimeline', '0', '0', '0', '0', 'monitor:clientTrack:list', 'share', '前端监控行为轨迹可视化（页面跳转 + 操作树）', '0', 'system', CURRENT_TIMESTAMP);

INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 2257);
