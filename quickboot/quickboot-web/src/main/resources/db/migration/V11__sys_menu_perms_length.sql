-- 菜单 perms 支持多个权限标识，英文逗号分隔存储；原 VARCHAR(100) 易溢出。
-- MySQL 使用 MODIFY；H2 开发库（MODE=MySQL）亦兼容 MODIFY COLUMN
ALTER TABLE sys_menu MODIFY COLUMN perms VARCHAR(500);
