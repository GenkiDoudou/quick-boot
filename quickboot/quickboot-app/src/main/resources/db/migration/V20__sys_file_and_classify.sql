-- 文件分类 / 文件管理：表、种子分类、菜单与超级管理员授权

CREATE TABLE IF NOT EXISTS sys_file_classify (
  classify_id        BIGINT        NOT NULL COMMENT '主键',
  classify           VARCHAR(64)   NOT NULL COMMENT '分类键（不可含斜杠；创建后不可改）',
  classify_name      VARCHAR(128)  NOT NULL COMMENT '展示名',
  limit_ext          VARCHAR(512)  NULL COMMENT '允许后缀，逗号分隔；空=内置默认白名单',
  limit_size_bytes   BIGINT        NOT NULL DEFAULT 10485760 COMMENT '单文件上限字节',
  limit_count        INT           NOT NULL DEFAULT 1 COMMENT '单次最多文件数',
  compress_enabled   CHAR(1)       NOT NULL DEFAULT '0' COMMENT '是否开启压缩配置(0否1是；本期不执行压缩)',
  anonymous          CHAR(1)       NOT NULL DEFAULT '0' COMMENT '是否允许匿名上传(0否1是)',
  status             CHAR(1)       NOT NULL DEFAULT '0' COMMENT '状态(0正常1停用)',
  del_flag           CHAR(1)       NOT NULL DEFAULT '0' COMMENT '删除标志(0存在1删除)',
  remark             VARCHAR(500)  NULL,
  create_by          VARCHAR(64)   NULL,
  create_time        TIMESTAMP     NULL,
  update_by          VARCHAR(64)   NULL,
  update_time        TIMESTAMP     NULL,
  PRIMARY KEY (classify_id),
  UNIQUE KEY uk_sys_file_classify_key (classify)
) COMMENT='文件上传分类配置';

CREATE TABLE IF NOT EXISTS sys_file (
  file_id             BIGINT        NOT NULL COMMENT '主键',
  original_name       VARCHAR(255)  NOT NULL COMMENT '原始文件名',
  ext                 VARCHAR(32)   NOT NULL DEFAULT '' COMMENT '扩展名小写无点',
  size_bytes          BIGINT        NOT NULL DEFAULT 0 COMMENT '大小字节',
  content_type        VARCHAR(128)  NULL COMMENT 'MIME',
  classify            VARCHAR(64)   NOT NULL COMMENT '上传分类键',
  relative_path       VARCHAR(512)  NOT NULL COMMENT '存储相对路径',
  uploader_user_id    BIGINT        NOT NULL DEFAULT 0 COMMENT '上传人用户ID',
  uploader_user_name  VARCHAR(64)   NOT NULL DEFAULT '' COMMENT '上传人用户名',
  upload_time         TIMESTAMP     NOT NULL COMMENT '上传时间',
  del_flag            CHAR(1)       NOT NULL DEFAULT '0' COMMENT '删除标志(0存在1删除)',
  remark              VARCHAR(500)  NULL,
  create_by           VARCHAR(64)   NULL,
  create_time         TIMESTAMP     NULL,
  update_by           VARCHAR(64)   NULL,
  update_time         TIMESTAMP     NULL,
  PRIMARY KEY (file_id),
  UNIQUE KEY uk_sys_file_relative_path (relative_path),
  KEY idx_sys_file_upload_time (upload_time),
  KEY idx_sys_file_uploader (uploader_user_id),
  KEY idx_sys_file_classify (classify),
  KEY idx_sys_file_original_name (original_name)
) COMMENT='系统文件元数据（管理端登记）';

-- 默认分类
INSERT INTO sys_file_classify (
  classify_id, classify, classify_name, limit_ext, limit_size_bytes, limit_count,
  compress_enabled, anonymous, status, del_flag, remark, create_time
)
SELECT 1, 'default', '默认', NULL, 10485760, 1, '0', '0', '0', '0', '系统种子分类', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_file_classify WHERE classify = 'default');

-- 文件分类菜单 2080
INSERT INTO sys_menu (menu_id, parent_id, menu_name, menu_type, path, component, route_name, perms, icon, order_num, status, del_flag, create_time)
SELECT 2080, 2000, '文件分类', 'C', 'fileClassify', 'system/fileClassify/index', 'SysFileClassify', 'system:fileClassify:list', 'list', 8, '0', '0', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2080);

INSERT INTO sys_menu (menu_id, parent_id, menu_name, menu_type, path, component, route_name, perms, icon, order_num, status, del_flag, create_time)
SELECT 2081, 2080, '分类查询', 'F', NULL, NULL, NULL, 'system:fileClassify:query', NULL, 1, '0', '0', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2081);

INSERT INTO sys_menu (menu_id, parent_id, menu_name, menu_type, path, component, route_name, perms, icon, order_num, status, del_flag, create_time)
SELECT 2082, 2080, '分类新增', 'F', NULL, NULL, NULL, 'system:fileClassify:add', NULL, 2, '0', '0', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2082);

INSERT INTO sys_menu (menu_id, parent_id, menu_name, menu_type, path, component, route_name, perms, icon, order_num, status, del_flag, create_time)
SELECT 2083, 2080, '分类修改', 'F', NULL, NULL, NULL, 'system:fileClassify:edit', NULL, 3, '0', '0', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2083);

INSERT INTO sys_menu (menu_id, parent_id, menu_name, menu_type, path, component, route_name, perms, icon, order_num, status, del_flag, create_time)
SELECT 2084, 2080, '分类删除', 'F', NULL, NULL, NULL, 'system:fileClassify:remove', NULL, 4, '0', '0', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2084);

-- 文件管理菜单 2090
INSERT INTO sys_menu (menu_id, parent_id, menu_name, menu_type, path, component, route_name, perms, icon, order_num, status, del_flag, create_time)
SELECT 2090, 2000, '文件管理', 'C', 'file', 'system/file/index', 'SysFile', 'system:file:list', 'upload', 9, '0', '0', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2090);

INSERT INTO sys_menu (menu_id, parent_id, menu_name, menu_type, path, component, route_name, perms, icon, order_num, status, del_flag, create_time)
SELECT 2091, 2090, '文件上传', 'F', NULL, NULL, NULL, 'system:file:upload', NULL, 1, '0', '0', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2091);

INSERT INTO sys_menu (menu_id, parent_id, menu_name, menu_type, path, component, route_name, perms, icon, order_num, status, del_flag, create_time)
SELECT 2092, 2090, '文件预览', 'F', NULL, NULL, NULL, 'system:file:view', NULL, 2, '0', '0', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2092);

INSERT INTO sys_menu (menu_id, parent_id, menu_name, menu_type, path, component, route_name, perms, icon, order_num, status, del_flag, create_time)
SELECT 2093, 2090, '文件下载', 'F', NULL, NULL, NULL, 'system:file:download', NULL, 3, '0', '0', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2093);

INSERT INTO sys_menu (menu_id, parent_id, menu_name, menu_type, path, component, route_name, perms, icon, order_num, status, del_flag, create_time)
SELECT 2094, 2090, '文件删除', 'F', NULL, NULL, NULL, 'system:file:remove', NULL, 4, '0', '0', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2094);

-- 超级管理员授权
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, 2080 WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 1 AND menu_id = 2080);
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, 2081 WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 1 AND menu_id = 2081);
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, 2082 WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 1 AND menu_id = 2082);
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, 2083 WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 1 AND menu_id = 2083);
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, 2084 WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 1 AND menu_id = 2084);
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, 2090 WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 1 AND menu_id = 2090);
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, 2091 WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 1 AND menu_id = 2091);
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, 2092 WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 1 AND menu_id = 2092);
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, 2093 WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 1 AND menu_id = 2093);
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, 2094 WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 1 AND menu_id = 2094);
