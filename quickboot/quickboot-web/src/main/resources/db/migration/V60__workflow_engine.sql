-- AI 工作流引擎：业务表 + 菜单权限（2310-2330）

CREATE TABLE IF NOT EXISTS wf_workflow (
    workflow_id            BIGINT        NOT NULL PRIMARY KEY COMMENT '工作流主键',
    name                   VARCHAR(128)  NOT NULL DEFAULT '' COMMENT '工作流名称',
    description            VARCHAR(512)  NOT NULL DEFAULT '' COMMENT '描述',
    status                 VARCHAR(16)   NOT NULL DEFAULT 'DRAFT' COMMENT 'DRAFT/PUBLISHED/DISABLED',
    published_version_id   BIGINT        NULL COMMENT '当前发布版本 wf_workflow_version.version_id',
    bot_enabled            TINYINT       NOT NULL DEFAULT 0 COMMENT '预留：是否允许 Bot 绑定，P0 默认 0',
    external_api_enabled   TINYINT       NOT NULL DEFAULT 0 COMMENT '预留：是否允许 API Key 调用，P0 默认 0',
    create_by              VARCHAR(64)   NOT NULL DEFAULT '' COMMENT '创建人',
    create_time            DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by              VARCHAR(64)   NOT NULL DEFAULT '' COMMENT '更新人',
    update_time            DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted                TINYINT       NOT NULL DEFAULT 0 COMMENT '逻辑删除：0否 1是',
    KEY idx_wf_workflow_name (name),
    KEY idx_wf_workflow_status (status)
) COMMENT '工作流定义';

CREATE TABLE IF NOT EXISTS wf_workflow_version (
    version_id     BIGINT        NOT NULL PRIMARY KEY COMMENT '版本主键',
    workflow_id    BIGINT        NOT NULL COMMENT '所属工作流 wf_workflow.workflow_id',
    version_no     INT           NOT NULL DEFAULT 1 COMMENT '版本序号，从 1 递增',
    graph_json     LONGTEXT      NOT NULL COMMENT 'DAG JSON DSL（nodes/edges）',
    checksum       VARCHAR(64)   NOT NULL DEFAULT '' COMMENT 'graph_json SHA-256',
    is_draft       TINYINT       NOT NULL DEFAULT 1 COMMENT '是否当前编辑草稿：1是 0否',
    remark         VARCHAR(512)  NOT NULL DEFAULT '' COMMENT '版本备注',
    create_by      VARCHAR(64)   NOT NULL DEFAULT '' COMMENT '创建人',
    create_time    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    KEY idx_wf_workflow_version_wf_id (workflow_id),
    KEY idx_wf_workflow_version_draft (workflow_id, is_draft)
) COMMENT '工作流版本（草稿/发布）';

CREATE TABLE IF NOT EXISTS wf_run (
    run_id           BIGINT        NOT NULL PRIMARY KEY COMMENT '运行实例主键',
    workflow_id      BIGINT        NOT NULL COMMENT '工作流 wf_workflow.workflow_id',
    version_id       BIGINT        NOT NULL COMMENT '运行版本 wf_workflow_version.version_id',
    trigger_type     VARCHAR(16)   NOT NULL DEFAULT 'DEBUG' COMMENT 'DEBUG/ASYNC/API',
    run_mode         VARCHAR(16)   NOT NULL DEFAULT 'SYNC' COMMENT 'SYNC/ASYNC',
    status           VARCHAR(16)   NOT NULL DEFAULT 'QUEUED' COMMENT 'QUEUED/RUNNING/SUCCESS/FAILED/CANCELLED',
    inputs_json      LONGTEXT      NULL COMMENT '运行入参 JSON',
    outputs_json     LONGTEXT      NULL COMMENT '运行出参 JSON',
    error_msg        VARCHAR(2000) NULL COMMENT '失败原因',
    duration_ms      BIGINT        NULL COMMENT '耗时毫秒',
    stream_enabled   TINYINT       NOT NULL DEFAULT 0 COMMENT '是否启用 SSE 流式：0否 1是',
    start_time       DATETIME      NULL COMMENT '开始时间',
    end_time         DATETIME      NULL COMMENT '结束时间',
    create_by        VARCHAR(64)   NOT NULL DEFAULT '' COMMENT '触发人',
    create_time      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    KEY idx_wf_run_workflow_id (workflow_id),
    KEY idx_wf_run_status (status),
    KEY idx_wf_run_create_by (create_by)
) COMMENT '工作流运行实例';

CREATE TABLE IF NOT EXISTS wf_run_step (
    step_id        BIGINT        NOT NULL PRIMARY KEY COMMENT '步骤主键',
    run_id         BIGINT        NOT NULL COMMENT '所属运行 wf_run.run_id',
    node_id        VARCHAR(64)   NOT NULL DEFAULT '' COMMENT '画布节点 ID',
    node_type      VARCHAR(32)   NOT NULL DEFAULT '' COMMENT '节点类型',
    status         VARCHAR(16)   NOT NULL DEFAULT 'RUNNING' COMMENT 'RUNNING/SUCCESS/FAILED/SKIPPED',
    inputs_json    LONGTEXT      NULL COMMENT '节点入参（脱敏后）',
    outputs_json   LONGTEXT      NULL COMMENT '节点出参（脱敏后）',
    error_msg      VARCHAR(2000) NULL COMMENT '失败原因',
    duration_ms    BIGINT        NULL COMMENT '耗时毫秒',
    order_no       INT           NOT NULL DEFAULT 0 COMMENT '执行顺序号',
    create_time    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    KEY idx_wf_run_step_run_id (run_id)
) COMMENT '工作流运行步骤 Trace';

CREATE TABLE IF NOT EXISTS wf_api_key (
    key_id         BIGINT        NOT NULL PRIMARY KEY COMMENT 'API Key 主键',
    workflow_id    BIGINT        NOT NULL COMMENT '绑定工作流 wf_workflow.workflow_id',
    api_key_hash   VARCHAR(128)  NOT NULL DEFAULT '' COMMENT 'API Key 哈希（P0 预留）',
    status         TINYINT       NOT NULL DEFAULT 0 COMMENT '状态：0正常 1停用',
    expire_time    DATETIME      NULL COMMENT '过期时间',
    create_by      VARCHAR(64)   NOT NULL DEFAULT '' COMMENT '创建人',
    create_time    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    KEY idx_wf_api_key_workflow_id (workflow_id)
) COMMENT '工作流对外 API Key（P0 建表不启用）';

-- 工作流顶级目录
INSERT IGNORE INTO sys_menu (menu_id, parent_id, menu_type, menu_name, order_num, path, component, query, route_name, is_frame, is_cache, visible, status, perms, icon, remark, del_flag, create_by, create_time)
VALUES (2310, -1, 'M', '工作流', 7, '/workflow', 'Layout', NULL, 'Workflow', '0', '0', '0', '0', NULL, 'connection', 'AI 工作流编排与运行', '0', 'system', CURRENT_TIMESTAMP);

INSERT IGNORE INTO sys_menu (menu_id, parent_id, menu_type, menu_name, order_num, path, component, query, route_name, is_frame, is_cache, visible, status, perms, icon, remark, del_flag, create_by, create_time)
VALUES (2311, 2310, 'C', '工作流列表', 1, 'list', 'workflow/list/index', NULL, 'WfWorkflowList', '0', '0', '0', '0', 'workflow:list', 'list', '工作流 CRUD', '0', 'system', CURRENT_TIMESTAMP);

INSERT IGNORE INTO sys_menu (menu_id, parent_id, menu_type, menu_name, order_num, path, component, query, route_name, is_frame, is_cache, visible, status, perms, icon, remark, del_flag, create_by, create_time)
VALUES (2312, 2311, 'F', '工作流查询', 1, '', NULL, NULL, NULL, '0', '0', '0', '0', 'workflow:query', '#', NULL, '0', 'system', CURRENT_TIMESTAMP);

INSERT IGNORE INTO sys_menu (menu_id, parent_id, menu_type, menu_name, order_num, path, component, query, route_name, is_frame, is_cache, visible, status, perms, icon, remark, del_flag, create_by, create_time)
VALUES (2313, 2311, 'F', '工作流新增', 2, '', NULL, NULL, NULL, '0', '0', '0', '0', 'workflow:add', '#', NULL, '0', 'system', CURRENT_TIMESTAMP);

INSERT IGNORE INTO sys_menu (menu_id, parent_id, menu_type, menu_name, order_num, path, component, query, route_name, is_frame, is_cache, visible, status, perms, icon, remark, del_flag, create_by, create_time)
VALUES (2314, 2311, 'F', '工作流修改', 3, '', NULL, NULL, NULL, '0', '0', '0', '0', 'workflow:edit', '#', NULL, '0', 'system', CURRENT_TIMESTAMP);

INSERT IGNORE INTO sys_menu (menu_id, parent_id, menu_type, menu_name, order_num, path, component, query, route_name, is_frame, is_cache, visible, status, perms, icon, remark, del_flag, create_by, create_time)
VALUES (2315, 2311, 'F', '工作流发布', 4, '', NULL, NULL, NULL, '0', '0', '0', '0', 'workflow:publish', '#', NULL, '0', 'system', CURRENT_TIMESTAMP);

INSERT IGNORE INTO sys_menu (menu_id, parent_id, menu_type, menu_name, order_num, path, component, query, route_name, is_frame, is_cache, visible, status, perms, icon, remark, del_flag, create_by, create_time)
VALUES (2316, 2311, 'F', '工作流删除', 5, '', NULL, NULL, NULL, '0', '0', '0', '0', 'workflow:remove', '#', NULL, '0', 'system', CURRENT_TIMESTAMP);

INSERT IGNORE INTO sys_menu (menu_id, parent_id, menu_type, menu_name, order_num, path, component, query, route_name, is_frame, is_cache, visible, status, perms, icon, remark, del_flag, create_by, create_time)
VALUES (2317, 2311, 'F', '工作流运行', 6, '', NULL, NULL, NULL, '0', '0', '0', '0', 'workflow:run', '#', NULL, '0', 'system', CURRENT_TIMESTAMP);

INSERT IGNORE INTO sys_menu (menu_id, parent_id, menu_type, menu_name, order_num, path, component, query, route_name, is_frame, is_cache, visible, status, perms, icon, remark, del_flag, create_by, create_time)
VALUES (2318, 2310, 'C', '工作流设计', 2, 'design/:id', 'workflow/design/index', NULL, 'WfWorkflowDesign', '0', '0', '1', '0', 'workflow:edit', 'edit', 'Vue Flow 画布设计器', '0', 'system', CURRENT_TIMESTAMP);

INSERT IGNORE INTO sys_menu (menu_id, parent_id, menu_type, menu_name, order_num, path, component, query, route_name, is_frame, is_cache, visible, status, perms, icon, remark, del_flag, create_by, create_time)
VALUES (2319, 2310, 'C', '运行记录', 3, 'run', 'workflow/run/index', NULL, 'WfRunList', '0', '0', '0', '0', 'workflow:list', 'time', '工作流运行历史', '0', 'system', CURRENT_TIMESTAMP);

INSERT IGNORE INTO sys_menu (menu_id, parent_id, menu_type, menu_name, order_num, path, component, query, route_name, is_frame, is_cache, visible, status, perms, icon, remark, del_flag, create_by, create_time)
VALUES (2320, 2319, 'F', '运行记录查询', 1, '', NULL, NULL, NULL, '0', '0', '0', '0', 'workflow:query', '#', NULL, '0', 'system', CURRENT_TIMESTAMP);

INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (1, 2310);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (1, 2311);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (1, 2312);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (1, 2313);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (1, 2314);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (1, 2315);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (1, 2316);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (1, 2317);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (1, 2318);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (1, 2319);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (1, 2320);
