-- 菜单、角色、角色-菜单、用户-角色；与 MyBatis-Plus 逻辑删除一致（del_flag：0 正常，1 已删除）。
-- 顶级 parent_id = -1。登录占位用户 id=1 绑定角色 1（admin），供 getRouters/getInfo 联调。

CREATE TABLE IF NOT EXISTS sys_role (
    role_id       BIGINT       NOT NULL PRIMARY KEY,
    role_name     VARCHAR(30)  NOT NULL,
    role_key      VARCHAR(100) NOT NULL,
    role_sort     INT          NOT NULL DEFAULT 0,
    status        CHAR(1)      NOT NULL DEFAULT '0',
    remark        VARCHAR(500) NULL,
    del_flag      CHAR(1)      NOT NULL DEFAULT '0',
    create_by     VARCHAR(64)  NULL,
    create_time   DATETIME     NULL DEFAULT CURRENT_TIMESTAMP,
    update_by     VARCHAR(64)  NULL,
    update_time   DATETIME     NULL
);

CREATE UNIQUE INDEX uk_sys_role_key ON sys_role (role_key);

CREATE TABLE IF NOT EXISTS sys_menu (
    menu_id       BIGINT       NOT NULL PRIMARY KEY,
    parent_id     BIGINT       NOT NULL DEFAULT -1,
    menu_type     CHAR(1)      NOT NULL,
    menu_name     VARCHAR(50)  NOT NULL,
    order_num     INT          NOT NULL DEFAULT 0,
    path          VARCHAR(200) NULL,
    component     VARCHAR(255) NULL,
    query         VARCHAR(255) NULL,
    route_name    VARCHAR(100) NULL,
    is_frame      CHAR(1)      NOT NULL DEFAULT '0',
    is_cache      CHAR(1)      NOT NULL DEFAULT '0',
    visible       CHAR(1)      NOT NULL DEFAULT '0',
    status        CHAR(1)      NOT NULL DEFAULT '0',
    perms         VARCHAR(100) NULL,
    icon          VARCHAR(100) NULL,
    remark        VARCHAR(500) NULL,
    del_flag      CHAR(1)      NOT NULL DEFAULT '0',
    create_by     VARCHAR(64)  NULL,
    create_time   DATETIME     NULL DEFAULT CURRENT_TIMESTAMP,
    update_by     VARCHAR(64)  NULL,
    update_time   DATETIME     NULL
);

CREATE INDEX idx_sys_menu_parent_del ON sys_menu (parent_id, del_flag);

CREATE TABLE IF NOT EXISTS sys_role_menu (
    role_id BIGINT NOT NULL,
    menu_id BIGINT NOT NULL,
    PRIMARY KEY (role_id, menu_id)
);

CREATE TABLE IF NOT EXISTS sys_user_role (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id)
);

INSERT INTO sys_role (role_id, role_name, role_key, role_sort, status, remark, del_flag, create_by, create_time)
VALUES (1, '超级管理员', 'admin', 1, '0', NULL, '0', 'system', CURRENT_TIMESTAMP);

INSERT INTO sys_user_role (user_id, role_id) VALUES (1, 1);

-- 系统管理（目录） + 菜单管理（菜单） + 按钮权限
INSERT INTO sys_menu (menu_id, parent_id, menu_type, menu_name, order_num, path, component, query, route_name, is_frame, is_cache, visible, status, perms, icon, remark, del_flag, create_by, create_time)
VALUES (2000, -1, 'M', '系统管理', 1, '/system', 'Layout', NULL, 'System', '0', '0', '0', '0', NULL, 'system', NULL, '0', 'system', CURRENT_TIMESTAMP);

INSERT INTO sys_menu (menu_id, parent_id, menu_type, menu_name, order_num, path, component, query, route_name, is_frame, is_cache, visible, status, perms, icon, remark, del_flag, create_by, create_time)
VALUES (2001, 2000, 'C', '菜单管理', 1, 'menu', 'system/menu/index', NULL, 'SysMenu', '0', '0', '0', '0', 'system:menu:list', 'tree-table', NULL, '0', 'system', CURRENT_TIMESTAMP);

INSERT INTO sys_menu (menu_id, parent_id, menu_type, menu_name, order_num, path, component, query, route_name, is_frame, is_cache, visible, status, perms, icon, remark, del_flag, create_by, create_time)
VALUES (2002, 2001, 'F', '菜单查询', 1, '', NULL, NULL, NULL, '0', '0', '0', '0', 'system:menu:query', '#', NULL, '0', 'system', CURRENT_TIMESTAMP);

INSERT INTO sys_menu (menu_id, parent_id, menu_type, menu_name, order_num, path, component, query, route_name, is_frame, is_cache, visible, status, perms, icon, remark, del_flag, create_by, create_time)
VALUES (2003, 2001, 'F', '菜单新增', 2, '', NULL, NULL, NULL, '0', '0', '0', '0', 'system:menu:add', '#', NULL, '0', 'system', CURRENT_TIMESTAMP);

INSERT INTO sys_menu (menu_id, parent_id, menu_type, menu_name, order_num, path, component, query, route_name, is_frame, is_cache, visible, status, perms, icon, remark, del_flag, create_by, create_time)
VALUES (2004, 2001, 'F', '菜单修改', 3, '', NULL, NULL, NULL, '0', '0', '0', '0', 'system:menu:edit', '#', NULL, '0', 'system', CURRENT_TIMESTAMP);

INSERT INTO sys_menu (menu_id, parent_id, menu_type, menu_name, order_num, path, component, query, route_name, is_frame, is_cache, visible, status, perms, icon, remark, del_flag, create_by, create_time)
VALUES (2005, 2001, 'F', '菜单删除', 4, '', NULL, NULL, NULL, '0', '0', '0', '0', 'system:menu:remove', '#', NULL, '0', 'system', CURRENT_TIMESTAMP);

INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 2000);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 2001);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 2002);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 2003);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 2004);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 2005);
