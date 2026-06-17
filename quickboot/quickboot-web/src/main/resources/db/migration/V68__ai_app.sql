-- AI 智能体应用：业务表 + 菜单权限（2400-2420）

CREATE TABLE IF NOT EXISTS ai_app (
    id                      BIGINT        NOT NULL PRIMARY KEY COMMENT '应用主键',
    name                    VARCHAR(128)  NOT NULL DEFAULT '' COMMENT '应用名称',
    description             VARCHAR(512)  NOT NULL DEFAULT '' COMMENT '功能介绍',
    icon                    VARCHAR(256)  NOT NULL DEFAULT '' COMMENT '图标URL或内置key',
    app_type                VARCHAR(16)   NOT NULL DEFAULT 'agent' COMMENT 'agent/workflow',
    status                  VARCHAR(16)   NOT NULL DEFAULT 'draft' COMMENT 'draft/published',
    config_json             LONGTEXT      NULL COMMENT '草稿配置JSON',
    published_config_json   LONGTEXT      NULL COMMENT '发布快照JSON',
    del_flag                TINYINT       NOT NULL DEFAULT 0 COMMENT '逻辑删除：0否 1是',
    create_by               VARCHAR(64)   NOT NULL DEFAULT '' COMMENT '创建人',
    create_time             DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by               VARCHAR(64)   NOT NULL DEFAULT '' COMMENT '更新人',
    update_time             DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    KEY idx_ai_app_name (name),
    KEY idx_ai_app_type (app_type),
    KEY idx_ai_app_status (status)
) COMMENT 'AI应用定义';

CREATE TABLE IF NOT EXISTS ai_app_session (
    id              BIGINT        NOT NULL PRIMARY KEY COMMENT '会话主键',
    app_id          BIGINT        NOT NULL COMMENT '关联应用 ai_app.id',
    user_key        VARCHAR(64)   NOT NULL DEFAULT '' COMMENT '登录userId或embed访客标识',
    title           VARCHAR(256)  NOT NULL DEFAULT '' COMMENT '会话标题',
    variables_json  LONGTEXT      NULL COMMENT '智能体变量记忆快照JSON',
    create_time     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    KEY idx_ai_app_session_app (app_id),
    KEY idx_ai_app_session_user (app_id, user_key)
) COMMENT 'AI应用会话';

CREATE TABLE IF NOT EXISTS ai_app_message (
    id              BIGINT        NOT NULL PRIMARY KEY COMMENT '消息主键',
    session_id      BIGINT        NOT NULL COMMENT '所属会话 ai_app_session.id',
    role            VARCHAR(16)   NOT NULL DEFAULT 'user' COMMENT 'user/assistant/tool',
    content         LONGTEXT      NULL COMMENT '消息正文',
    metadata_json   LONGTEXT      NULL COMMENT '工具调用、工作流runId、引用等',
    create_time     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    KEY idx_ai_app_message_session (session_id)
) COMMENT 'AI应用会话消息';

CREATE TABLE IF NOT EXISTS ai_app_publish (
    id               BIGINT        NOT NULL PRIMARY KEY COMMENT '发布配置主键',
    app_id           BIGINT        NOT NULL COMMENT '关联应用 ai_app.id',
    embed_token      VARCHAR(128)  NOT NULL DEFAULT '' COMMENT '嵌入访问令牌（唯一）',
    allowed_origins  VARCHAR(1024) NOT NULL DEFAULT '' COMMENT '域名白名单，逗号分隔',
    menu_path        VARCHAR(256)  NOT NULL DEFAULT '' COMMENT '系统菜单路由',
    menu_component   VARCHAR(256)  NOT NULL DEFAULT '' COMMENT '前端组件路径',
    enabled          TINYINT       NOT NULL DEFAULT 0 COMMENT '是否启用嵌入：0否 1是',
    create_by        VARCHAR(64)   NOT NULL DEFAULT '' COMMENT '创建人',
    create_time      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by        VARCHAR(64)   NOT NULL DEFAULT '' COMMENT '更新人',
    update_time      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_ai_app_publish_token (embed_token),
    KEY idx_ai_app_publish_app (app_id)
) COMMENT 'AI应用发布与嵌入配置';

-- AI 应用顶级目录
INSERT IGNORE INTO sys_menu (menu_id, parent_id, menu_type, menu_name, order_num, path, component, query, route_name, is_frame, is_cache, visible, status, perms, icon, remark, del_flag, create_by, create_time)
VALUES (2400, -1, 'M', 'AI应用', 8, '/ai/app', 'Layout', NULL, 'AiApp', '0', '0', '0', '0', NULL, 'robot', 'AI智能体应用编排与发布', '0', 'system', CURRENT_TIMESTAMP);

INSERT IGNORE INTO sys_menu (menu_id, parent_id, menu_type, menu_name, order_num, path, component, query, route_name, is_frame, is_cache, visible, status, perms, icon, remark, del_flag, create_by, create_time)
VALUES (2401, 2400, 'C', '应用列表', 1, 'list', 'ai/app/list/index', NULL, 'AiAppList', '0', '0', '0', '0', 'aiapp:list', 'list', 'AI应用 CRUD', '0', 'system', CURRENT_TIMESTAMP);

INSERT IGNORE INTO sys_menu (menu_id, parent_id, menu_type, menu_name, order_num, path, component, query, route_name, is_frame, is_cache, visible, status, perms, icon, remark, del_flag, create_by, create_time)
VALUES (2402, 2401, 'F', '应用查询', 1, '', NULL, NULL, NULL, '0', '0', '0', '0', 'aiapp:query', '#', NULL, '0', 'system', CURRENT_TIMESTAMP);

INSERT IGNORE INTO sys_menu (menu_id, parent_id, menu_type, menu_name, order_num, path, component, query, route_name, is_frame, is_cache, visible, status, perms, icon, remark, del_flag, create_by, create_time)
VALUES (2403, 2401, 'F', '应用新增', 2, '', NULL, NULL, NULL, '0', '0', '0', '0', 'aiapp:add', '#', NULL, '0', 'system', CURRENT_TIMESTAMP);

INSERT IGNORE INTO sys_menu (menu_id, parent_id, menu_type, menu_name, order_num, path, component, query, route_name, is_frame, is_cache, visible, status, perms, icon, remark, del_flag, create_by, create_time)
VALUES (2404, 2401, 'F', '应用修改', 3, '', NULL, NULL, NULL, '0', '0', '0', '0', 'aiapp:edit', '#', NULL, '0', 'system', CURRENT_TIMESTAMP);

INSERT IGNORE INTO sys_menu (menu_id, parent_id, menu_type, menu_name, order_num, path, component, query, route_name, is_frame, is_cache, visible, status, perms, icon, remark, del_flag, create_by, create_time)
VALUES (2405, 2401, 'F', '应用发布', 4, '', NULL, NULL, NULL, '0', '0', '0', '0', 'aiapp:publish', '#', NULL, '0', 'system', CURRENT_TIMESTAMP);

INSERT IGNORE INTO sys_menu (menu_id, parent_id, menu_type, menu_name, order_num, path, component, query, route_name, is_frame, is_cache, visible, status, perms, icon, remark, del_flag, create_by, create_time)
VALUES (2406, 2401, 'F', '应用删除', 5, '', NULL, NULL, NULL, '0', '0', '0', '0', 'aiapp:remove', '#', NULL, '0', 'system', CURRENT_TIMESTAMP);

INSERT IGNORE INTO sys_menu (menu_id, parent_id, menu_type, menu_name, order_num, path, component, query, route_name, is_frame, is_cache, visible, status, perms, icon, remark, del_flag, create_by, create_time)
VALUES (2407, 2401, 'F', '应用对话', 6, '', NULL, NULL, NULL, '0', '0', '0', '0', 'aiapp:chat', '#', NULL, '0', 'system', CURRENT_TIMESTAMP);

INSERT IGNORE INTO sys_menu (menu_id, parent_id, menu_type, menu_name, order_num, path, component, query, route_name, is_frame, is_cache, visible, status, perms, icon, remark, del_flag, create_by, create_time)
VALUES (2408, 2400, 'C', '智能体编排', 2, 'agent/:id', 'ai/app/agent/index', NULL, 'AiAppAgentDesign', '0', '0', '1', '0', 'aiapp:edit', 'edit', '智能体三栏编排', '0', 'system', CURRENT_TIMESTAMP);

INSERT IGNORE INTO sys_menu (menu_id, parent_id, menu_type, menu_name, order_num, path, component, query, route_name, is_frame, is_cache, visible, status, perms, icon, remark, del_flag, create_by, create_time)
VALUES (2409, 2400, 'C', '高级编排', 3, 'workflow/:id', 'ai/app/workflow/index', NULL, 'AiAppWorkflowDesign', '0', '0', '1', '0', 'aiapp:edit', 'connection', '工作流绑定编排', '0', 'system', CURRENT_TIMESTAMP);

INSERT IGNORE INTO sys_menu (menu_id, parent_id, menu_type, menu_name, order_num, path, component, query, route_name, is_frame, is_cache, visible, status, perms, icon, remark, del_flag, create_by, create_time)
VALUES (2410, 2400, 'C', '演示聊天', 4, 'chat/:appId', 'ai/app/chat/index', NULL, 'AiAppChat', '0', '0', '1', '0', 'aiapp:chat', 'message', '管理端演示聊天', '0', 'system', CURRENT_TIMESTAMP);

INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (1, 2400);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (1, 2401);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (1, 2402);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (1, 2403);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (1, 2404);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (1, 2405);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (1, 2406);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (1, 2407);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (1, 2408);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (1, 2409);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (1, 2410);
