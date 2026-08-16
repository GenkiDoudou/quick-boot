/*
 * 积木报表 / JimuBI 升级至 2.5.0（Flyway V5）
 * 来源：https://github.com/jeecgboot/jimureport/releases/tag/v2.5.0
 * 依赖：V3__jimureport.sql 已建积木表；已有库增量执行本脚本。
 */
/*
 * QuickBoot note: MySQL-only clauses stripped for H2 MODE=MySQL compatibility
 * (USING BTREE / ENGINE / CHARSET / index COMMENT / double(p,s) / unsigned / ON UPDATE).
 * Behavior remains valid on MySQL/MariaDB.
 */

-- AI 数据建模：表元数据（同步 + 手动维护，支持多数据源）
CREATE TABLE IF NOT EXISTS `chat2bi_table_meta` (
  `id` varchar(32) NOT NULL COMMENT '主键',
  `db_source_type` varchar(32) NOT NULL COMMENT '数据源来源表标识, 如: db_source / api_source / file_source / system_source',
  `db_source_id` varchar(32) NOT NULL COMMENT '对应来源表的主键ID',
  `schema_name` varchar(128) NULL DEFAULT NULL COMMENT 'Schema名',
  `table_name` varchar(128) NOT NULL COMMENT '表名',
  `source_comment` varchar(512) NULL DEFAULT NULL COMMENT '表注释-源(同步写入, 每次同步覆盖)',
  `table_comment` varchar(512) NULL DEFAULT NULL COMMENT '表注释-用户(用户手动填写, 同步不覆盖)',
  `columns_json` text NOT NULL COMMENT '列定义[{name,type,source_comment,comment,is_primary,sample_values,nulltable}]',
  `relations_json` text NULL COMMENT '关联关系[{column,ref_table,ref_column,type}]',
  `is_enabled` tinyint(4) NOT NULL DEFAULT 1 COMMENT '对LLM可见: 0=否 1=是',
  `synced_time` datetime NULL DEFAULT NULL COMMENT '最近同步时间',
  `create_by` varchar(32) NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(32) NULL DEFAULT NULL COMMENT '更新人',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `tenant_id` int(11) NULL DEFAULT NULL COMMENT '多租户标识',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_source_table`(`db_source_type`, `db_source_id`, `schema_name`, `table_name`),
  KEY `idx_source`(`db_source_type`, `db_source_id`),
  KEY `idx_table_name`(`table_name`),
  -- utf8mb4 下整列索引易超 InnoDB 3072 字节上限，使用前缀
  KEY `idx_table_name_comment`(`table_name`, `source_comment`(100), `table_comment`(100)),
  KEY `idx_table_comment`(`table_comment`(191))
);

-- 修复错误接口数据（演示数据集 URL）
UPDATE onl_drag_dataset_head
SET query_sql = REPLACE(query_sql, 'https://apijeecgcom/', 'http://api.jeecg.com/')
WHERE query_sql LIKE '%https://apijeecgcom/%';
