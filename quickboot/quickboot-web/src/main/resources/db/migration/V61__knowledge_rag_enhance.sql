-- 知识库 RAG 增强：分块全文/启用开关、检索历史、隐藏独立检索与问答菜单

ALTER TABLE kb_document_chunk
    ADD COLUMN content_full TEXT NULL COMMENT '完整分块正文（检索与编辑用）' AFTER content_preview,
    ADD COLUMN enabled TINYINT NOT NULL DEFAULT 1 COMMENT '是否参与检索：0禁用 1启用' AFTER page_number;

-- 历史数据：用摘要回填全文（编辑能力受限于原 500 字摘要）
UPDATE kb_document_chunk SET content_full = content_preview WHERE content_full IS NULL;

CREATE TABLE IF NOT EXISTS kb_retrieval_log (
    log_id                 BIGINT        NOT NULL PRIMARY KEY COMMENT '记录主键',
    kb_id                  BIGINT        NOT NULL COMMENT '知识库ID',
    query                  VARCHAR(2000) NOT NULL DEFAULT '' COMMENT '检索词',
    search_mode            VARCHAR(16)   NOT NULL DEFAULT 'HYBRID' COMMENT 'VECTOR/HYBRID',
    top_k                  INT           NOT NULL DEFAULT 8 COMMENT '返回条数',
    similarity_threshold   DOUBLE        NOT NULL DEFAULT 0.5 COMMENT '相似度阈值',
    hit_count              INT           NOT NULL DEFAULT 0 COMMENT '命中条数',
    create_by              VARCHAR(64)   NOT NULL DEFAULT '' COMMENT '操作人',
    create_time            DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    KEY idx_kb_retrieval_log_kb_id (kb_id),
    KEY idx_kb_retrieval_log_create_time (create_time)
) COMMENT '知识库检索测试历史';

-- 隐藏独立「语义检索」「RAG 问答」菜单，功能并入知识库详情
UPDATE sys_menu SET visible = '1' WHERE menu_id IN (2291, 2293);
