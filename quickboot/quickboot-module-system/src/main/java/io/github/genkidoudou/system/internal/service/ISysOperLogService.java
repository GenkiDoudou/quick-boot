package io.github.genkidoudou.system.internal.service;

import io.github.genkidoudou.common.api.PageInfo;
import io.github.genkidoudou.common.api.PageRequest;
import io.github.genkidoudou.system.internal.vo.SysOperLogVo;

import java.util.Collection;
import java.util.List;

/**
 * 操作日志管理服务。
 */
public interface ISysOperLogService {

  /**
   * 分页查询。
   *
   * @param pageRequest 分页与筛选
   * @return 分页结果
   */
  PageInfo<SysOperLogVo> page(PageRequest<SysOperLogVo> pageRequest);

  /**
   * 详情。
   *
   * @param operId 主键
   * @return 详情
   */
  SysOperLogVo getDetail(Long operId);

  /**
   * 批量删除。
   *
   * @param ids 主键集合
   */
  void remove(Collection<Long> ids);

  /**
   * 清空全部操作日志。
   */
  void cleanAll();

  /**
   * 导出列表（由 Controller 调用 ExcelUtils）。
   *
   * @param query 筛选或 ids
   * @return 导出行
   */
  List<SysOperLogVo> export(SysOperLogVo query);
}
