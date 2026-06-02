-- 文件管理：文件元数据表、菜单与按钮权限

CREATE TABLE IF NOT EXISTS sys_file (
    file_id           BIGINT        NOT NULL PRIMARY KEY COMMENT '文件主键',
    original_name     VARCHAR(255)  NOT NULL DEFAULT '' COMMENT '原始文件名',
    ext               VARCHAR(32)   NOT NULL DEFAULT '' COMMENT '扩展名（小写，不含点）',
    size_bytes        BIGINT        NOT NULL DEFAULT 0 COMMENT '文件大小（字节）',
    content_type      VARCHAR(128)  NULL COMMENT 'Content-Type',
    classify          VARCHAR(64)   NOT NULL DEFAULT '' COMMENT '分类（FileTemplate classify）',
    relative_path     VARCHAR(500)  NOT NULL COMMENT '相对路径（FileTemplate.upload 返回值，唯一）',
    uploader_user_id  BIGINT        NOT NULL DEFAULT 0 COMMENT '上传人用户ID（无登录态为 0）',
    uploader_user_name VARCHAR(64)  NOT NULL DEFAULT '' COMMENT '上传人用户名（无登录态为空）',
    upload_time       DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
    deleted           TINYINT       NOT NULL DEFAULT 0 COMMENT '是否删除：0否 1是',
    delete_by         VARCHAR(64)   NULL COMMENT '删除人',
    delete_time       DATETIME      NULL COMMENT '删除时间',
    UNIQUE KEY uk_sys_file_relative_path (relative_path),
    KEY idx_sys_file_upload_time (upload_time),
    KEY idx_sys_file_uploader_user_id (uploader_user_id),
    KEY idx_sys_file_original_name (original_name)
) COMMENT '文件管理：上传文件元数据';

-- 文件管理菜单与按钮权限（挂载系统管理 parent_id=2000）
INSERT IGNORE INTO sys_menu (menu_id, parent_id, menu_type, menu_name, order_num, path, component, query, route_name, is_frame, is_cache, visible, status, perms, icon, remark, del_flag, create_by, create_time)
VALUES (2258, 2000, 'C', '文件管理', 20, 'file', 'system/file/index', NULL, 'SysFile', '0', '0', '0', '0', 'system:file:list', 'folder', '系统上传文件统一管理（上传/预览/下载/删除）', '0', 'system', CURRENT_TIMESTAMP);

INSERT IGNORE INTO sys_menu (menu_id, parent_id, menu_type, menu_name, order_num, path, component, query, route_name, is_frame, is_cache, visible, status, perms, icon, remark, del_flag, create_by, create_time)
VALUES (2259, 2258, 'F', '文件查询', 1, '', NULL, NULL, NULL, '0', '0', '0', '0', 'system:file:list', '#', NULL, '0', 'system', CURRENT_TIMESTAMP);

INSERT IGNORE INTO sys_menu (menu_id, parent_id, menu_type, menu_name, order_num, path, component, query, route_name, is_frame, is_cache, visible, status, perms, icon, remark, del_flag, create_by, create_time)
VALUES (2260, 2258, 'F', '文件上传', 2, '', NULL, NULL, NULL, '0', '0', '0', '0', 'system:file:upload', '#', NULL, '0', 'system', CURRENT_TIMESTAMP);

INSERT IGNORE INTO sys_menu (menu_id, parent_id, menu_type, menu_name, order_num, path, component, query, route_name, is_frame, is_cache, visible, status, perms, icon, remark, del_flag, create_by, create_time)
VALUES (2261, 2258, 'F', '文件预览', 3, '', NULL, NULL, NULL, '0', '0', '0', '0', 'system:file:view', '#', NULL, '0', 'system', CURRENT_TIMESTAMP);

INSERT IGNORE INTO sys_menu (menu_id, parent_id, menu_type, menu_name, order_num, path, component, query, route_name, is_frame, is_cache, visible, status, perms, icon, remark, del_flag, create_by, create_time)
VALUES (2262, 2258, 'F', '文件下载', 4, '', NULL, NULL, NULL, '0', '0', '0', '0', 'system:file:download', '#', NULL, '0', 'system', CURRENT_TIMESTAMP);

INSERT IGNORE INTO sys_menu (menu_id, parent_id, menu_type, menu_name, order_num, path, component, query, route_name, is_frame, is_cache, visible, status, perms, icon, remark, del_flag, create_by, create_time)
VALUES (2263, 2258, 'F', '文件删除', 5, '', NULL, NULL, NULL, '0', '0', '0', '0', 'system:file:remove', '#', NULL, '0', 'system', CURRENT_TIMESTAMP);

INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (1, 2258);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (1, 2259);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (1, 2260);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (1, 2261);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (1, 2262);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (1, 2263);

