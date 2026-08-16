-- =============================================================================
-- QUICKBOOT 系统监控态势大屏 · 指标统计 SQL（方案 A）
-- 库：MariaDB / MySQL · 对齐 quickboot.sql DDL 与现网字典
--
-- 状态约定：
--   sys_logininfor.status  : '0' 成功 / '1' 失败
--   sys_oper_log.status    : 0 正常 / 1 异常
--   sys_job_log.status     : '0' 成功 / '1' 失败
--
-- 时间窗参数（按需替换）：
--   @start_time / @end_time
-- 示例（今日）：
--   SET @start_time = CONCAT(CURDATE(), ' 00:00:00');
--   SET @end_time   = NOW();
-- 昨日：
--   SET @start_time = CONCAT(DATE_SUB(CURDATE(), INTERVAL 1 DAY), ' 00:00:00');
--   SET @end_time   = CONCAT(CURDATE(), ' 00:00:00');
-- 本周（周一 00:00 起，WEEKDAY: 周一=0）：
--   SET @start_time = CONCAT(DATE_SUB(CURDATE(), INTERVAL WEEKDAY(CURDATE()) DAY), ' 00:00:00');
--   SET @end_time   = NOW();
-- 近7天：
--   SET @start_time = DATE_SUB(NOW(), INTERVAL 7 DAY);
--   SET @end_time   = NOW();
-- 本月：
--   SET @start_time = DATE_FORMAT(NOW(), '%Y-%m-01 00:00:00');
--   SET @end_time   = NOW();
-- =============================================================================

SET @start_time = CONCAT(CURDATE(), ' 00:00:00');
SET @end_time   = NOW();

-- ---------------------------------------------------------------------------
-- 1. 总用户（全量，不受时间窗）
-- ---------------------------------------------------------------------------
SELECT COUNT(*) AS total_users
FROM sys_user
WHERE del_flag = '0';

-- ---------------------------------------------------------------------------
-- 2. 登录用户数（时间窗内成功登录的去重用户）
-- ---------------------------------------------------------------------------
SELECT COUNT(DISTINCT user_name) AS login_users
FROM sys_logininfor
WHERE status = '0'
  AND login_time >= @start_time
  AND login_time <  @end_time
  AND user_name IS NOT NULL
  AND user_name <> '';

-- ---------------------------------------------------------------------------
-- 3. 登录成功 / 失败次数
-- ---------------------------------------------------------------------------
SELECT
  SUM(CASE WHEN status = '0' THEN 1 ELSE 0 END) AS login_success,
  SUM(CASE WHEN status = '1' THEN 1 ELSE 0 END) AS login_fail
FROM sys_logininfor
WHERE login_time >= @start_time
  AND login_time <  @end_time;

-- ---------------------------------------------------------------------------
-- 4. 登录趋势（今日/昨日：按小时；本周/近7天/本月：按日）
-- 4a. 按小时
-- ---------------------------------------------------------------------------
SELECT
  DATE_FORMAT(login_time, '%Y-%m-%d %H:00:00') AS bucket,
  SUM(CASE WHEN status = '0' THEN 1 ELSE 0 END) AS success_cnt,
  SUM(CASE WHEN status = '1' THEN 1 ELSE 0 END) AS fail_cnt
FROM sys_logininfor
WHERE login_time >= @start_time
  AND login_time <  @end_time
GROUP BY DATE_FORMAT(login_time, '%Y-%m-%d %H:00:00')
ORDER BY bucket;

-- 4b. 按日
SELECT
  DATE(login_time) AS bucket,
  SUM(CASE WHEN status = '0' THEN 1 ELSE 0 END) AS success_cnt,
  SUM(CASE WHEN status = '1' THEN 1 ELSE 0 END) AS fail_cnt
FROM sys_logininfor
WHERE login_time >= @start_time
  AND login_time <  @end_time
GROUP BY DATE(login_time)
ORDER BY bucket;

-- ---------------------------------------------------------------------------
-- 5–6. （已废弃）原 sys_client_track 访问页/会话/热门页面指标已随表 DROP 移除
-- ---------------------------------------------------------------------------
-- 访问与行为大盘指标不再维护；排障请查 sys_trace_index / sys_trace_span。


-- ---------------------------------------------------------------------------
-- 7. 请求次数 / 错误次数（sys_oper_log）
-- ---------------------------------------------------------------------------
SELECT
  COUNT(*) AS request_count,
  SUM(CASE WHEN status <> 0 THEN 1 ELSE 0 END) AS error_count
FROM sys_oper_log
WHERE oper_time >= @start_time
  AND oper_time <  @end_time;

-- ---------------------------------------------------------------------------
-- 8. 请求 / 错误趋势
-- 8a. 按小时
-- ---------------------------------------------------------------------------
SELECT
  DATE_FORMAT(oper_time, '%Y-%m-%d %H:00:00') AS bucket,
  COUNT(*) AS request_count,
  SUM(CASE WHEN status <> 0 THEN 1 ELSE 0 END) AS error_count
FROM sys_oper_log
WHERE oper_time >= @start_time
  AND oper_time <  @end_time
GROUP BY DATE_FORMAT(oper_time, '%Y-%m-%d %H:00:00')
ORDER BY bucket;

-- 8b. 按日
SELECT
  DATE(oper_time) AS bucket,
  COUNT(*) AS request_count,
  SUM(CASE WHEN status <> 0 THEN 1 ELSE 0 END) AS error_count
FROM sys_oper_log
WHERE oper_time >= @start_time
  AND oper_time <  @end_time
GROUP BY DATE(oper_time)
ORDER BY bucket;

-- ---------------------------------------------------------------------------
-- 9. 慢 SQL 次数 / 均耗时 / 最大耗时
-- ---------------------------------------------------------------------------
SELECT
  COUNT(*)              AS slow_sql_count,
  ROUND(AVG(cost_time)) AS slow_avg_ms,
  MAX(cost_time)        AS slow_max_ms
FROM sys_slow_sql
WHERE create_time >= @start_time
  AND create_time <  @end_time;

-- ---------------------------------------------------------------------------
-- 10. 慢 SQL 来源分布
-- ---------------------------------------------------------------------------
SELECT
  sql_source,
  COUNT(*) AS cnt
FROM sys_slow_sql
WHERE create_time >= @start_time
  AND create_time <  @end_time
GROUP BY sql_source
ORDER BY cnt DESC;

-- ---------------------------------------------------------------------------
-- 11. 最慢 SQL Top5
-- ---------------------------------------------------------------------------
SELECT
  slow_id,
  sql_source,
  sql_type,
  mapper_id,
  LEFT(sql_text, 200) AS sql_text,
  cost_time,
  request_uri,
  oper_name,
  create_time
FROM sys_slow_sql
WHERE create_time >= @start_time
  AND create_time <  @end_time
ORDER BY cost_time DESC
LIMIT 5;

-- ---------------------------------------------------------------------------
-- 12. 定时任务成功 / 失败 / 失败率
-- ---------------------------------------------------------------------------
SELECT
  SUM(CASE WHEN status = '0' THEN 1 ELSE 0 END) AS job_success,
  SUM(CASE WHEN status = '1' THEN 1 ELSE 0 END) AS job_fail_count,
  ROUND(
    SUM(CASE WHEN status = '1' THEN 1 ELSE 0 END) * 100.0
      / NULLIF(COUNT(*), 0),
    1
  ) AS job_fail_rate_pct
FROM sys_job_log
WHERE create_time >= @start_time
  AND create_time <  @end_time;

-- ---------------------------------------------------------------------------
-- 13. 最近失败任务（默认 5 条）
-- ---------------------------------------------------------------------------
SELECT
  job_log_id,
  job_name,
  job_group,
  invoke_target,
  job_message,
  LEFT(exception_info, 200) AS exception_info,
  create_time
FROM sys_job_log
WHERE status = '1'
  AND create_time >= @start_time
  AND create_time <  @end_time
ORDER BY create_time DESC
LIMIT 5;

-- ---------------------------------------------------------------------------
-- 14. 中上 KPI 一览（可一次取出）
-- ---------------------------------------------------------------------------
SELECT
  (SELECT COUNT(*) FROM sys_oper_log
    WHERE oper_time >= @start_time AND oper_time < @end_time) AS request_count,
  (SELECT SUM(CASE WHEN status <> 0 THEN 1 ELSE 0 END) FROM sys_oper_log
    WHERE oper_time >= @start_time AND oper_time < @end_time) AS error_count,
  (SELECT COUNT(*) FROM sys_slow_sql
    WHERE create_time >= @start_time AND create_time < @end_time) AS slow_sql_count,
  (SELECT COUNT(*) FROM sys_job_log
    WHERE status = '1'
      AND create_time >= @start_time AND create_time < @end_time) AS job_fail_count;
