package io.github.genkidoudou.system.internal.service;

import io.github.genkidoudou.common.api.PageInfo;
import io.github.genkidoudou.common.api.PageRequest;
import io.github.genkidoudou.system.internal.vo.SysLogininforVo;

import java.util.Collection;
import java.util.List;

/**
 * 登录日志管理服务。
 */
public interface ISysLogininforService {

  /**
   * 分页查询。
   *
   * @param pageRequest 分页与筛选
   * @return 分页结果
   */
  PageInfo<SysLogininforVo> page(PageRequest<SysLogininforVo> pageRequest);

  /**
   * 批量删除。
   *
   * @param ids 主键集合
   */
  void remove(Collection<Long> ids);

  /**
   * 清空全部登录日志。
   */
  void cleanAll();

  /**
   * 导出列表。
   *
   * @param query 筛选或 ids
   * @return 导出行
   */
  List<SysLogininforVo> export(SysLogininforVo query);

  /**
   * 写入一条登录访问日志。
   *
   * @param username  用户名
   * @param userId    用户主键，失败可空
   * @param clientId  客户端 ID
   * @param ip        客户端 IP
   * @param userAgent User-Agent，可空
   * @param status    {@code 0} 成功 / {@code 1} 失败
   * @param msg       提示消息
   */
  void record(String username, Long userId, String clientId, String ip, String userAgent, String status, String msg);
}
