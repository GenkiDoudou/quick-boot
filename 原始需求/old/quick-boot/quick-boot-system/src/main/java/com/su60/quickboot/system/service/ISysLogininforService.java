package com.su60.quickboot.system.service;

import com.su60.quickboot.common.core.PageInfo;
import com.su60.quickboot.system.entity.SysLogininforEntity;
import com.su60.quickboot.system.dos.SysLogininforDo;

/**
 * <p>
 * 系统访问记录 服务类
 * </p>
 *
 * @author luyanan
 * @since 2024/11/15
 */

public interface ISysLogininforService {

	/**
	 * 保存日志
	 * @since 2025/12/21
	 * @param sysLogininforDo
	 * @return
	 */

	void saveLog(SysLogininforDo sysLogininforDo);

	/**
	 * 分页查询
	 * @since 2025/12/30
	 * @param sysLogininforDo
	 * @return
	 */

	PageInfo<SysLogininforDo> page(SysLogininforDo sysLogininforDo);

	/**
	 * 根据id查询
	 * @since 2025/12/30
	 * @param id  id
	 * @return
	 */
	SysLogininforDo getVoById(Long id);
}
