-- 代码生成元数据表、菜单与授权（V18）

CREATE TABLE IF NOT EXISTS gen_table (
    table_id           BIGINT       NOT NULL PRIMARY KEY,
    table_name         VARCHAR(200) NOT NULL,
    table_comment      VARCHAR(500) NULL,
    sub_table_name     VARCHAR(64)  NULL,
    sub_table_fk_name  VARCHAR(64)  NULL,
    class_name         VARCHAR(100) NULL,
    tpl_category       VARCHAR(200) NULL DEFAULT 'crud',
    tpl_web_type       VARCHAR(30)  NULL DEFAULT 'element-plus',
    package_name       VARCHAR(100) NULL,
    module_name        VARCHAR(30)  NULL,
    business_name      VARCHAR(30)  NULL,
    function_name      VARCHAR(50)  NULL,
    function_author    VARCHAR(50)  NULL,
    gen_type           CHAR(1)      NULL DEFAULT '0',
    gen_path           VARCHAR(200) NULL,
    parent_menu_id     BIGINT       NULL,
    tree_code          VARCHAR(64)  NULL,
    tree_parent_code   VARCHAR(64)  NULL,
    tree_name          VARCHAR(64)  NULL,
    options            VARCHAR(1000) NULL,
    remark             VARCHAR(500) NULL,
    create_by          VARCHAR(64)  NULL,
    create_time        DATETIME     NULL,
    update_by          VARCHAR(64)  NULL,
    update_time        DATETIME     NULL
);

CREATE UNIQUE INDEX uk_gen_table_table_name ON gen_table (table_name);

CREATE TABLE IF NOT EXISTS gen_table_column (
    column_id       BIGINT       NOT NULL PRIMARY KEY,
    table_id        BIGINT       NOT NULL,
    column_name     VARCHAR(200) NULL,
    column_comment  VARCHAR(500) NULL,
    column_type     VARCHAR(100) NULL,
    java_type       VARCHAR(500) NULL,
    java_field      VARCHAR(200) NULL,
    is_pk           CHAR(1)      NULL,
    is_increment    CHAR(1)      NULL,
    is_required     CHAR(1)      NULL,
    is_insert       CHAR(1)      NULL,
    is_edit         CHAR(1)      NULL,
    is_list         CHAR(1)      NULL,
    is_query        CHAR(1)      NULL,
    query_type      VARCHAR(200) NULL,
    html_type       VARCHAR(200) NULL,
    dict_type       VARCHAR(200) NULL,
    sort            INT          NULL,
    create_by       VARCHAR(64)  NULL,
    create_time     DATETIME     NULL,
    update_by       VARCHAR(64)  NULL,
    update_time     DATETIME     NULL
);

CREATE INDEX idx_gen_table_column_table_id ON gen_table_column (table_id);

INSERT INTO sys_menu (menu_id, parent_id, menu_type, menu_name, order_num, path, component, query, route_name, is_frame, is_cache, visible, status, perms, icon, remark, del_flag, create_by, create_time)
VALUES (2300, -1, 'M', '系统工具', 5, '/tool', 'Layout', NULL, 'Tool', '0', '0', '0', '0', NULL, 'tool', NULL, '0', 'system', CURRENT_TIMESTAMP);

INSERT INTO sys_menu (menu_id, parent_id, menu_type, menu_name, order_num, path, component, query, route_name, is_frame, is_cache, visible, status, perms, icon, remark, del_flag, create_by, create_time)
VALUES (2301, 2300, 'C', '代码生成', 1, 'gen', 'tool/gen/index', NULL, 'ToolGen', '0', '0', '0', '0', 'tool:gen:list', 'code', NULL, '0', 'system', CURRENT_TIMESTAMP);

INSERT INTO sys_menu (menu_id, parent_id, menu_type, menu_name, order_num, path, component, query, route_name, is_frame, is_cache, visible, status, perms, icon, remark, del_flag, create_by, create_time)
VALUES (2302, 2301, 'F', '代码生成导入', 1, '', NULL, NULL, NULL, '0', '0', '0', '0', 'tool:gen:import', '#', NULL, '0', 'system', CURRENT_TIMESTAMP);

INSERT INTO sys_menu (menu_id, parent_id, menu_type, menu_name, order_num, path, component, query, route_name, is_frame, is_cache, visible, status, perms, icon, remark, del_flag, create_by, create_time)
VALUES (2303, 2301, 'F', '代码生成建表', 2, '', NULL, NULL, NULL, '0', '0', '0', '0', 'tool:gen:create', '#', NULL, '0', 'system', CURRENT_TIMESTAMP);

INSERT INTO sys_menu (menu_id, parent_id, menu_type, menu_name, order_num, path, component, query, route_name, is_frame, is_cache, visible, status, perms, icon, remark, del_flag, create_by, create_time)
VALUES (2304, 2301, 'F', '代码生成修改', 3, '', NULL, NULL, NULL, '0', '0', '0', '0', 'tool:gen:edit', '#', NULL, '0', 'system', CURRENT_TIMESTAMP);

INSERT INTO sys_menu (menu_id, parent_id, menu_type, menu_name, order_num, path, component, query, route_name, is_frame, is_cache, visible, status, perms, icon, remark, del_flag, create_by, create_time)
VALUES (2305, 2301, 'F', '代码生成删除', 4, '', NULL, NULL, NULL, '0', '0', '0', '0', 'tool:gen:remove', '#', NULL, '0', 'system', CURRENT_TIMESTAMP);

INSERT INTO sys_menu (menu_id, parent_id, menu_type, menu_name, order_num, path, component, query, route_name, is_frame, is_cache, visible, status, perms, icon, remark, del_flag, create_by, create_time)
VALUES (2306, 2301, 'F', '代码生成预览', 5, '', NULL, NULL, NULL, '0', '0', '0', '0', 'tool:gen:preview', '#', NULL, '0', 'system', CURRENT_TIMESTAMP);

INSERT INTO sys_menu (menu_id, parent_id, menu_type, menu_name, order_num, path, component, query, route_name, is_frame, is_cache, visible, status, perms, icon, remark, del_flag, create_by, create_time)
VALUES (2307, 2301, 'F', '代码生成代码', 6, '', NULL, NULL, NULL, '0', '0', '0', '0', 'tool:gen:code', '#', NULL, '0', 'system', CURRENT_TIMESTAMP);

INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 2300);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 2301);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 2302);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 2303);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 2304);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 2305);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 2306);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 2307);
