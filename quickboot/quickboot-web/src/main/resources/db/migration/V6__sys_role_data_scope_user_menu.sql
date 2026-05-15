-- 角色数据权限、角色-部门、用户表；角色管理菜单及超级管理员菜单授权

ALTER TABLE sys_role
    ADD COLUMN data_scope CHAR(1) NOT NULL DEFAULT '1' COMMENT '数据范围:1全部2自定义3本部门4本部门及以下5仅本人' AFTER remark;

CREATE TABLE IF NOT EXISTS sys_role_dept (
    role_id BIGINT NOT NULL,
    dept_id BIGINT NOT NULL,
    PRIMARY KEY (role_id, dept_id)
);

CREATE TABLE IF NOT EXISTS sys_user (
    user_id       BIGINT       NOT NULL PRIMARY KEY,
    dept_id       BIGINT       NULL,
    user_name     VARCHAR(30)  NOT NULL,
    nick_name     VARCHAR(30)  NULL,
    user_type     VARCHAR(2)   NULL DEFAULT '00',
    email         VARCHAR(50)  NULL,
    phonenumber   VARCHAR(11)  NULL,
    sex           CHAR(1)      NULL DEFAULT '0',
    password      VARCHAR(100) NULL,
    status        CHAR(1)      NOT NULL DEFAULT '0',
    del_flag      CHAR(1)      NOT NULL DEFAULT '0',
    remark        VARCHAR(500) NULL,
    create_by     VARCHAR(64)  NULL,
    create_time   DATETIME     NULL DEFAULT CURRENT_TIMESTAMP,
    update_by     VARCHAR(64)  NULL,
    update_time   DATETIME     NULL
);

CREATE UNIQUE INDEX uk_sys_user_name ON sys_user (user_name);

INSERT INTO sys_user (user_id, dept_id, user_name, nick_name, password, status, del_flag, create_by, create_time)
VALUES (1, NULL, 'admin', '管理员', '', '0', '0', 'system', CURRENT_TIMESTAMP);

-- 角色管理菜单（系统管理下）
INSERT INTO sys_menu (menu_id, parent_id, menu_type, menu_name, order_num, path, component, query, route_name, is_frame, is_cache, visible, status, perms, icon, remark, del_flag, create_by, create_time)
VALUES (2010, 2000, 'C', '角色管理', 4, 'role', 'system/role/index', NULL, 'SysRole', '0', '0', '0', '0', 'system:role:list', 'peoples', NULL, '0', 'system', CURRENT_TIMESTAMP);

INSERT INTO sys_menu (menu_id, parent_id, menu_type, menu_name, order_num, path, component, query, route_name, is_frame, is_cache, visible, status, perms, icon, remark, del_flag, create_by, create_time)
VALUES (2011, 2010, 'F', '角色新增', 1, '', NULL, NULL, NULL, '0', '0', '0', '0', 'system:role:add', '#', NULL, '0', 'system', CURRENT_TIMESTAMP);

INSERT INTO sys_menu (menu_id, parent_id, menu_type, menu_name, order_num, path, component, query, route_name, is_frame, is_cache, visible, status, perms, icon, remark, del_flag, create_by, create_time)
VALUES (2012, 2010, 'F', '角色修改', 2, '', NULL, NULL, NULL, '0', '0', '0', '0', 'system:role:edit', '#', NULL, '0', 'system', CURRENT_TIMESTAMP);

INSERT INTO sys_menu (menu_id, parent_id, menu_type, menu_name, order_num, path, component, query, route_name, is_frame, is_cache, visible, status, perms, icon, remark, del_flag, create_by, create_time)
VALUES (2013, 2010, 'F', '角色删除', 3, '', NULL, NULL, NULL, '0', '0', '0', '0', 'system:role:remove', '#', NULL, '0', 'system', CURRENT_TIMESTAMP);

INSERT INTO sys_menu (menu_id, parent_id, menu_type, menu_name, order_num, path, component, query, route_name, is_frame, is_cache, visible, status, perms, icon, remark, del_flag, create_by, create_time)
VALUES (2014, 2010, 'F', '角色导出', 4, '', NULL, NULL, NULL, '0', '0', '0', '0', 'system:role:export', '#', NULL, '0', 'system', CURRENT_TIMESTAMP);

INSERT INTO sys_menu (menu_id, parent_id, menu_type, menu_name, order_num, path, component, query, route_name, is_frame, is_cache, visible, status, perms, icon, remark, del_flag, create_by, create_time)
VALUES (2015, 2010, 'F', '分配数据权限', 5, '', NULL, NULL, NULL, '0', '0', '0', '0', 'system:role:dataScope', '#', NULL, '0', 'system', CURRENT_TIMESTAMP);

INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 2010);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 2011);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 2012);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 2013);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 2014);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 2015);
