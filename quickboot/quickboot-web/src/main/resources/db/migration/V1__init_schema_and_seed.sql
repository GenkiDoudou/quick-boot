-- 初始库表 + 本地联调种子（Flyway 版本化：仅空库首次执行一次）
-- 适用：H2 MODE=MySQL / MySQL；唯一约束用 CONSTRAINT ... UNIQUE
-- 不含 sys_role_menu 授权（避免与人工勾选冲突；默认按钮授权见 V2）

CREATE TABLE sys_user (
  user_id      VARCHAR(64)  NOT NULL,
  dept_id      BIGINT,
  user_name    VARCHAR(64)  NOT NULL,
  nick_name    VARCHAR(64),
  user_type    VARCHAR(32),
  email        VARCHAR(128),
  phonenumber  VARCHAR(32),
  sex          CHAR(1),
  password     VARCHAR(200) NOT NULL,
  status       CHAR(1)      NOT NULL DEFAULT '0',
  del_flag     CHAR(1)      NOT NULL DEFAULT '0',
  remark       VARCHAR(500),
  create_by    VARCHAR(64),
  create_time  TIMESTAMP    NULL,
  update_by    VARCHAR(64),
  update_time  TIMESTAMP    NULL,
  PRIMARY KEY (user_id),
  CONSTRAINT uk_sys_user_name UNIQUE (user_name)
);

CREATE TABLE sys_oauth_user_bind (
  id                 BIGINT       NOT NULL AUTO_INCREMENT,
  user_id            VARCHAR(64)  NOT NULL,
  registration_id    VARCHAR(64)  NOT NULL,
  external_subject   VARCHAR(128) NOT NULL,
  display_name       VARCHAR(128),
  avatar             VARCHAR(512),
  create_time        TIMESTAMP    NULL,
  PRIMARY KEY (id),
  CONSTRAINT uk_oauth_bind UNIQUE (registration_id, external_subject)
);

CREATE TABLE sys_oauth_client (
  id                 BIGINT       NOT NULL,
  client_id          VARCHAR(64)  NOT NULL,
  client_secret      VARCHAR(256) NOT NULL,
  client_name        VARCHAR(128),
  api_path_patterns  VARCHAR(2000),
  token_timeout      BIGINT,
  check_captcha      CHAR(1)      NOT NULL DEFAULT '0',
  status             CHAR(1)      NOT NULL DEFAULT '0',
  del_flag           CHAR(1)      NOT NULL DEFAULT '0',
  remark             VARCHAR(500),
  create_by          VARCHAR(64),
  create_time        TIMESTAMP    NULL,
  update_by          VARCHAR(64),
  update_time        TIMESTAMP    NULL,
  PRIMARY KEY (id),
  CONSTRAINT uk_sys_oauth_client_client_id UNIQUE (client_id)
);

CREATE TABLE sys_role (
  role_id      BIGINT       NOT NULL,
  role_name    VARCHAR(64)  NOT NULL,
  role_key     VARCHAR(64)  NOT NULL,
  role_sort    INT          NOT NULL DEFAULT 0,
  data_scope   CHAR(1)      NOT NULL DEFAULT '1',
  status       CHAR(1)      NOT NULL DEFAULT '0',
  del_flag     CHAR(1)      NOT NULL DEFAULT '0',
  remark       VARCHAR(500),
  create_by    VARCHAR(64),
  create_time  TIMESTAMP    NULL,
  update_by    VARCHAR(64),
  update_time  TIMESTAMP    NULL,
  PRIMARY KEY (role_id),
  CONSTRAINT uk_sys_role_key UNIQUE (role_key)
);

-- menu_type M=目录 C=菜单 F=按钮；visible 0显示1隐藏；is_frame 0否1外链；is_cache 0缓存1不缓存
CREATE TABLE sys_menu (
  menu_id      BIGINT       NOT NULL,
  parent_id    BIGINT       NOT NULL DEFAULT 0,
  menu_name    VARCHAR(64)  NOT NULL,
  menu_type    CHAR(1)      NOT NULL,
  path         VARCHAR(200),
  component    VARCHAR(255),
  route_name   VARCHAR(64),
  perms        VARCHAR(500),
  icon         VARCHAR(64),
  order_num    INT          NOT NULL DEFAULT 0,
  query        VARCHAR(255),
  is_frame     CHAR(1)      NOT NULL DEFAULT '0',
  is_cache     CHAR(1)      NOT NULL DEFAULT '0',
  visible      CHAR(1)      NOT NULL DEFAULT '0',
  status       CHAR(1)      NOT NULL DEFAULT '0',
  del_flag     CHAR(1)      NOT NULL DEFAULT '0',
  remark       VARCHAR(500),
  create_by    VARCHAR(64),
  create_time  TIMESTAMP    NULL,
  update_by    VARCHAR(64),
  update_time  TIMESTAMP    NULL,
  PRIMARY KEY (menu_id)
);

CREATE TABLE sys_role_menu (
  role_id  BIGINT NOT NULL,
  menu_id  BIGINT NOT NULL,
  PRIMARY KEY (role_id, menu_id)
);

CREATE TABLE sys_user_role (
  user_id  VARCHAR(64) NOT NULL,
  role_id  BIGINT      NOT NULL,
  PRIMARY KEY (user_id, role_id)
);

-- 种子：admin / admin123
INSERT INTO sys_user (user_id, user_name, password, nick_name, status, del_flag, create_time)
VALUES (
  '1',
  'admin',
  '{bcrypt}$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2',
  '管理员',
  '0',
  '0',
  CURRENT_TIMESTAMP
);

INSERT INTO sys_oauth_client (
  id, client_id, client_secret, client_name, api_path_patterns,
  token_timeout, check_captcha,
  status, del_flag, remark, create_by, create_time
) VALUES (
  1,
  'quick-ui',
  'quick-ui-secret',
  'Quick UI',
  '/**',
  604800,
  '0',
  '0',
  '0',
  '本地 SPA 默认客户端',
  'system',
  CURRENT_TIMESTAMP
);

INSERT INTO sys_role (role_id, role_name, role_key, role_sort, data_scope, status, del_flag, remark, create_time)
VALUES (1, '超级管理员', 'admin', 1, '1', '0', '0', '系统内置', CURRENT_TIMESTAMP);

INSERT INTO sys_menu (menu_id, parent_id, menu_name, menu_type, path, component, route_name, perms, icon, order_num, status, del_flag, create_time)
VALUES (2000, 0, '系统管理', 'M', 'system', NULL, 'System', NULL, 'system', 1, '0', '0', CURRENT_TIMESTAMP);

INSERT INTO sys_menu (menu_id, parent_id, menu_name, menu_type, path, component, route_name, perms, icon, order_num, status, del_flag, create_time)
VALUES (2001, 2000, '客户端管理', 'C', 'oauth-client', 'system/oauthClient/index', 'OauthClient', 'system:oauthClient:list', 'client', 1, '0', '0', CURRENT_TIMESTAMP);

INSERT INTO sys_menu (menu_id, parent_id, menu_name, menu_type, path, component, route_name, perms, icon, order_num, status, del_flag, create_time)
VALUES (2002, 2001, '客户端查询', 'F', NULL, NULL, NULL, 'system:oauthClient:query', NULL, 1, '0', '0', CURRENT_TIMESTAMP);

INSERT INTO sys_menu (menu_id, parent_id, menu_name, menu_type, path, component, route_name, perms, icon, order_num, status, del_flag, create_time)
VALUES (2003, 2001, '客户端新增', 'F', NULL, NULL, NULL, 'system:oauthClient:add', NULL, 2, '0', '0', CURRENT_TIMESTAMP);

INSERT INTO sys_menu (menu_id, parent_id, menu_name, menu_type, path, component, route_name, perms, icon, order_num, status, del_flag, create_time)
VALUES (2004, 2001, '客户端修改', 'F', NULL, NULL, NULL, 'system:oauthClient:edit', NULL, 3, '0', '0', CURRENT_TIMESTAMP);

INSERT INTO sys_menu (menu_id, parent_id, menu_name, menu_type, path, component, route_name, perms, icon, order_num, status, del_flag, create_time)
VALUES (2005, 2001, '客户端删除', 'F', NULL, NULL, NULL, 'system:oauthClient:remove', NULL, 4, '0', '0', CURRENT_TIMESTAMP);

INSERT INTO sys_menu (menu_id, parent_id, menu_name, menu_type, path, component, route_name, perms, icon, order_num, status, del_flag, create_time)
VALUES (2006, 2001, '查看密钥', 'F', NULL, NULL, NULL, 'system:oauthClient:secret', NULL, 5, '0', '0', CURRENT_TIMESTAMP);

INSERT INTO sys_menu (menu_id, parent_id, menu_name, menu_type, path, component, route_name, perms, icon, order_num, status, del_flag, create_time)
VALUES (2010, 2000, '角色管理', 'C', 'role', 'system/role/index', 'SysRole', 'system:role:list', 'peoples', 2, '0', '0', CURRENT_TIMESTAMP);

INSERT INTO sys_menu (menu_id, parent_id, menu_name, menu_type, path, component, route_name, perms, icon, order_num, status, del_flag, create_time)
VALUES (2011, 2010, '角色查询', 'F', NULL, NULL, NULL, 'system:role:list', NULL, 1, '0', '0', CURRENT_TIMESTAMP);

INSERT INTO sys_menu (menu_id, parent_id, menu_name, menu_type, path, component, route_name, perms, icon, order_num, status, del_flag, create_time)
VALUES (2012, 2010, '角色新增', 'F', NULL, NULL, NULL, 'system:role:add', NULL, 2, '0', '0', CURRENT_TIMESTAMP);

INSERT INTO sys_menu (menu_id, parent_id, menu_name, menu_type, path, component, route_name, perms, icon, order_num, status, del_flag, create_time)
VALUES (2013, 2010, '角色修改', 'F', NULL, NULL, NULL, 'system:role:edit', NULL, 3, '0', '0', CURRENT_TIMESTAMP);

INSERT INTO sys_menu (menu_id, parent_id, menu_name, menu_type, path, component, route_name, perms, icon, order_num, status, del_flag, create_time)
VALUES (2014, 2010, '角色删除', 'F', NULL, NULL, NULL, 'system:role:remove', NULL, 4, '0', '0', CURRENT_TIMESTAMP);

INSERT INTO sys_menu (menu_id, parent_id, menu_name, menu_type, path, component, route_name, perms, icon, order_num, status, del_flag, create_time)
VALUES (2015, 2010, '菜单权限', 'F', NULL, NULL, NULL, 'system:role:menu', NULL, 5, '0', '0', CURRENT_TIMESTAMP);

INSERT INTO sys_menu (menu_id, parent_id, menu_name, menu_type, path, component, route_name, perms, icon, order_num, status, del_flag, create_time)
VALUES (2016, 2010, '分配用户', 'F', NULL, NULL, NULL, 'system:role:authUser', NULL, 6, '0', '0', CURRENT_TIMESTAMP);

INSERT INTO sys_menu (menu_id, parent_id, menu_name, menu_type, path, component, route_name, perms, icon, order_num, query, is_frame, is_cache, visible, status, del_flag, create_time)
VALUES (2020, 2000, '菜单管理', 'C', 'menu', 'system/menu/index', 'SysMenu', 'system:menu:list', 'tree-table', 3, NULL, '0', '0', '0', '0', '0', CURRENT_TIMESTAMP);

INSERT INTO sys_menu (menu_id, parent_id, menu_name, menu_type, path, component, route_name, perms, icon, order_num, status, del_flag, create_time)
VALUES (2021, 2020, '菜单查询', 'F', NULL, NULL, NULL, 'system:menu:query', NULL, 1, '0', '0', CURRENT_TIMESTAMP);

INSERT INTO sys_menu (menu_id, parent_id, menu_name, menu_type, path, component, route_name, perms, icon, order_num, status, del_flag, create_time)
VALUES (2022, 2020, '菜单新增', 'F', NULL, NULL, NULL, 'system:menu:add', NULL, 2, '0', '0', CURRENT_TIMESTAMP);

INSERT INTO sys_menu (menu_id, parent_id, menu_name, menu_type, path, component, route_name, perms, icon, order_num, status, del_flag, create_time)
VALUES (2023, 2020, '菜单修改', 'F', NULL, NULL, NULL, 'system:menu:edit', NULL, 3, '0', '0', CURRENT_TIMESTAMP);

INSERT INTO sys_menu (menu_id, parent_id, menu_name, menu_type, path, component, route_name, perms, icon, order_num, status, del_flag, create_time)
VALUES (2024, 2020, '菜单删除', 'F', NULL, NULL, NULL, 'system:menu:remove', NULL, 4, '0', '0', CURRENT_TIMESTAMP);

INSERT INTO sys_user_role (user_id, role_id) VALUES ('1', 1);
