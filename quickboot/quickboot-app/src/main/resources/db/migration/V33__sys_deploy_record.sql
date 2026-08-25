-- 发布记录表 + 监控菜单（parent=2100，menu_id 2180–2182）

CREATE TABLE IF NOT EXISTS sys_deploy_record (
  record_id      BIGINT       NOT NULL COMMENT '主键',
  app_name       VARCHAR(64)  NOT NULL DEFAULT '' COMMENT '应用名，如 quickboot',
  env            VARCHAR(32)  NOT NULL DEFAULT '' COMMENT '环境 test/prod/dev',
  operate        VARCHAR(32)  NOT NULL DEFAULT '' COMMENT 'deploy/rollback',
  branch         VARCHAR(128) NOT NULL DEFAULT '' COMMENT 'Git 分支',
  hosts          VARCHAR(512) NOT NULL DEFAULT '' COMMENT '部署主机，逗号分隔',
  build_number   VARCHAR(32)  NOT NULL DEFAULT '' COMMENT 'Jenkins 构建号',
  build_url      VARCHAR(512) NOT NULL DEFAULT '' COMMENT '构建链接',
  git_commit     VARCHAR(64)  NOT NULL DEFAULT '' COMMENT 'Git commit',
  release_notes  TEXT         NULL COMMENT '发版说明（手填+git log）',
  status         CHAR(1)      NOT NULL DEFAULT '0' COMMENT '0=成功',
  del_flag       CHAR(1)      NOT NULL DEFAULT '0' COMMENT '0正常 1删除',
  remark         VARCHAR(500) NULL COMMENT '备注',
  create_by      VARCHAR(64)  NULL COMMENT '创建者',
  create_time    DATETIME     NULL COMMENT '创建时间',
  update_by      VARCHAR(64)  NULL COMMENT '更新者',
  update_time    DATETIME     NULL COMMENT '更新时间',
  PRIMARY KEY (record_id),
  KEY idx_deploy_record_env_time (env, create_time),
  KEY idx_deploy_record_app (app_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Jenkins 发布记录';

INSERT INTO sys_menu (menu_id, parent_id, menu_name, menu_type, path, component, route_name, perms, icon, order_num, status, del_flag, create_time)
SELECT 2180, 2100, '发布记录', 'C', 'deployRecord', 'monitor/deployRecord/index', 'MonitorDeployRecord', 'monitor:deployRecord:list', 'documentation', 55, '0', '0', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2180);

INSERT INTO sys_menu (menu_id, parent_id, menu_name, menu_type, path, component, route_name, perms, icon, order_num, status, del_flag, create_time)
SELECT 2181, 2180, '发布记录查询', 'F', NULL, NULL, NULL, 'monitor:deployRecord:list', NULL, 1, '0', '0', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2181);

INSERT INTO sys_menu (menu_id, parent_id, menu_name, menu_type, path, component, route_name, perms, icon, order_num, status, del_flag, create_time)
SELECT 2182, 2180, '发布记录详情', 'F', NULL, NULL, NULL, 'monitor:deployRecord:query', NULL, 2, '0', '0', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2182);

INSERT INTO sys_role_menu (role_id, menu_id) SELECT 1, 2180 WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 1 AND menu_id = 2180);
INSERT INTO sys_role_menu (role_id, menu_id) SELECT 1, 2181 WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 1 AND menu_id = 2181);
INSERT INTO sys_role_menu (role_id, menu_id) SELECT 1, 2182 WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 1 AND menu_id = 2182);
