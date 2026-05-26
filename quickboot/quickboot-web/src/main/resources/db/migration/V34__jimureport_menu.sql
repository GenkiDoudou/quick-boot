-- 积木报表 / JimuBI 菜单（外链 iframe，is_frame=1 表示外链）

INSERT INTO sys_menu (menu_id, parent_id, menu_type, menu_name, order_num, path, component, query, route_name, is_frame, is_cache, visible, status, perms, icon, remark, del_flag, create_by, create_time)
VALUES (3000, -1, 'M', '数据可视化', 50, '/visual', 'Layout', NULL, 'Visual', '0', '0', '0', '0', NULL, 'chart', NULL, '0', 'system', CURRENT_TIMESTAMP);

INSERT INTO sys_menu (menu_id, parent_id, menu_type, menu_name, order_num, path, component, query, route_name, is_frame, is_cache, visible, status, perms, icon, remark, del_flag, create_by, create_time)
VALUES (3001, 3000, 'C', '报表工作台', 1, 'jimu-report', 'InnerLink', '/jmreport/list', 'JimuReportList', '1', '1', '0', '0', 'report:jimu:list', 'documentation', NULL, '0', 'system', CURRENT_TIMESTAMP);

INSERT INTO sys_menu (menu_id, parent_id, menu_type, menu_name, order_num, path, component, query, route_name, is_frame, is_cache, visible, status, perms, icon, remark, del_flag, create_by, create_time)
VALUES (3002, 3000, 'C', 'BI工作台', 2, 'jimu-bi', 'InnerLink', '/drag/list', 'JimuBiList', '1', '1', '0', '0', 'report:jimubi:list', 'dashboard', NULL, '0', 'system', CURRENT_TIMESTAMP);

INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 3000);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 3001);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 3002);
