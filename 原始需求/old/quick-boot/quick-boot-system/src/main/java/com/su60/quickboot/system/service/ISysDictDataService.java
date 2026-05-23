package com.su60.quickboot.system.service;

import com.su60.quickboot.common.core.PageInfo;
import com.su60.quickboot.system.entity.SysDictDataEntity;
import com.su60.quickboot.system.dos.SysDictDataDo;

import java.util.List;

/**
 * <p>
 * 字典数据表 服务类
 * </p>
 *
 * @author luyanan
 * @since 2024/08/07
 */

public interface ISysDictDataService {

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

	/**
	 * 分页查询
	 * @since 2025/12/30
	 * @param sysDictDataDo
	 * @return
	 */

	PageInfo<SysDictDataDo> page(SysDictDataDo sysDictDataDo);

	/**
	 * 保存
	 * @since 2025/12/30
	 * @param sysDictDataDo
	 * @return
	 */

	Boolean saveVo(SysDictDataDo sysDictDataDo);

	/**
	 * 根据id修改
	 * @since 2025/12/30
	 * @param sysDictDataDo
	 * @return
	 */
	Boolean updateVoById(SysDictDataDo sysDictDataDo);

	/**
	 * 根据id集合删除
	 * @since 2025/12/30
	 * @param ids  id集合
	 * @return
	 */
	Boolean deleteByIds(List<Long> ids);

	/**
	 * 根据id查询
	 * @since 2025/12/30
	 * @param id  id
	 * @return
	 */
	SysDictDataDo getVoById(Long id);
}
