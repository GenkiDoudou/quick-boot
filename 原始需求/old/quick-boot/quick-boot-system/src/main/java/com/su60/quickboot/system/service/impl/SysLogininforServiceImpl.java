package com.su60.quickboot.system.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.su60.quickboot.common.core.PageInfo;
import com.su60.quickboot.data.mybatisplus.BaseVoServiceImpl;
import com.su60.quickboot.data.mybatisplus.PageVoHandler;
import com.su60.quickboot.system.dos.SysLogininforDo;
import com.su60.quickboot.system.entity.SysLogininforEntity;
import com.su60.quickboot.system.mapper.SysLogininforMapper;
import com.su60.quickboot.system.service.ISysLogininforService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 系统访问记录 服务实现类
 * </p>
 *
 * @author luyanan
 * @since 2024/11/15
 */
@RequiredArgsConstructor
@Service
public class SysLogininforServiceImpl extends BaseVoServiceImpl<SysLogininforMapper, SysLogininforEntity, SysLogininforDo> implements ISysLogininforService {


	@Override
	public Boolean saveVo(SysLogininforDo sysLogininforDo) {
		return super.saveVo(sysLogininforDo);
	}

	@Async
	@Override
	public void saveLog(SysLogininforDo sysLogininforDo) {
		super.saveVo(sysLogininforDo);
	}

	@Override
	public PageInfo<SysLogininforDo> page(SysLogininforDo sysLogininforDo) {
		return super.page(sysLogininforDo, new PageVoHandler<SysLogininforEntity, SysLogininforDo>() {
			@Override
			public void queryWrapperHandler(SysLogininforDo vo, SysLogininforEntity sysLogininforEntity, LambdaQueryWrapper<SysLogininforEntity> queryWrapper) {


				queryWrapper.like(StrUtil.isNotBlank(vo.getUserName()), SysLogininforEntity::getUserName, vo.getUserName());
				sysLogininforEntity.setUserName(null);
				queryWrapper.like(StrUtil.isNotBlank(vo.getIpaddr()), SysLogininforEntity::getIpaddr, vo.getIpaddr());
				sysLogininforEntity.setIpaddr(null);
				queryWrapper.orderByDesc(SysLogininforEntity::getLoginTime);

				// 登录时间筛选
				List<String> loginTimes = vo.getLoginTimes();
				if (CollectionUtil.isNotEmpty(loginTimes) && loginTimes.size() == 2) {
					queryWrapper.between(SysLogininforEntity::getLoginTime,
							DateUtil.beginOfDay(DateUtil.parse(loginTimes.get(0), DatePattern.NORM_DATE_PATTERN)),
							DateUtil.endOfDay(DateUtil.parse(loginTimes.get(1), DatePattern.NORM_DATE_PATTERN)));
				}
			}


		});
	}

	@Override
	public SysLogininforDo getVoById(Long id) {
		return super.getVoById(id);
	}
}

