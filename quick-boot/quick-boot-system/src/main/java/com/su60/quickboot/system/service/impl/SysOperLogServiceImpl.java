package com.su60.quickboot.system.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.su60.quickboot.common.core.PageInfo;
import com.su60.quickboot.data.mybatisplus.BaseVoServiceImpl;
import com.su60.quickboot.data.mybatisplus.PageVoHandler;
import com.su60.quickboot.system.dos.SysOperLogDo;
import com.su60.quickboot.system.entity.SysOperLogEntity;
import com.su60.quickboot.system.mapper.SysOperLogMapper;
import com.su60.quickboot.system.service.ISysOperLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 操作日志记录 服务实现类
 * </p>
 *
 * @author luyanan
 * @since 2024/11/15
 */
@RequiredArgsConstructor
@Service
public class SysOperLogServiceImpl extends BaseVoServiceImpl<SysOperLogMapper, SysOperLogEntity, SysOperLogDo> implements ISysOperLogService {

	@Async
	@Override
	public void saveLog(SysOperLogDo sysOperLogDo) {
		super.saveVo(sysOperLogDo);
	}

	@Override
	public PageInfo<SysOperLogDo> page(SysOperLogDo sysOperLogDo) {
		return super.page(sysOperLogDo, new PageVoHandler<SysOperLogEntity, SysOperLogDo>() {
			@Override
			public void queryWrapperHandler(SysOperLogDo vo, SysOperLogEntity sysOperLogEntity, LambdaQueryWrapper<SysOperLogEntity> queryWrapper) {


				queryWrapper.like(StrUtil.isNotBlank(vo.getMethod()), SysOperLogEntity::getMethod, vo.getMethod());
				sysOperLogEntity.setMethod(null);
				queryWrapper.like(StrUtil.isNotBlank(vo.getRequestMethod()), SysOperLogEntity::getRequestMethod, vo.getRequestMethod());
				sysOperLogEntity.setRequestMethod(null);
				queryWrapper.like(StrUtil.isNotBlank(vo.getOperName()), SysOperLogEntity::getOperName, vo.getOperName());
				sysOperLogEntity.setOperName(null);
				queryWrapper.like(StrUtil.isNotBlank(vo.getOperUrl()), SysOperLogEntity::getOperUrl, vo.getOperUrl());
				sysOperLogEntity.setOperUrl(null);

				// 主机地址
				queryWrapper.like(StrUtil.isNotBlank(vo.getOperIp()), SysOperLogEntity::getOperIp, vo.getOperIp());
				sysOperLogEntity.setOperIp(null);
				queryWrapper.orderByDesc(SysOperLogEntity::getOperTime);
				Integer status = vo.getStatus();
				if (null != status) {
					if (status == 0) {
						queryWrapper.eq(SysOperLogEntity::getStatus, 200);
					} else {
						queryWrapper.ne(SysOperLogEntity::getStatus, 200);
					}
				}
				sysOperLogEntity.setStatus(null);

				// 操作时间区间筛选
				List<String> operTimes =
						vo.getOperTimes();

				if (CollectionUtil.isNotEmpty(operTimes) && operTimes.size() == 2) {
					queryWrapper.between(SysOperLogEntity::getOperTime,
							DateUtil.beginOfDay(DateUtil.parse(operTimes.get(0), DatePattern.NORM_DATE_PATTERN)),
							DateUtil.endOfDay(DateUtil.parse(operTimes.get(1), DatePattern.NORM_DATE_PATTERN)));
				}
			}


		});
	}

	@Override
	public SysOperLogDo getVoById(Long id) {
		return super.getVoById(id);
	}
}

