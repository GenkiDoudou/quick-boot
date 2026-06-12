-- 知识管理库：业务表 + 菜单权限

CREATE TABLE IF NOT EXISTS kb_knowledge_base (
    kb_id          BIGINT        NOT NULL PRIMARY KEY COMMENT '知识库主键',
    name           VARCHAR(100)  NOT NULL DEFAULT '' COMMENT '知识库名称',
    description    VARCHAR(500)  NOT NULL DEFAULT '' COMMENT '描述',
    chunk_size     INT           NOT NULL DEFAULT 800 COMMENT '分块 token 上限',
    chunk_overlap  INT           NOT NULL DEFAULT 120 COMMENT '分块重叠 token 数',
    status         TINYINT       NOT NULL DEFAULT 0 COMMENT '状态：0正常 1停用',
    create_by      VARCHAR(64)   NOT NULL DEFAULT '' COMMENT '创建人',
    create_time    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by      VARCHAR(64)   NOT NULL DEFAULT '' COMMENT '更新人',
    update_time    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted        TINYINT       NOT NULL DEFAULT 0 COMMENT '逻辑删除：0否 1是',
    KEY idx_kb_knowledge_base_name (name),
    KEY idx_kb_knowledge_base_status (status)
) COMMENT '知识库';

CREATE TABLE IF NOT EXISTS kb_document (
    doc_id         BIGINT        NOT NULL PRIMARY KEY COMMENT '文档主键',
    kb_id          BIGINT        NOT NULL COMMENT '所属知识库 kb_knowledge_base.kb_id',
    file_id        BIGINT        NOT NULL COMMENT '关联 sys_file.file_id',
    title          VARCHAR(255)  NOT NULL DEFAULT '' COMMENT '展示标题',
    doc_status     VARCHAR(16)   NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING/PARSING/INDEXED/FAILED',
    chunk_count    INT           NOT NULL DEFAULT 0 COMMENT '成功入库分块数',
    error_msg      VARCHAR(1000) NULL COMMENT '失败原因',
    create_by      VARCHAR(64)   NOT NULL DEFAULT '' COMMENT '创建人',
    create_time    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by      VARCHAR(64)   NOT NULL DEFAULT '' COMMENT '更新人',
    update_time    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted        TINYINT       NOT NULL DEFAULT 0 COMMENT '逻辑删除：0否 1是',
    KEY idx_kb_document_kb_id (kb_id),
    KEY idx_kb_document_file_id (file_id),
    KEY idx_kb_document_status (doc_status)
) COMMENT '知识库文档';

CREATE TABLE IF NOT EXISTS kb_document_chunk (
    chunk_id         BIGINT        NOT NULL PRIMARY KEY COMMENT '分块主键',
    doc_id           BIGINT        NOT NULL COMMENT '所属文档 kb_document.doc_id',
    chunk_index      INT           NOT NULL DEFAULT 0 COMMENT '文档内序号',
    content_preview  VARCHAR(500)  NOT NULL DEFAULT '' COMMENT '片段摘要',
    vector_id        VARCHAR(64)   NOT NULL DEFAULT '' COMMENT 'PGVector Document id',
    token_count      INT           NULL COMMENT 'token 数',
    page_number      INT           NULL COMMENT 'PDF 页码（可选）',
    create_time      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    KEY idx_kb_document_chunk_doc_id (doc_id)
) COMMENT '知识库文档分块';

CREATE TABLE IF NOT EXISTS kb_ingest_task (
    task_id        BIGINT        NOT NULL PRIMARY KEY COMMENT '任务主键',
    doc_id         BIGINT        NOT NULL COMMENT '目标文档 kb_document.doc_id',
    status         VARCHAR(16)   NOT NULL DEFAULT 'QUEUED' COMMENT 'QUEUED/RUNNING/SUCCESS/FAILED',
    progress       INT           NOT NULL DEFAULT 0 COMMENT '进度 0-100',
    retry_count    INT           NOT NULL DEFAULT 0 COMMENT '重试次数',
    error_msg      VARCHAR(1000) NULL COMMENT '失败信息',
    start_time     DATETIME      NULL COMMENT '开始时间',
    end_time       DATETIME      NULL COMMENT '结束时间',
    create_by      VARCHAR(64)   NOT NULL DEFAULT '' COMMENT '创建人',
    create_time    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    KEY idx_kb_ingest_task_doc_id (doc_id),
    KEY idx_kb_ingest_task_status (status)
) COMMENT '知识库文档入库任务';

-- 知识管理顶级目录（与系统工具 2300 并列）
INSERT IGNORE INTO sys_menu (menu_id, parent_id, menu_type, menu_name, order_num, path, component, query, route_name, is_frame, is_cache, visible, status, perms, icon, remark, del_flag, create_by, create_time)
VALUES (2280, -1, 'M', '知识管理', 6, '/knowledge', 'Layout', NULL, 'Knowledge', '0', '0', '0', '0', NULL, 'education', 'Spring AI 本地 RAG 知识库', '0', 'system', CURRENT_TIMESTAMP);

INSERT IGNORE INTO sys_menu (menu_id, parent_id, menu_type, menu_name, order_num, path, component, query, route_name, is_frame, is_cache, visible, status, perms, icon, remark, del_flag, create_by, create_time)
VALUES (2281, 2280, 'C', '知识库', 1, 'base', 'knowledge/base/index', NULL, 'KbKnowledgeBase', '0', '0', '0', '0', 'knowledge:base:list', 'collection', '知识库 CRUD', '0', 'system', CURRENT_TIMESTAMP);

INSERT IGNORE INTO sys_menu (menu_id, parent_id, menu_type, menu_name, order_num, path, component, query, route_name, is_frame, is_cache, visible, status, perms, icon, remark, del_flag, create_by, create_time)
VALUES (2282, 2281, 'F', '知识库查询', 1, '', NULL, NULL, NULL, '0', '0', '0', '0', 'knowledge:base:query', '#', NULL, '0', 'system', CURRENT_TIMESTAMP);

INSERT IGNORE INTO sys_menu (menu_id, parent_id, menu_type, menu_name, order_num, path, component, query, route_name, is_frame, is_cache, visible, status, perms, icon, remark, del_flag, create_by, create_time)
VALUES (2283, 2281, 'F', '知识库新增', 2, '', NULL, NULL, NULL, '0', '0', '0', '0', 'knowledge:base:add', '#', NULL, '0', 'system', CURRENT_TIMESTAMP);

INSERT IGNORE INTO sys_menu (menu_id, parent_id, menu_type, menu_name, order_num, path, component, query, route_name, is_frame, is_cache, visible, status, perms, icon, remark, del_flag, create_by, create_time)
VALUES (2284, 2281, 'F', '知识库修改', 3, '', NULL, NULL, NULL, '0', '0', '0', '0', 'knowledge:base:edit', '#', NULL, '0', 'system', CURRENT_TIMESTAMP);

INSERT IGNORE INTO sys_menu (menu_id, parent_id, menu_type, menu_name, order_num, path, component, query, route_name, is_frame, is_cache, visible, status, perms, icon, remark, del_flag, create_by, create_time)
VALUES (2285, 2281, 'F', '知识库删除', 4, '', NULL, NULL, NULL, '0', '0', '0', '0', 'knowledge:base:remove', '#', NULL, '0', 'system', CURRENT_TIMESTAMP);

INSERT IGNORE INTO sys_menu (menu_id, parent_id, menu_type, menu_name, order_num, path, component, query, route_name, is_frame, is_cache, visible, status, perms, icon, remark, del_flag, create_by, create_time)
VALUES (2286, 2280, 'C', '文档管理', 2, 'document', 'knowledge/document/index', NULL, 'KbDocument', '0', '0', '0', '0', 'knowledge:doc:list', 'document', '文档上传与入库', '0', 'system', CURRENT_TIMESTAMP);

INSERT IGNORE INTO sys_menu (menu_id, parent_id, menu_type, menu_name, order_num, path, component, query, route_name, is_frame, is_cache, visible, status, perms, icon, remark, del_flag, create_by, create_time)
VALUES (2287, 2286, 'F', '文档查询', 1, '', NULL, NULL, NULL, '0', '0', '0', '0', 'knowledge:doc:list', '#', NULL, '0', 'system', CURRENT_TIMESTAMP);

INSERT IGNORE INTO sys_menu (menu_id, parent_id, menu_type, menu_name, order_num, path, component, query, route_name, is_frame, is_cache, visible, status, perms, icon, remark, del_flag, create_by, create_time)
VALUES (2288, 2286, 'F', '文档上传', 2, '', NULL, NULL, NULL, '0', '0', '0', '0', 'knowledge:doc:upload', '#', NULL, '0', 'system', CURRENT_TIMESTAMP);

INSERT IGNORE INTO sys_menu (menu_id, parent_id, menu_type, menu_name, order_num, path, component, query, route_name, is_frame, is_cache, visible, status, perms, icon, remark, del_flag, create_by, create_time)
VALUES (2289, 2286, 'F', '文档重索引', 3, '', NULL, NULL, NULL, '0', '0', '0', '0', 'knowledge:doc:reindex', '#', NULL, '0', 'system', CURRENT_TIMESTAMP);

INSERT IGNORE INTO sys_menu (menu_id, parent_id, menu_type, menu_name, order_num, path, component, query, route_name, is_frame, is_cache, visible, status, perms, icon, remark, del_flag, create_by, create_time)
VALUES (2290, 2286, 'F', '文档删除', 4, '', NULL, NULL, NULL, '0', '0', '0', '0', 'knowledge:doc:remove', '#', NULL, '0', 'system', CURRENT_TIMESTAMP);

INSERT IGNORE INTO sys_menu (menu_id, parent_id, menu_type, menu_name, order_num, path, component, query, route_name, is_frame, is_cache, visible, status, perms, icon, remark, del_flag, create_by, create_time)
VALUES (2291, 2280, 'C', '语义检索', 3, 'search', 'knowledge/search/index', NULL, 'KbSearch', '0', '0', '0', '0', 'knowledge:search', 'search', '向量语义检索', '0', 'system', CURRENT_TIMESTAMP);

INSERT IGNORE INTO sys_menu (menu_id, parent_id, menu_type, menu_name, order_num, path, component, query, route_name, is_frame, is_cache, visible, status, perms, icon, remark, del_flag, create_by, create_time)
VALUES (2292, 2291, 'F', '语义检索', 1, '', NULL, NULL, NULL, '0', '0', '0', '0', 'knowledge:search', '#', NULL, '0', 'system', CURRENT_TIMESTAMP);

INSERT IGNORE INTO sys_menu (menu_id, parent_id, menu_type, menu_name, order_num, path, component, query, route_name, is_frame, is_cache, visible, status, perms, icon, remark, del_flag, create_by, create_time)
VALUES (2293, 2280, 'C', 'RAG 问答', 4, 'chat', 'knowledge/chat/index', NULL, 'KbChat', '0', '0', '0', '0', 'knowledge:chat', 'chat-dot-round', '检索增强问答', '0', 'system', CURRENT_TIMESTAMP);

INSERT IGNORE INTO sys_menu (menu_id, parent_id, menu_type, menu_name, order_num, path, component, query, route_name, is_frame, is_cache, visible, status, perms, icon, remark, del_flag, create_by, create_time)
VALUES (2294, 2293, 'F', 'RAG 问答', 1, '', NULL, NULL, NULL, '0', '0', '0', '0', 'knowledge:chat', '#', NULL, '0', 'system', CURRENT_TIMESTAMP);

INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (1, 2280);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (1, 2281);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (1, 2282);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (1, 2283);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (1, 2284);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (1, 2285);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (1, 2286);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (1, 2287);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (1, 2288);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (1, 2289);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (1, 2290);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (1, 2291);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (1, 2292);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (1, 2293);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (1, 2294);
