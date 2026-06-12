-- 修复：V63 曾被「隐藏文档菜单」占用时，ai_model 与 KB/WF 绑定列可能未创建。
-- flyway:executeInTransaction=false

CREATE TABLE IF NOT EXISTS ai_model (
    model_id             BIGINT         NOT NULL PRIMARY KEY COMMENT '模型主键',
    name                 VARCHAR(100)   NOT NULL DEFAULT '' COMMENT '展示名称',
    code                 VARCHAR(64)    NOT NULL DEFAULT '' COMMENT '唯一编码',
    description          VARCHAR(500)   NOT NULL DEFAULT '' COMMENT '备注说明',
    model_type           VARCHAR(16)    NOT NULL DEFAULT 'CHAT' COMMENT 'CHAT/EMBEDDING',
    provider             VARCHAR(24)    NOT NULL DEFAULT 'OPENAI_COMPAT' COMMENT 'OPENAI_COMPAT/OLLAMA',
    base_url             VARCHAR(2048)  NOT NULL DEFAULT '' COMMENT 'API 根地址',
    api_key_type         VARCHAR(16)    NOT NULL DEFAULT 'PLAIN' COMMENT 'PLAIN/SECRET/ENV_REF',
    api_key              VARCHAR(2000)  NOT NULL DEFAULT '' COMMENT '按 api_key_type 解释的密钥',
    model_name           VARCHAR(128)   NOT NULL DEFAULT '' COMMENT '厂商模型名',
    completions_path     VARCHAR(128)   NULL COMMENT 'OpenAI 兼容 Chat 路径覆盖',
    embeddings_path      VARCHAR(128)   NULL COMMENT 'OpenAI 兼容 Embedding 路径覆盖',
    dimensions           INT            NULL COMMENT 'Embedding 向量维度',
    temperature          DECIMAL(4,2)   NULL COMMENT 'Chat 默认温度',
    max_tokens           INT            NULL COMMENT 'Chat 最大 token',
    request_timeout_ms   INT            NOT NULL DEFAULT 60000 COMMENT '请求超时毫秒',
    default_slot         VARCHAR(24)    NULL COMMENT 'CHAT/EMBEDDING/WORKFLOW_CHAT 全局默认槽位',
    status               TINYINT        NOT NULL DEFAULT 0 COMMENT '状态：0正常 1停用',
    last_test_status     VARCHAR(16)    NULL COMMENT 'SUCCESS/FAILED/UNTESTED',
    last_test_msg        VARCHAR(1000)  NULL COMMENT '最近探测摘要',
    last_test_time       DATETIME       NULL COMMENT '最近探测时间',
    create_by            VARCHAR(64)    NOT NULL DEFAULT '' COMMENT '创建人',
    create_time          DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by            VARCHAR(64)    NOT NULL DEFAULT '' COMMENT '更新人',
    update_time          DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted              TINYINT        NOT NULL DEFAULT 0 COMMENT '逻辑删除：0否 1是',
    UNIQUE KEY uk_ai_model_code (code),
    KEY idx_ai_model_type (model_type),
    KEY idx_ai_model_provider (provider),
    KEY idx_ai_model_status (status),
    KEY idx_ai_model_default_slot (default_slot)
) COMMENT 'AI 大模型配置';

ALTER TABLE kb_knowledge_base
    ADD COLUMN chat_model_id BIGINT NULL COMMENT 'Chat 模型 ai_model.model_id，NULL 表示使用全局默认' AFTER status;

ALTER TABLE kb_knowledge_base
    ADD COLUMN embedding_model_id BIGINT NULL COMMENT 'Embedding 模型 ai_model.model_id，NULL 表示使用全局默认' AFTER chat_model_id;

ALTER TABLE wf_workflow
    ADD COLUMN chat_model_id BIGINT NULL COMMENT 'Chat 模型 ai_model.model_id，NULL 表示 WORKFLOW_CHAT/CHAT 默认' AFTER external_api_enabled;

INSERT IGNORE INTO sys_menu (menu_id, parent_id, menu_type, menu_name, order_num, path, component, query, route_name, is_frame, is_cache, visible, status, perms, icon, remark, del_flag, create_by, create_time)
VALUES (2320, -1, 'M', 'AI 能力', 5, '/ai', 'Layout', NULL, 'AiCapability', '0', '0', '0', '0', NULL, 'cpu', 'AI 大模型与 MCP 管理', '0', 'system', CURRENT_TIMESTAMP);

INSERT IGNORE INTO sys_menu (menu_id, parent_id, menu_type, menu_name, order_num, path, component, query, route_name, is_frame, is_cache, visible, status, perms, icon, remark, del_flag, create_by, create_time)
VALUES (2321, 2320, 'C', '大模型管理', 1, 'model', 'ai/model/index', NULL, 'AiModel', '0', '0', '0', '0', 'ai:model:list', 'monitor', 'AI 大模型 CRUD', '0', 'system', CURRENT_TIMESTAMP);

INSERT IGNORE INTO sys_menu (menu_id, parent_id, menu_type, menu_name, order_num, path, component, query, route_name, is_frame, is_cache, visible, status, perms, icon, remark, del_flag, create_by, create_time)
VALUES (2322, 2321, 'F', '模型查询', 1, '', NULL, NULL, NULL, '0', '0', '0', '0', 'ai:model:query', '#', NULL, '0', 'system', CURRENT_TIMESTAMP);

INSERT IGNORE INTO sys_menu (menu_id, parent_id, menu_type, menu_name, order_num, path, component, query, route_name, is_frame, is_cache, visible, status, perms, icon, remark, del_flag, create_by, create_time)
VALUES (2323, 2321, 'F', '模型新增', 2, '', NULL, NULL, NULL, '0', '0', '0', '0', 'ai:model:add', '#', NULL, '0', 'system', CURRENT_TIMESTAMP);

INSERT IGNORE INTO sys_menu (menu_id, parent_id, menu_type, menu_name, order_num, path, component, query, route_name, is_frame, is_cache, visible, status, perms, icon, remark, del_flag, create_by, create_time)
VALUES (2324, 2321, 'F', '模型修改', 3, '', NULL, NULL, NULL, '0', '0', '0', '0', 'ai:model:edit', '#', NULL, '0', 'system', CURRENT_TIMESTAMP);

INSERT IGNORE INTO sys_menu (menu_id, parent_id, menu_type, menu_name, order_num, path, component, query, route_name, is_frame, is_cache, visible, status, perms, icon, remark, del_flag, create_by, create_time)
VALUES (2325, 2321, 'F', '模型删除', 4, '', NULL, NULL, NULL, '0', '0', '0', '0', 'ai:model:remove', '#', NULL, '0', 'system', CURRENT_TIMESTAMP);

INSERT IGNORE INTO sys_menu (menu_id, parent_id, menu_type, menu_name, order_num, path, component, query, route_name, is_frame, is_cache, visible, status, perms, icon, remark, del_flag, create_by, create_time)
VALUES (2326, 2321, 'F', '模型测试', 5, '', NULL, NULL, NULL, '0', '0', '0', '0', 'ai:model:test', '#', NULL, '0', 'system', CURRENT_TIMESTAMP);

INSERT IGNORE INTO sys_menu (menu_id, parent_id, menu_type, menu_name, order_num, path, component, query, route_name, is_frame, is_cache, visible, status, perms, icon, remark, del_flag, create_by, create_time)
VALUES (2327, 2321, 'F', '模型导出', 6, '', NULL, NULL, NULL, '0', '0', '0', '0', 'ai:model:export', '#', NULL, '0', 'system', CURRENT_TIMESTAMP);

INSERT IGNORE INTO sys_menu (menu_id, parent_id, menu_type, menu_name, order_num, path, component, query, route_name, is_frame, is_cache, visible, status, perms, icon, remark, del_flag, create_by, create_time)
VALUES (2328, 2321, 'F', '模型导出密钥', 7, '', NULL, NULL, NULL, '0', '0', '0', '0', 'ai:model:export:secrets', '#', NULL, '0', 'system', CURRENT_TIMESTAMP);

UPDATE sys_menu
SET parent_id = 2320,
    path = 'mcp',
    component = 'ai/mcp/index',
    order_num = 2,
    perms = 'ai:mcp:list'
WHERE menu_id = 2301;

UPDATE sys_menu SET perms = 'ai:mcp:query' WHERE menu_id = 2302;
UPDATE sys_menu SET perms = 'ai:mcp:add' WHERE menu_id = 2303;
UPDATE sys_menu SET perms = 'ai:mcp:edit' WHERE menu_id = 2304;
UPDATE sys_menu SET perms = 'ai:mcp:remove' WHERE menu_id = 2305;
UPDATE sys_menu SET perms = 'ai:mcp:test' WHERE menu_id = 2306;
UPDATE sys_menu SET perms = 'ai:mcp:export' WHERE menu_id = 2307;
UPDATE sys_menu SET perms = 'ai:mcp:export:secrets' WHERE menu_id = 2308;

INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (1, 2320);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (1, 2321);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (1, 2322);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (1, 2323);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (1, 2324);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (1, 2325);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (1, 2326);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (1, 2327);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (1, 2328);
