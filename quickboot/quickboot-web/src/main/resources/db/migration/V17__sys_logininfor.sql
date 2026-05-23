-- 登录访问日志表、登录状态字典、登录锁定内置参数、登录日志菜单与授权（V17：避免与 V15__sys_config_button_menus 版本冲突）

CREATE TABLE IF NOT EXISTS sys_logininfor (
    info_id         BIGINT       NOT NULL PRIMARY KEY,
    user_id         BIGINT       NULL,
    user_name       VARCHAR(64)  NOT NULL,
    ipaddr          VARCHAR(128) NULL,
    login_location  VARCHAR(255) NULL,
    browser         VARCHAR(100) NULL,
    os              VARCHAR(100) NULL,
    status          CHAR(1)      NOT NULL,
    msg             VARCHAR(512) NULL,
    login_time      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_sys_logininfor_login_time ON sys_logininfor (login_time);
CREATE INDEX idx_sys_logininfor_user_name ON sys_logininfor (user_name);

INSERT INTO sys_dict_type (dict_id, dict_name, dict_type, status, remark, del_flag, create_by, create_time)
VALUES (800003, '登录状态', 'sys_login_status', '0', 'Flyway：登录日志成功/失败', '0', 'system', CURRENT_TIMESTAMP);

INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, status, remark, del_flag, create_by, create_time)
VALUES (800032, 1, '成功', '0', 'sys_login_status', NULL, 'success', '0', NULL, '0', 'system', CURRENT_TIMESTAMP);

INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, status, remark, del_flag, create_by, create_time)
VALUES (800033, 2, '失败', '1', 'sys_login_status', NULL, 'danger', '0', NULL, '0', 'system', CURRENT_TIMESTAMP);

INSERT INTO sys_config (config_id, config_name, config_key, config_value, config_type, remark, del_flag, create_by, create_time)
VALUES (900001, '登录失败锁定开关', 'qc.login.fail-lock-enabled', 'true', '1', 'true 开启失败锁定；false 关闭', '0', 'system', CURRENT_TIMESTAMP);

INSERT INTO sys_config (config_id, config_name, config_key, config_value, config_type, remark, del_flag, create_by, create_time)
VALUES (900002, '登录失败锁定阈值', 'qc.login.max-retry', '5', '1', '连续失败次数达到后锁定', '0', 'system', CURRENT_TIMESTAMP);

INSERT INTO sys_config (config_id, config_name, config_key, config_value, config_type, remark, del_flag, create_by, create_time)
VALUES (900003, '登录锁定时长(分钟)', 'qc.login.lock-minutes', '30', '1', '锁定持续时间', '0', 'system', CURRENT_TIMESTAMP);

INSERT INTO sys_menu (menu_id, parent_id, menu_type, menu_name, order_num, path, component, query, route_name, is_frame, is_cache, visible, status, perms, icon, remark, del_flag, create_by, create_time)
VALUES (2220, 2000, 'C', '登录日志', 8, 'logininfor', 'monitor/logininfor/index', NULL, 'SysLogininfor', '0', '0', '0', '0', 'monitor:logininfor:list', 'documentation', NULL, '0', 'system', CURRENT_TIMESTAMP);

INSERT INTO sys_menu (menu_id, parent_id, menu_type, menu_name, order_num, path, component, query, route_name, is_frame, is_cache, visible, status, perms, icon, remark, del_flag, create_by, create_time)
VALUES (2221, 2220, 'F', '登录日志删除', 1, '', NULL, NULL, NULL, '0', '0', '0', '0', 'monitor:logininfor:remove', '#', NULL, '0', 'system', CURRENT_TIMESTAMP);

INSERT INTO sys_menu (menu_id, parent_id, menu_type, menu_name, order_num, path, component, query, route_name, is_frame, is_cache, visible, status, perms, icon, remark, del_flag, create_by, create_time)
VALUES (2222, 2220, 'F', '登录日志导出', 2, '', NULL, NULL, NULL, '0', '0', '0', '0', 'monitor:logininfor:export', '#', NULL, '0', 'system', CURRENT_TIMESTAMP);

INSERT INTO sys_menu (menu_id, parent_id, menu_type, menu_name, order_num, path, component, query, route_name, is_frame, is_cache, visible, status, perms, icon, remark, del_flag, create_by, create_time)
VALUES (2223, 2220, 'F', '账户解锁', 3, '', NULL, NULL, NULL, '0', '0', '0', '0', 'monitor:logininfor:unlock', '#', NULL, '0', 'system', CURRENT_TIMESTAMP);

INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 2220);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 2221);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 2222);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 2223);
