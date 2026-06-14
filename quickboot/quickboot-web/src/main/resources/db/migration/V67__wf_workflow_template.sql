-- 工作流模板库：业务表 + 菜单权限（2336-2340）

CREATE TABLE IF NOT EXISTS wf_workflow_template (
    template_id    BIGINT        NOT NULL PRIMARY KEY COMMENT '模板主键',
    code           VARCHAR(64)   NOT NULL COMMENT '唯一编码，新建工作流时传入',
    name           VARCHAR(128)  NOT NULL DEFAULT '' COMMENT '展示名称',
    description    VARCHAR(512)  NOT NULL DEFAULT '' COMMENT '描述说明',
    graph_json     LONGTEXT      NOT NULL COMMENT '图 DSL JSON（nodes/edges/version）',
    builtin        TINYINT       NOT NULL DEFAULT 0 COMMENT '是否内置：0否 1是（内置不可删除）',
    status         VARCHAR(16)   NOT NULL DEFAULT 'ENABLED' COMMENT 'ENABLED启用 / DISABLED停用',
    sort_order     INT           NOT NULL DEFAULT 0 COMMENT '下拉排序，升序',
    create_by      VARCHAR(64)   NOT NULL DEFAULT '' COMMENT '创建人',
    create_time    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by      VARCHAR(64)   NOT NULL DEFAULT '' COMMENT '更新人',
    update_time    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted        TINYINT       NOT NULL DEFAULT 0 COMMENT '逻辑删除：0否 1是',
    UNIQUE KEY uk_wf_workflow_template_code (code),
    KEY idx_wf_workflow_template_status (status),
    KEY idx_wf_workflow_template_sort (sort_order)
) COMMENT '工作流内置/自定义模板';

INSERT INTO sys_menu (menu_id, parent_id, menu_type, menu_name, order_num, path, component, query, route_name, is_frame, is_cache, visible, status, perms, icon, remark, del_flag, create_by, create_time)
VALUES (2336, 2310, 'C', '工作流模板', 4, 'template', 'workflow/template/index', NULL, 'WfWorkflowTemplate', '0', '0', '0', '0', 'workflow:template:list', 'document', '工作流模板 CRUD 与图编辑', '0', 'system', CURRENT_TIMESTAMP);

INSERT INTO sys_menu (menu_id, parent_id, menu_type, menu_name, order_num, path, component, query, route_name, is_frame, is_cache, visible, status, perms, icon, remark, del_flag, create_by, create_time)
VALUES (2337, 2336, 'F', '模板查询', 1, '', NULL, NULL, NULL, '0', '0', '0', '0', 'workflow:template:query', '#', NULL, '0', 'system', CURRENT_TIMESTAMP);

INSERT INTO sys_menu (menu_id, parent_id, menu_type, menu_name, order_num, path, component, query, route_name, is_frame, is_cache, visible, status, perms, icon, remark, del_flag, create_by, create_time)
VALUES (2338, 2336, 'F', '模板新增', 2, '', NULL, NULL, NULL, '0', '0', '0', '0', 'workflow:template:add', '#', NULL, '0', 'system', CURRENT_TIMESTAMP);

INSERT INTO sys_menu (menu_id, parent_id, menu_type, menu_name, order_num, path, component, query, route_name, is_frame, is_cache, visible, status, perms, icon, remark, del_flag, create_by, create_time)
VALUES (2339, 2336, 'F', '模板修改', 3, '', NULL, NULL, NULL, '0', '0', '0', '0', 'workflow:template:edit', '#', NULL, '0', 'system', CURRENT_TIMESTAMP);

INSERT INTO sys_menu (menu_id, parent_id, menu_type, menu_name, order_num, path, component, query, route_name, is_frame, is_cache, visible, status, perms, icon, remark, del_flag, create_by, create_time)
VALUES (2340, 2336, 'F', '模板删除', 4, '', NULL, NULL, NULL, '0', '0', '0', '0', 'workflow:template:remove', '#', NULL, '0', 'system', CURRENT_TIMESTAMP);
