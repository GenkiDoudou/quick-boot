-- 知识库外部 MCP 管理：业务表 + 菜单权限

CREATE TABLE IF NOT EXISTS kb_mcp_server (
    mcp_id             BIGINT        NOT NULL PRIMARY KEY COMMENT 'MCP 主键',
    name               VARCHAR(100)  NOT NULL DEFAULT '' COMMENT '展示名称',
    code               VARCHAR(64)   NOT NULL DEFAULT '' COMMENT '唯一编码，导出 mcpServers key',
    description        VARCHAR(500)  NOT NULL DEFAULT '' COMMENT '备注说明',
    transport          VARCHAR(24)   NOT NULL DEFAULT 'STDIO' COMMENT 'STDIO/SSE/STREAMABLE_HTTP',
    command            VARCHAR(255)  NULL COMMENT 'STDIO 命令',
    args_json          JSON          NULL COMMENT 'STDIO 参数 JSON 数组',
    url                VARCHAR(2048) NULL COMMENT '远程 MCP URL',
    headers_json       JSON          NULL COMMENT '远程 HTTP 头 JSON',
    request_timeout_ms INT           NOT NULL DEFAULT 30000 COMMENT '请求超时毫秒',
    status             TINYINT       NOT NULL DEFAULT 0 COMMENT '状态：0正常 1停用',
    last_test_status   VARCHAR(16)   NULL COMMENT 'SUCCESS/FAILED/UNTESTED',
    last_test_msg      VARCHAR(1000) NULL COMMENT '最近探测摘要',
    last_test_time     DATETIME      NULL COMMENT '最近探测时间',
    create_by          VARCHAR(64)   NOT NULL DEFAULT '' COMMENT '创建人',
    create_time        DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by          VARCHAR(64)   NOT NULL DEFAULT '' COMMENT '更新人',
    update_time        DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted            TINYINT       NOT NULL DEFAULT 0 COMMENT '逻辑删除：0否 1是',
    UNIQUE KEY uk_kb_mcp_server_code (code),
    KEY idx_kb_mcp_server_status (status),
    KEY idx_kb_mcp_server_transport (transport)
) COMMENT '外部 MCP 服务配置';

CREATE TABLE IF NOT EXISTS kb_mcp_env (
    env_id      BIGINT        NOT NULL PRIMARY KEY COMMENT '环境变量主键',
    mcp_id      BIGINT        NOT NULL COMMENT '关联 kb_mcp_server.mcp_id',
    env_key     VARCHAR(128)  NOT NULL DEFAULT '' COMMENT '变量名',
    value_type  VARCHAR(16)   NOT NULL DEFAULT 'PLAIN' COMMENT 'PLAIN/SECRET/ENV_REF',
    env_value   VARCHAR(2000) NOT NULL DEFAULT '' COMMENT '明文/SM4密文/环境变量名',
    sort_order  INT           NOT NULL DEFAULT 0 COMMENT '排序',
    create_by   VARCHAR(64)   NOT NULL DEFAULT '' COMMENT '创建人',
    create_time DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by   VARCHAR(64)   NOT NULL DEFAULT '' COMMENT '更新人',
    update_time DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted     TINYINT       NOT NULL DEFAULT 0 COMMENT '逻辑删除：0否 1是',
    KEY idx_kb_mcp_env_mcp (mcp_id)
) COMMENT 'MCP 环境变量';

CREATE TABLE IF NOT EXISTS kb_knowledge_base_mcp (
    id          BIGINT      NOT NULL PRIMARY KEY COMMENT '主键',
    kb_id       BIGINT      NOT NULL COMMENT '知识库 kb_knowledge_base.kb_id',
    mcp_id      BIGINT      NOT NULL COMMENT 'MCP kb_mcp_server.mcp_id',
    order_num   INT         NOT NULL DEFAULT 0 COMMENT '同库多 MCP 合并顺序',
    create_by   VARCHAR(64) NOT NULL DEFAULT '' COMMENT '创建人',
    create_time DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by   VARCHAR(64) NOT NULL DEFAULT '' COMMENT '更新人',
    update_time DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted     TINYINT     NOT NULL DEFAULT 0 COMMENT '逻辑删除：0否 1是',
    UNIQUE KEY uk_kb_knowledge_base_mcp (kb_id, mcp_id),
    KEY idx_kb_kb_mcp_kb (kb_id),
    KEY idx_kb_kb_mcp_mcp (mcp_id)
) COMMENT '知识库与 MCP 绑定';

-- MCP 管理菜单（2301+）
INSERT IGNORE INTO sys_menu (menu_id, parent_id, menu_type, menu_name, order_num, path, component, query, route_name, is_frame, is_cache, visible, status, perms, icon, remark, del_flag, create_by, create_time)
VALUES (2301, 2280, 'C', 'MCP 管理', 6, 'mcp', 'knowledge/mcp/index', NULL, 'KbMcp', '0', '0', '0', '0', 'knowledge:mcp:list', 'connection', '外部 MCP 配置', '0', 'system', CURRENT_TIMESTAMP);

INSERT IGNORE INTO sys_menu (menu_id, parent_id, menu_type, menu_name, order_num, path, component, query, route_name, is_frame, is_cache, visible, status, perms, icon, remark, del_flag, create_by, create_time)
VALUES (2302, 2301, 'F', 'MCP 查询', 1, '', NULL, NULL, NULL, '0', '0', '0', '0', 'knowledge:mcp:query', '#', NULL, '0', 'system', CURRENT_TIMESTAMP);

INSERT IGNORE INTO sys_menu (menu_id, parent_id, menu_type, menu_name, order_num, path, component, query, route_name, is_frame, is_cache, visible, status, perms, icon, remark, del_flag, create_by, create_time)
VALUES (2303, 2301, 'F', 'MCP 新增', 2, '', NULL, NULL, NULL, '0', '0', '0', '0', 'knowledge:mcp:add', '#', NULL, '0', 'system', CURRENT_TIMESTAMP);

INSERT IGNORE INTO sys_menu (menu_id, parent_id, menu_type, menu_name, order_num, path, component, query, route_name, is_frame, is_cache, visible, status, perms, icon, remark, del_flag, create_by, create_time)
VALUES (2304, 2301, 'F', 'MCP 修改', 3, '', NULL, NULL, NULL, '0', '0', '0', '0', 'knowledge:mcp:edit', '#', NULL, '0', 'system', CURRENT_TIMESTAMP);

INSERT IGNORE INTO sys_menu (menu_id, parent_id, menu_type, menu_name, order_num, path, component, query, route_name, is_frame, is_cache, visible, status, perms, icon, remark, del_flag, create_by, create_time)
VALUES (2305, 2301, 'F', 'MCP 删除', 4, '', NULL, NULL, NULL, '0', '0', '0', '0', 'knowledge:mcp:remove', '#', NULL, '0', 'system', CURRENT_TIMESTAMP);

INSERT IGNORE INTO sys_menu (menu_id, parent_id, menu_type, menu_name, order_num, path, component, query, route_name, is_frame, is_cache, visible, status, perms, icon, remark, del_flag, create_by, create_time)
VALUES (2306, 2301, 'F', 'MCP 测试', 5, '', NULL, NULL, NULL, '0', '0', '0', '0', 'knowledge:mcp:test', '#', NULL, '0', 'system', CURRENT_TIMESTAMP);

INSERT IGNORE INTO sys_menu (menu_id, parent_id, menu_type, menu_name, order_num, path, component, query, route_name, is_frame, is_cache, visible, status, perms, icon, remark, del_flag, create_by, create_time)
VALUES (2307, 2301, 'F', 'MCP 导出', 6, '', NULL, NULL, NULL, '0', '0', '0', '0', 'knowledge:mcp:export', '#', NULL, '0', 'system', CURRENT_TIMESTAMP);

INSERT IGNORE INTO sys_menu (menu_id, parent_id, menu_type, menu_name, order_num, path, component, query, route_name, is_frame, is_cache, visible, status, perms, icon, remark, del_flag, create_by, create_time)
VALUES (2308, 2301, 'F', 'MCP 导出密钥', 7, '', NULL, NULL, NULL, '0', '0', '0', '0', 'knowledge:mcp:export:secrets', '#', NULL, '0', 'system', CURRENT_TIMESTAMP);

INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (1, 2301);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (1, 2302);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (1, 2303);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (1, 2304);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (1, 2305);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (1, 2306);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (1, 2307);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (1, 2308);
