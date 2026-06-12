-- 知识库多来源入库与分段策略扩展：表结构、历史回填、文档库菜单

-- 知识库默认分段策略
ALTER TABLE kb_knowledge_base
    ADD COLUMN segment_mode VARCHAR(16) NOT NULL DEFAULT 'AUTO' COMMENT '分段模式：AUTO/CUSTOM' AFTER chunk_overlap,
    ADD COLUMN chunk_delimiter VARCHAR(16) NOT NULL DEFAULT 'DOUBLE_NEWLINE' COMMENT 'CUSTOM 分隔符：SINGLE_NEWLINE/DOUBLE_NEWLINE' AFTER segment_mode,
    ADD COLUMN preprocess_normalize_ws TINYINT NOT NULL DEFAULT 1 COMMENT '预处理：归一化连续空白 0否1是' AFTER chunk_delimiter,
    ADD COLUMN preprocess_remove_url TINYINT NOT NULL DEFAULT 0 COMMENT '预处理：删除URL 0否1是' AFTER preprocess_normalize_ws,
    ADD COLUMN preprocess_remove_email TINYINT NOT NULL DEFAULT 0 COMMENT '预处理：删除邮箱 0否1是' AFTER preprocess_remove_url;

-- 文档来源与策略快照
ALTER TABLE kb_document
    ADD COLUMN source_type VARCHAR(16) NOT NULL DEFAULT 'FILE' COMMENT '来源：FILE/MANUAL/WEB/LIBRARY' AFTER kb_id,
    ADD COLUMN library_file_id BIGINT NULL COMMENT '文档库文件 kb_doc_library_file.lib_file_id' AFTER file_id,
    ADD COLUMN source_url VARCHAR(2048) NULL COMMENT '网页来源 URL' AFTER library_file_id,
    ADD COLUMN segment_mode VARCHAR(16) NULL COMMENT '入库快照：AUTO/CUSTOM' AFTER source_url,
    ADD COLUMN chunk_size INT NULL COMMENT '入库快照：分块 token 上限' AFTER segment_mode,
    ADD COLUMN chunk_overlap INT NULL COMMENT '入库快照：分块重叠 token' AFTER chunk_size,
    ADD COLUMN chunk_delimiter VARCHAR(16) NULL COMMENT '入库快照：分隔符' AFTER chunk_overlap,
    ADD COLUMN preprocess_normalize_ws TINYINT NULL COMMENT '入库快照：归一化空白' AFTER chunk_delimiter,
    ADD COLUMN preprocess_remove_url TINYINT NULL COMMENT '入库快照：删除URL' AFTER preprocess_normalize_ws,
    ADD COLUMN preprocess_remove_email TINYINT NULL COMMENT '入库快照：删除邮箱' AFTER preprocess_remove_url;

ALTER TABLE kb_document MODIFY COLUMN file_id BIGINT NULL COMMENT '关联 sys_file.file_id';

-- 历史文档回填快照（从所属知识库复制）
UPDATE kb_document d
    INNER JOIN kb_knowledge_base kb ON d.kb_id = kb.kb_id
SET d.segment_mode = IFNULL(d.segment_mode, kb.segment_mode),
    d.chunk_size = IFNULL(d.chunk_size, kb.chunk_size),
    d.chunk_overlap = IFNULL(d.chunk_overlap, kb.chunk_overlap),
    d.chunk_delimiter = IFNULL(d.chunk_delimiter, kb.chunk_delimiter),
    d.preprocess_normalize_ws = IFNULL(d.preprocess_normalize_ws, kb.preprocess_normalize_ws),
    d.preprocess_remove_url = IFNULL(d.preprocess_remove_url, kb.preprocess_remove_url),
    d.preprocess_remove_email = IFNULL(d.preprocess_remove_email, kb.preprocess_remove_email)
WHERE d.deleted = 0;

CREATE TABLE IF NOT EXISTS kb_doc_library_folder (
    folder_id    BIGINT       NOT NULL PRIMARY KEY COMMENT '目录主键',
    parent_id    BIGINT       NOT NULL DEFAULT 0 COMMENT '父目录 ID，0 为根',
    name         VARCHAR(100) NOT NULL DEFAULT '' COMMENT '目录名称',
    order_num    INT          NOT NULL DEFAULT 0 COMMENT '排序',
    create_by    VARCHAR(64)  NOT NULL DEFAULT '' COMMENT '创建人',
    create_time  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by    VARCHAR(64)  NOT NULL DEFAULT '' COMMENT '更新人',
    update_time  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted      TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除：0否1是',
    KEY idx_kb_doc_library_folder_parent (parent_id)
) COMMENT '知识文档库目录';

CREATE TABLE IF NOT EXISTS kb_doc_library_file (
    lib_file_id  BIGINT       NOT NULL PRIMARY KEY COMMENT '文档库文件主键',
    folder_id    BIGINT       NOT NULL COMMENT '所属目录',
    file_id      BIGINT       NOT NULL COMMENT '关联 sys_file.file_id',
    title        VARCHAR(255) NOT NULL DEFAULT '' COMMENT '展示标题',
    file_ext     VARCHAR(32)  NOT NULL DEFAULT '' COMMENT '扩展名',
    file_size    BIGINT       NOT NULL DEFAULT 0 COMMENT '文件大小字节',
    remark       VARCHAR(500) NOT NULL DEFAULT '' COMMENT '备注',
    create_by    VARCHAR(64)  NOT NULL DEFAULT '' COMMENT '创建人',
    create_time  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by    VARCHAR(64)  NOT NULL DEFAULT '' COMMENT '更新人',
    update_time  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted      TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除：0否1是',
    KEY idx_kb_doc_library_file_folder (folder_id),
    KEY idx_kb_doc_library_file_file (file_id)
) COMMENT '知识文档库文件';

-- 文档库菜单（2295+）
INSERT IGNORE INTO sys_menu (menu_id, parent_id, menu_type, menu_name, order_num, path, component, query, route_name, is_frame, is_cache, visible, status, perms, icon, remark, del_flag, create_by, create_time)
VALUES (2295, 2280, 'C', '文档库', 5, 'library', 'knowledge/library/index', NULL, 'KbDocLibrary', '0', '0', '0', '0', 'knowledge:library:list', 'folder', '知识文档库', '0', 'system', CURRENT_TIMESTAMP);

INSERT IGNORE INTO sys_menu (menu_id, parent_id, menu_type, menu_name, order_num, path, component, query, route_name, is_frame, is_cache, visible, status, perms, icon, remark, del_flag, create_by, create_time)
VALUES (2296, 2295, 'F', '文档库查询', 1, '', NULL, NULL, NULL, '0', '0', '0', '0', 'knowledge:library:list', '#', NULL, '0', 'system', CURRENT_TIMESTAMP);

INSERT IGNORE INTO sys_menu (menu_id, parent_id, menu_type, menu_name, order_num, path, component, query, route_name, is_frame, is_cache, visible, status, perms, icon, remark, del_flag, create_by, create_time)
VALUES (2297, 2295, 'F', '文档库新增', 2, '', NULL, NULL, NULL, '0', '0', '0', '0', 'knowledge:library:add', '#', NULL, '0', 'system', CURRENT_TIMESTAMP);

INSERT IGNORE INTO sys_menu (menu_id, parent_id, menu_type, menu_name, order_num, path, component, query, route_name, is_frame, is_cache, visible, status, perms, icon, remark, del_flag, create_by, create_time)
VALUES (2298, 2295, 'F', '文档库修改', 3, '', NULL, NULL, NULL, '0', '0', '0', '0', 'knowledge:library:edit', '#', NULL, '0', 'system', CURRENT_TIMESTAMP);

INSERT IGNORE INTO sys_menu (menu_id, parent_id, menu_type, menu_name, order_num, path, component, query, route_name, is_frame, is_cache, visible, status, perms, icon, remark, del_flag, create_by, create_time)
VALUES (2299, 2295, 'F', '文档库删除', 4, '', NULL, NULL, NULL, '0', '0', '0', '0', 'knowledge:library:remove', '#', NULL, '0', 'system', CURRENT_TIMESTAMP);

INSERT IGNORE INTO sys_menu (menu_id, parent_id, menu_type, menu_name, order_num, path, component, query, route_name, is_frame, is_cache, visible, status, perms, icon, remark, del_flag, create_by, create_time)
VALUES (2300, 2295, 'F', '文档库上传', 5, '', NULL, NULL, NULL, '0', '0', '0', '0', 'knowledge:library:upload', '#', NULL, '0', 'system', CURRENT_TIMESTAMP);

INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (1, 2295);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (1, 2296);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (1, 2297);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (1, 2298);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (1, 2299);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (1, 2300);
