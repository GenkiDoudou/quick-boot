-- =============================================================================
-- Flyway V2：部门表 status（005-dept-mgmt / data-model.md）
-- 0 启用，1 停用
-- =============================================================================
ALTER TABLE sys_dept
    ADD COLUMN status TINYINT NOT NULL DEFAULT 0 COMMENT '状态 0启用 1停用' AFTER leader;
