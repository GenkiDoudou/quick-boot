-- 部门表：与 MyBatis-Plus 全局逻辑删除一致（del_flag：0 正常，1 已删除）。
-- 顶级 parent_id = -1（业务约定）。

CREATE TABLE IF NOT EXISTS sys_dept (
    dept_id       BIGINT       NOT NULL PRIMARY KEY,
    parent_id     BIGINT       NOT NULL DEFAULT -1,
    dept_name     VARCHAR(100) NOT NULL,
    order_num     INT          NOT NULL DEFAULT 0,
    leader        VARCHAR(50)  NULL,
    phone         VARCHAR(20)  NULL,
    email         VARCHAR(100) NULL,
    status        CHAR(1)      NOT NULL DEFAULT '0',
    remark        VARCHAR(500) NULL,
    del_flag      CHAR(1)      NOT NULL DEFAULT '0',
    create_by     VARCHAR(64)  NULL,
    create_time   DATETIME     NULL DEFAULT CURRENT_TIMESTAMP,
    update_by     VARCHAR(64)  NULL,
    update_time   DATETIME     NULL
);

CREATE INDEX idx_sys_dept_parent_del ON sys_dept (parent_id, del_flag);

INSERT INTO sys_dept (dept_id, parent_id, dept_name, order_num, leader, phone, email, status, remark, del_flag, create_by, create_time)
VALUES (100, -1, '演示总公司', 0, '张总', NULL, NULL, '0', NULL, '0', 'system', CURRENT_TIMESTAMP);

INSERT INTO sys_dept (dept_id, parent_id, dept_name, order_num, leader, phone, email, status, remark, del_flag, create_by, create_time)
VALUES (101, 100, '演示研发部', 1, '李工', NULL, NULL, '0', NULL, '0', 'system', CURRENT_TIMESTAMP);
