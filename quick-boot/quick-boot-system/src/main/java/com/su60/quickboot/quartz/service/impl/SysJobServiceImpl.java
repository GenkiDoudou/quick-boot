package com.su60.quickboot.quartz.service.impl;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.su60.quickboot.common.bean.BeanConvertUtils;
import com.su60.quickboot.common.core.PageInfo;
import com.su60.quickboot.common.exception.WarningException;
import com.su60.quickboot.data.mybatisplus.BaseVoServiceImpl;
import com.su60.quickboot.data.mybatisplus.PageVoHandler;
import com.su60.quickboot.data.spring.SpringContextHolder;
import com.su60.quickboot.quartz.dos.SysJobDo;
import com.su60.quickboot.quartz.entity.SysJobEntity;
import com.su60.quickboot.quartz.mapper.SysJobMapper;
import com.su60.quickboot.quartz.service.ISysJobService;
import com.su60.quickboot.quartz.utils.AbstractQuartzJob;
import com.su60.quickboot.quartz.utils.CronUtils;
import com.su60.quickboot.quartz.utils.ITask;
import com.su60.quickboot.quartz.utils.ScheduleUtils;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.quartz.JobDataMap;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 定时任务调度表 服务实现类
 * </p>
 *
 * @author luyanan
 * @since 2024/11/07
 */
@RequiredArgsConstructor
@Service
public class SysJobServiceImpl extends BaseVoServiceImpl<SysJobMapper, SysJobEntity, SysJobDo> implements ISysJobService {
	@Autowired
	private Scheduler scheduler;

	@Override
	public List<SysJobDo> listAll() {
		return BeanConvertUtils.convertListTo(super.list(), SysJobDo::new);
	}

	@Override
	public Boolean changeStatus(Long id, String status) {
		try {
			SysJobDo sysJobDo = super.getVoById(id);
			this.updateById(new SysJobEntity()
					.setStatus(status)
					.setId(id));
			if (status.equals("0")) {
				// 恢复任务
				scheduler.resumeJob(ScheduleUtils.getJobKey(id, sysJobDo.getJobGroup()));
			} else if (status.equals("1")) {
				// 暂停任务
				scheduler.pauseJob(ScheduleUtils.getJobKey(id, sysJobDo.getJobGroup()));
			}
		} catch (SchedulerException e) {
			throw new RuntimeException(e);
		}
		return true;
	}


	@SneakyThrows
	@Override
	public Boolean run(Long id) {
		SysJobDo sysJobDo = super.getVoById(id);
		JobDataMap jobDataMap = new JobDataMap();
		jobDataMap.put(AbstractQuartzJob.TASK_PROPERTIES, sysJobDo);
		JobKey jobKey = ScheduleUtils.getJobKey(id, sysJobDo.getJobGroup());
		boolean result = false;
		if (scheduler.checkExists(jobKey)) {
			result = true;
			scheduler.triggerJob(jobKey, jobDataMap);
		}
		return result;
	}

	@Override
	public PageInfo<SysJobDo> page(SysJobDo sysJobDo) {
		return super.page(sysJobDo, new PageVoHandler<SysJobEntity, SysJobDo>() {
			@Override
			public void queryWrapperHandler(SysJobDo vo, SysJobEntity sysJobEntity, LambdaQueryWrapper<SysJobEntity> queryWrapper) {


				queryWrapper.like(StrUtil.isNotBlank(vo.getJobName()), SysJobEntity::getJobName, vo.getJobName());
				sysJobEntity.setJobName(null);
				queryWrapper.like(StrUtil.isNotBlank(vo.getInvokeTarget()), SysJobEntity::getInvokeTarget, vo.getInvokeTarget());
				sysJobEntity.setInvokeTarget(null);
			}
		});
	}

	@Override
	public Boolean saveVo(SysJobDo sysJobDo) {

		// 校验cron表达式是否正确
		if (!CronUtils.isValid(sysJobDo.getCronExpression())) {
			throw new WarningException(300000);
		}
		// 校验bean是否存在
		Object bean = null;


		try {
			bean = SpringContextHolder.getBean(sysJobDo.getInvokeTarget());
		} catch (Exception e) {
		}

		if (null == bean) {
			throw new WarningException(300001);
		}
		if (!(bean instanceof ITask)) {
			throw new WarningException(300002);
		}

		sysJobDo.setStatus("1");

		Boolean b = super.saveVo(sysJobDo);
		try {
			ScheduleUtils.createScheduleJob(scheduler, sysJobDo);
		} catch (SchedulerException e) {
			e.printStackTrace();
		}
		return b;
	}


	@Override
	public Boolean updateVoById(SysJobDo sysJobDo) {
		// 校验cron表达式是否正确
		if (!CronUtils.isValid(sysJobDo.getCronExpression())) {
			throw new WarningException(300000);
		}
		// 校验bean是否存在
		Object bean = null;


		try {
			bean = SpringContextHolder.getBean(sysJobDo.getInvokeTarget());
		} catch (Exception e) {
		}

		if (null == bean) {
			throw new WarningException(300001);
		}
		if (!(bean instanceof ITask)) {
			throw new WarningException(300002);
		}


		boolean b = super.updateVoById(sysJobDo);
		SysJobEntity sysJob = this.getById(sysJobDo.getId());
		try {
			ScheduleUtils.updateScheduleJob(scheduler, BeanConvertUtils.convertTo(sysJob, SysJobDo.class));
		} catch (SchedulerException e) {
			e.printStackTrace();
		}
		return b;
	}


	@SneakyThrows
	@Override
	public Boolean deleteByIds(List<Long> ids) {

		List<SysJobEntity> sysJobEntities = super.listByIds(ids);
		Boolean b = super.deleteByIds(ids);
		if (b) {

			List<SysJobDo> sysJobDos = BeanConvertUtils.convertListTo(sysJobEntities, SysJobDo.class);
			for (SysJobDo sysJobDo : sysJobDos) {
				scheduler.deleteJob(ScheduleUtils.getJobKey(sysJobDo.getId(), sysJobDo.getJobGroup()));
			}
		}
		return b;
	}

	@Override
	public SysJobDo getVoById(Long id) {

		SysJobDo sysJobDo = super.getVoById(id);
		List<Date> nextExecutions = CronUtils.getNextExecutions(sysJobDo.getCronExpression(), 5);
		sysJobDo.setNextTime(nextExecutions.stream().map(a -> DateUtil.format(a, DatePattern.NORM_DATETIME_PATTERN)).collect(Collectors.joining("\n")));

		return sysJobDo;
	}
}

