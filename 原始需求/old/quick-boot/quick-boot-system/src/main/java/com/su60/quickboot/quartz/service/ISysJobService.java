package com.su60.quickboot.quartz.service;

import com.su60.quickboot.common.core.PageInfo;
import com.su60.quickboot.quartz.entity.SysJobEntity;
import com.su60.quickboot.quartz.dos.SysJobDo;

import java.util.List;

/**
 * <p>
 * 定时任务调度表 服务类
 * </p>
 *
 * @author luyanan
 * @since 2024/11/07
 */

public interface ISysJobService {

	/**
	 * 查询所有的定时任务
	 *
	 * @return 任务集合
	 * @since 2024/11/12
	 */
	List<SysJobDo> listAll();


	/**
	 * 修改状态
	 *
	 * @param id     任务id
	 * @param status 状态
	 * @return 是否成功
	 * @since 2024/11/13
	 */
	Boolean changeStatus(Long id, String status);

	/**
	 * 立即执行
	 *
	 * @param id 任务id
	 * @return 是否成功
	 * @since 2024/11/13
	 */
	Boolean run(Long id);

	/**
	 * 分页查询
	 * @since 2025/12/30
	 * @param sysJobDo
	 * @return
	 */

	PageInfo<SysJobDo> page(SysJobDo sysJobDo);

	/**
	 * 保存
	 * @since 2025/12/30
	 * @param sysJobDo
	 * @return
	 */
	Boolean saveVo(SysJobDo sysJobDo);

	/**
	 * 修改
	 * @since 2025/12/30
	 * @param sysJobDo
	 * @return
	 */
	Boolean updateVoById(SysJobDo sysJobDo);

	/**
	 * 根据id查询
	 * @since 2025/12/30
	 * @param id id
	 * @return
	 */
	SysJobDo getVoById(Long id);

	/**
	 * 根据id集合删除
	 * @since 2025/12/30
	 * @param ids  id集合
	 * @return
	 */
	Boolean deleteByIds(List<Long> ids);
}
