package io.github.genkidoudou.system.internal.mapper;

import io.github.genkidoudou.common.mybatisplus.BaseBaseMapper;
import io.github.genkidoudou.system.api.LoginInfoBucketView;
import io.github.genkidoudou.system.api.LoginInfoSummaryView;
import io.github.genkidoudou.system.internal.entity.SysLogininfor;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 登录日志 Mapper。
 */
@Mapper
public interface SysLogininforMapper extends BaseBaseMapper<SysLogininfor> {

  @Select("""
    SELECT
      COUNT(DISTINCT CASE
        WHEN status = '0' AND user_name IS NOT NULL AND user_name <> '' THEN user_name
      END) AS loginUsers,
      COALESCE(SUM(CASE WHEN status = '0' THEN 1 ELSE 0 END), 0) AS successCount,
      COALESCE(SUM(CASE WHEN status = '1' THEN 1 ELSE 0 END), 0) AS failCount
    FROM sys_logininfor
    WHERE login_time >= #{start} AND login_time < #{end}
    """)
  LoginInfoSummaryView summarizeWindow(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

  @Select("""
    SELECT
      DATE_FORMAT(login_time, '%Y-%m-%d %H:00:00') AS bucket,
      COALESCE(SUM(CASE WHEN status = '0' THEN 1 ELSE 0 END), 0) AS successCount,
      COALESCE(SUM(CASE WHEN status = '1' THEN 1 ELSE 0 END), 0) AS failCount
    FROM sys_logininfor
    WHERE login_time >= #{start} AND login_time < #{end}
    GROUP BY DATE_FORMAT(login_time, '%Y-%m-%d %H:00:00')
    ORDER BY bucket
    """)
  List<LoginInfoBucketView> trendHourly(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

  @Select("""
    SELECT
      DATE_FORMAT(login_time, '%Y-%m-%d') AS bucket,
      COALESCE(SUM(CASE WHEN status = '0' THEN 1 ELSE 0 END), 0) AS successCount,
      COALESCE(SUM(CASE WHEN status = '1' THEN 1 ELSE 0 END), 0) AS failCount
    FROM sys_logininfor
    WHERE login_time >= #{start} AND login_time < #{end}
    GROUP BY DATE_FORMAT(login_time, '%Y-%m-%d')
    ORDER BY bucket
    """)
  List<LoginInfoBucketView> trendDaily(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}
