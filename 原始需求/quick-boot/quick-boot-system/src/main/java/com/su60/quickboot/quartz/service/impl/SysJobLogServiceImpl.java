package com.su60.quickboot.quartz.service.impl;

import cn.hutool.core.util.StrUtil;
import com.su60.quickboot.common.core.PageInfo;
import com.su60.quickboot.data.mybatisplus.BaseServiceImpl;
import com.su60.quickboot.data.mybatisplus.BaseVoServiceImpl;
import com.su60.quickboot.data.mybatisplus.PageVoHandler;
import com.su60.quickboot.quartz.entity.SysJobLogEntity;
import com.su60.quickboot.quartz.dos.SysJobLogDo;
import com.su60.quickboot.quartz.mapper.SysJobLogMapper;
import com.su60.quickboot.quartz.service.ISysJobLogService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

/**
 * <p>
 * 定时任务调度日志表 服务实现类
 * </p>
 *
 * @author luyanan
 * @since 2024/11/07
 */
@RequiredArgsConstructor
@Service
public class SysJobLogServiceImpl extends BaseVoServiceImpl<SysJobLogMapper, SysJobLogEntity, SysJobLogDo> implements ISysJobLogService {

	@Override
	public void addJobLog(SysJobLogDo sysJobLog) {
		this.saveVo(sysJobLog);
	}

	@Override
	public Boolean clean(Long jobId) {
		return super.remove(new LambdaQueryWrapper<SysJobLogEntity>()
				.eq(null != jobId, SysJobLogEntity::getJobId, jobId));
	}

	@Override
	public PageInfo<SysJobLogDo> page(SysJobLogDo sysJobLogDo) {
		return super.page(sysJobLogDo, new PageVoHandler<SysJobLogEntity, SysJobLogDo>() {
			@Override
			public void queryWrapperHandler(SysJobLogDo vo, SysJobLogEntity sysJobLogEntity, LambdaQueryWrapper<SysJobLogEntity> queryWrapper) {
				queryWrapper.like(StrUtil.isNotBlank(sysJobLogEntity.getJobName()), SysJobLogEntity::getJobName, vo.getJobName());
				sysJobLogEntity.setJobName(null);
				queryWrapper.like(StrUtil.isNotBlank(sysJobLogEntity.getInvokeTarget()), SysJobLogEntity::getInvokeTarget, vo.getInvokeTarget());
				sysJobLogEntity.setInvokeTarget(null);
				queryWrapper.orderByDesc(SysJobLogEntity::getCreateTime);

			}


		});
	}

	@Override
	public SysJobLogDo getVoById(Long id) {
		return super.getVoById(id);
	}
}

