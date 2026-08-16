package io.github.genkidoudou.quartz.internal.mapper;

import io.github.genkidoudou.common.mybatisplus.BaseBaseMapper;
import io.github.genkidoudou.quartz.api.JobLogFailView;
import io.github.genkidoudou.quartz.api.JobLogSummaryView;
import io.github.genkidoudou.quartz.internal.entity.SysJobLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 调度日志 Mapper。
 */
@Mapper
public interface SysJobLogMapper extends BaseBaseMapper<SysJobLog> {

  @Select("""
    SELECT
      COALESCE(SUM(CASE WHEN status = '0' THEN 1 ELSE 0 END), 0) AS successCount,
      COALESCE(SUM(CASE WHEN status = '1' THEN 1 ELSE 0 END), 0) AS failCount,
      ROUND(
        SUM(CASE WHEN status = '1' THEN 1 ELSE 0 END) * 100.0 / NULLIF(COUNT(*), 0),
        1
      ) AS failRatePct
    FROM sys_job_log
    WHERE create_time >= #{start} AND create_time < #{end}
    """)
  JobLogSummaryView summarizeWindow(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

  @Select("""
    SELECT
      job_log_id AS jobLogId,
      job_name AS jobName,
      job_group AS jobGroup,
      invoke_target AS invokeTarget,
      job_message AS jobMessage,
      LEFT(exception_info, 200) AS exceptionInfo,
      create_time AS createTime
    FROM sys_job_log
    WHERE status = '1'
      AND create_time >= #{start}
      AND create_time < #{end}
    ORDER BY create_time DESC
    LIMIT #{limit}
    """)
  List<JobLogFailView> listRecentFails(
    @Param("start") LocalDateTime start,
    @Param("end") LocalDateTime end,
    @Param("limit") int limit);
}
