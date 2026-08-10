/*
 * 字典种子补齐：按 code_formater.md §4.3，为业务枚举字段提供可复用 / 表名_字段名 字典类型。
 * 依赖：V7（sys_normal_disable）、V8（操作/登录日志字典）、V9（is_default 归一 0/1）。
 * 说明：表列 COMMENT 仍因 H2/MySQL 方言差异不在此强制 ALTER；语义以字典 seed + 实体/VO 注解为准。
 */

-- ========== 类型 ==========
INSERT INTO sys_dict_type (dict_id, dict_name, dict_type, status, del_flag, remark, create_time)
SELECT 20, '用户性别', 'sys_user_sex', '0', '0', '用户性别', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE dict_type = 'sys_user_sex');

INSERT INTO sys_dict_type (dict_id, dict_name, dict_type, status, del_flag, remark, create_time)
SELECT 21, '系统是否', 'sys_yes_no', '0', '0', '通用是否：0否 1是', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE dict_type = 'sys_yes_no');

INSERT INTO sys_dict_type (dict_id, dict_name, dict_type, status, del_flag, remark, create_time)
SELECT 22, '菜单类型', 'sys_menu_menu_type', '0', '0', 'M目录 C菜单 F按钮', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE dict_type = 'sys_menu_menu_type');

INSERT INTO sys_dict_type (dict_id, dict_name, dict_type, status, del_flag, remark, create_time)
SELECT 23, '菜单缓存', 'sys_menu_is_cache', '0', '0', '0缓存 1不缓存', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE dict_type = 'sys_menu_is_cache');

INSERT INTO sys_dict_type (dict_id, dict_name, dict_type, status, del_flag, remark, create_time)
SELECT 24, '显示隐藏', 'sys_show_hide', '0', '0', '0显示 1隐藏', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE dict_type = 'sys_show_hide');

INSERT INTO sys_dict_type (dict_id, dict_name, dict_type, status, del_flag, remark, create_time)
SELECT 25, '角色数据范围', 'sys_role_data_scope', '0', '0', '数据权限范围（预留）', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE dict_type = 'sys_role_data_scope');

-- ========== 数据（is_default 用 0/1）==========
INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, list_class, is_default, status, del_flag, create_time)
SELECT 200, 1, '男', '0', 'sys_user_sex', 'default', '1', '0', '0', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_code = 200);
INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, list_class, is_default, status, del_flag, create_time)
SELECT 201, 2, '女', '1', 'sys_user_sex', 'default', '0', '0', '0', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_code = 201);
INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, list_class, is_default, status, del_flag, create_time)
SELECT 202, 3, '未知', '2', 'sys_user_sex', 'info', '0', '0', '0', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_code = 202);

INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, list_class, is_default, status, del_flag, create_time)
SELECT 210, 1, '否', '0', 'sys_yes_no', 'info', '1', '0', '0', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_code = 210);
INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, list_class, is_default, status, del_flag, create_time)
SELECT 211, 2, '是', '1', 'sys_yes_no', 'primary', '0', '0', '0', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_code = 211);

INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, list_class, is_default, status, del_flag, create_time)
SELECT 220, 1, '目录', 'M', 'sys_menu_menu_type', 'primary', '0', '0', '0', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_code = 220);
INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, list_class, is_default, status, del_flag, create_time)
SELECT 221, 2, '菜单', 'C', 'sys_menu_menu_type', 'success', '1', '0', '0', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_code = 221);
INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, list_class, is_default, status, del_flag, create_time)
SELECT 222, 3, '按钮', 'F', 'sys_menu_menu_type', 'info', '0', '0', '0', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_code = 222);

INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, list_class, is_default, status, del_flag, create_time)
SELECT 230, 1, '缓存', '0', 'sys_menu_is_cache', 'success', '1', '0', '0', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_code = 230);
INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, list_class, is_default, status, del_flag, create_time)
SELECT 231, 2, '不缓存', '1', 'sys_menu_is_cache', 'info', '0', '0', '0', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_code = 231);

INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, list_class, is_default, status, del_flag, create_time)
SELECT 240, 1, '显示', '0', 'sys_show_hide', 'primary', '1', '0', '0', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_code = 240);
INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, list_class, is_default, status, del_flag, create_time)
SELECT 241, 2, '隐藏', '1', 'sys_show_hide', 'info', '0', '0', '0', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_code = 241);

INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, list_class, is_default, status, del_flag, create_time)
SELECT 250, 1, '全部数据权限', '1', 'sys_role_data_scope', 'primary', '1', '0', '0', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_code = 250);
INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, list_class, is_default, status, del_flag, create_time)
SELECT 251, 2, '自定数据权限', '2', 'sys_role_data_scope', 'default', '0', '0', '0', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_code = 251);
INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, list_class, is_default, status, del_flag, create_time)
SELECT 252, 3, '本部门数据权限', '3', 'sys_role_data_scope', 'default', '0', '0', '0', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_code = 252);
INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, list_class, is_default, status, del_flag, create_time)
SELECT 253, 4, '本部门及以下数据权限', '4', 'sys_role_data_scope', 'default', '0', '0', '0', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_code = 253);
INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, list_class, is_default, status, del_flag, create_time)
SELECT 254, 5, '仅本人数据权限', '5', 'sys_role_data_scope', 'default', '0', '0', '0', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_code = 254);
