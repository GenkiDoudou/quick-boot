-- 参数设置（系统管理下）；顶级「组件演示」目录及 C7 E2E 子菜单；超级管理员授权。
-- 主键 204x 与 V8 用户菜单错开；210x 为 Demo 目录与子菜单。

INSERT INTO sys_menu (menu_id, parent_id, menu_type, menu_name, order_num, path, component, query, route_name, is_frame, is_cache, visible, status, perms, icon, remark, del_flag, create_by, create_time)
VALUES (2040, 2000, 'C', '参数设置', 6, 'config', 'system/config/index', NULL, 'SysConfig', '0', '0', '0', '0', 'system:config:list', 'tools', NULL, '0', 'system', CURRENT_TIMESTAMP);

INSERT INTO sys_menu (menu_id, parent_id, menu_type, menu_name, order_num, path, component, query, route_name, is_frame, is_cache, visible, status, perms, icon, remark, del_flag, create_by, create_time)
VALUES (2100, -1, 'M', '组件演示', 2, '/demo', 'Layout', NULL, 'Demo', '0', '0', '0', '0', NULL, 'component', NULL, '0', 'system', CURRENT_TIMESTAMP);

INSERT INTO sys_menu (menu_id, parent_id, menu_type, menu_name, order_num, path, component, query, route_name, is_frame, is_cache, visible, status, perms, icon, remark, del_flag, create_by, create_time)
VALUES (2101, 2100, 'C', 'C7Button E2E', 1, 'c7-button-e2e', 'dev/C7ButtonE2E', NULL, 'C7ButtonE2E', '0', '1', '0', '0', NULL, 'button', NULL, '0', 'system', CURRENT_TIMESTAMP);

INSERT INTO sys_menu (menu_id, parent_id, menu_type, menu_name, order_num, path, component, query, route_name, is_frame, is_cache, visible, status, perms, icon, remark, del_flag, create_by, create_time)
VALUES (2102, 2100, 'C', 'C7Select Dev', 2, 'c7-select-e2e', 'dev/C7SelectE2E', NULL, 'C7SelectE2E', '0', '1', '0', '0', NULL, 'select', NULL, '0', 'system', CURRENT_TIMESTAMP);

INSERT INTO sys_menu (menu_id, parent_id, menu_type, menu_name, order_num, path, component, query, route_name, is_frame, is_cache, visible, status, perms, icon, remark, del_flag, create_by, create_time)
VALUES (2103, 2100, 'C', 'C7Cascader Dev', 3, 'c7-cascader-e2e', 'dev/C7CascaderE2E', NULL, 'C7CascaderE2E', '0', '1', '0', '0', NULL, 'cascader', NULL, '0', 'system', CURRENT_TIMESTAMP);

INSERT INTO sys_menu (menu_id, parent_id, menu_type, menu_name, order_num, path, component, query, route_name, is_frame, is_cache, visible, status, perms, icon, remark, del_flag, create_by, create_time)
VALUES (2104, 2100, 'C', 'C7Pagination Dev', 4, 'c7-pagination-e2e', 'dev/C7PaginationE2E', NULL, 'C7PaginationE2E', '0', '1', '1', '0', NULL, 'documentation', NULL, '0', 'system', CURRENT_TIMESTAMP);

INSERT INTO sys_menu (menu_id, parent_id, menu_type, menu_name, order_num, path, component, query, route_name, is_frame, is_cache, visible, status, perms, icon, remark, del_flag, create_by, create_time)
VALUES (2105, 2100, 'C', 'C7Copy Dev', 5, 'c7-copy-e2e', 'dev/C7CopyE2E', NULL, 'C7CopyE2E', '0', '1', '1', '0', NULL, 'documentation', NULL, '0', 'system', CURRENT_TIMESTAMP);

INSERT INTO sys_menu (menu_id, parent_id, menu_type, menu_name, order_num, path, component, query, route_name, is_frame, is_cache, visible, status, perms, icon, remark, del_flag, create_by, create_time)
VALUES (2106, 2100, 'C', 'C7Card Dev', 6, 'c7-card-e2e', 'dev/C7CardE2E', NULL, 'C7CardE2E', '0', '1', '1', '0', NULL, 'documentation', NULL, '0', 'system', CURRENT_TIMESTAMP);

INSERT INTO sys_menu (menu_id, parent_id, menu_type, menu_name, order_num, path, component, query, route_name, is_frame, is_cache, visible, status, perms, icon, remark, del_flag, create_by, create_time)
VALUES (2107, 2100, 'C', 'C7Checkbox Dev', 7, 'c7-checkbox-e2e', 'dev/C7CheckboxE2E', NULL, 'C7CheckboxE2E', '0', '1', '1', '0', NULL, 'documentation', NULL, '0', 'system', CURRENT_TIMESTAMP);

INSERT INTO sys_menu (menu_id, parent_id, menu_type, menu_name, order_num, path, component, query, route_name, is_frame, is_cache, visible, status, perms, icon, remark, del_flag, create_by, create_time)
VALUES (2108, 2100, 'C', 'C7Radio Dev', 8, 'c7-radio-e2e', 'dev/C7RadioE2E', NULL, 'C7RadioE2E', '0', '1', '1', '0', NULL, 'documentation', NULL, '0', 'system', CURRENT_TIMESTAMP);

INSERT INTO sys_menu (menu_id, parent_id, menu_type, menu_name, order_num, path, component, query, route_name, is_frame, is_cache, visible, status, perms, icon, remark, del_flag, create_by, create_time)
VALUES (2109, 2100, 'C', 'C7Switch Dev', 9, 'c7-switch-e2e', 'dev/C7SwitchE2E', NULL, 'C7SwitchE2E', '0', '1', '1', '0', NULL, 'documentation', NULL, '0', 'system', CURRENT_TIMESTAMP);

INSERT INTO sys_menu (menu_id, parent_id, menu_type, menu_name, order_num, path, component, query, route_name, is_frame, is_cache, visible, status, perms, icon, remark, del_flag, create_by, create_time)
VALUES (2110, 2100, 'C', 'C7DatePicker Dev', 10, 'c7-datepicker-e2e', 'dev/C7DatePickerE2E', NULL, 'C7DatePickerE2E', '0', '1', '1', '0', NULL, 'documentation', NULL, '0', 'system', CURRENT_TIMESTAMP);

INSERT INTO sys_menu (menu_id, parent_id, menu_type, menu_name, order_num, path, component, query, route_name, is_frame, is_cache, visible, status, perms, icon, remark, del_flag, create_by, create_time)
VALUES (2111, 2100, 'C', 'C7TimePicker Dev', 11, 'c7-timepicker-e2e', 'dev/C7TimePickerE2E', NULL, 'C7TimePickerE2E', '0', '1', '1', '0', NULL, 'documentation', NULL, '0', 'system', CURRENT_TIMESTAMP);

INSERT INTO sys_menu (menu_id, parent_id, menu_type, menu_name, order_num, path, component, query, route_name, is_frame, is_cache, visible, status, perms, icon, remark, del_flag, create_by, create_time)
VALUES (2112, 2100, 'C', 'C7Title Dev', 12, 'c7-title-e2e', 'dev/C7TitleE2E', NULL, 'C7TitleE2E', '0', '1', '1', '0', NULL, 'documentation', NULL, '0', 'system', CURRENT_TIMESTAMP);

INSERT INTO sys_menu (menu_id, parent_id, menu_type, menu_name, order_num, path, component, query, route_name, is_frame, is_cache, visible, status, perms, icon, remark, del_flag, create_by, create_time)
VALUES (2113, 2100, 'C', 'C7Dialog Dev', 13, 'c7-dialog-e2e', 'dev/C7DialogE2E', NULL, 'C7DialogE2E', '0', '1', '1', '0', NULL, 'documentation', NULL, '0', 'system', CURRENT_TIMESTAMP);

INSERT INTO sys_menu (menu_id, parent_id, menu_type, menu_name, order_num, path, component, query, route_name, is_frame, is_cache, visible, status, perms, icon, remark, del_flag, create_by, create_time)
VALUES (2114, 2100, 'C', 'C7DictTag Dev', 14, 'c7-dict-tag-e2e', 'dev/C7DictTagE2E', NULL, 'C7DictTagE2E', '0', '1', '1', '0', NULL, 'documentation', NULL, '0', 'system', CURRENT_TIMESTAMP);

INSERT INTO sys_menu (menu_id, parent_id, menu_type, menu_name, order_num, path, component, query, route_name, is_frame, is_cache, visible, status, perms, icon, remark, del_flag, create_by, create_time)
VALUES (2115, 2100, 'C', 'C7Watermark Dev', 15, 'c7-watermark-e2e', 'dev/C7WatermarkE2E', NULL, 'C7WatermarkE2E', '0', '1', '1', '0', NULL, 'documentation', NULL, '0', 'system', CURRENT_TIMESTAMP);

INSERT INTO sys_menu (menu_id, parent_id, menu_type, menu_name, order_num, path, component, query, route_name, is_frame, is_cache, visible, status, perms, icon, remark, del_flag, create_by, create_time)
VALUES (2116, 2100, 'C', 'C7Preview Dev', 16, 'c7-preview-e2e', 'dev/C7PreviewE2E', NULL, 'C7PreviewE2E', '0', '1', '1', '0', NULL, 'documentation', NULL, '0', 'system', CURRENT_TIMESTAMP);

INSERT INTO sys_menu (menu_id, parent_id, menu_type, menu_name, order_num, path, component, query, route_name, is_frame, is_cache, visible, status, perms, icon, remark, del_flag, create_by, create_time)
VALUES (2117, 2100, 'C', 'C7JsonTableColumn Dev', 17, 'c7-json-table-column-e2e', 'dev/C7JsonTableColumnE2E', NULL, 'C7JsonTableColumnE2E', '0', '1', '1', '0', NULL, 'documentation', NULL, '0', 'system', CURRENT_TIMESTAMP);

INSERT INTO sys_menu (menu_id, parent_id, menu_type, menu_name, order_num, path, component, query, route_name, is_frame, is_cache, visible, status, perms, icon, remark, del_flag, create_by, create_time)
VALUES (2118, 2100, 'C', 'C7JsonTable Dev', 18, 'c7-json-table-e2e', 'dev/C7JsonTableE2E', NULL, 'C7JsonTableE2E', '0', '1', '1', '0', NULL, 'documentation', NULL, '0', 'system', CURRENT_TIMESTAMP);

INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 2040);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 2100);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 2101);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 2102);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 2103);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 2104);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 2105);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 2106);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 2107);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 2108);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 2109);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 2110);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 2111);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 2112);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 2113);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 2114);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 2115);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 2116);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 2117);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 2118);
