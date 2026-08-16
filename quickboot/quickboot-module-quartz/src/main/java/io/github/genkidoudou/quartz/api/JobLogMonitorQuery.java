package io.github.genkidoudou.quartz.api;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 定时任务调度日志只读聚合（供监控态势总览；不暴露实体）。
 */
public interface JobLogMonitorQuery {

  /**
   * 时间窗半开区间 {@code [start, end)} 内成功/失败汇总。
   *
   * @param start 起始（含）
   * @param end   截止（不含）
   * @return 汇总
   */
  JobLogSummaryView summarize(LocalDateTime start, LocalDateTime end);

  /**
   * 时间窗内最近失败任务。
   *
   * @param start 起始（含）
   * @param end   截止（不含）
   * @param limit 条数上限
   * @return 失败列表，按时间倒序
   */
  List<JobLogFailView> listRecentFails(LocalDateTime start, LocalDateTime end, int limit);
}
