/*
 * DROP 已下线的前端行为批次表；概览菜单名与产品树对齐。
 */

DROP TABLE IF EXISTS sys_client_track;

UPDATE sys_menu SET menu_name = '监控概览'
WHERE menu_id = 2169;
