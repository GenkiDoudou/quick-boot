-- 顶级目录 path 须以 / 开头，否则 Vue Router 4 addRoute 报错
UPDATE sys_menu SET path = '/visual' WHERE menu_id = 3000 AND path = 'visual';
