-- OAuth2 管理菜单（系统管理 parent_id=1000）

INSERT INTO sys_menu (menu_id, parent_id, menu_type, menu_name, order_num, path, component, query, route_name, is_frame, is_cache, visible, status, perms, icon, remark, del_flag, create_by, create_time)
VALUES (2260, 1000, 'C', 'OAuth客户端', 12, 'oauthClient', 'system/oauthClient/index', NULL, 'SysOauthClient', '0', '0', '0', '0', 'system:oauthClient:list', 'link', NULL, '0', 'system', CURRENT_TIMESTAMP);

INSERT INTO sys_menu (menu_id, parent_id, menu_type, menu_name, order_num, path, component, query, route_name, is_frame, is_cache, visible, status, perms, icon, remark, del_flag, create_by, create_time)
VALUES (2261, 2260, 'F', 'OAuth客户端查询', 1, '', NULL, NULL, NULL, '0', '0', '0', '0', 'system:oauthClient:query', '#', NULL, '0', 'system', CURRENT_TIMESTAMP);
INSERT INTO sys_menu (menu_id, parent_id, menu_type, menu_name, order_num, path, component, query, route_name, is_frame, is_cache, visible, status, perms, icon, remark, del_flag, create_by, create_time)
VALUES (2262, 2260, 'F', 'OAuth客户端新增', 2, '', NULL, NULL, NULL, '0', '0', '0', '0', 'system:oauthClient:add', '#', NULL, '0', 'system', CURRENT_TIMESTAMP);
INSERT INTO sys_menu (menu_id, parent_id, menu_type, menu_name, order_num, path, component, query, route_name, is_frame, is_cache, visible, status, perms, icon, remark, del_flag, create_by, create_time)
VALUES (2263, 2260, 'F', 'OAuth客户端修改', 3, '', NULL, NULL, NULL, '0', '0', '0', '0', 'system:oauthClient:edit', '#', NULL, '0', 'system', CURRENT_TIMESTAMP);
INSERT INTO sys_menu (menu_id, parent_id, menu_type, menu_name, order_num, path, component, query, route_name, is_frame, is_cache, visible, status, perms, icon, remark, del_flag, create_by, create_time)
VALUES (2264, 2260, 'F', 'OAuth客户端删除', 4, '', NULL, NULL, NULL, '0', '0', '0', '0', 'system:oauthClient:remove', '#', NULL, '0', 'system', CURRENT_TIMESTAMP);

INSERT INTO sys_menu (menu_id, parent_id, menu_type, menu_name, order_num, path, component, query, route_name, is_frame, is_cache, visible, status, perms, icon, remark, del_flag, create_by, create_time)
VALUES (2270, 1000, 'C', '外部IdP', 13, 'oauthProvider', 'system/oauthProvider/index', NULL, 'SysOauthProvider', '0', '0', '0', '0', 'system:oauthProvider:list', 'guide', NULL, '0', 'system', CURRENT_TIMESTAMP);

INSERT INTO sys_menu (menu_id, parent_id, menu_type, menu_name, order_num, path, component, query, route_name, is_frame, is_cache, visible, status, perms, icon, remark, del_flag, create_by, create_time)
VALUES (2271, 2270, 'F', '外部IdP查询', 1, '', NULL, NULL, NULL, '0', '0', '0', '0', 'system:oauthProvider:query', '#', NULL, '0', 'system', CURRENT_TIMESTAMP);
INSERT INTO sys_menu (menu_id, parent_id, menu_type, menu_name, order_num, path, component, query, route_name, is_frame, is_cache, visible, status, perms, icon, remark, del_flag, create_by, create_time)
VALUES (2272, 2270, 'F', '外部IdP新增', 2, '', NULL, NULL, NULL, '0', '0', '0', '0', 'system:oauthProvider:add', '#', NULL, '0', 'system', CURRENT_TIMESTAMP);
INSERT INTO sys_menu (menu_id, parent_id, menu_type, menu_name, order_num, path, component, query, route_name, is_frame, is_cache, visible, status, perms, icon, remark, del_flag, create_by, create_time)
VALUES (2273, 2270, 'F', '外部IdP修改', 3, '', NULL, NULL, NULL, '0', '0', '0', '0', 'system:oauthProvider:edit', '#', NULL, '0', 'system', CURRENT_TIMESTAMP);
INSERT INTO sys_menu (menu_id, parent_id, menu_type, menu_name, order_num, path, component, query, route_name, is_frame, is_cache, visible, status, perms, icon, remark, del_flag, create_by, create_time)
VALUES (2274, 2270, 'F', '外部IdP删除', 4, '', NULL, NULL, NULL, '0', '0', '0', '0', 'system:oauthProvider:remove', '#', NULL, '0', 'system', CURRENT_TIMESTAMP);

INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 2260);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 2261);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 2262);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 2263);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 2264);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 2270);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 2271);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 2272);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 2273);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 2274);
