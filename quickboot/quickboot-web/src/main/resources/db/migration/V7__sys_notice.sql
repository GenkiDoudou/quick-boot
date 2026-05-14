-- 通知公告表、字典种子、菜单与授权（版本独立，避免与 V6 角色菜单主键冲突）

CREATE TABLE IF NOT EXISTS sys_notice (
    notice_id BIGINT NOT NULL PRIMARY KEY,
    notice_title VARCHAR(50) NOT NULL,
    notice_type CHAR(1) NOT NULL,
    notice_content LONGTEXT NULL,
    status CHAR(1) NOT NULL DEFAULT '0',
    create_by VARCHAR(64) NULL,
    create_time DATETIME NULL DEFAULT CURRENT_TIMESTAMP,
    update_by VARCHAR(64) NULL,
    update_time DATETIME NULL
);

CREATE INDEX IF NOT EXISTS idx_sys_notice_create_time ON sys_notice (create_time);

INSERT INTO sys_dict_type (dict_id, dict_name, dict_type, status, remark, del_flag, create_by, create_time)
VALUES (800001, '通知公告类型', 'sys_notice_type', '0', 'Flyway 种子：通知/公告', '0', 'system', CURRENT_TIMESTAMP);

INSERT INTO sys_dict_type (dict_id, dict_name, dict_type, status, remark, del_flag, create_by, create_time)
VALUES (800002, '通知公告状态', 'sys_notice_status', '0', 'Flyway 种子：正常/关闭', '0', 'system', CURRENT_TIMESTAMP);

INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, status, remark, del_flag, create_by, create_time)
VALUES (800010, 1, '通知', '1', 'sys_notice_type', NULL, 'default', '0', NULL, '0', 'system', CURRENT_TIMESTAMP);

INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, status, remark, del_flag, create_by, create_time)
VALUES (800011, 2, '公告', '2', 'sys_notice_type', NULL, 'default', '0', NULL, '0', 'system', CURRENT_TIMESTAMP);

INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, status, remark, del_flag, create_by, create_time)
VALUES (800020, 1, '正常', '0', 'sys_notice_status', NULL, 'success', '0', NULL, '0', 'system', CURRENT_TIMESTAMP);

INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, status, remark, del_flag, create_by, create_time)
VALUES (800021, 2, '关闭', '1', 'sys_notice_status', NULL, 'info', '0', NULL, '0', 'system', CURRENT_TIMESTAMP);

-- 菜单主键 202x，避免与 V6 角色管理 2010–2015 冲突
INSERT INTO sys_menu (menu_id, parent_id, menu_type, menu_name, order_num, path, component, query, route_name, is_frame, is_cache, visible, status, perms, icon, remark, del_flag, create_by, create_time)
VALUES (2020, 2000, 'C', '通知公告', 3, 'notice', 'system/notice/index', NULL, 'SysNotice', '0', '0', '0', '0', 'system:notice:list', 'message', NULL, '0', 'system', CURRENT_TIMESTAMP);

INSERT INTO sys_menu (menu_id, parent_id, menu_type, menu_name, order_num, path, component, query, route_name, is_frame, is_cache, visible, status, perms, icon, remark, del_flag, create_by, create_time)
VALUES (2021, 2020, 'F', '通知公告新增', 1, '', NULL, NULL, NULL, '0', '0', '0', '0', 'system:notice:add', '#', NULL, '0', 'system', CURRENT_TIMESTAMP);

INSERT INTO sys_menu (menu_id, parent_id, menu_type, menu_name, order_num, path, component, query, route_name, is_frame, is_cache, visible, status, perms, icon, remark, del_flag, create_by, create_time)
VALUES (2022, 2020, 'F', '通知公告修改', 2, '', NULL, NULL, NULL, '0', '0', '0', '0', 'system:notice:edit', '#', NULL, '0', 'system', CURRENT_TIMESTAMP);

INSERT INTO sys_menu (menu_id, parent_id, menu_type, menu_name, order_num, path, component, query, route_name, is_frame, is_cache, visible, status, perms, icon, remark, del_flag, create_by, create_time)
VALUES (2023, 2020, 'F', '通知公告删除', 3, '', NULL, NULL, NULL, '0', '0', '0', '0', 'system:notice:remove', '#', NULL, '0', 'system', CURRENT_TIMESTAMP);

INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 2020);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 2021);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 2022);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 2023);
