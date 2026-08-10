/*
 * 规范对齐：sys_dict_data.is_default 由 Y/N 归一为 0/1（0=否，1=是）。
 * 说明：历史建表脚本缺少 COMMENT；因 H2/MySQL 注释 DDL 方言不一，表列 COMMENT 不在本迁移强制执行，
 * 字段含义以实体 JavaDoc / code_formater.md 为准，后续可按目标库单独补注释脚本。
 * 依赖：V7、V8 已创建 sys_dict_data。
 */

UPDATE sys_dict_data SET is_default = '1' WHERE is_default IN ('Y', 'y');
UPDATE sys_dict_data SET is_default = '0' WHERE is_default IN ('N', 'n');
UPDATE sys_dict_data SET is_default = '0' WHERE is_default IS NULL;

ALTER TABLE sys_dict_data ALTER COLUMN is_default SET DEFAULT '0';
