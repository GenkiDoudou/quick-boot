-- 补齐超级管理员（role_id=1）对 V1 核心「系统管理」目录/菜单及缺失按钮的授权。
-- 背景：buildRouters 严格按 sys_role_menu；V1 种子未写绑定，V2+ 只绑了部分 F，导致清库后侧栏缺系统管理整树。

-- 目录 / 页面（M/C）
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, 2000 WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 1 AND menu_id = 2000);
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, 2001 WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 1 AND menu_id = 2001);
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, 2010 WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 1 AND menu_id = 2010);
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, 2020 WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 1 AND menu_id = 2020);

-- 客户端管理按钮（2006/2007/2008 已在 V2–V4）
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, 2002 WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 1 AND menu_id = 2002);
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, 2003 WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 1 AND menu_id = 2003);
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, 2004 WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 1 AND menu_id = 2004);
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, 2005 WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 1 AND menu_id = 2005);

-- 角色管理按钮（2015–2018 已在 V2/V5）
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, 2011 WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 1 AND menu_id = 2011);
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, 2012 WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 1 AND menu_id = 2012);
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, 2013 WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 1 AND menu_id = 2013);
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, 2014 WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 1 AND menu_id = 2014);

-- 菜单管理按钮（2025/2026 已在 V6）
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, 2021 WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 1 AND menu_id = 2021);
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, 2022 WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 1 AND menu_id = 2022);
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, 2023 WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 1 AND menu_id = 2023);
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, 2024 WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 1 AND menu_id = 2024);
