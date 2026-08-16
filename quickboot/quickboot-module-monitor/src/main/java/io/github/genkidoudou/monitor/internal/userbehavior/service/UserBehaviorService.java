package io.github.genkidoudou.monitor.internal.userbehavior.service;

import io.github.genkidoudou.monitor.internal.userbehavior.dto.UserBehaviorNodeVo;
import io.github.genkidoudou.monitor.internal.userbehavior.dto.UserBehaviorSessionQueryBo;
import io.github.genkidoudou.monitor.internal.userbehavior.dto.UserBehaviorSessionVo;

import java.util.List;

/**
 * 用户行为分析服务：基于 RUM 事件聚合会话与时间线。
 */
public interface UserBehaviorService {

  /**
   * 按用户或 sessionId 查询会话摘要列表。
   *
   * @param query 用户标识、时间范围与条数限制
   * @return 会话摘要列表
   */
  List<UserBehaviorSessionVo> listSessions(UserBehaviorSessionQueryBo query);

  /**
   * 查询指定会话内的事件时间线。
   *
   * @param sessionId  会话标识
   * @param beginTime  可选，开始时间
   * @param endTime    可选，结束时间
   * @return 按时间排序的行为节点列表
   */
  List<UserBehaviorNodeVo> timeline(String sessionId, String beginTime, String endTime);
}
