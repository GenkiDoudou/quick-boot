-- 修复「AI 能力 / MCP 管理」菜单因 menu_id 与代码生成(2301)、工作流按钮(2320) 冲突导致不可见的问题。
-- 影响：sys_menu、sys_role_menu；依赖 V60 工作流、V18 代码生成、V63/V65 AI 能力迁移已执行。
-- 策略：释放 2320 给「AI 能力」根菜单；恢复 2301 代码生成；新建 2340 MCP 管理独立菜单。

-- 1) 若 2320 被工作流「运行记录查询」占用，迁到 2390
INSERT IGNORE INTO sys_menu (menu_id, parent_id, menu_type, menu_name, order_num, path, component, query, route_name, is_frame, is_cache, visible, status, perms, icon, remark, del_flag, create_by, create_time)
SELECT 2390, 2319, 'F', '运行记录查询', 1, '', NULL, NULL, NULL, '0', '0', '0', '0', 'workflow:query', '#', '自 menu_id=2320 迁出，避免与 AI 能力根菜单冲突', '0', 'system', CURRENT_TIMESTAMP
FROM sys_menu
WHERE menu_id = 2320 AND parent_id = 2319 AND perms = 'workflow:query'
LIMIT 1;

INSERT IGNORE INTO sys_role_menu (role_id, menu_id)
SELECT rm.role_id, 2390
FROM sys_role_menu rm
INNER JOIN sys_menu m ON m.menu_id = 2320 AND m.parent_id = 2319 AND m.perms = 'workflow:query'
WHERE rm.menu_id = 2320;

DELETE rm FROM sys_role_menu rm
INNER JOIN sys_menu m ON m.menu_id = 2320 AND m.parent_id = 2319 AND m.perms = 'workflow:query'
WHERE rm.menu_id = 2320;

DELETE FROM sys_menu
WHERE menu_id = 2320 AND parent_id = 2319 AND perms = 'workflow:query';

-- 2) 确保「AI 能力」根菜单占用 2320
INSERT IGNORE INTO sys_menu (menu_id, parent_id, menu_type, menu_name, order_num, path, component, query, route_name, is_frame, is_cache, visible, status, perms, icon, remark, del_flag, create_by, create_time)
VALUES (2320, -1, 'M', 'AI 能力', 5, '/ai', 'Layout', NULL, 'AiCapability', '0', '0', '0', '0', NULL, 'cpu', 'AI 大模型、MCP 与提示词管理', '0', 'system', CURRENT_TIMESTAMP);

UPDATE sys_menu
SET parent_id = -1,
    menu_type = 'M',
    menu_name = 'AI 能力',
    order_num = 5,
    path = '/ai',
    component = 'Layout',
    route_name = 'AiCapability',
    perms = NULL,
    icon = 'cpu',
    visible = '0',
    status = '0',
    remark = 'AI 大模型、MCP 与提示词管理'
WHERE menu_id = 2320;

-- 3) 恢复 2301 为「代码生成」（曾被误 UPDATE 为 MCP 路径）
UPDATE sys_menu
SET parent_id = 2300,
    menu_type = 'C',
    menu_name = '代码生成',
    order_num = 1,
    path = 'gen',
    component = 'tool/gen/index',
    route_name = 'ToolGen',
    perms = 'tool:gen:list',
    icon = 'code',
    visible = '0',
    status = '0',
    remark = NULL
WHERE menu_id = 2301;

UPDATE sys_menu SET parent_id = 2301, menu_type = 'F', menu_name = '代码生成导入', perms = 'tool:gen:import' WHERE menu_id = 2302;
UPDATE sys_menu SET parent_id = 2301, menu_type = 'F', menu_name = '代码生成建表', perms = 'tool:gen:create' WHERE menu_id = 2303;
UPDATE sys_menu SET parent_id = 2301, menu_type = 'F', menu_name = '代码生成修改', perms = 'tool:gen:edit' WHERE menu_id = 2304;
UPDATE sys_menu SET parent_id = 2301, menu_type = 'F', menu_name = '代码生成删除', perms = 'tool:gen:remove' WHERE menu_id = 2305;
UPDATE sys_menu SET parent_id = 2301, menu_type = 'F', menu_name = '代码生成预览', perms = 'tool:gen:preview' WHERE menu_id = 2306;
UPDATE sys_menu SET parent_id = 2301, menu_type = 'F', menu_name = '代码生成代码', perms = 'tool:gen:code' WHERE menu_id = 2307;

-- 4) 新建 MCP 管理菜单（2340+，避免再占用 2301/2308）
INSERT IGNORE INTO sys_menu (menu_id, parent_id, menu_type, menu_name, order_num, path, component, query, route_name, is_frame, is_cache, visible, status, perms, icon, remark, del_flag, create_by, create_time)
VALUES (2340, 2320, 'C', 'MCP 管理', 2, 'mcp', 'ai/mcp/index', NULL, 'AiMcp', '0', '0', '0', '0', 'ai:mcp:list', 'connection', '外部 MCP 服务配置', '0', 'system', CURRENT_TIMESTAMP);

INSERT IGNORE INTO sys_menu (menu_id, parent_id, menu_type, menu_name, order_num, path, component, query, route_name, is_frame, is_cache, visible, status, perms, icon, remark, del_flag, create_by, create_time)
VALUES (2341, 2340, 'F', 'MCP 查询', 1, '', NULL, NULL, NULL, '0', '0', '0', '0', 'ai:mcp:query', '#', NULL, '0', 'system', CURRENT_TIMESTAMP);

INSERT IGNORE INTO sys_menu (menu_id, parent_id, menu_type, menu_name, order_num, path, component, query, route_name, is_frame, is_cache, visible, status, perms, icon, remark, del_flag, create_by, create_time)
VALUES (2342, 2340, 'F', 'MCP 新增', 2, '', NULL, NULL, NULL, '0', '0', '0', '0', 'ai:mcp:add', '#', NULL, '0', 'system', CURRENT_TIMESTAMP);

INSERT IGNORE INTO sys_menu (menu_id, parent_id, menu_type, menu_name, order_num, path, component, query, route_name, is_frame, is_cache, visible, status, perms, icon, remark, del_flag, create_by, create_time)
VALUES (2343, 2340, 'F', 'MCP 修改', 3, '', NULL, NULL, NULL, '0', '0', '0', '0', 'ai:mcp:edit', '#', NULL, '0', 'system', CURRENT_TIMESTAMP);

INSERT IGNORE INTO sys_menu (menu_id, parent_id, menu_type, menu_name, order_num, path, component, query, route_name, is_frame, is_cache, visible, status, perms, icon, remark, del_flag, create_by, create_time)
VALUES (2344, 2340, 'F', 'MCP 删除', 4, '', NULL, NULL, NULL, '0', '0', '0', '0', 'ai:mcp:remove', '#', NULL, '0', 'system', CURRENT_TIMESTAMP);

INSERT IGNORE INTO sys_menu (menu_id, parent_id, menu_type, menu_name, order_num, path, component, query, route_name, is_frame, is_cache, visible, status, perms, icon, remark, del_flag, create_by, create_time)
VALUES (2345, 2340, 'F', 'MCP 测试', 5, '', NULL, NULL, NULL, '0', '0', '0', '0', 'ai:mcp:test', '#', NULL, '0', 'system', CURRENT_TIMESTAMP);

INSERT IGNORE INTO sys_menu (menu_id, parent_id, menu_type, menu_name, order_num, path, component, query, route_name, is_frame, is_cache, visible, status, perms, icon, remark, del_flag, create_by, create_time)
VALUES (2346, 2340, 'F', 'MCP 导出', 6, '', NULL, NULL, NULL, '0', '0', '0', '0', 'ai:mcp:export', '#', NULL, '0', 'system', CURRENT_TIMESTAMP);

INSERT IGNORE INTO sys_menu (menu_id, parent_id, menu_type, menu_name, order_num, path, component, query, route_name, is_frame, is_cache, visible, status, perms, icon, remark, del_flag, create_by, create_time)
VALUES (2347, 2340, 'F', 'MCP 导出密钥', 7, '', NULL, NULL, NULL, '0', '0', '0', '0', 'ai:mcp:export:secrets', '#', NULL, '0', 'system', CURRENT_TIMESTAMP);

-- 5) 校正 AI 能力子菜单挂载
UPDATE sys_menu SET parent_id = 2320, order_num = 1 WHERE menu_id = 2321;
UPDATE sys_menu SET parent_id = 2320, order_num = 3 WHERE menu_id = 2330;

-- 6) 管理员角色授权
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (1, 2320);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (1, 2340);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (1, 2341);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (1, 2342);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (1, 2343);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (1, 2344);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (1, 2345);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (1, 2346);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (1, 2347);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (1, 2390);
