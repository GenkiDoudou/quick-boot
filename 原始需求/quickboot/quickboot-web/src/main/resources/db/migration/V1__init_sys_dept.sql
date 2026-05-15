-- =============================================================================
-- Flyway V1：部门表 sys_dept 及样例数据（规格 002-dept-mgmt / data-model.md）
-- =============================================================================
-- 部门表（逻辑删除 del_flag：0 正常 1 已删除）
CREATE TABLE IF NOT EXISTS sys_dept (
    dept_id     BIGINT       NOT NULL COMMENT '部门id',
    parent_id   BIGINT       NOT NULL DEFAULT 0 COMMENT '父部门id，0 表示根',
    dept_name   VARCHAR(100) NOT NULL COMMENT '部门名称',
    order_num   INT          NOT NULL DEFAULT 0 COMMENT '同级排序',
    leader      VARCHAR(100) NULL COMMENT '负责人（自由文本）',
    del_flag    TINYINT      NOT NULL DEFAULT 0 COMMENT '删除标志 0正常 1删除',
    create_time DATETIME     NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME     NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (dept_id),
    KEY idx_parent_id (parent_id),
    KEY idx_del_flag (del_flag)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='部门表';

-- 验收样例数据（固定 id，便于本地联调）
INSERT INTO sys_dept (dept_id, parent_id, dept_name, order_num, leader, del_flag) VALUES
(100, 0, '总公司', 0, '负责人A', 0),
(101, 100, '研发部', 1, NULL, 0),
(102, 100, '市场部', 2, '负责人B', 0),
(103, 101, '后端组', 0, '小李', 0),
(104, 101, '前端组', 1, NULL, 0);
