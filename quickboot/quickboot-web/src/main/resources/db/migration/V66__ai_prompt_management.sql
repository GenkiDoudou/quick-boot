-- AI 提示词管理：业务表 + 菜单权限（2330-2335）

CREATE TABLE IF NOT EXISTS ai_prompt (
    prompt_id            BIGINT         NOT NULL PRIMARY KEY COMMENT '提示词主键',
    code                 VARCHAR(64)    NOT NULL DEFAULT '' COMMENT '唯一编码',
    name                 VARCHAR(100)   NOT NULL DEFAULT '' COMMENT '展示名称',
    description          VARCHAR(500)   NOT NULL DEFAULT '' COMMENT '备注说明',
    prompt_type          VARCHAR(24)    NOT NULL DEFAULT 'LLM' COMMENT 'LLM/RAG/CLASSIFIER/EXTRACTOR/CUSTOM',
    domain               VARCHAR(32)    NOT NULL DEFAULT '' COMMENT '业务域',
    category             VARCHAR(64)    NOT NULL DEFAULT '' COMMENT '分类',
    tags                 JSON           NULL COMMENT '标签 JSON 数组',
    status               VARCHAR(16)    NOT NULL DEFAULT 'DRAFT' COMMENT 'DRAFT/PUBLISHED/ARCHIVED',
    current_version_id   BIGINT         NULL COMMENT '当前已发布版本 ID',
    current_version_no   INT            NOT NULL DEFAULT 0 COMMENT '当前版本号',
    optimize_model_id    BIGINT         NULL COMMENT '优化/A/B 默认 Chat 模型',
    create_by            VARCHAR(64)    NOT NULL DEFAULT '' COMMENT '创建人',
    create_time          DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by            VARCHAR(64)    NOT NULL DEFAULT '' COMMENT '更新人',
    update_time          DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted              TINYINT        NOT NULL DEFAULT 0 COMMENT '逻辑删除：0否 1是',
    UNIQUE KEY uk_ai_prompt_code (code),
    KEY idx_ai_prompt_type (prompt_type),
    KEY idx_ai_prompt_status (status),
    KEY idx_ai_prompt_domain (domain)
) COMMENT 'AI 提示词模板';

CREATE TABLE IF NOT EXISTS ai_prompt_content (
    content_id           BIGINT         NOT NULL PRIMARY KEY COMMENT '内容主键',
    prompt_id            BIGINT         NOT NULL COMMENT '提示词 ID',
    version_id           BIGINT         NOT NULL DEFAULT 0 COMMENT '0=草稿，非0=版本快照',
    section_key          VARCHAR(64)    NOT NULL DEFAULT '' COMMENT '段键名',
    content              TEXT           NULL COMMENT '段正文',
    KEY idx_ai_prompt_content_pid (prompt_id, version_id)
) COMMENT 'AI 提示词内容段';

CREATE TABLE IF NOT EXISTS ai_prompt_variable (
    variable_id          BIGINT         NOT NULL PRIMARY KEY COMMENT '变量主键',
    prompt_id            BIGINT         NOT NULL COMMENT '提示词 ID',
    version_id           BIGINT         NOT NULL DEFAULT 0 COMMENT '0=草稿，非0=版本快照',
    var_key              VARCHAR(64)    NOT NULL DEFAULT '' COMMENT '变量名',
    var_type             VARCHAR(16)    NOT NULL DEFAULT 'string' COMMENT 'string/number/array/object',
    required             TINYINT        NOT NULL DEFAULT 0 COMMENT '是否必填：0否 1是',
    description          VARCHAR(200)   NOT NULL DEFAULT '' COMMENT '说明',
    sort                 INT            NOT NULL DEFAULT 0 COMMENT '排序',
    KEY idx_ai_prompt_variable_pid (prompt_id, version_id)
) COMMENT 'AI 提示词变量声明';

CREATE TABLE IF NOT EXISTS ai_prompt_version (
    version_id           BIGINT         NOT NULL PRIMARY KEY COMMENT '版本主键',
    prompt_id            BIGINT         NOT NULL COMMENT '提示词 ID',
    version_no           INT            NOT NULL DEFAULT 1 COMMENT '版本号',
    change_summary       VARCHAR(500)   NOT NULL DEFAULT '' COMMENT '变更摘要',
    snapshot_json        JSON           NULL COMMENT 'sections+variables 快照',
    source               VARCHAR(24)    NOT NULL DEFAULT 'EDIT' COMMENT 'EDIT/OPTIMIZE/AB_ADOPT/ROLLBACK/PUBLISH',
    create_by            VARCHAR(64)    NOT NULL DEFAULT '' COMMENT '创建人',
    create_time          DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    KEY idx_ai_prompt_version_pid (prompt_id, version_no)
) COMMENT 'AI 提示词版本快照';

CREATE TABLE IF NOT EXISTS ai_prompt_optimize_session (
    session_id           BIGINT         NOT NULL PRIMARY KEY COMMENT '会话主键',
    prompt_id            BIGINT         NOT NULL COMMENT '提示词 ID',
    model_id             BIGINT         NULL COMMENT '使用的 Chat 模型',
    optimize_goal        VARCHAR(1000)  NOT NULL DEFAULT '' COMMENT '优化目标',
    original_snapshot    JSON           NULL COMMENT '优化前快照',
    result_snapshot      JSON           NULL COMMENT '优化结果快照',
    status               VARCHAR(16)    NOT NULL DEFAULT 'SUCCESS' COMMENT 'SUCCESS/FAILED',
    error_msg            VARCHAR(500)   NOT NULL DEFAULT '' COMMENT '失败原因',
    create_time          DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    KEY idx_ai_prompt_opt_session_pid (prompt_id)
) COMMENT 'AI 提示词优化会话';

CREATE TABLE IF NOT EXISTS ai_prompt_ab_run (
    run_id               BIGINT         NOT NULL PRIMARY KEY COMMENT '运行主键',
    prompt_id            BIGINT         NOT NULL COMMENT '提示词 ID',
    model_id             BIGINT         NULL COMMENT 'Chat 模型',
    variant_a_version_id BIGINT         NOT NULL DEFAULT 0 COMMENT '版本 A，0=草稿',
    variant_b_version_id BIGINT         NOT NULL DEFAULT 0 COMMENT '版本 B，0=草稿',
    sample_input_json    JSON           NULL COMMENT '样例变量',
    rendered_prompt_a    TEXT           NULL COMMENT '渲染后 prompt A',
    rendered_prompt_b    TEXT           NULL COMMENT '渲染后 prompt B',
    output_a             TEXT           NULL COMMENT '模型输出 A',
    output_b             TEXT           NULL COMMENT '模型输出 B',
    score_a              TINYINT        NULL COMMENT '评分 1-5',
    score_b              TINYINT        NULL COMMENT '评分 1-5',
    winner               VARCHAR(8)     NULL COMMENT 'A/B/TIE',
    remark               VARCHAR(500)   NOT NULL DEFAULT '' COMMENT '备注',
    create_time          DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    KEY idx_ai_prompt_ab_run_pid (prompt_id)
) COMMENT 'AI 提示词 A/B 对比记录';

-- 提示词管理菜单
INSERT IGNORE INTO sys_menu (menu_id, parent_id, menu_type, menu_name, order_num, path, component, query, route_name, is_frame, is_cache, visible, status, perms, icon, remark, del_flag, create_by, create_time)
VALUES (2330, 2320, 'C', '提示词管理', 3, 'prompt', 'ai/prompt/index', NULL, 'AiPrompt', '0', '0', '0', '0', 'ai:prompt:list', 'edit', 'AI 提示词 CRUD 与优化', '0', 'system', CURRENT_TIMESTAMP);

INSERT IGNORE INTO sys_menu (menu_id, parent_id, menu_type, menu_name, order_num, path, component, query, route_name, is_frame, is_cache, visible, status, perms, icon, remark, del_flag, create_by, create_time)
VALUES (2331, 2330, 'F', '提示词查询', 1, '', NULL, NULL, NULL, '0', '0', '0', '0', 'ai:prompt:query', '#', NULL, '0', 'system', CURRENT_TIMESTAMP);

INSERT IGNORE INTO sys_menu (menu_id, parent_id, menu_type, menu_name, order_num, path, component, query, route_name, is_frame, is_cache, visible, status, perms, icon, remark, del_flag, create_by, create_time)
VALUES (2332, 2330, 'F', '提示词新增', 2, '', NULL, NULL, NULL, '0', '0', '0', '0', 'ai:prompt:add', '#', NULL, '0', 'system', CURRENT_TIMESTAMP);

INSERT IGNORE INTO sys_menu (menu_id, parent_id, menu_type, menu_name, order_num, path, component, query, route_name, is_frame, is_cache, visible, status, perms, icon, remark, del_flag, create_by, create_time)
VALUES (2333, 2330, 'F', '提示词修改', 3, '', NULL, NULL, NULL, '0', '0', '0', '0', 'ai:prompt:edit', '#', NULL, '0', 'system', CURRENT_TIMESTAMP);

INSERT IGNORE INTO sys_menu (menu_id, parent_id, menu_type, menu_name, order_num, path, component, query, route_name, is_frame, is_cache, visible, status, perms, icon, remark, del_flag, create_by, create_time)
VALUES (2334, 2330, 'F', '提示词删除', 4, '', NULL, NULL, NULL, '0', '0', '0', '0', 'ai:prompt:remove', '#', NULL, '0', 'system', CURRENT_TIMESTAMP);

INSERT IGNORE INTO sys_menu (menu_id, parent_id, menu_type, menu_name, order_num, path, component, query, route_name, is_frame, is_cache, visible, status, perms, icon, remark, del_flag, create_by, create_time)
VALUES (2335, 2330, 'F', '提示词优化', 5, '', NULL, NULL, NULL, '0', '0', '0', '0', 'ai:prompt:optimize', '#', NULL, '0', 'system', CURRENT_TIMESTAMP);

UPDATE sys_menu SET remark = 'AI 大模型、MCP 与提示词管理' WHERE menu_id = 2320;
