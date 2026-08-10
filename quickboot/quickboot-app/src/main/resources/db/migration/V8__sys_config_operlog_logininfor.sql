-- 参数配置 / 操作日志 / 登录日志：表 + 字典种子 + 菜单权限

CREATE TABLE IF NOT EXISTS sys_config (
  config_id    BIGINT       NOT NULL,
  config_name  VARCHAR(100) NOT NULL,
  config_key   VARCHAR(100) NOT NULL,
  config_value VARCHAR(500),
  config_type  CHAR(1)      NOT NULL DEFAULT '0',
  del_flag     CHAR(1)      NOT NULL DEFAULT '0',
  remark       VARCHAR(500),
  create_by    VARCHAR(64),
  create_time  TIMESTAMP    NULL,
  update_by    VARCHAR(64),
  update_time  TIMESTAMP    NULL,
  PRIMARY KEY (config_id),
  CONSTRAINT uk_sys_config_key UNIQUE (config_key)
);

CREATE TABLE IF NOT EXISTS sys_oper_log (
  oper_id              BIGINT        NOT NULL,
  title                VARCHAR(100),
  business_type        INT           NOT NULL DEFAULT 0,
  method               VARCHAR(200),
  request_method       VARCHAR(16),
  operator_type        INT           NOT NULL DEFAULT 0,
  oper_name            VARCHAR(64),
  dept_name            VARCHAR(64),
  oper_url             VARCHAR(500),
  oper_ip              VARCHAR(128),
  oper_location        VARCHAR(255),
  oper_param           VARCHAR(4000),
  json_result          VARCHAR(4000),
  status               INT           NOT NULL DEFAULT 0,
  error_msg            VARCHAR(4000),
  oper_time            TIMESTAMP     NULL,
  cost_time            BIGINT,
  trace_id             VARCHAR(64),
  client_operation_id  VARCHAR(64),
  client_id            VARCHAR(64),
  PRIMARY KEY (oper_id)
);

CREATE TABLE IF NOT EXISTS sys_logininfor (
  info_id         BIGINT       NOT NULL,
  user_id         BIGINT,
  user_name       VARCHAR(64),
  client_id       VARCHAR(64),
  ipaddr          VARCHAR(128),
  login_location  VARCHAR(255),
  browser         VARCHAR(64),
  os              VARCHAR(64),
  status          CHAR(1)      NOT NULL DEFAULT '0',
  msg             VARCHAR(500),
  login_time      TIMESTAMP    NULL,
  PRIMARY KEY (info_id)
);

-- 配置种子
INSERT INTO sys_config (config_id, config_name, config_key, config_value, config_type, del_flag, remark, create_time)
SELECT 1, '用户管理-账号初始密码', 'sys.user.initPassword', 'admin123', '1', '0', '初始化密码', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_config WHERE config_id = 1);

INSERT INTO sys_config (config_id, config_name, config_key, config_value, config_type, del_flag, remark, create_time)
SELECT 2, '账号自助-验证码开关', 'sys.account.captchaEnabled', 'true', '1', '0', '是否开启验证码', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_config WHERE config_id = 2);

INSERT INTO sys_config (config_id, config_name, config_key, config_value, config_type, del_flag, remark, create_time)
SELECT 3, '操作日志采集开关', 'qc.monitor.operlog.capture-enabled', 'true', '0', '0', '宽切面开关（亦可用 yml）', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_config WHERE config_id = 3);

-- 字典类型
INSERT INTO sys_dict_type (dict_id, dict_name, dict_type, status, del_flag, remark, create_time)
SELECT 10, '操作状态', 'sys_oper_status', '0', '0', '操作日志状态', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE dict_type = 'sys_oper_status');

INSERT INTO sys_dict_type (dict_id, dict_name, dict_type, status, del_flag, remark, create_time)
SELECT 11, '操作业务类型', 'sys_oper_business_type', '0', '0', '操作日志业务类型', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE dict_type = 'sys_oper_business_type');

INSERT INTO sys_dict_type (dict_id, dict_name, dict_type, status, del_flag, remark, create_time)
SELECT 12, '操作类别', 'sys_oper_operator_type', '0', '0', '操作者类别', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE dict_type = 'sys_oper_operator_type');

INSERT INTO sys_dict_type (dict_id, dict_name, dict_type, status, del_flag, remark, create_time)
SELECT 13, '登录状态', 'sys_login_status', '0', '0', '登录日志状态', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE dict_type = 'sys_login_status');

INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, list_class, is_default, status, del_flag, create_time)
SELECT 100, 1, '成功', '0', 'sys_oper_status', 'success', 'Y', '0', '0', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_code = 100);
INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, list_class, is_default, status, del_flag, create_time)
SELECT 101, 2, '失败', '1', 'sys_oper_status', 'danger', 'N', '0', '0', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_code = 101);

INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, list_class, is_default, status, del_flag, create_time)
SELECT 110, 1, '其它', '0', 'sys_oper_business_type', 'info', 'Y', '0', '0', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_code = 110);
INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, list_class, is_default, status, del_flag, create_time)
SELECT 111, 2, '新增', '1', 'sys_oper_business_type', 'primary', 'N', '0', '0', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_code = 111);
INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, list_class, is_default, status, del_flag, create_time)
SELECT 112, 3, '修改', '2', 'sys_oper_business_type', 'warning', 'N', '0', '0', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_code = 112);
INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, list_class, is_default, status, del_flag, create_time)
SELECT 113, 4, '删除', '3', 'sys_oper_business_type', 'danger', 'N', '0', '0', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_code = 113);
INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, list_class, is_default, status, del_flag, create_time)
SELECT 114, 5, '导出', '4', 'sys_oper_business_type', 'info', 'N', '0', '0', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_code = 114);
INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, list_class, is_default, status, del_flag, create_time)
SELECT 115, 6, '导入', '5', 'sys_oper_business_type', 'info', 'N', '0', '0', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_code = 115);

INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, list_class, is_default, status, del_flag, create_time)
SELECT 120, 1, '其它', '0', 'sys_oper_operator_type', 'info', 'Y', '0', '0', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_code = 120);
INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, list_class, is_default, status, del_flag, create_time)
SELECT 121, 2, '后台用户', '1', 'sys_oper_operator_type', 'primary', 'N', '0', '0', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_code = 121);
INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, list_class, is_default, status, del_flag, create_time)
SELECT 122, 3, '手机端用户', '2', 'sys_oper_operator_type', 'success', 'N', '0', '0', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_code = 122);

INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, list_class, is_default, status, del_flag, create_time)
SELECT 130, 1, '成功', '0', 'sys_login_status', 'success', 'Y', '0', '0', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_code = 130);
INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, list_class, is_default, status, del_flag, create_time)
SELECT 131, 2, '失败', '1', 'sys_login_status', 'danger', 'N', '0', '0', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_code = 131);

-- 参数设置 2070（系统管理下）
INSERT INTO sys_menu (menu_id, parent_id, menu_name, menu_type, path, component, route_name, perms, icon, order_num, status, del_flag, create_time)
SELECT 2070, 2000, '参数设置', 'C', 'config', 'system/config/index', 'SysConfig', 'system:config:list', 'edit', 7, '0', '0', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2070);
INSERT INTO sys_menu (menu_id, parent_id, menu_name, menu_type, path, component, route_name, perms, icon, order_num, status, del_flag, create_time)
SELECT 2071, 2070, '参数查询', 'F', NULL, NULL, NULL, 'system:config:query', NULL, 1, '0', '0', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2071);
INSERT INTO sys_menu (menu_id, parent_id, menu_name, menu_type, path, component, route_name, perms, icon, order_num, status, del_flag, create_time)
SELECT 2072, 2070, '参数新增', 'F', NULL, NULL, NULL, 'system:config:add', NULL, 2, '0', '0', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2072);
INSERT INTO sys_menu (menu_id, parent_id, menu_name, menu_type, path, component, route_name, perms, icon, order_num, status, del_flag, create_time)
SELECT 2073, 2070, '参数修改', 'F', NULL, NULL, NULL, 'system:config:edit', NULL, 3, '0', '0', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2073);
INSERT INTO sys_menu (menu_id, parent_id, menu_name, menu_type, path, component, route_name, perms, icon, order_num, status, del_flag, create_time)
SELECT 2074, 2070, '参数删除', 'F', NULL, NULL, NULL, 'system:config:remove', NULL, 4, '0', '0', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2074);
INSERT INTO sys_menu (menu_id, parent_id, menu_name, menu_type, path, component, route_name, perms, icon, order_num, status, del_flag, create_time)
SELECT 2075, 2070, '参数导出', 'F', NULL, NULL, NULL, 'system:config:export', NULL, 5, '0', '0', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2075);
INSERT INTO sys_menu (menu_id, parent_id, menu_name, menu_type, path, component, route_name, perms, icon, order_num, status, del_flag, create_time)
SELECT 2076, 2070, '参数导入', 'F', NULL, NULL, NULL, 'system:config:import', NULL, 6, '0', '0', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2076);

-- 系统监控 2100
INSERT INTO sys_menu (menu_id, parent_id, menu_name, menu_type, path, component, route_name, perms, icon, order_num, status, del_flag, create_time)
SELECT 2100, 0, '系统监控', 'M', 'monitor', NULL, NULL, NULL, 'monitor', 2, '0', '0', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2100);

INSERT INTO sys_menu (menu_id, parent_id, menu_name, menu_type, path, component, route_name, perms, icon, order_num, status, del_flag, create_time)
SELECT 2110, 2100, '操作日志', 'C', 'operlog', 'monitor/operlog/index', 'SysOperlog', 'monitor:operlog:list', 'form', 1, '0', '0', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2110);
INSERT INTO sys_menu (menu_id, parent_id, menu_name, menu_type, path, component, route_name, perms, icon, order_num, status, del_flag, create_time)
SELECT 2111, 2110, '操作查询', 'F', NULL, NULL, NULL, 'monitor:operlog:query', NULL, 1, '0', '0', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2111);
INSERT INTO sys_menu (menu_id, parent_id, menu_name, menu_type, path, component, route_name, perms, icon, order_num, status, del_flag, create_time)
SELECT 2112, 2110, '操作删除', 'F', NULL, NULL, NULL, 'monitor:operlog:remove', NULL, 2, '0', '0', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2112);
INSERT INTO sys_menu (menu_id, parent_id, menu_name, menu_type, path, component, route_name, perms, icon, order_num, status, del_flag, create_time)
SELECT 2113, 2110, '日志导出', 'F', NULL, NULL, NULL, 'monitor:operlog:export', NULL, 3, '0', '0', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2113);

INSERT INTO sys_menu (menu_id, parent_id, menu_name, menu_type, path, component, route_name, perms, icon, order_num, status, del_flag, create_time)
SELECT 2120, 2100, '登录日志', 'C', 'logininfor', 'monitor/logininfor/index', 'SysLogininfor', 'monitor:logininfor:list', 'logininfor', 2, '0', '0', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2120);
INSERT INTO sys_menu (menu_id, parent_id, menu_name, menu_type, path, component, route_name, perms, icon, order_num, status, del_flag, create_time)
SELECT 2121, 2120, '登录查询', 'F', NULL, NULL, NULL, 'monitor:logininfor:query', NULL, 1, '0', '0', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2121);
INSERT INTO sys_menu (menu_id, parent_id, menu_name, menu_type, path, component, route_name, perms, icon, order_num, status, del_flag, create_time)
SELECT 2122, 2120, '登录删除', 'F', NULL, NULL, NULL, 'monitor:logininfor:remove', NULL, 2, '0', '0', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2122);
INSERT INTO sys_menu (menu_id, parent_id, menu_name, menu_type, path, component, route_name, perms, icon, order_num, status, del_flag, create_time)
SELECT 2123, 2120, '日志导出', 'F', NULL, NULL, NULL, 'monitor:logininfor:export', NULL, 3, '0', '0', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2123);
INSERT INTO sys_menu (menu_id, parent_id, menu_name, menu_type, path, component, route_name, perms, icon, order_num, status, del_flag, create_time)
SELECT 2124, 2120, '账户解锁', 'F', NULL, NULL, NULL, 'monitor:logininfor:unlock', NULL, 4, '0', '0', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2124);

-- 超管授权
INSERT INTO sys_role_menu (role_id, menu_id) SELECT 1, 2070 WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 1 AND menu_id = 2070);
INSERT INTO sys_role_menu (role_id, menu_id) SELECT 1, 2071 WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 1 AND menu_id = 2071);
INSERT INTO sys_role_menu (role_id, menu_id) SELECT 1, 2072 WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 1 AND menu_id = 2072);
INSERT INTO sys_role_menu (role_id, menu_id) SELECT 1, 2073 WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 1 AND menu_id = 2073);
INSERT INTO sys_role_menu (role_id, menu_id) SELECT 1, 2074 WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 1 AND menu_id = 2074);
INSERT INTO sys_role_menu (role_id, menu_id) SELECT 1, 2075 WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 1 AND menu_id = 2075);
INSERT INTO sys_role_menu (role_id, menu_id) SELECT 1, 2076 WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 1 AND menu_id = 2076);
INSERT INTO sys_role_menu (role_id, menu_id) SELECT 1, 2100 WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 1 AND menu_id = 2100);
INSERT INTO sys_role_menu (role_id, menu_id) SELECT 1, 2110 WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 1 AND menu_id = 2110);
INSERT INTO sys_role_menu (role_id, menu_id) SELECT 1, 2111 WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 1 AND menu_id = 2111);
INSERT INTO sys_role_menu (role_id, menu_id) SELECT 1, 2112 WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 1 AND menu_id = 2112);
INSERT INTO sys_role_menu (role_id, menu_id) SELECT 1, 2113 WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 1 AND menu_id = 2113);
INSERT INTO sys_role_menu (role_id, menu_id) SELECT 1, 2120 WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 1 AND menu_id = 2120);
INSERT INTO sys_role_menu (role_id, menu_id) SELECT 1, 2121 WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 1 AND menu_id = 2121);
INSERT INTO sys_role_menu (role_id, menu_id) SELECT 1, 2122 WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 1 AND menu_id = 2122);
INSERT INTO sys_role_menu (role_id, menu_id) SELECT 1, 2123 WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 1 AND menu_id = 2123);
INSERT INTO sys_role_menu (role_id, menu_id) SELECT 1, 2124 WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 1 AND menu_id = 2124);
