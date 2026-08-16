/*
 * 监控控制台 v2：停用概览；rum_event.uin；用户行为 / 日志中心菜单。
 * menu_id：2175–2178；overview 2169–2170。
 */

UPDATE sys_menu SET status = '1', menu_name = CONCAT('[下线]', menu_name)
WHERE menu_id IN (2169, 2170)
  AND menu_name NOT LIKE '[下线]%';

DELETE FROM sys_role_menu WHERE menu_id IN (2169, 2170);

ALTER TABLE sys_rum_event
  ADD COLUMN uin VARCHAR(64) NULL COMMENT '用户标识' AFTER session_id;

CREATE INDEX idx_sys_rum_event_uin_time ON sys_rum_event (uin, event_time);
CREATE INDEX idx_sys_rum_event_session_time ON sys_rum_event (session_id, event_time);

INSERT INTO sys_menu (menu_id, parent_id, menu_name, menu_type, path, component, route_name, perms, icon, order_num, status, del_flag, create_time)
SELECT 2175, 2100, '用户行为', 'C', 'userBehavior', 'monitor/userBehavior/index', 'MonitorUserBehavior', 'monitor:userBehavior:query', 'user', 12, '0', '0', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2175);

INSERT INTO sys_menu (menu_id, parent_id, menu_name, menu_type, path, component, route_name, perms, icon, order_num, status, del_flag, create_time)
SELECT 2176, 2175, '用户行为查询', 'F', NULL, NULL, NULL, 'monitor:userBehavior:query', NULL, 1, '0', '0', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2176);

INSERT INTO sys_menu (menu_id, parent_id, menu_name, menu_type, path, component, route_name, perms, icon, order_num, status, del_flag, create_time)
SELECT 2177, 2100, '日志中心', 'C', 'logHub', 'monitor/logHub/index', 'MonitorLogHub', 'monitor:logHub:query', 'documentation', 13, '0', '0', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2177);

INSERT INTO sys_menu (menu_id, parent_id, menu_name, menu_type, path, component, route_name, perms, icon, order_num, status, del_flag, create_time)
SELECT 2178, 2177, '日志中心查询', 'F', NULL, NULL, NULL, 'monitor:logHub:query', NULL, 1, '0', '0', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2178);

INSERT INTO sys_role_menu (role_id, menu_id) SELECT 1, 2175 WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 1 AND menu_id = 2175);
INSERT INTO sys_role_menu (role_id, menu_id) SELECT 1, 2176 WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 1 AND menu_id = 2176);
INSERT INTO sys_role_menu (role_id, menu_id) SELECT 1, 2177 WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 1 AND menu_id = 2177);
INSERT INTO sys_role_menu (role_id, menu_id) SELECT 1, 2178 WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 1 AND menu_id = 2178);
