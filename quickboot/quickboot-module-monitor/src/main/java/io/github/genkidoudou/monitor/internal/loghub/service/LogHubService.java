package io.github.genkidoudou.monitor.internal.loghub.service;

import io.github.genkidoudou.monitor.internal.loghub.dto.LogHubListVo;
import io.github.genkidoudou.monitor.internal.loghub.dto.LogHubQueryBo;

/**
 * 日志中心合并查询服务：聚合 RUM、慢 SQL、操作日志、登录日志等来源。
 */
public interface LogHubService {

  /**
   * 按条件从多来源拉取日志并合并排序。
   *
   * @param query 时间范围、来源、关键字等筛选条件
   * @return 近似分页的合并结果；未指定时间时默认近 24 小时
   */
  LogHubListVo list(LogHubQueryBo query);
}
