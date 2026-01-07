package com.su60.quickboot.system.service;

import com.su60.quickboot.common.core.PageInfo;
import com.su60.quickboot.system.entity.SysOperLogEntity;
import com.su60.quickboot.system.dos.SysOperLogDo;

/**
 * <p>
 * 操作日志记录 服务类
 * </p>
 *
 * @author luyanan
 * @since 2024/11/15
 */

public interface ISysOperLogService {
	/**
	 * 保存日志
	 * @since 2025/9/17 
	 * @param sysOperLogDo 日志
	 * @return
	 */
	void saveLog(SysOperLogDo sysOperLogDo);

	/**
	 * 分页查询
	 * @since 2025/12/30
	 * @param sysOperLogDo
	 * @return
	 */
	PageInfo<SysOperLogDo> page(SysOperLogDo sysOperLogDo);

	/**
	 * 根据id查询
	 * @since 2025/12/30
	 * @param id  id
	 * @return
	 */
	SysOperLogDo getVoById(Long id);
}
