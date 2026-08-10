-- 超级管理员默认按钮权限（Flyway 只执行一次；之后在页面取消不会因重启补回）
-- 2006 查看密钥；2015 菜单权限；2016 分配用户
-- WHERE NOT EXISTS 仅兼容「已有库 baseline 后首次跑 V2、行可能已存在」；成功写入 history 后不会再跑

INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, 2006 WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 1 AND menu_id = 2006);

INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, 2015 WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 1 AND menu_id = 2015);

INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, 2016 WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 1 AND menu_id = 2016);
