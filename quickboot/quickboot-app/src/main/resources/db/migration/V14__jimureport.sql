/*
 * QuickBoot 积木报表 / JimuBI 初始化（Flyway V3）
 * 合并原 V33 表结构、V34/V35/V38 菜单、V37 报表目录 OAuth 路径。
 * 依赖：V1 业务（sys_menu / sys_oauth_client）。
 * 演示数据见可选 V4__jimureport_demo.sql。
 */
/*
 * QuickBoot note: MySQL-only clauses stripped for H2 MODE=MySQL compatibility
 * (USING BTREE / ENGINE / CHARSET / index COMMENT / double(p,s) / unsigned / ON UPDATE).
 * Behavior remains valid on MySQL/MariaDB.
 */

-- ========== 原 V33__jimureport_init.sql ==========
-- JimuReport + JimuBI 官方 MySQL 5.7 表结构（MariaDB / MySQL 完整方言）
-- 来源: https://github.com/jeecgboot/jimureport/blob/master/db/jimureport.mysql5.7.create.sql
-- 演示数据见可选 V4__jimureport_demo.sql

-- Table structure for table `huiyuan_age`

DROP TABLE IF EXISTS `huiyuan_age`;
CREATE TABLE `huiyuan_age` (
  `id` varchar(36) NOT NULL,
  `name` varchar(50) DEFAULT NULL COMMENT '名称',
  `value` varchar(20) DEFAULT NULL COMMENT '值',
  PRIMARY KEY (`id`)
);
-- Dumping data for table `huiyuan_age`


-- Table structure for table `huiyuan_fengongsi`

DROP TABLE IF EXISTS `huiyuan_fengongsi`;
CREATE TABLE `huiyuan_fengongsi` (
  `id` varchar(36) NOT NULL COMMENT '主键',
  `name` varchar(50) DEFAULT NULL COMMENT '名称',
  `value` varchar(20) DEFAULT NULL COMMENT '值',
  `type` varchar(32) DEFAULT NULL COMMENT '类型',
  PRIMARY KEY (`id`)
);
-- Dumping data for table `huiyuan_fengongsi`


-- Table structure for table `huiyuan_huoyuedu`

DROP TABLE IF EXISTS `huiyuan_huoyuedu`;
CREATE TABLE `huiyuan_huoyuedu` (
  `id` varchar(36) NOT NULL,
  `name` varchar(50) DEFAULT NULL COMMENT '名称',
  `value` varchar(20) DEFAULT NULL COMMENT '值',
  PRIMARY KEY (`id`)
);
-- Dumping data for table `huiyuan_huoyuedu`


-- Table structure for table `huiyuan_sex`

DROP TABLE IF EXISTS `huiyuan_sex`;
CREATE TABLE `huiyuan_sex` (
  `id` varchar(36) NOT NULL,
  `name` varchar(50) DEFAULT NULL COMMENT '性别',
  `value` varchar(20) DEFAULT NULL COMMENT '值',
  PRIMARY KEY (`id`)
);
-- Dumping data for table `huiyuan_sex`


-- Table structure for table `huiyuan_work`

DROP TABLE IF EXISTS `huiyuan_work`;
CREATE TABLE `huiyuan_work` (
  `id` varchar(36) NOT NULL,
  `name` varchar(50) DEFAULT NULL COMMENT '姓名',
  `value` varchar(20) DEFAULT NULL COMMENT '值',
  PRIMARY KEY (`id`)
);
-- Dumping data for table `huiyuan_work`


-- Table structure for table `huiyuan_wxtl`

DROP TABLE IF EXISTS `huiyuan_wxtl`;
CREATE TABLE `huiyuan_wxtl` (
  `id` varchar(36) NOT NULL,
  `name` varchar(50) DEFAULT NULL COMMENT '名称',
  `value` varchar(20) DEFAULT NULL COMMENT '值',
  PRIMARY KEY (`id`)
);
-- Dumping data for table `huiyuan_wxtl`


-- Table structure for table `huiyuan_wxtlshuliang`

DROP TABLE IF EXISTS `huiyuan_wxtlshuliang`;
CREATE TABLE `huiyuan_wxtlshuliang` (
  `id` varchar(36) NOT NULL COMMENT '主键',
  `name` varchar(50) DEFAULT NULL COMMENT '名称',
  `value` int(10) DEFAULT NULL COMMENT '值',
  PRIMARY KEY (`id`)
);
-- Dumping data for table `huiyuan_wxtlshuliang`


-- Table structure for table `huiyuan_xueli`

DROP TABLE IF EXISTS `huiyuan_xueli`;
CREATE TABLE `huiyuan_xueli` (
  `id` varchar(36) NOT NULL,
  `name` varchar(50) DEFAULT NULL COMMENT '名称',
  `value` varchar(20) DEFAULT NULL COMMENT '值',
  PRIMARY KEY (`id`)
);
-- Dumping data for table `huiyuan_xueli`


-- Table structure for table `jimu_dict`

DROP TABLE IF EXISTS `jimu_dict`;
CREATE TABLE `jimu_dict` (
  `id` varchar(32) NOT NULL,
  `dict_name` varchar(100) DEFAULT NULL COMMENT '字典名称',
  `dict_code` varchar(100) DEFAULT NULL COMMENT '字典编码',
  `description` varchar(255) DEFAULT NULL COMMENT '描述',
  `del_flag` int(1) DEFAULT NULL COMMENT '删除状态',
  `create_by` varchar(32) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(32) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `type` int(1) DEFAULT '0' COMMENT '字典类型0为string,1为number',
  `tenant_id` varchar(10) DEFAULT NULL COMMENT '多租户标识',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sd_dict_code` (`dict_code`)
);
-- Dumping data for table `jimu_dict`


-- Table structure for table `jimu_dict_item`

DROP TABLE IF EXISTS `jimu_dict_item`;
CREATE TABLE `jimu_dict_item` (
  `id` varchar(32) NOT NULL,
  `dict_id` varchar(32) DEFAULT NULL COMMENT '字典id',
  `item_text` varchar(100) DEFAULT NULL COMMENT '字典项文本',
  `item_value` varchar(100) DEFAULT NULL COMMENT '字典项值',
  `description` varchar(255) DEFAULT NULL COMMENT '描述',
  `sort_order` int(10) DEFAULT NULL COMMENT '排序',
  `status` int(11) DEFAULT NULL COMMENT '状态（1启用 0不启用）',
  `create_by` varchar(32) DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  `update_by` varchar(32) DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_sdi_role_dict_id` (`dict_id`),
  KEY `idx_sdi_role_sort_order` (`sort_order`),
  KEY `idx_sdi_status` (`status`),
  KEY `idx_sdi_dict_val` (`dict_id`,`item_value`)
);
-- Dumping data for table `jimu_dict_item`


-- Table structure for table `jimu_report`

DROP TABLE IF EXISTS `jimu_report`;
CREATE TABLE `jimu_report` (
  `id` varchar(32) NOT NULL COMMENT '主键',
  `code` varchar(50) DEFAULT NULL COMMENT '编码',
  `name` varchar(50) DEFAULT NULL COMMENT '名称',
  `note` varchar(255) DEFAULT NULL COMMENT '说明',
  `status` varchar(10) DEFAULT NULL COMMENT '状态',
  `type` varchar(50) DEFAULT NULL COMMENT '类型',
  `json_str` longtext COMMENT 'json字符串',
  `api_url` varchar(255) DEFAULT NULL COMMENT '请求地址',
  `thumb` text COMMENT '缩略图',
  `create_by` varchar(50) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(50) DEFAULT NULL COMMENT '修改人',
  `update_time` datetime DEFAULT NULL COMMENT '修改时间',
  `del_flag` tinyint(1) DEFAULT NULL COMMENT '删除标识0-正常,1-已删除',
  `api_method` varchar(255) DEFAULT NULL COMMENT '请求方法0-get,1-post',
  `api_code` varchar(255) DEFAULT NULL COMMENT '请求编码',
  `template` tinyint(1) DEFAULT NULL COMMENT '是否是模板 0-是,1-不是',
  `view_count` bigint(15) DEFAULT '0' COMMENT '浏览次数',
  `css_str` text COMMENT 'css增强',
  `js_str` text COMMENT 'js增强',
  `py_str` text COMMENT 'py增强',
  `tenant_id` varchar(10) DEFAULT NULL COMMENT '多租户标识',
  `update_count` int(11) DEFAULT '0' COMMENT '乐观锁版本',
  `submit_form` tinyint(1) DEFAULT NULL COMMENT '是否填报报表 0不是,1是',
  `is_multi_sheet` tinyint(4) DEFAULT NULL COMMENT '是否多sheet报表 1是 0否',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uniq_jmreport_code` (`code`),
  KEY `uniq_jmreport_createby` (`create_by`),
  KEY `uniq_jmreport_delflag` (`del_flag`)
);
-- Dumping data for table `jimu_report`


-- Table structure for table `jimu_report_category`

DROP TABLE IF EXISTS `jimu_report_category`;
CREATE TABLE `jimu_report_category` (
  `id` varchar(32) NOT NULL COMMENT '主键',
  `name` varchar(100) NOT NULL COMMENT '分类名称',
  `parent_id` varchar(32) DEFAULT NULL COMMENT '父级id',
  `iz_leaf` int(1) DEFAULT NULL COMMENT '是否为叶子节点(0 否 1是)',
  `source_type` varchar(10) DEFAULT NULL COMMENT '来源类型( report 积木报表 screen 大屏  drag 仪表盘)',
  `create_by` varchar(32) DEFAULT NULL COMMENT '创建人',
  `create_time` timestamp NULL DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(32) DEFAULT NULL COMMENT '更新人',
  `update_time` timestamp NULL DEFAULT NULL COMMENT '更新时间',
  `tenant_id` varchar(11) DEFAULT NULL COMMENT '租户id',
  `del_flag` int(1) DEFAULT NULL COMMENT '删除状态(0未删除，1已删除，2临时删除)',
  `sort_no` int(11) DEFAULT NULL COMMENT '排序',
  PRIMARY KEY (`id`)
);
-- Dumping data for table `jimu_report_category`


-- Table structure for table `jimu_report_data_source`

DROP TABLE IF EXISTS `jimu_report_data_source`;
CREATE TABLE `jimu_report_data_source` (
  `id` varchar(36) NOT NULL,
  `name` varchar(100) DEFAULT NULL COMMENT '数据源名称',
  `report_id` varchar(100) DEFAULT NULL COMMENT '报表_id',
  `code` varchar(100) DEFAULT NULL COMMENT '编码',
  `remark` varchar(200) DEFAULT NULL COMMENT '备注',
  `db_type` varchar(10) DEFAULT NULL COMMENT '数据库类型',
  `db_driver` varchar(100) DEFAULT NULL COMMENT '驱动类',
  `db_url` varchar(500) DEFAULT NULL COMMENT '数据源地址',
  `db_username` varchar(100) DEFAULT NULL COMMENT '用户名',
  `db_password` varchar(100) DEFAULT NULL COMMENT '密码',
  `create_by` varchar(50) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT NULL COMMENT '创建日期',
  `update_by` varchar(50) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT NULL COMMENT '更新日期',
  `connect_times` int(11) DEFAULT '0' COMMENT '连接失败次数',
  `tenant_id` varchar(10) DEFAULT NULL COMMENT '多租户标识',
  `type` varchar(10) DEFAULT NULL COMMENT '类型(report:报表;drag:仪表盘)',
  PRIMARY KEY (`id`),
  KEY `idx_jmdatasource_report_id` (`report_id`),
  KEY `idx_jmdatasource_code` (`code`)
);
-- Dumping data for table `jimu_report_data_source`


-- Table structure for table `jimu_report_db`

DROP TABLE IF EXISTS `jimu_report_db`;
CREATE TABLE `jimu_report_db` (
  `id` varchar(36) NOT NULL COMMENT 'id',
  `jimu_report_id` varchar(32) DEFAULT NULL COMMENT '主键字段',
  `create_by` varchar(50) DEFAULT NULL COMMENT '创建人登录名称',
  `update_by` varchar(50) DEFAULT NULL COMMENT '更新人登录名称',
  `create_time` datetime DEFAULT NULL COMMENT '创建日期',
  `update_time` datetime DEFAULT NULL COMMENT '更新日期',
  `db_code` varchar(32) DEFAULT NULL COMMENT '数据集编码',
  `db_ch_name` varchar(50) DEFAULT NULL COMMENT '数据集名字',
  `db_type` varchar(32) DEFAULT NULL COMMENT '数据源类型',
  `db_table_name` varchar(32) DEFAULT NULL COMMENT '数据库表名',
  `db_dyn_sql` longtext COMMENT '动态查询SQL',
  `db_key` varchar(32) DEFAULT NULL COMMENT '数据源KEY',
  `tb_db_key` varchar(32) DEFAULT NULL COMMENT '填报数据源',
  `tb_db_table_name` varchar(32) DEFAULT NULL COMMENT '填报数据表',
  `java_type` varchar(32) DEFAULT NULL COMMENT 'java类数据集  类型（spring:springkey,class:java类名）',
  `java_value` varchar(255) DEFAULT NULL COMMENT 'java类数据源  数值（bean key/java类名）',
  `api_url` varchar(255) DEFAULT NULL COMMENT '请求地址',
  `api_method` varchar(255) DEFAULT NULL COMMENT '请求方法0-get,1-post',
  `is_list` varchar(10) DEFAULT '0' COMMENT '是否是列表0否1是 默认0',
  `is_page` varchar(10) DEFAULT NULL COMMENT '是否作为分页,0:不分页，1:分页',
  `db_source` varchar(255) DEFAULT NULL COMMENT '数据源',
  `db_source_type` varchar(50) DEFAULT NULL COMMENT '数据库类型 MYSQL ORACLE SQLSERVER',
  `json_data` text COMMENT 'json数据，直接解析json内容',
  `api_convert` varchar(255) DEFAULT NULL COMMENT 'api转换器',
  `iz_shared_source` int(1) DEFAULT NULL COMMENT '是否为共享数据源(0 否 1 是)',
  `jimu_shared_source_id` varchar(32) DEFAULT NULL COMMENT '指向共享数据集的id',
  PRIMARY KEY (`id`),
  KEY `idx_jmreportdb_db_key` (`db_key`),
  KEY `idx_jimu_report_id` (`jimu_report_id`),
  KEY `idx_db_source_id` (`db_source`)
);
-- Dumping data for table `jimu_report_db`


-- Table structure for table `jimu_report_db_field`

DROP TABLE IF EXISTS `jimu_report_db_field`;
CREATE TABLE `jimu_report_db_field` (
  `id` varchar(36) NOT NULL COMMENT 'id',
  `create_by` varchar(50) DEFAULT NULL COMMENT '创建人登录名称',
  `create_time` datetime DEFAULT NULL COMMENT '创建日期',
  `update_by` varchar(50) DEFAULT NULL COMMENT '更新人登录名称',
  `update_time` datetime DEFAULT NULL COMMENT '更新日期',
  `jimu_report_db_id` varchar(32) DEFAULT NULL COMMENT '数据源ID',
  `field_name` varchar(80) DEFAULT NULL COMMENT '字段名',
  `field_name_physics` varchar(200) DEFAULT NULL COMMENT '物理字段名（文件数据集使用，存的是excel的字段标题）',
  `field_text` varchar(50) DEFAULT NULL COMMENT '字段文本',
  `widget_type` varchar(50) DEFAULT NULL COMMENT '控件类型',
  `widget_width` int(10) DEFAULT NULL COMMENT '控件宽度',
  `order_num` int(3) DEFAULT NULL COMMENT '排序',
  `search_flag` int(3) DEFAULT '0' COMMENT '查询标识0否1是 默认0',
  `search_mode` int(3) DEFAULT NULL COMMENT '查询模式1简单2范围',
  `dict_code` varchar(255) DEFAULT NULL COMMENT '字典编码支持从表中取数据',
  `search_value` varchar(100) DEFAULT NULL COMMENT '查询默认值',
  `search_format` varchar(50) DEFAULT NULL COMMENT '查询时间格式化表达式',
  `ext_json` text COMMENT '参数配置',
  PRIMARY KEY (`id`),
  KEY `idx_jrdf_jimu_report_db_id` (`jimu_report_db_id`),
  KEY `idx_dbfield_order_num` (`order_num`)
);
-- Dumping data for table `jimu_report_db_field`


-- Table structure for table `jimu_report_db_param`

DROP TABLE IF EXISTS `jimu_report_db_param`;
CREATE TABLE `jimu_report_db_param` (
  `id` varchar(36) NOT NULL,
  `jimu_report_head_id` varchar(36) NOT NULL COMMENT '动态报表ID',
  `param_name` varchar(32) NOT NULL COMMENT '参数字段',
  `param_txt` varchar(32) DEFAULT NULL COMMENT '参数文本',
  `param_value` varchar(1000) DEFAULT NULL COMMENT '参数默认值',
  `order_num` int(11) DEFAULT NULL COMMENT '排序',
  `create_by` varchar(50) DEFAULT NULL COMMENT '创建人登录名称',
  `create_time` datetime DEFAULT NULL COMMENT '创建日期',
  `update_by` varchar(50) DEFAULT NULL COMMENT '更新人登录名称',
  `update_time` datetime DEFAULT NULL COMMENT '更新日期',
  `search_flag` int(1) DEFAULT NULL COMMENT '查询标识0否1是 默认0',
  `widget_type` varchar(50) DEFAULT NULL COMMENT '查询控件类型',
  `search_mode` int(1) DEFAULT NULL COMMENT '查询模式1简单2范围',
  `dict_code` varchar(255) DEFAULT NULL COMMENT '字典',
  `search_format` varchar(50) DEFAULT NULL COMMENT '查询时间格式化表达式',
  `ext_json` text COMMENT '参数配置',
  PRIMARY KEY (`id`),
  KEY `idx_jrdp_jimu_report_head_id` (`jimu_report_head_id`)
);
-- Dumping data for table `jimu_report_db_param`


-- Table structure for table `jimu_report_export_job`

DROP TABLE IF EXISTS `jimu_report_export_job`;
CREATE TABLE `jimu_report_export_job` (
  `id` varchar(32) NOT NULL COMMENT '主键',
  `name` varchar(100) DEFAULT NULL COMMENT '任务名称',
  `begin_time` datetime DEFAULT NULL COMMENT '开始时间',
  `end_time` datetime DEFAULT NULL COMMENT '结束时间',
  `exec_interval` varchar(100) DEFAULT NULL COMMENT '执行频率',
  `report_conf` text COMMENT '导出报表配置',
  `last_run_time` datetime DEFAULT NULL COMMENT '最后执行时间',
  `receiver_email` text COMMENT '接收通知的邮件',
  `file_sync_path` varchar(255) DEFAULT NULL COMMENT '文件同步路径',
  `status` int(11) DEFAULT NULL COMMENT '状态(0:停止;1:启动)',
  `create_by` varchar(50) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(50) DEFAULT NULL COMMENT '修改人',
  `update_time` datetime DEFAULT NULL COMMENT '修改时间',
  `tenant_id` varchar(10) DEFAULT NULL COMMENT '多租户标识',
  PRIMARY KEY (`id`)
);
-- Dumping data for table `jimu_report_export_job`


-- Table structure for table `jimu_report_export_log`

DROP TABLE IF EXISTS `jimu_report_export_log`;
CREATE TABLE `jimu_report_export_log` (
  `id` varchar(32) NOT NULL,
  `batch_no` varchar(50) DEFAULT NULL COMMENT '批次编号',
  `export_channel` varchar(20) DEFAULT NULL COMMENT '导出渠道',
  `export_from` varchar(20) DEFAULT NULL COMMENT '发起来源',
  `from_id` varchar(32) DEFAULT NULL COMMENT '来源id',
  `export_type` varchar(10) DEFAULT NULL COMMENT '导出类型',
  `report_id` text COMMENT '报表id',
  `download_path` varchar(255) DEFAULT NULL COMMENT '下载路径',
  `status` varchar(15) DEFAULT NULL COMMENT '状态',
  `err_msg` text COMMENT '错误消息',
  `create_by` varchar(32) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `tenant_id` varchar(10) DEFAULT NULL COMMENT '多租户标识',
  PRIMARY KEY (`id`)
);
-- Dumping data for table `jimu_report_export_log`


-- Table structure for table `jimu_report_ext_data`

DROP TABLE IF EXISTS `jimu_report_ext_data`;
CREATE TABLE `jimu_report_ext_data` (
  `id` varchar(32) NOT NULL COMMENT '主键ID',
  `biz_type` varchar(100) NOT NULL COMMENT '业务类型标识，如 report_share、temp_config 等',
  `name` varchar(200) DEFAULT NULL COMMENT '名称，展示用',
  `descr` varchar(500) DEFAULT NULL COMMENT '描述信息',
  `tags` varchar(255) DEFAULT NULL COMMENT '标签，多个用逗号分隔',
  `data_value` longtext COMMENT '实际存储内容',
  `metadata` varchar(500) DEFAULT NULL COMMENT '元数据，用于存储补充信息',
  `status` tinyint(4) DEFAULT '1' COMMENT '状态标识：1正常 0禁用',
  `create_by` varchar(50) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(50) DEFAULT NULL COMMENT '修改人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (`id`),
  KEY `idx_biz` (`biz_type`)
);
-- Dumping data for table `jimu_report_ext_data`


-- Table structure for table `jimu_report_icon_lib`

DROP TABLE IF EXISTS `jimu_report_icon_lib`;
CREATE TABLE `jimu_report_icon_lib` (
  `id` varchar(32) NOT NULL COMMENT '主键',
  `name` varchar(100) DEFAULT NULL COMMENT '图片名称',
  `type` varchar(32) DEFAULT NULL COMMENT '图片类型',
  `image_url` varchar(255) DEFAULT NULL COMMENT '图片地址',
  `create_by` varchar(32) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(32) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `tenant_id` int(11) DEFAULT NULL COMMENT '租户id',
  PRIMARY KEY (`id`)
);
-- Dumping data for table `jimu_report_icon_lib`


-- Table structure for table `jimu_report_link`

DROP TABLE IF EXISTS `jimu_report_link`;
CREATE TABLE `jimu_report_link` (
  `id` varchar(32) NOT NULL COMMENT '主键id',
  `report_id` varchar(32) DEFAULT NULL COMMENT '积木设计器id',
  `parameter` text COMMENT '参数',
  `eject_type` varchar(1) DEFAULT NULL COMMENT '弹出方式（0 当前页面 1 新窗口）',
  `link_name` varchar(255) DEFAULT NULL COMMENT '链接名称',
  `api_method` varchar(1) DEFAULT NULL COMMENT '请求方法0-get,1-post',
  `link_type` varchar(1) DEFAULT NULL COMMENT '链接方式(0 网络报表 1 网络连接 2 图表联动)',
  `api_url` varchar(1000) DEFAULT NULL COMMENT '外网api',
  `link_chart_id` varchar(50) DEFAULT NULL COMMENT '联动图表的ID',
  `expression` varchar(255) DEFAULT NULL COMMENT '表达式',
  `requirement` varchar(255) DEFAULT NULL COMMENT '条件',
  PRIMARY KEY (`id`),
  KEY `uniq_link_reportid` (`report_id`)
);
-- Dumping data for table `jimu_report_link`


-- Table structure for table `jimu_report_map`

DROP TABLE IF EXISTS `jimu_report_map`;
CREATE TABLE `jimu_report_map` (
  `id` varchar(64) NOT NULL COMMENT '主键',
  `label` varchar(125) DEFAULT NULL COMMENT '地图名称',
  `name` varchar(125) DEFAULT NULL COMMENT '地图编码',
  `data` longtext COMMENT '地图数据',
  `create_by` varchar(32) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(32) DEFAULT NULL COMMENT '修改人',
  `update_time` datetime DEFAULT NULL COMMENT '修改时间',
  `del_flag` varchar(1) DEFAULT NULL COMMENT '0表示未删除,1表示删除',
  `sys_org_code` varchar(64) DEFAULT NULL COMMENT '所属部门',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uniq_jmreport_map_name` (`name`)
);
-- Dumping data for table `jimu_report_map`


-- Table structure for table `jimu_report_share`

DROP TABLE IF EXISTS `jimu_report_share`;
CREATE TABLE `jimu_report_share` (
  `id` varchar(32) NOT NULL COMMENT '主键',
  `report_id` varchar(32) DEFAULT NULL COMMENT '在线excel设计器id',
  `preview_url` varchar(1000) DEFAULT NULL COMMENT '预览地址',
  `preview_lock` varchar(4) DEFAULT NULL COMMENT '密码锁',
  `last_update_time` datetime DEFAULT NULL COMMENT '最后更新时间',
  `term_of_validity` varchar(1) DEFAULT NULL COMMENT '有效期(0:永久有效，1:1天，2:7天)',
  `status` varchar(1) DEFAULT NULL COMMENT '是否过期(0未过期，1已过期)',
  `preview_lock_status` varchar(1) DEFAULT NULL COMMENT '密码锁状态(0不存在密码锁，1存在密码锁)',
  `share_token` varchar(50) DEFAULT NULL COMMENT '分享token',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uniq_report_id` (`report_id`),
  KEY `idx_jrs_share_token` (`share_token`)
);
-- Dumping data for table `jimu_report_share`


-- Table structure for table `jimu_report_sheet`

DROP TABLE IF EXISTS `jimu_report_sheet`;
CREATE TABLE `jimu_report_sheet` (
  `id` varchar(64) NOT NULL COMMENT '主键（Sheet ID）',
  `report_id` varchar(64) NOT NULL COMMENT '报表ID',
  `sheet_name` varchar(255) NOT NULL COMMENT 'Sheet名称',
  `sheet_order` int(11) NOT NULL COMMENT '排序（可以为负数，负数表示在默认sheet前面）',
  `json_str` longtext COMMENT '该sheet的完整jsonStr',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `create_by` varchar(64) DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(64) DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`id`),
  KEY `idx_report_id` (`report_id`),
  KEY `idx_sheet_order` (`report_id`,`sheet_order`)
);
-- Dumping data for table `jimu_report_sheet`


-- Table structure for table `onl_drag_comp`

DROP TABLE IF EXISTS `onl_drag_comp`;
CREATE TABLE `onl_drag_comp` (
  `id` varchar(32) NOT NULL COMMENT '主键',
  `parent_id` varchar(32) DEFAULT NULL,
  `comp_name` varchar(50) DEFAULT NULL COMMENT '组件名称',
  `comp_type` varchar(20) DEFAULT NULL,
  `icon` varchar(50) DEFAULT NULL COMMENT '图标',
  `order_num` int(11) DEFAULT NULL COMMENT '排序',
  `type_id` int(11) DEFAULT NULL COMMENT '组件类型',
  `comp_config` longtext COMMENT '组件配置',
  `status` varchar(2) DEFAULT '0' COMMENT '状态0:无效 1:有效',
  `create_by` varchar(50) DEFAULT NULL COMMENT '创建人登录名称',
  `create_time` datetime DEFAULT NULL COMMENT '创建日期',
  `update_by` varchar(50) DEFAULT NULL COMMENT '更新人登录名称',
  `update_time` datetime DEFAULT NULL COMMENT '更新日期',
  PRIMARY KEY (`id`)
);
-- Dumping data for table `onl_drag_comp`


-- Table structure for table `onl_drag_dataset_head`

DROP TABLE IF EXISTS `onl_drag_dataset_head`;
CREATE TABLE `onl_drag_dataset_head` (
  `id` varchar(32) NOT NULL COMMENT 'id',
  `name` varchar(100) NOT NULL COMMENT '名称',
  `code` varchar(36) DEFAULT NULL COMMENT '编码',
  `parent_id` varchar(36) DEFAULT NULL COMMENT '父id',
  `db_source` varchar(100) DEFAULT NULL COMMENT '动态数据源',
  `query_sql` varchar(5000) DEFAULT '0' COMMENT '查询数据SQL',
  `content` varchar(1000) DEFAULT NULL COMMENT '描述',
  `iz_agent` varchar(10) DEFAULT '0' COMMENT 'iz_agent',
  `data_type` varchar(50) DEFAULT NULL COMMENT '数据类型',
  `api_method` varchar(10) DEFAULT NULL COMMENT 'api方法：get/post',
  `create_time` datetime DEFAULT NULL,
  `create_by` varchar(50) DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  `update_by` varchar(50) DEFAULT NULL,
  `low_app_id` varchar(32) DEFAULT NULL COMMENT '应用ID',
  `tenant_id` int(10) DEFAULT NULL COMMENT '租户ID',
  PRIMARY KEY (`id`)
);
-- Dumping data for table `onl_drag_dataset_head`


-- Table structure for table `onl_drag_dataset_item`

DROP TABLE IF EXISTS `onl_drag_dataset_item`;
CREATE TABLE `onl_drag_dataset_item` (
  `id` varchar(32) NOT NULL COMMENT 'id',
  `head_id` varchar(36) NOT NULL COMMENT '主表ID',
  `field_name` varchar(36) DEFAULT NULL COMMENT '字段名',
  `field_txt` varchar(1000) DEFAULT NULL COMMENT '字段文本',
  `field_type` varchar(10) DEFAULT NULL COMMENT '字段类型',
  `widget_type` varchar(30) DEFAULT NULL COMMENT '控件类型',
  `dict_code` varchar(500) DEFAULT NULL COMMENT '字典Code',
  `dict_table` varchar(125) DEFAULT NULL,
  `dict_text` varchar(125) DEFAULT NULL,
  `iz_show` varchar(5) DEFAULT NULL COMMENT '是否列表显示',
  `iz_search` varchar(10) DEFAULT NULL COMMENT '是否查询',
  `iz_total` varchar(5) DEFAULT NULL COMMENT '是否计算总计（仅对数值有效）',
  `search_mode` varchar(10) DEFAULT NULL COMMENT '查询模式',
  `order_num` int(11) DEFAULT NULL COMMENT '排序',
  `create_by` varchar(32) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(32) DEFAULT NULL COMMENT '修改人',
  `update_time` datetime DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`),
  KEY `idx_oddi_head_id` (`head_id`)
);
-- Dumping data for table `onl_drag_dataset_item`


-- Table structure for table `onl_drag_dataset_param`

DROP TABLE IF EXISTS `onl_drag_dataset_param`;
CREATE TABLE `onl_drag_dataset_param` (
  `id` varchar(36) NOT NULL,
  `head_id` varchar(36) NOT NULL COMMENT '动态报表ID',
  `param_name` varchar(32) NOT NULL COMMENT '参数字段',
  `param_txt` varchar(32) DEFAULT NULL COMMENT '参数文本',
  `param_value` varchar(1000) DEFAULT NULL COMMENT '参数默认值',
  `order_num` int(11) DEFAULT NULL COMMENT '排序',
  `iz_search` int(11) DEFAULT NULL COMMENT '查询标识0否1是 默认0',
  `widget_type` varchar(50) DEFAULT NULL COMMENT '查询控件类型',
  `search_mode` int(11) DEFAULT NULL COMMENT '查询模式1简单2范围',
  `dict_code` varchar(255) DEFAULT NULL COMMENT '字典',
  `create_by` varchar(50) DEFAULT NULL COMMENT '创建人登录名称',
  `create_time` datetime DEFAULT NULL COMMENT '创建日期',
  `update_by` varchar(50) DEFAULT NULL COMMENT '更新人登录名称',
  `update_time` datetime DEFAULT NULL COMMENT '更新日期',
  PRIMARY KEY (`id`),
  KEY `idx_oddp_head_id` (`head_id`)
);
-- Dumping data for table `onl_drag_dataset_param`


-- Table structure for table `onl_drag_page`

DROP TABLE IF EXISTS `onl_drag_page`;
CREATE TABLE `onl_drag_page` (
  `id` varchar(50) NOT NULL COMMENT '主键',
  `name` varchar(100) DEFAULT NULL COMMENT '界面名称',
  `path` varchar(100) DEFAULT NULL COMMENT '访问路径',
  `background_color` varchar(10) DEFAULT NULL COMMENT '背景色',
  `background_image` varchar(255) DEFAULT NULL COMMENT '背景图',
  `design_type` int(1) DEFAULT NULL COMMENT '设计模式(1:pc,2:手机,3:平板)',
  `theme` varchar(10) DEFAULT NULL COMMENT '主题色',
  `style` varchar(20) DEFAULT NULL COMMENT '面板主题',
  `cover_url` varchar(500) DEFAULT NULL COMMENT '封面图',
  `des_json` varchar(1000) DEFAULT NULL COMMENT '仪表盘主配置JSON',
  `template` longtext COMMENT '布局json',
  `protection_code` varchar(32) DEFAULT NULL COMMENT '保护码',
  `type` varchar(64) DEFAULT NULL COMMENT '文件夹类',
  `iz_template` varchar(10) DEFAULT '0' COMMENT '是否模板(1:是；0不是)',
  `create_by` varchar(50) DEFAULT NULL COMMENT '创建人登录名称',
  `create_time` datetime DEFAULT NULL COMMENT '创建日期',
  `update_by` varchar(50) DEFAULT NULL COMMENT '更新人登录名称',
  `update_time` datetime DEFAULT NULL COMMENT '更新日期',
  `low_app_id` varchar(50) DEFAULT NULL COMMENT '应用ID',
  `tenant_id` int(10) DEFAULT NULL COMMENT '租户ID',
  `update_count` int(10) DEFAULT '1',
  `visits_num` int(11) DEFAULT NULL COMMENT '访问次数',
  `del_flag` int(11) DEFAULT NULL COMMENT '删除状态( 0未删除 1已删除)',
  PRIMARY KEY (`id`)
);
-- Dumping data for table `onl_drag_page`


-- Table structure for table `onl_drag_page_comp`

DROP TABLE IF EXISTS `onl_drag_page_comp`;
CREATE TABLE `onl_drag_page_comp` (
  `id` varchar(32) NOT NULL COMMENT '主键',
  `parent_id` varchar(32) DEFAULT NULL COMMENT '父组件ID',
  `page_Id` varchar(50) DEFAULT NULL COMMENT '界面ID',
  `comp_id` varchar(32) DEFAULT NULL COMMENT '组件库ID',
  `component` varchar(50) DEFAULT NULL COMMENT '组件名称',
  `config` longtext COMMENT '组件配置',
  `create_by` varchar(50) DEFAULT NULL COMMENT '创建人登录名称',
  `create_time` datetime DEFAULT NULL COMMENT '创建日期',
  `update_by` varchar(50) DEFAULT NULL COMMENT '更新人登录名称',
  `update_time` datetime DEFAULT NULL COMMENT '更新日期',
  PRIMARY KEY (`id`)
);
-- Dumping data for table `onl_drag_page_comp`


-- Table structure for table `onl_drag_share`

DROP TABLE IF EXISTS `onl_drag_share`;
CREATE TABLE `onl_drag_share` (
  `id` varchar(32) NOT NULL COMMENT '主键',
  `drag_id` varchar(32) DEFAULT NULL COMMENT '在线仪表盘设计器id',
  `preview_url` varchar(1000) DEFAULT NULL COMMENT '预览地址',
  `preview_lock` varchar(4) DEFAULT NULL COMMENT '密码锁',
  `last_update_time` datetime DEFAULT NULL COMMENT '最后更新时间',
  `term_of_validity` varchar(1) DEFAULT NULL COMMENT '有效期(0:永久有效，1:1天，7:7天)',
  `status` varchar(1) DEFAULT NULL COMMENT '是否过期(0未过期，1已过期)',
  `preview_lock_status` varchar(1) DEFAULT NULL COMMENT '是否为密码锁(0 否,1是)',
  `share_token` varchar(32) DEFAULT NULL COMMENT '分享token',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uniq_ods_drag_id` (`drag_id`)
);
-- Dumping data for table `onl_drag_share`


-- Table structure for table `onl_drag_table_relation`

DROP TABLE IF EXISTS `onl_drag_table_relation`;
CREATE TABLE `onl_drag_table_relation` (
  `id` varchar(50) NOT NULL COMMENT '主键',
  `aggregation_name` varchar(100) DEFAULT NULL COMMENT '聚合表名称',
  `aggregation_desc` varchar(100) DEFAULT NULL COMMENT '聚合表描述',
  `relation_forms` longtext COMMENT '关联表单',
  `filter_condition` longtext COMMENT '过滤条件',
  `header_fields` longtext COMMENT '表头字段',
  `calculate_fields` longtext COMMENT '公式字段',
  `validate_info` longtext COMMENT '校验信息',
  `del_flag` tinyint(1) DEFAULT NULL COMMENT '删除状态(0-正常,1-已删除)',
  `low_app_id` varchar(50) DEFAULT NULL COMMENT '应用ID',
  `tenant_id` int(11) DEFAULT NULL COMMENT '租户ID',
  `create_by` varchar(50) DEFAULT NULL COMMENT '创建人登录名称',
  `create_time` datetime DEFAULT NULL COMMENT '创建日期',
  `update_by` varchar(50) DEFAULT NULL COMMENT '更新人登录名称',
  `update_time` datetime DEFAULT NULL COMMENT '更新日期',
  PRIMARY KEY (`id`),
  KEY `idx_aggregation_name` (`aggregation_name`),
  KEY `idx_del_flag` (`del_flag`),
  KEY `idx_tenant_id` (`tenant_id`),
  KEY `idx_create_by` (`create_by`)
);
-- Dumping data for table `onl_drag_table_relation`


-- Table structure for table `rep_demo_dxtj`

DROP TABLE IF EXISTS `rep_demo_dxtj`;
CREATE TABLE `rep_demo_dxtj` (
  `id` varchar(36) NOT NULL COMMENT '主键',
  `name` varchar(50) DEFAULT NULL COMMENT '姓名',
  `gtime` datetime DEFAULT NULL COMMENT '雇佣日期',
  `update_by` varchar(50) DEFAULT NULL COMMENT '职务',
  `jphone` varchar(125) DEFAULT NULL COMMENT '家庭电话',
  `birth` datetime DEFAULT NULL COMMENT '出生日期',
  `hukou` varchar(32) DEFAULT NULL COMMENT '户口所在地',
  `laddress` varchar(125) DEFAULT NULL COMMENT '联系地址',
  `jperson` varchar(32) DEFAULT NULL COMMENT '紧急联系人',
  `sex` varchar(32) DEFAULT NULL COMMENT 'xingbie',
  PRIMARY KEY (`id`)
);
-- Dumping data for table `rep_demo_dxtj`


-- Table structure for table `rep_demo_employee`

DROP TABLE IF EXISTS `rep_demo_employee`;
CREATE TABLE `rep_demo_employee` (
  `id` varchar(10) NOT NULL COMMENT '主键',
  `num` varchar(50) DEFAULT NULL COMMENT '编号',
  `name` varchar(100) DEFAULT NULL COMMENT '姓名',
  `sex` varchar(10) DEFAULT NULL COMMENT '性别',
  `birthday` datetime DEFAULT NULL COMMENT '出生日期',
  `nation` varchar(30) DEFAULT NULL COMMENT '民族',
  `political` varchar(30) DEFAULT NULL COMMENT '政治面貌',
  `native_place` varchar(30) DEFAULT NULL COMMENT '籍贯',
  `height` varchar(30) DEFAULT NULL COMMENT '身高',
  `weight` varchar(30) DEFAULT NULL COMMENT '体重',
  `health` varchar(30) DEFAULT NULL COMMENT '健康状况',
  `id_card` varchar(80) DEFAULT NULL COMMENT '身份证号',
  `education` varchar(30) DEFAULT NULL COMMENT '学历',
  `school` varchar(80) DEFAULT NULL COMMENT '毕业学校',
  `major` varchar(80) DEFAULT NULL COMMENT '专业',
  `address` varchar(100) DEFAULT NULL COMMENT '联系地址',
  `zip_code` varchar(30) DEFAULT NULL COMMENT '邮编',
  `email` varchar(30) DEFAULT NULL COMMENT 'Email',
  `phone` varchar(30) DEFAULT NULL COMMENT '手机号',
  `foreign_language` varchar(30) DEFAULT NULL COMMENT '外语语种',
  `foreign_language_level` varchar(30) DEFAULT NULL COMMENT '外语水平',
  `computer_level` varchar(30) DEFAULT NULL COMMENT '计算机水平',
  `graduation_time` datetime DEFAULT NULL COMMENT '毕业时间',
  `arrival_time` datetime DEFAULT NULL COMMENT '到职时间',
  `positional_titles` varchar(30) DEFAULT NULL COMMENT '职称',
  `education_experience` text COMMENT '教育经历',
  `work_experience` text COMMENT '工作经历',
  `create_by` varchar(32) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(32) DEFAULT NULL COMMENT '修改人',
  `update_time` datetime DEFAULT NULL COMMENT '修改时间',
  `del_flag` tinyint(1) DEFAULT NULL COMMENT '删除标识0-正常,1-已删除',
  PRIMARY KEY (`id`)
);
-- Dumping data for table `rep_demo_employee`


-- Table structure for table `rep_demo_gongsi`

DROP TABLE IF EXISTS `rep_demo_gongsi`;
CREATE TABLE `rep_demo_gongsi` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `gname` varchar(125) NOT NULL COMMENT '货品名称',
  `gdata` varchar(255) NOT NULL COMMENT '返利',
  `tdata` varchar(125) NOT NULL COMMENT '备注',
  `didian` varchar(255) NOT NULL,
  `zhaiyao` varchar(255) NOT NULL,
  `num` varchar(255) NOT NULL,
  PRIMARY KEY (`id`)
);
-- Dumping data for table `rep_demo_gongsi`


-- Table structure for table `rep_demo_jianpiao`

DROP TABLE IF EXISTS `rep_demo_jianpiao`;
CREATE TABLE `rep_demo_jianpiao` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `bnum` varchar(125) NOT NULL,
  `ftime` varchar(125) NOT NULL,
  `sfkong` varchar(125) NOT NULL,
  `kaishi` varchar(125) NOT NULL,
  `jieshu` varchar(125) NOT NULL,
  `hezairen` varchar(125) NOT NULL,
  `jpnum` varchar(125) NOT NULL,
  `shihelv` varchar(125) NOT NULL,
  `s_id` int(11) NOT NULL,
  PRIMARY KEY (`id`)
);
-- Dumping data for table `rep_demo_jianpiao`


-- Table structure for table `rep_demo_xiaoshou`

DROP TABLE IF EXISTS `rep_demo_xiaoshou`;
CREATE TABLE `rep_demo_xiaoshou` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `hnum` varchar(125) NOT NULL COMMENT '货品编码',
  `hname` varchar(125) NOT NULL COMMENT '货品名称',
  `xinghao` varchar(125) NOT NULL COMMENT '单位',
  `fahuocangku` varchar(125) NOT NULL COMMENT '数量',
  `danwei` varchar(125) NOT NULL COMMENT '单价',
  `num` int(11) NOT NULL COMMENT '返利',
  `danjia` varchar(125) NOT NULL COMMENT '备注',
  `zhekoulv` int(11) NOT NULL,
  `xiaoshoujine` varchar(125) NOT NULL,
  `beizhu` varchar(125) DEFAULT NULL,
  `s_id` varchar(11) NOT NULL,
  PRIMARY KEY (`id`)
);
-- Dumping data for table `rep_demo_xiaoshou`


-- Table structure for table `test_customer`

DROP TABLE IF EXISTS `test_customer`;
CREATE TABLE `test_customer` (
  `id` int(32) NOT NULL AUTO_INCREMENT,
  `code` varchar(50) DEFAULT NULL COMMENT '客户编号',
  `name` varchar(50) DEFAULT NULL COMMENT '客户名称',
  `address` varchar(100) DEFAULT NULL COMMENT '客户地址',
  `yylx` varchar(2) DEFAULT NULL COMMENT '营业类型',
  `zyyw` varchar(255) DEFAULT NULL COMMENT '主营业务',
  `clsj` date DEFAULT NULL COMMENT '成立时间',
  `fzr` varchar(50) DEFAULT NULL COMMENT '负责人',
  `phone` varchar(11) DEFAULT NULL COMMENT '手机号',
  `khyj` varchar(255) DEFAULT NULL COMMENT '客户意见',
  `xypd` varchar(255) DEFAULT NULL COMMENT '信用评定',
  `tbr` varchar(50) DEFAULT NULL COMMENT '填表人',
  `depts` varchar(50) DEFAULT NULL COMMENT '部门',
  PRIMARY KEY (`id`)
);
-- Dumping data for table `test_customer`


-- Table structure for table `test_monthly_report`

DROP TABLE IF EXISTS `test_monthly_report`;
CREATE TABLE `test_monthly_report` (
  `id` varchar(50) NOT NULL COMMENT '主键',
  `by_sjfdl_yg` double DEFAULT NULL COMMENT '本月-实际发电量-有功',
  `lj_sjfdl_yg` double DEFAULT NULL COMMENT '累计-实际发电量-有功',
  `by_sjfdl_wg` double DEFAULT NULL COMMENT '本月-实际发电量-无功',
  `lj_sjfdl_wg` double DEFAULT NULL COMMENT '累计-实际发电量-无功',
  `by_khfdl` double DEFAULT NULL COMMENT '本月-考核发电量',
  `lj_khfdl` double DEFAULT NULL COMMENT '累计-考核发电量',
  `by_zdrfd` double DEFAULT NULL COMMENT '本月-最大日发电',
  `lj_zdrfd` double DEFAULT NULL COMMENT '累计-最大日发电',
  `by_ypjzdcl` double DEFAULT NULL COMMENT '本月-月平均最大出力',
  `lj_ypjzdcl` double DEFAULT NULL COMMENT '累计-月平均最大出力',
  `by_ypjzxcl` double DEFAULT NULL COMMENT '本月-月平均最小出力',
  `lj_ypjzxcl` double DEFAULT NULL COMMENT '累计-月平均最小出力',
  `by_qmzjrl` double DEFAULT NULL COMMENT '本月-期末装机容量',
  `lj_qmzjrl` double DEFAULT NULL COMMENT '累计-期末装机容量',
  `by_fdddjh` double DEFAULT NULL COMMENT '本月-发电调度计划',
  `lj_fdddjh` double DEFAULT NULL COMMENT '累计-发电调度计划',
  `by_jkzsl` double DEFAULT NULL COMMENT '本月-进库总水量',
  `lj_jkzsl` double DEFAULT NULL COMMENT '累计-进库总水量',
  `by_jyl` double DEFAULT NULL COMMENT '本月-降雨量',
  `lj_jyl` double DEFAULT NULL COMMENT '累计-降雨量',
  `by_zdjyl_zhi` double DEFAULT NULL COMMENT '本月-最大降雨量-值',
  `lj_zdjyl_zhi` double DEFAULT NULL COMMENT '累计-最大降雨量-值',
  `by_zdjyl_sj` double DEFAULT NULL COMMENT '本月-最大降雨量-时间',
  `lj_zdjyl_sj` double DEFAULT NULL COMMENT '累计-最大降雨量-时间',
  `by_zdrkll_zhi` double DEFAULT NULL COMMENT '本月-最大入库流量-值',
  `lj_zdrkll_zhi` double DEFAULT NULL COMMENT '累计-最大入库流量-值',
  `by_zdrkll_sj` double DEFAULT NULL COMMENT '本月-最大入库流量-时间',
  `lj_zdrkll_sj` double DEFAULT NULL COMMENT '累计-最大入库流量-时间',
  `by_zxrkll_zhi` double DEFAULT NULL COMMENT '本月-最小入库流量-值',
  `lj_zxrkll_zhi` double DEFAULT NULL COMMENT '累计-最小入库流量-值',
  `by_zxrkll_sj` double DEFAULT NULL COMMENT '本月-最小入库流量-时间',
  `lj_zxrkll_sj` double DEFAULT NULL COMMENT '累计-最小入库流量-时间',
  `by_jyts` double DEFAULT NULL COMMENT '本月-降雨天数',
  `lj_jyts` double DEFAULT NULL COMMENT '累计-降雨天数',
  `by_qsts` double DEFAULT NULL COMMENT '本月-弃水天数',
  `lj_qsts` double DEFAULT NULL COMMENT '累计-弃水天数',
  PRIMARY KEY (`id`)
);
-- Dumping data for table `test_monthly_report`


-- Table structure for table `test_order`

DROP TABLE IF EXISTS `test_order`;
CREATE TABLE `test_order` (
  `id` varchar(32) NOT NULL,
  `order_name` varchar(50) DEFAULT NULL COMMENT '订单名称',
  `order_no` varchar(50) DEFAULT NULL COMMENT '订单编号',
  `order_sign_date` datetime DEFAULT NULL COMMENT '订单签订日期',
  `order_delivery_date` datetime DEFAULT NULL COMMENT '订单交付日期',
  `order_coms` varchar(50) DEFAULT NULL COMMENT '客户名称',
  `order_pers` varchar(50) DEFAULT NULL COMMENT '客户联系人',
  `order_phone` varchar(15) DEFAULT NULL COMMENT '客户联系方式',
  `fzr` varchar(50) DEFAULT NULL COMMENT '负责人',
  `depts` varchar(50) DEFAULT NULL COMMENT '负责人部门',
  PRIMARY KEY (`id`)
);
-- Dumping data for table `test_order`


-- Table structure for table `test_order_pros`

DROP TABLE IF EXISTS `test_order_pros`;
CREATE TABLE `test_order_pros` (
  `id` varchar(32) NOT NULL,
  `pro_name` varchar(50) DEFAULT NULL COMMENT '产品名称',
  `pro_no` varchar(50) DEFAULT NULL COMMENT '产品编号',
  `pro_count` varchar(11) DEFAULT NULL COMMENT '产品数量',
  `pro_price` decimal(10,2) DEFAULT NULL COMMENT '产品单价',
  `pro_unit` varchar(10) DEFAULT NULL COMMENT '单位',
  `pro_model` varchar(10) DEFAULT NULL COMMENT '型号',
  `main_id` varchar(32) DEFAULT NULL COMMENT '外键',
  PRIMARY KEY (`id`)
);
-- Dumping data for table `test_order_pros`


-- Table structure for table `test_resume`

DROP TABLE IF EXISTS `test_resume`;
CREATE TABLE `test_resume` (
  `id` int(32) NOT NULL AUTO_INCREMENT,
  `name` varchar(50) DEFAULT NULL COMMENT '姓名',
  `sex` varchar(2) DEFAULT NULL COMMENT '性别',
  `birthday` date DEFAULT NULL COMMENT '出生日期',
  `phone` varchar(11) DEFAULT NULL COMMENT '手机号',
  `email` varchar(50) DEFAULT NULL COMMENT '邮箱',
  `address` varchar(255) DEFAULT NULL COMMENT '现住址',
  `native_place` varchar(200) DEFAULT NULL COMMENT '籍贯',
  `nation` varchar(100) DEFAULT NULL COMMENT '民族',
  `political_outlook` varchar(50) DEFAULT NULL COMMENT '政治面貌',
  `education` varchar(10) DEFAULT NULL COMMENT '学历',
  `graduation_school` varchar(50) DEFAULT NULL COMMENT '毕业院校',
  `self_evaluation` varchar(255) DEFAULT NULL COMMENT '自我评价',
  `salary_expectation` decimal(10,2) DEFAULT NULL COMMENT '期望薪资',
  `edu_experience` varchar(255) DEFAULT NULL COMMENT '教育经历',
  `work_experience` varchar(255) DEFAULT NULL COMMENT '工作经历',
  PRIMARY KEY (`id`)
);
-- Dumping data for table `test_resume`


-- Table structure for table `tmp_report_data_1`

DROP TABLE IF EXISTS `tmp_report_data_1`;
CREATE TABLE `tmp_report_data_1` (
  `monty` varchar(255) DEFAULT NULL COMMENT '月份',
  `main_income` decimal(10,2) DEFAULT NULL,
  `total` decimal(10,2) DEFAULT NULL,
  `his_lowest` decimal(10,2) DEFAULT NULL,
  `his_average` decimal(10,2) DEFAULT NULL,
  `his_highest` decimal(10,2) DEFAULT NULL
);
-- Dumping data for table `tmp_report_data_1`


-- Table structure for table `tmp_report_data_income`

DROP TABLE IF EXISTS `tmp_report_data_income`;
CREATE TABLE `tmp_report_data_income` (
  `biz_income` varchar(100) DEFAULT NULL,
  `bx_jj_yongjin` decimal(10,2) DEFAULT NULL,
  `bx_zx_money` decimal(10,2) DEFAULT NULL,
  `chengbao_gz_money` decimal(10,2) DEFAULT NULL,
  `bx_gg_moeny` decimal(10,2) DEFAULT NULL,
  `tb_zx_money` decimal(10,2) DEFAULT NULL,
  `neikong_zx_money` decimal(10,2) DEFAULT NULL,
  `total` decimal(10,2) DEFAULT NULL
);
-- Dumping data for table `tmp_report_data_income`

-- Dump completed on 2026-04-10 18:25:14

-- ========== 原 V34__jimureport_menu.sql ==========
-- 积木报表 / JimuBI 菜单（外链 iframe，is_frame=1 表示外链）

INSERT INTO sys_menu (menu_id, parent_id, menu_type, menu_name, order_num, path, component, query, route_name, is_frame, is_cache, visible, status, perms, icon, remark, del_flag, create_by, create_time)
VALUES (3000, -1, 'M', '数据可视化', 50, '/visual', 'Layout', NULL, 'Visual', '0', '0', '0', '0', NULL, 'chart', NULL, '0', 'system', CURRENT_TIMESTAMP);

INSERT INTO sys_menu (menu_id, parent_id, menu_type, menu_name, order_num, path, component, query, route_name, is_frame, is_cache, visible, status, perms, icon, remark, del_flag, create_by, create_time)
VALUES (3001, 3000, 'C', '报表工作台', 1, 'jimu-report', 'InnerLink', '/jmreport/list', 'JimuReportList', '1', '1', '0', '0', 'report:jimu:list', 'documentation', NULL, '0', 'system', CURRENT_TIMESTAMP);

INSERT INTO sys_menu (menu_id, parent_id, menu_type, menu_name, order_num, path, component, query, route_name, is_frame, is_cache, visible, status, perms, icon, remark, del_flag, create_by, create_time)
VALUES (3002, 3000, 'C', 'BI工作台', 2, 'jimu-bi', 'InnerLink', '/drag/list', 'JimuBiList', '1', '1', '0', '0', 'report:jimubi:list', 'dashboard', NULL, '0', 'system', CURRENT_TIMESTAMP);

INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 3000);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 3001);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 3002);

-- ========== 原 V35__jimureport_menu_path_fix.sql ==========
-- 顶级目录 path 须以 / 开头，否则 Vue Router 4 addRoute 报错
UPDATE sys_menu SET path = '/visual' WHERE menu_id = 3000 AND path = 'visual';


-- ========== 原 V38__jimubi_menu_view_query_fix.sql ==========
-- 积木 BI 大屏预览：DragIndexController 映射为 /drag/view?pageId=，非 /drag/page/view（后者为 REST 前缀，无页面处理器）
UPDATE sys_menu
SET query = CONCAT('/drag/view?pageId=', SUBSTRING(query, LENGTH('/drag/page/view?id=') + 1))
WHERE is_frame = '1'
  AND query LIKE '/drag/page/view?id=%';

UPDATE sys_menu
SET query = CONCAT('/drag/view?pageId=', SUBSTRING(query, LENGTH('/drag/page/view/') + 1))
WHERE is_frame = '1'
  AND query LIKE '/drag/page/view/%'
  AND query NOT LIKE '/drag/page/view?id=%';
