package io.github.genkidoudou.system.internal.mapper;

import io.github.genkidoudou.common.mybatisplus.BaseBaseMapper;
import io.github.genkidoudou.system.api.OperLogBucketView;
import io.github.genkidoudou.system.api.OperLogSummaryView;
import io.github.genkidoudou.system.internal.entity.SysOperLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 操作日志 Mapper。
 */
@Mapper
public interface SysOperLogMapper extends BaseBaseMapper<SysOperLog> {

  @Select("""
    SELECT
      COUNT(*) AS requestCount,
      COALESCE(SUM(CASE WHEN status <> 0 THEN 1 ELSE 0 END), 0) AS errorCount
    FROM sys_oper_log
    WHERE oper_time >= #{start} AND oper_time < #{end}
    """)
  OperLogSummaryView summarizeWindow(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

  @Select("""
    SELECT
      DATE_FORMAT(oper_time, '%Y-%m-%d %H:00:00') AS bucket,
      COUNT(*) AS requestCount,
      COALESCE(SUM(CASE WHEN status <> 0 THEN 1 ELSE 0 END), 0) AS errorCount
    FROM sys_oper_log
    WHERE oper_time >= #{start} AND oper_time < #{end}
    GROUP BY DATE_FORMAT(oper_time, '%Y-%m-%d %H:00:00')
    ORDER BY bucket
    """)
  List<OperLogBucketView> trendHourly(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

  @Select("""
    SELECT
      DATE_FORMAT(oper_time, '%Y-%m-%d') AS bucket,
      COUNT(*) AS requestCount,
      COALESCE(SUM(CASE WHEN status <> 0 THEN 1 ELSE 0 END), 0) AS errorCount
    FROM sys_oper_log
    WHERE oper_time >= #{start} AND oper_time < #{end}
    GROUP BY DATE_FORMAT(oper_time, '%Y-%m-%d')
    ORDER BY bucket
    """)
  List<OperLogBucketView> trendDaily(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}
