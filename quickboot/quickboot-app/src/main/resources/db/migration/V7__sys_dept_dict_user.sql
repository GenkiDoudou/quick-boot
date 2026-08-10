-- 部门 / 字典 / 用户管理：表结构 + 种子 + 菜单权限

CREATE TABLE IF NOT EXISTS sys_dept (
  dept_id      BIGINT       NOT NULL,
  parent_id    BIGINT       NOT NULL DEFAULT 0,
  dept_name    VARCHAR(64)  NOT NULL,
  order_num    INT          NOT NULL DEFAULT 0,
  leader       VARCHAR(64),
  phone        VARCHAR(32),
  email        VARCHAR(128),
  status       CHAR(1)      NOT NULL DEFAULT '0',
  del_flag     CHAR(1)      NOT NULL DEFAULT '0',
  remark       VARCHAR(500),
  create_by    VARCHAR(64),
  create_time  TIMESTAMP    NULL,
  update_by    VARCHAR(64),
  update_time  TIMESTAMP    NULL,
  PRIMARY KEY (dept_id)
);

CREATE TABLE IF NOT EXISTS sys_dict_type (
  dict_id      BIGINT       NOT NULL,
  dict_name    VARCHAR(100) NOT NULL,
  dict_type    VARCHAR(100) NOT NULL,
  status       CHAR(1)      NOT NULL DEFAULT '0',
  del_flag     CHAR(1)      NOT NULL DEFAULT '0',
  remark       VARCHAR(500),
  create_by    VARCHAR(64),
  create_time  TIMESTAMP    NULL,
  update_by    VARCHAR(64),
  update_time  TIMESTAMP    NULL,
  PRIMARY KEY (dict_id),
  CONSTRAINT uk_sys_dict_type UNIQUE (dict_type)
);

CREATE TABLE IF NOT EXISTS sys_dict_data (
  dict_code    BIGINT       NOT NULL,
  dict_sort    INT          NOT NULL DEFAULT 0,
  dict_label   VARCHAR(100) NOT NULL,
  dict_value   VARCHAR(100) NOT NULL,
  dict_type    VARCHAR(100) NOT NULL,
  css_class    VARCHAR(100),
  list_class   VARCHAR(100),
  is_default   CHAR(1)      NOT NULL DEFAULT 'N',
  status       CHAR(1)      NOT NULL DEFAULT '0',
  del_flag     CHAR(1)      NOT NULL DEFAULT '0',
  remark       VARCHAR(500),
  create_by    VARCHAR(64),
  create_time  TIMESTAMP    NULL,
  update_by    VARCHAR(64),
  update_time  TIMESTAMP    NULL,
  PRIMARY KEY (dict_code)
);

INSERT INTO sys_dept (dept_id, parent_id, dept_name, order_num, status, del_flag, create_time)
SELECT 100, 0, '总公司', 0, '0', '0', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_dept WHERE dept_id = 100);

INSERT INTO sys_dict_type (dict_id, dict_name, dict_type, status, del_flag, remark, create_time)
SELECT 1, '系统开关', 'sys_normal_disable', '0', '0', '正常/停用', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE dict_type = 'sys_normal_disable');

INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, list_class, is_default, status, del_flag, create_time)
SELECT 1, 1, '正常', '0', 'sys_normal_disable', 'primary', 'Y', '0', '0', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_code = 1);

INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, list_class, is_default, status, del_flag, create_time)
SELECT 2, 2, '停用', '1', 'sys_normal_disable', 'danger', 'N', '0', '0', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_code = 2);

-- 部门管理 2030
INSERT INTO sys_menu (menu_id, parent_id, menu_name, menu_type, path, component, route_name, perms, icon, order_num, status, del_flag, create_time)
SELECT 2030, 2000, '部门管理', 'C', 'dept', 'system/dept/index', 'SysDept', 'system:dept:list', 'tree', 4, '0', '0', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2030);

INSERT INTO sys_menu (menu_id, parent_id, menu_name, menu_type, path, component, route_name, perms, icon, order_num, status, del_flag, create_time)
SELECT 2031, 2030, '部门查询', 'F', NULL, NULL, NULL, 'system:dept:query', NULL, 1, '0', '0', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2031);
INSERT INTO sys_menu (menu_id, parent_id, menu_name, menu_type, path, component, route_name, perms, icon, order_num, status, del_flag, create_time)
SELECT 2032, 2030, '部门新增', 'F', NULL, NULL, NULL, 'system:dept:add', NULL, 2, '0', '0', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2032);
INSERT INTO sys_menu (menu_id, parent_id, menu_name, menu_type, path, component, route_name, perms, icon, order_num, status, del_flag, create_time)
SELECT 2033, 2030, '部门修改', 'F', NULL, NULL, NULL, 'system:dept:edit', NULL, 3, '0', '0', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2033);
INSERT INTO sys_menu (menu_id, parent_id, menu_name, menu_type, path, component, route_name, perms, icon, order_num, status, del_flag, create_time)
SELECT 2034, 2030, '部门删除', 'F', NULL, NULL, NULL, 'system:dept:remove', NULL, 4, '0', '0', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2034);
INSERT INTO sys_menu (menu_id, parent_id, menu_name, menu_type, path, component, route_name, perms, icon, order_num, status, del_flag, create_time)
SELECT 2035, 2030, '部门导出', 'F', NULL, NULL, NULL, 'system:dept:export', NULL, 5, '0', '0', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2035);
INSERT INTO sys_menu (menu_id, parent_id, menu_name, menu_type, path, component, route_name, perms, icon, order_num, status, del_flag, create_time)
SELECT 2036, 2030, '部门导入', 'F', NULL, NULL, NULL, 'system:dept:import', NULL, 6, '0', '0', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2036);

-- 字典管理 2040
INSERT INTO sys_menu (menu_id, parent_id, menu_name, menu_type, path, component, route_name, perms, icon, order_num, status, del_flag, create_time)
SELECT 2040, 2000, '字典管理', 'C', 'dict', 'system/dict/type/index', 'SysDictType', 'system:dict:list', 'dict', 5, '0', '0', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2040);

INSERT INTO sys_menu (menu_id, parent_id, menu_name, menu_type, path, component, route_name, perms, icon, order_num, status, del_flag, create_time)
SELECT 2041, 2040, '字典查询', 'F', NULL, NULL, NULL, 'system:dict:query', NULL, 1, '0', '0', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2041);
INSERT INTO sys_menu (menu_id, parent_id, menu_name, menu_type, path, component, route_name, perms, icon, order_num, status, del_flag, create_time)
SELECT 2042, 2040, '字典新增', 'F', NULL, NULL, NULL, 'system:dict:add', NULL, 2, '0', '0', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2042);
INSERT INTO sys_menu (menu_id, parent_id, menu_name, menu_type, path, component, route_name, perms, icon, order_num, status, del_flag, create_time)
SELECT 2043, 2040, '字典修改', 'F', NULL, NULL, NULL, 'system:dict:edit', NULL, 3, '0', '0', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2043);
INSERT INTO sys_menu (menu_id, parent_id, menu_name, menu_type, path, component, route_name, perms, icon, order_num, status, del_flag, create_time)
SELECT 2044, 2040, '字典删除', 'F', NULL, NULL, NULL, 'system:dict:remove', NULL, 4, '0', '0', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2044);
INSERT INTO sys_menu (menu_id, parent_id, menu_name, menu_type, path, component, route_name, perms, icon, order_num, status, del_flag, create_time)
SELECT 2045, 2040, '字典导出', 'F', NULL, NULL, NULL, 'system:dict:export', NULL, 5, '0', '0', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2045);
INSERT INTO sys_menu (menu_id, parent_id, menu_name, menu_type, path, component, route_name, perms, icon, order_num, status, del_flag, create_time)
SELECT 2046, 2040, '字典导入', 'F', NULL, NULL, NULL, 'system:dict:import', NULL, 6, '0', '0', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2046);
INSERT INTO sys_menu (menu_id, parent_id, menu_name, menu_type, path, component, route_name, perms, icon, order_num, status, del_flag, create_time)
SELECT 2047, 2040, '字典刷新', 'F', NULL, NULL, NULL, 'system:dict:refresh', NULL, 7, '0', '0', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2047);

-- 字典项按钮挂在字典管理下（数据页由类型跳转）
INSERT INTO sys_menu (menu_id, parent_id, menu_name, menu_type, path, component, route_name, perms, icon, order_num, status, del_flag, create_time)
SELECT 2048, 2040, '字典项查询', 'F', NULL, NULL, NULL, 'system:dictData:list', NULL, 8, '0', '0', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2048);
INSERT INTO sys_menu (menu_id, parent_id, menu_name, menu_type, path, component, route_name, perms, icon, order_num, status, del_flag, create_time)
SELECT 2049, 2040, '字典项新增', 'F', NULL, NULL, NULL, 'system:dictData:add', NULL, 9, '0', '0', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2049);
INSERT INTO sys_menu (menu_id, parent_id, menu_name, menu_type, path, component, route_name, perms, icon, order_num, status, del_flag, create_time)
SELECT 2058, 2040, '字典项修改', 'F', NULL, NULL, NULL, 'system:dictData:edit', NULL, 10, '0', '0', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2058);
INSERT INTO sys_menu (menu_id, parent_id, menu_name, menu_type, path, component, route_name, perms, icon, order_num, status, del_flag, create_time)
SELECT 2059, 2040, '字典项删除', 'F', NULL, NULL, NULL, 'system:dictData:remove', NULL, 11, '0', '0', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2059);
INSERT INTO sys_menu (menu_id, parent_id, menu_name, menu_type, path, component, route_name, perms, icon, order_num, status, del_flag, create_time)
SELECT 2060, 2040, '字典项导出', 'F', NULL, NULL, NULL, 'system:dictData:export', NULL, 12, '0', '0', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2060);
INSERT INTO sys_menu (menu_id, parent_id, menu_name, menu_type, path, component, route_name, perms, icon, order_num, status, del_flag, create_time)
SELECT 2061, 2040, '字典项导入', 'F', NULL, NULL, NULL, 'system:dictData:import', NULL, 13, '0', '0', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2061);

-- 用户管理 2050
INSERT INTO sys_menu (menu_id, parent_id, menu_name, menu_type, path, component, route_name, perms, icon, order_num, status, del_flag, create_time)
SELECT 2050, 2000, '用户管理', 'C', 'user', 'system/user/index', 'SysUser', 'system:user:list', 'user', 6, '0', '0', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2050);

INSERT INTO sys_menu (menu_id, parent_id, menu_name, menu_type, path, component, route_name, perms, icon, order_num, status, del_flag, create_time)
SELECT 2051, 2050, '用户查询', 'F', NULL, NULL, NULL, 'system:user:query', NULL, 1, '0', '0', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2051);
INSERT INTO sys_menu (menu_id, parent_id, menu_name, menu_type, path, component, route_name, perms, icon, order_num, status, del_flag, create_time)
SELECT 2052, 2050, '用户新增', 'F', NULL, NULL, NULL, 'system:user:add', NULL, 2, '0', '0', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2052);
INSERT INTO sys_menu (menu_id, parent_id, menu_name, menu_type, path, component, route_name, perms, icon, order_num, status, del_flag, create_time)
SELECT 2053, 2050, '用户修改', 'F', NULL, NULL, NULL, 'system:user:edit', NULL, 3, '0', '0', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2053);
INSERT INTO sys_menu (menu_id, parent_id, menu_name, menu_type, path, component, route_name, perms, icon, order_num, status, del_flag, create_time)
SELECT 2054, 2050, '用户删除', 'F', NULL, NULL, NULL, 'system:user:remove', NULL, 4, '0', '0', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2054);
INSERT INTO sys_menu (menu_id, parent_id, menu_name, menu_type, path, component, route_name, perms, icon, order_num, status, del_flag, create_time)
SELECT 2055, 2050, '用户导出', 'F', NULL, NULL, NULL, 'system:user:export', NULL, 5, '0', '0', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2055);
INSERT INTO sys_menu (menu_id, parent_id, menu_name, menu_type, path, component, route_name, perms, icon, order_num, status, del_flag, create_time)
SELECT 2056, 2050, '用户导入', 'F', NULL, NULL, NULL, 'system:user:import', NULL, 6, '0', '0', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2056);
INSERT INTO sys_menu (menu_id, parent_id, menu_name, menu_type, path, component, route_name, perms, icon, order_num, status, del_flag, create_time)
SELECT 2057, 2050, '重置密码', 'F', NULL, NULL, NULL, 'system:user:resetPwd', NULL, 7, '0', '0', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2057);

-- 超管授权（2030-2061 中存在的菜单）
INSERT INTO sys_role_menu (role_id, menu_id) SELECT 1, 2030 WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 1 AND menu_id = 2030);
INSERT INTO sys_role_menu (role_id, menu_id) SELECT 1, 2031 WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 1 AND menu_id = 2031);
INSERT INTO sys_role_menu (role_id, menu_id) SELECT 1, 2032 WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 1 AND menu_id = 2032);
INSERT INTO sys_role_menu (role_id, menu_id) SELECT 1, 2033 WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 1 AND menu_id = 2033);
INSERT INTO sys_role_menu (role_id, menu_id) SELECT 1, 2034 WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 1 AND menu_id = 2034);
INSERT INTO sys_role_menu (role_id, menu_id) SELECT 1, 2035 WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 1 AND menu_id = 2035);
INSERT INTO sys_role_menu (role_id, menu_id) SELECT 1, 2036 WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 1 AND menu_id = 2036);
INSERT INTO sys_role_menu (role_id, menu_id) SELECT 1, 2040 WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 1 AND menu_id = 2040);
INSERT INTO sys_role_menu (role_id, menu_id) SELECT 1, 2041 WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 1 AND menu_id = 2041);
INSERT INTO sys_role_menu (role_id, menu_id) SELECT 1, 2042 WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 1 AND menu_id = 2042);
INSERT INTO sys_role_menu (role_id, menu_id) SELECT 1, 2043 WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 1 AND menu_id = 2043);
INSERT INTO sys_role_menu (role_id, menu_id) SELECT 1, 2044 WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 1 AND menu_id = 2044);
INSERT INTO sys_role_menu (role_id, menu_id) SELECT 1, 2045 WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 1 AND menu_id = 2045);
INSERT INTO sys_role_menu (role_id, menu_id) SELECT 1, 2046 WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 1 AND menu_id = 2046);
INSERT INTO sys_role_menu (role_id, menu_id) SELECT 1, 2047 WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 1 AND menu_id = 2047);
INSERT INTO sys_role_menu (role_id, menu_id) SELECT 1, 2048 WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 1 AND menu_id = 2048);
INSERT INTO sys_role_menu (role_id, menu_id) SELECT 1, 2049 WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 1 AND menu_id = 2049);
INSERT INTO sys_role_menu (role_id, menu_id) SELECT 1, 2050 WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 1 AND menu_id = 2050);
INSERT INTO sys_role_menu (role_id, menu_id) SELECT 1, 2051 WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 1 AND menu_id = 2051);
INSERT INTO sys_role_menu (role_id, menu_id) SELECT 1, 2052 WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 1 AND menu_id = 2052);
INSERT INTO sys_role_menu (role_id, menu_id) SELECT 1, 2053 WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 1 AND menu_id = 2053);
INSERT INTO sys_role_menu (role_id, menu_id) SELECT 1, 2054 WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 1 AND menu_id = 2054);
INSERT INTO sys_role_menu (role_id, menu_id) SELECT 1, 2055 WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 1 AND menu_id = 2055);
INSERT INTO sys_role_menu (role_id, menu_id) SELECT 1, 2056 WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 1 AND menu_id = 2056);
INSERT INTO sys_role_menu (role_id, menu_id) SELECT 1, 2057 WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 1 AND menu_id = 2057);
INSERT INTO sys_role_menu (role_id, menu_id) SELECT 1, 2058 WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 1 AND menu_id = 2058);
INSERT INTO sys_role_menu (role_id, menu_id) SELECT 1, 2059 WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 1 AND menu_id = 2059);
INSERT INTO sys_role_menu (role_id, menu_id) SELECT 1, 2060 WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 1 AND menu_id = 2060);
INSERT INTO sys_role_menu (role_id, menu_id) SELECT 1, 2061 WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 1 AND menu_id = 2061);
