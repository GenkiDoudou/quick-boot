package com.su60.quickboot.system.service;

import com.su60.quickboot.common.core.PageInfo;
import com.su60.quickboot.system.dos.SysConfigDo;

import java.util.List;

/**
 * <p>
 * 参数配置表 服务类
 * </p>
 *
 * @author luyanan
 * @since 2026/01/11
 */

public interface ISysConfigService {


	/**
	 * 分页查询
	 * @since 2026/01/11
	 * @param sysConfigDo 参数
	 * @return
	 */
	PageInfo<SysConfigDo> page(SysConfigDo sysConfigDo);

	/**
	 * 保存
	 * @since 2026/01/11
	 * @param sysConfigDo 参数
	 * @return
	 */
	Boolean save(SysConfigDo sysConfigDo);

	/**
	 * 根据id修改
	 * @since 2026/1/8
	 * @param sysConfigDo 参数
	 * @return
	 */
	Boolean updateById(SysConfigDo sysConfigDo);

	/**
	 * 根据id查询
	 * @since 2026/01/11
	 * @param id id
	 * @return
	 */
	SysConfigDo getVoById(Long id);

	/**
	 * 根据id集合查询
	 * @since 2026/01/11
	 * @param ids id集合
	 * @return
	 */
	Boolean deleteByIds(List<Long> ids);


	/**
	 * 根据键值名获取键值
	 * @since 2026/1/11
	 * @param configKey 参数键值名
	 * @return
	 */
	String getConfigValue(String configKey);
}
