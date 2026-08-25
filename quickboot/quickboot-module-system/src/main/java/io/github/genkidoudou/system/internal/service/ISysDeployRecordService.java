package io.github.genkidoudou.system.internal.service;

import io.github.genkidoudou.common.api.PageInfo;
import io.github.genkidoudou.common.api.PageRequest;
import io.github.genkidoudou.system.internal.dto.DeployRecordCallbackBo;
import io.github.genkidoudou.system.internal.vo.SysDeployRecordVo;

/**
 * 发布记录：Jenkins 回调入库与管理端查询。
 */
public interface ISysDeployRecordService {

  /**
   * Jenkins 成功回调写入一条记录（status=0）。
   *
   * @param bo 回调体
   */
  void saveCallback(DeployRecordCallbackBo bo);

  /**
   * 分页查询。
   *
   * @param pageRequest 分页与筛选
   * @return 分页结果
   */
  PageInfo<SysDeployRecordVo> page(PageRequest<SysDeployRecordVo> pageRequest);

  /**
   * 详情。
   *
   * @param recordId 主键
   * @return VO
   */
  SysDeployRecordVo getDetail(Long recordId);
}
