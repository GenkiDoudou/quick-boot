-- 在线用户菜单 + 代码生成表/菜单/参数（V13）
-- 依赖：V8（监控 2100、sys_config）、菜单 ID 避开 2130–2142（job）

-- ========== gen_table / gen_table_column ==========
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
    create_time        TIMESTAMP    NULL,
    update_by          VARCHAR(64)  NULL,
    update_time        TIMESTAMP    NULL
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
    create_time     TIMESTAMP    NULL,
    update_by       VARCHAR(64)  NULL,
    update_time     TIMESTAMP    NULL
);

CREATE INDEX idx_gen_table_column_table_id ON gen_table_column (table_id);

-- ========== 在线用户（parent=2100，menu_id 2150+） ==========
INSERT INTO sys_menu (menu_id, parent_id, menu_name, menu_type, path, component, route_name, perms, icon, order_num, status, del_flag, create_time)
SELECT 2150, 2100, '在线用户', 'C', 'online', 'monitor/online/index', 'SysUserOnline', 'monitor:online:list', 'online', 5, '0', '0', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2150);

INSERT INTO sys_menu (menu_id, parent_id, menu_name, menu_type, path, component, route_name, perms, icon, order_num, status, del_flag, create_time)
SELECT 2151, 2150, '在线用户查询', 'F', NULL, NULL, NULL, 'monitor:online:list', NULL, 1, '0', '0', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2151);

INSERT INTO sys_menu (menu_id, parent_id, menu_name, menu_type, path, component, route_name, perms, icon, order_num, status, del_flag, create_time)
SELECT 2152, 2150, '在线用户强退', 'F', NULL, NULL, NULL, 'monitor:online:forceLogout', NULL, 2, '0', '0', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2152);

INSERT INTO sys_role_menu (role_id, menu_id) SELECT 1, 2150 WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 1 AND menu_id = 2150);
INSERT INTO sys_role_menu (role_id, menu_id) SELECT 1, 2151 WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 1 AND menu_id = 2151);
INSERT INTO sys_role_menu (role_id, menu_id) SELECT 1, 2152 WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 1 AND menu_id = 2152);

-- ========== 系统工具 / 代码生成（menu_id 2300+） ==========
INSERT INTO sys_menu (menu_id, parent_id, menu_name, menu_type, path, component, route_name, perms, icon, order_num, status, del_flag, create_time)
SELECT 2300, 0, '系统工具', 'M', 'tool', NULL, 'Tool', NULL, 'tool', 5, '0', '0', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2300);

INSERT INTO sys_menu (menu_id, parent_id, menu_name, menu_type, path, component, route_name, perms, icon, order_num, status, del_flag, create_time)
SELECT 2301, 2300, '代码生成', 'C', 'gen', 'tool/gen/index', 'ToolGen', 'tool:gen:list', 'code', 1, '0', '0', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2301);

INSERT INTO sys_menu (menu_id, parent_id, menu_name, menu_type, path, component, route_name, perms, icon, order_num, status, del_flag, create_time)
SELECT 2302, 2301, '代码生成导入', 'F', NULL, NULL, NULL, 'tool:gen:import', NULL, 1, '0', '0', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2302);

INSERT INTO sys_menu (menu_id, parent_id, menu_name, menu_type, path, component, route_name, perms, icon, order_num, status, del_flag, create_time)
SELECT 2303, 2301, '代码生成建表', 'F', NULL, NULL, NULL, 'tool:gen:create', NULL, 2, '0', '0', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2303);

INSERT INTO sys_menu (menu_id, parent_id, menu_name, menu_type, path, component, route_name, perms, icon, order_num, status, del_flag, create_time)
SELECT 2304, 2301, '代码生成修改', 'F', NULL, NULL, NULL, 'tool:gen:edit', NULL, 3, '0', '0', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2304);

INSERT INTO sys_menu (menu_id, parent_id, menu_name, menu_type, path, component, route_name, perms, icon, order_num, status, del_flag, create_time)
SELECT 2305, 2301, '代码生成删除', 'F', NULL, NULL, NULL, 'tool:gen:remove', NULL, 4, '0', '0', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2305);

INSERT INTO sys_menu (menu_id, parent_id, menu_name, menu_type, path, component, route_name, perms, icon, order_num, status, del_flag, create_time)
SELECT 2306, 2301, '代码生成预览', 'F', NULL, NULL, NULL, 'tool:gen:preview', NULL, 5, '0', '0', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2306);

INSERT INTO sys_menu (menu_id, parent_id, menu_name, menu_type, path, component, route_name, perms, icon, order_num, status, del_flag, create_time)
SELECT 2307, 2301, '代码生成代码', 'F', NULL, NULL, NULL, 'tool:gen:code', NULL, 6, '0', '0', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2307);

INSERT INTO sys_role_menu (role_id, menu_id) SELECT 1, 2300 WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 1 AND menu_id = 2300);
INSERT INTO sys_role_menu (role_id, menu_id) SELECT 1, 2301 WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 1 AND menu_id = 2301);
INSERT INTO sys_role_menu (role_id, menu_id) SELECT 1, 2302 WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 1 AND menu_id = 2302);
INSERT INTO sys_role_menu (role_id, menu_id) SELECT 1, 2303 WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 1 AND menu_id = 2303);
INSERT INTO sys_role_menu (role_id, menu_id) SELECT 1, 2304 WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 1 AND menu_id = 2304);
INSERT INTO sys_role_menu (role_id, menu_id) SELECT 1, 2305 WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 1 AND menu_id = 2305);
INSERT INTO sys_role_menu (role_id, menu_id) SELECT 1, 2306 WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 1 AND menu_id = 2306);
INSERT INTO sys_role_menu (role_id, menu_id) SELECT 1, 2307 WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 1 AND menu_id = 2307);

-- ========== qc.gen.* 参数种子 ==========
INSERT INTO sys_config (config_id, config_name, config_key, config_value, config_type, del_flag, remark, create_time)
SELECT 900010, '代码生成默认作者', 'qc.gen.author', 'quickboot', '1', '0', '生成代码注释作者', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_config WHERE config_key = 'qc.gen.author');

INSERT INTO sys_config (config_id, config_name, config_key, config_value, config_type, del_flag, remark, create_time)
SELECT 900011, '代码生成默认包路径', 'qc.gen.package-name', 'io.github.genkidoudou.system', '1', '0', 'Java 模块根包名（生成物落在 .internal.*）', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_config WHERE config_key = 'qc.gen.package-name');

INSERT INTO sys_config (config_id, config_name, config_key, config_value, config_type, del_flag, remark, create_time)
SELECT 900012, '代码生成默认模块名', 'qc.gen.module-name', 'system', '1', '0', '前端/权限模块名', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_config WHERE config_key = 'qc.gen.module-name');

INSERT INTO sys_config (config_id, config_name, config_key, config_value, config_type, del_flag, remark, create_time)
SELECT 900013, '代码生成默认模板类型', 'qc.gen.tpl-category', 'crud', '1', '0', 'crud 单表；tree 仅保存配置', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_config WHERE config_key = 'qc.gen.tpl-category');

INSERT INTO sys_config (config_id, config_name, config_key, config_value, config_type, del_flag, remark, create_time)
SELECT 900014, '代码生成默认上级菜单', 'qc.gen.parent-menu-id', '2300', '1', '0', '生成菜单挂载的上级 menu_id', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_config WHERE config_key = 'qc.gen.parent-menu-id');
