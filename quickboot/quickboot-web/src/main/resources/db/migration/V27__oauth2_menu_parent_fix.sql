-- 修复 V26：OAuth 菜单应挂在「系统管理」目录（menu_id=2000，见 V5__sys_menu.sql），勿用不存在的 1000

UPDATE sys_menu SET parent_id = 2000 WHERE menu_id IN (2260, 2270) AND parent_id <> 2000;
