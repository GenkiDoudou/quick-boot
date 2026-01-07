package com.su60.quickboot.system.service;

import com.su60.quickboot.common.core.PageInfo;
import com.su60.quickboot.system.dos.SysDictTypeDo;
import com.su60.quickboot.system.entity.SysDictTypeEntity;
import jakarta.servlet.http.HttpServletResponse;

import java.util.List;

/**
 * <p>
 * 字典类型表 服务类
 * </p>
 *
 * @author luyanan
 * @since 2024/08/07
 */

public interface ISysDictTypeService {


	/**
	 * 查询所有的字典
	 *
	 * @return 所有的字典
	 * @since 2024/10/26
	 */
	List<SysDictTypeEntity> listAll();

	/**
	 * excel导出
	 * @since 2025/12/20
	 * @param dictTypeDo
	 * @param response
	 * @return
	 */

	void export(SysDictTypeDo dictTypeDo, HttpServletResponse response) throws Exception;

	/**
	 * 分页查询
	 * @since 2025/12/30
	 * @param sysDictTypeDo
	 * @return
	 */
	PageInfo<SysDictTypeDo> page(SysDictTypeDo sysDictTypeDo);

	/**
	 * 根据 id查询
	 * @since 2025/12/30
	 * @param id
	 * @return
	 */
	SysDictTypeDo getVoById(Long id);

	/**
	 * 保存
	 * @since 2025/12/30
	 * @param sysDictTypeDo
	 * @return
	 */

	Boolean saveVo(SysDictTypeDo sysDictTypeDo);

	/**
	 * 根据id修改
	 * @since 2025/12/30
	 * @param sysDictTypeDo
	 * @return
	 */
	Boolean updateVoById(SysDictTypeDo sysDictTypeDo);

	/**
	 * 根据id集合删除
	 * @since 2025/12/30
	 * @param ids  id集合
	 * @return
	 */
	Boolean deleteByIds(List<Long> ids);
}
