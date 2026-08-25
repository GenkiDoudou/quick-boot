package io.github.genkidoudou.system.internal.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.github.genkidoudou.common.api.PageInfo;
import io.github.genkidoudou.common.api.PageRequest;
import io.github.genkidoudou.common.exception.ErrorCodes;
import io.github.genkidoudou.common.exception.WarningException;
import io.github.genkidoudou.common.mybatisplus.BaseServiceImpl;
import io.github.genkidoudou.system.internal.dto.DeployRecordCallbackBo;
import io.github.genkidoudou.system.internal.entity.SysDeployRecord;
import io.github.genkidoudou.system.internal.mapper.SysDeployRecordMapper;
import io.github.genkidoudou.system.internal.service.ISysDeployRecordService;
import io.github.genkidoudou.system.internal.vo.SysDeployRecordVo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 发布记录服务实现。
 */
@Service
public class SysDeployRecordServiceImpl extends BaseServiceImpl<SysDeployRecordMapper, SysDeployRecord>
  implements ISysDeployRecordService {

  @Transactional(rollbackFor = Exception.class)
  @Override
  public void saveCallback(DeployRecordCallbackBo bo) {
    if (bo == null) {
      throw WarningException.literal(ErrorCodes.Common.INVALID_PARAM, "回调体不能为空");
    }
    SysDeployRecord row = new SysDeployRecord();
    row.setAppName(StrUtil.blankToDefault(bo.getAppName(), "").trim());
    row.setEnv(StrUtil.blankToDefault(bo.getEnv(), "").trim());
    row.setOperate(StrUtil.blankToDefault(bo.getOperate(), "").trim());
    row.setBranch(StrUtil.blankToDefault(bo.getBranch(), "").trim());
    row.setHosts(StrUtil.blankToDefault(bo.getHosts(), "").trim());
    row.setBuildNumber(StrUtil.blankToDefault(bo.getBuildNumber(), "").trim());
    row.setBuildUrl(StrUtil.blankToDefault(bo.getBuildUrl(), "").trim());
    row.setGitCommit(StrUtil.blankToDefault(bo.getGitCommit(), "").trim());
    row.setReleaseNotes(bo.getReleaseNotes());
    row.setStatus("0");
    this.save(row);
  }

  @Override
  public PageInfo<SysDeployRecordVo> page(PageRequest<SysDeployRecordVo> pageRequest) {
    SysDeployRecordVo param = pageRequest != null ? pageRequest.getParam() : null;
    return this.page(pageRequest, q -> {
      applyQuery(q, param);
      q.orderByDesc(SysDeployRecord::getCreateTime);
    }, SysDeployRecordVo.class);
  }

  @Override
  public SysDeployRecordVo getDetail(Long recordId) {
    SysDeployRecord row = this.getById(recordId);
    if (row == null) {
      throw WarningException.literal(ErrorCodes.Common.INVALID_PARAM, "发布记录不存在");
    }
    return toVo(row, SysDeployRecordVo.class);
  }

  private void applyQuery(LambdaQueryWrapper<SysDeployRecord> q, SysDeployRecordVo param) {
    if (param == null) {
      return;
    }
    q.eq(StrUtil.isNotBlank(param.getAppName()), SysDeployRecord::getAppName, param.getAppName());
    q.like(StrUtil.isNotBlank(param.getAppNameLike()), SysDeployRecord::getAppName, param.getAppNameLike());
    q.eq(StrUtil.isNotBlank(param.getEnv()), SysDeployRecord::getEnv, param.getEnv());
    q.eq(StrUtil.isNotBlank(param.getOperate()), SysDeployRecord::getOperate, param.getOperate());
    q.eq(StrUtil.isNotBlank(param.getStatus()), SysDeployRecord::getStatus, param.getStatus());
    q.like(StrUtil.isNotBlank(param.getBranch()), SysDeployRecord::getBranch, param.getBranch());
  }
}
