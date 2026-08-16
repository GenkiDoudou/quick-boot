/*
 * 下线 clientTrack / 全链路监控菜单（表 DROP 见 V26）。
 * 并将 Lite Trace 菜单显示名改为「请求链路」；隐藏独立「链路查询台」。
 * menu_id：2163–2168 行为/全链路；2171–2174 Lite Trace。
 */

UPDATE sys_menu SET status = '1', menu_name = CONCAT('[下线]', menu_name)
WHERE menu_id IN (2163, 2164, 2165, 2166, 2167, 2168)
  AND menu_name NOT LIKE '[下线]%';

DELETE FROM sys_role_menu WHERE menu_id IN (2163, 2164, 2165, 2166, 2167, 2168);

UPDATE sys_menu SET menu_name = '请求链路', path = 'liteTrace', component = 'monitor/liteTrace/index'
WHERE menu_id = 2171;

UPDATE sys_menu SET status = '1', menu_name = CONCAT('[下线]', menu_name)
WHERE menu_id IN (2173, 2174)
  AND menu_name NOT LIKE '[下线]%';

DELETE FROM sys_role_menu WHERE menu_id IN (2173, 2174);
