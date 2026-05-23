package com.su60.quickboot.quartz.service;

import com.su60.quickboot.common.core.PageInfo;
import com.su60.quickboot.quartz.entity.SysJobLogEntity;
import com.su60.quickboot.quartz.dos.SysJobLogDo;

/**
 * <p>
 * 定时任务调度日志表 服务类
 * </p>
 *
 * @author luyanan
 * @since 2024/11/07
 */

public interface ISysJobLogService {

	/**
	 * 添加任务
	 *
	 * @param sysJobLog 任务日志
	 * @since 2024/11/11
	 */
	void addJobLog(SysJobLogDo sysJobLog);

	/**
	 * 清空日志
	 *
	 * @param jobId 任务id
	 * @return 是否成功
	 * @since 2024/11/14
	 */
	Boolean clean(Long jobId);

	/**
	 * 分页
	 * @since 2025/12/30
	 * @param sysJobLogDo
	 * @return
	 */
	PageInfo<SysJobLogDo> page(SysJobLogDo sysJobLogDo);

	/**
	 * 根据id查询
	 * @since 2025/12/30
	 * @param id  id
	 * @return
	 */
	SysJobLogDo getVoById(Long id);
}
