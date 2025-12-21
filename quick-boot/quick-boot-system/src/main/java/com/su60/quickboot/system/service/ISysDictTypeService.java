package com.su60.quickboot.system.service;

import com.su60.quickboot.data.mybatisplus.IBaseService2;
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

public interface ISysDictTypeService extends IBaseService2<SysDictTypeEntity, SysDictTypeDo> {


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
}
