package io.github.genkidoudou.monitor.internal.slowsql.mapper;

import io.github.genkidoudou.common.mybatisplus.BaseBaseMapper;
import io.github.genkidoudou.monitor.internal.slowsql.dto.SlowTopRow;
import io.github.genkidoudou.monitor.internal.slowsql.entity.SysSlowSql;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 慢 SQL 记录 Mapper。
 */
@Mapper
public interface SysSlowSqlMapper extends BaseBaseMapper<SysSlowSql> {

  /**
   * 统计指定时间窗口内慢 SQL 数量与耗时指标。
   *
   * @param start 窗口开始（含）
   * @param end   窗口结束（不含）
   * @return 含 slowSqlCount、slowAvgMs、slowMaxMs 的映射
   */
  @Select("""
    SELECT
      COUNT(*) AS slowSqlCount,
      ROUND(AVG(cost_time)) AS slowAvgMs,
      MAX(cost_time) AS slowMaxMs
    FROM sys_slow_sql
    WHERE create_time >= #{start} AND create_time < #{end}
    """)
  Map<String, Object> summarizeSlow(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

  /**
   * 查询指定时间窗口内耗时最高的 Top 5 慢 SQL。
   *
   * @param start 窗口开始（含）
   * @param end   窗口结束（不含）
   * @return 慢 SQL 摘要行列表
   */
  @Select("""
    SELECT
      slow_id AS slowId,
      sql_source AS sqlSource,
      sql_type AS sqlType,
      mapper_id AS mapperId,
      LEFT(sql_text, 200) AS sqlText,
      cost_time AS costTime
    FROM sys_slow_sql
    WHERE create_time >= #{start} AND create_time < #{end}
    ORDER BY cost_time DESC
    LIMIT 5
    """)
  List<SlowTopRow> listTopSlow(
    @Param("start") LocalDateTime start,
    @Param("end") LocalDateTime end);
}
