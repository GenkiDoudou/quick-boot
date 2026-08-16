package io.github.genkidoudou.system.api;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 登录日志只读聚合（供监控态势总览跨模块消费；不暴露实体）。
 */
public interface LoginInfoMonitorQuery {

  /**
   * 时间窗半开区间 {@code [start, end)} 内的登录汇总。
   *
   * @param start 起始（含）
   * @param end   截止（不含）
   * @return 汇总；无数据时计数为 0
   */
  LoginInfoSummaryView summarize(LocalDateTime start, LocalDateTime end);

  /**
   * 登录成功/失败分桶趋势。
   *
   * @param start   起始（含）
   * @param end     截止（不含）
   * @param hourly  {@code true} 按小时，否则按日
   * @return 分桶列表，按 bucket 升序
   */
  List<LoginInfoBucketView> trend(LocalDateTime start, LocalDateTime end, boolean hourly);

  /**
   * 日志中心：按时间窗与可选条件取最近若干条（倒序）。
   */
  List<LoginInfoHubView> listForHub(
    LocalDateTime beginTime,
    LocalDateTime endTime,
    String userName,
    String keyword,
    String status,
    String clientId,
    int limit);
}
