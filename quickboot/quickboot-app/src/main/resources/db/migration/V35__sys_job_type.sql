/*
 * 定时任务类型扩展：Bean / HTTP / Script。
 * 依赖：V11（sys_job、sys_job_* 字典）。
 *
 * 字典列语义：
 *   sys_job.job_type → 任务类型(sys_job_type)，0=Bean 1=HTTP 2=Script
 */

ALTER TABLE sys_job ADD COLUMN job_type CHAR(1) NOT NULL DEFAULT '0';

ALTER TABLE sys_job
  MODIFY COLUMN params VARCHAR(2000);

UPDATE sys_job SET job_type = '0' WHERE job_type IS NULL OR job_type = '';

INSERT INTO sys_dict_type (dict_id, dict_name, dict_type, status, del_flag, remark, create_time)
SELECT 35, '任务类型', 'sys_job_type', '0', '0', '0Bean 1HTTP 2Script', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE dict_type = 'sys_job_type');

INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, list_class, is_default, status, del_flag, create_time)
SELECT 320, 1, 'Bean 任务', '0', 'sys_job_type', 'primary', '1', '0', '0', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_code = 320);
INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, list_class, is_default, status, del_flag, create_time)
SELECT 321, 2, 'HTTP 请求', '1', 'sys_job_type', 'success', '0', '0', '0', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_code = 321);
INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, list_class, is_default, status, del_flag, create_time)
SELECT 322, 3, '本地脚本', '2', 'sys_job_type', 'warning', '0', '0', '0', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_code = 322);
