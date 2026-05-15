-- 菜单 perms 支持多个权限标识，英文逗号分隔存储；原 VARCHAR(100) 易溢出。
ALTER TABLE sys_menu ALTER COLUMN perms VARCHAR(500);
