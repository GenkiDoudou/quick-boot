-- 代码生成默认参数（可在「参数设置」中修改，config_type=1 为内置）

INSERT INTO sys_config (config_id, config_name, config_key, config_value, config_type, remark, del_flag, create_by, create_time)
VALUES (900010, '代码生成默认作者', 'qc.gen.author', 'quickboot', '1', '生成代码注释作者', '0', 'system', CURRENT_TIMESTAMP);

INSERT INTO sys_config (config_id, config_name, config_key, config_value, config_type, remark, del_flag, create_by, create_time)
VALUES (900011, '代码生成默认包路径', 'qc.gen.package-name', 'io.github.genkidoudou.web', '1', 'Java 根包名', '0', 'system', CURRENT_TIMESTAMP);

INSERT INTO sys_config (config_id, config_name, config_key, config_value, config_type, remark, del_flag, create_by, create_time)
VALUES (900012, '代码生成默认模块名', 'qc.gen.module-name', 'system', '1', '生成模块名', '0', 'system', CURRENT_TIMESTAMP);

INSERT INTO sys_config (config_id, config_name, config_key, config_value, config_type, remark, del_flag, create_by, create_time)
VALUES (900013, '代码生成默认模板类型', 'qc.gen.tpl-category', 'crud', '1', 'crud 单表；tree 仅保存配置', '0', 'system', CURRENT_TIMESTAMP);

INSERT INTO sys_config (config_id, config_name, config_key, config_value, config_type, remark, del_flag, create_by, create_time)
VALUES (900014, '代码生成默认上级菜单', 'qc.gen.parent-menu-id', '2300', '1', '生成菜单挂载的上级 menu_id，如系统工具 2300', '0', 'system', CURRENT_TIMESTAMP);
