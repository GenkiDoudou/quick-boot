-- 操作日志表、字典、菜单（与 V15 登录日志/参数按钮迁移序号不冲突）

CREATE TABLE IF NOT EXISTS sys_oper_log (
    oper_id          BIGINT         NOT NULL PRIMARY KEY,
    title            VARCHAR(100)   NOT NULL DEFAULT '',
    business_type    INT            NOT NULL DEFAULT 0,
    method           VARCHAR(255)   NOT NULL DEFAULT '',
    request_method   VARCHAR(20)    NOT NULL DEFAULT '',
    operator_type    INT            NOT NULL DEFAULT 0,
    oper_name        VARCHAR(64)    NOT NULL DEFAULT '',
    dept_name        VARCHAR(100)   NOT NULL DEFAULT '',
    oper_url         VARCHAR(500)   NOT NULL DEFAULT '',
    oper_ip          VARCHAR(128)   NOT NULL DEFAULT '',
    oper_location    VARCHAR(255)   NULL DEFAULT '',
    oper_param       VARCHAR(4000)  NULL,
    json_result      VARCHAR(4000)  NULL,
    status           INT            NOT NULL DEFAULT 0,
    error_msg        VARCHAR(4000)  NULL,
    oper_time        DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    cost_time        BIGINT         NOT NULL DEFAULT 0,
    trace_id         VARCHAR(64)    NULL
);

CREATE INDEX idx_sys_oper_log_oper_time ON sys_oper_log (oper_time);
CREATE INDEX idx_sys_oper_log_trace_id ON sys_oper_log (trace_id);
CREATE INDEX idx_sys_oper_log_oper_name ON sys_oper_log (oper_name);

INSERT INTO sys_dict_type (dict_id, dict_name, dict_type, status, remark, del_flag, create_by, create_time)
VALUES (800004, '操作业务类型', 'sys_oper_business_type', '0', 'Flyway：操作日志业务类型', '0', 'system', CURRENT_TIMESTAMP);

INSERT INTO sys_dict_type (dict_id, dict_name, dict_type, status, remark, del_flag, create_by, create_time)
VALUES (800005, '操作类别', 'sys_oper_operator_type', '0', 'Flyway：操作者类别', '0', 'system', CURRENT_TIMESTAMP);

INSERT INTO sys_dict_type (dict_id, dict_name, dict_type, status, remark, del_flag, create_by, create_time)
VALUES (800006, '操作状态', 'sys_oper_status', '0', 'Flyway：操作成功/异常', '0', 'system', CURRENT_TIMESTAMP);

INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, status, remark, del_flag, create_by, create_time)
VALUES (800040, 1, '其它', '0', 'sys_oper_business_type', NULL, 'info', '0', NULL, '0', 'system', CURRENT_TIMESTAMP);

INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, status, remark, del_flag, create_by, create_time)
VALUES (800041, 2, '新增', '1', 'sys_oper_business_type', NULL, 'primary', '0', NULL, '0', 'system', CURRENT_TIMESTAMP);

INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, status, remark, del_flag, create_by, create_time)
VALUES (800042, 3, '修改', '2', 'sys_oper_business_type', NULL, 'warning', '0', NULL, '0', 'system', CURRENT_TIMESTAMP);

INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, status, remark, del_flag, create_by, create_time)
VALUES (800043, 4, '删除', '3', 'sys_oper_business_type', NULL, 'danger', '0', NULL, '0', 'system', CURRENT_TIMESTAMP);

INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, status, remark, del_flag, create_by, create_time)
VALUES (800044, 5, '导出', '4', 'sys_oper_business_type', NULL, 'default', '0', NULL, '0', 'system', CURRENT_TIMESTAMP);

INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, status, remark, del_flag, create_by, create_time)
VALUES (800050, 1, '其它', '0', 'sys_oper_operator_type', NULL, 'info', '0', NULL, '0', 'system', CURRENT_TIMESTAMP);

INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, status, remark, del_flag, create_by, create_time)
VALUES (800051, 2, '后台用户', '1', 'sys_oper_operator_type', NULL, 'primary', '0', NULL, '0', 'system', CURRENT_TIMESTAMP);

INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, status, remark, del_flag, create_by, create_time)
VALUES (800052, 3, '手机端', '2', 'sys_oper_operator_type', NULL, 'success', '0', NULL, '0', 'system', CURRENT_TIMESTAMP);

INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, status, remark, del_flag, create_by, create_time)
VALUES (800060, 1, '正常', '0', 'sys_oper_status', NULL, 'success', '0', NULL, '0', 'system', CURRENT_TIMESTAMP);

INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, status, remark, del_flag, create_by, create_time)
VALUES (800061, 2, '异常', '1', 'sys_oper_status', NULL, 'danger', '0', NULL, '0', 'system', CURRENT_TIMESTAMP);

INSERT INTO sys_menu (menu_id, parent_id, menu_type, menu_name, order_num, path, component, query, route_name, is_frame, is_cache, visible, status, perms, icon, remark, del_flag, create_by, create_time)
VALUES (2230, 2000, 'C', '操作日志', 9, 'operlog', 'monitor/operlog/index', NULL, 'SysOperLog', '0', '0', '0', '0', 'monitor:operlog:query', 'log', NULL, '0', 'system', CURRENT_TIMESTAMP);

INSERT INTO sys_menu (menu_id, parent_id, menu_type, menu_name, order_num, path, component, query, route_name, is_frame, is_cache, visible, status, perms, icon, remark, del_flag, create_by, create_time)
VALUES (2231, 2230, 'F', '操作日志删除', 1, '', NULL, NULL, NULL, '0', '0', '0', '0', 'monitor:operlog:remove', '#', NULL, '0', 'system', CURRENT_TIMESTAMP);

INSERT INTO sys_menu (menu_id, parent_id, menu_type, menu_name, order_num, path, component, query, route_name, is_frame, is_cache, visible, status, perms, icon, remark, del_flag, create_by, create_time)
VALUES (2232, 2230, 'F', '操作日志导出', 2, '', NULL, NULL, NULL, '0', '0', '0', '0', 'monitor:operlog:export', '#', NULL, '0', 'system', CURRENT_TIMESTAMP);

INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 2230);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 2231);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 2232);
