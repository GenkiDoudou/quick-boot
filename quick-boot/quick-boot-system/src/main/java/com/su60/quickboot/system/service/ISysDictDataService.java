package com.su60.quickboot.system.service;

import com.su60.quickboot.system.entity.SysDictDataEntity;
import com.su60.quickboot.system.dos.SysDictDataDo;
import com.su60.quickboot.data.mybatisplus.IBaseService2;

import java.util.List;

/**
 * <p>
 * 字典数据表 服务类
 * </p>
 *
 * @author luyanan
 * @since 2024/08/07
 */

public interface ISysDictDataService extends IBaseService2<SysDictDataEntity, SysDictDataDo> {

	/**
	 * 根据字段类型查询字典项的信息
	 *
	 * @param dictType 字典类型
	 * @return 字典项的信息
	 * @since 2024/8/18
	 */
	List<SysDictDataDo> listByDictType(String dictType);

	/**
	 * 是否包含字典项
	 *
	 * @param dictType 字典类型
	 * @return 是否包含字典项
	 * @since 2024/10/12
	 */
	boolean hasData(String dictType);


	/**
	 * 清空缓存
	 * @since 2025/11/29
	 * @return
	 */
	void clear();
}
