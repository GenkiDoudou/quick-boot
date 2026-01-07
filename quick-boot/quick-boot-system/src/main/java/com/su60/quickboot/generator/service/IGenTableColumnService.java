package com.su60.quickboot.generator.service;

import com.su60.quickboot.common.core.PageInfo;
import com.su60.quickboot.generator.entity.GenTableColumnEntity;
import com.su60.quickboot.generator.dos.GenTableColumnDo;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

/**
 * <p>
 * 代码生成业务表字段 服务类
 * </p>
 *
 * @author luyanan
 * @since 2024/10/24
 */

public interface IGenTableColumnService {

	/**
	 * 批量插入
	 *
	 * @param tableColumnEntities 表字段集合
	 * @since 2024/10/24
	 */
	void saveBatch(List<GenTableColumnEntity> tableColumnEntities);

	/**
	 * 根据表id删除
	 *
	 * @param ids 表id
	 * @since 2024/10/26
	 */
	void deleteByTables(Collection<? extends Serializable> ids);

	/**
	 * 批量修改
	 *
	 * @param genTableColumnDos 字段集合
	 * @since 2024/10/27
	 */
	void updateBatch(List<GenTableColumnDo> genTableColumnDos);

	/**
	 * 根据表id集合获取字段配置
	 *
	 * @param tableIds 表id集合
	 * @return 表字段配置
	 * @since 2024/10/28
	 */
	List<GenTableColumnEntity> listByTableIds(List<Long> tableIds);

	/**
	 * 根据表id查询
	 *
	 * @param tableId 表id
	 * @return 表字典配置
	 * @since 2024/10/30
	 */
	List<GenTableColumnEntity> listByTableId(Long tableId);

	/**
	 * 分页查询
	 * @since 2025/12/30
	 * @param genTableColumnDo
	 * @return
	 */

	PageInfo<GenTableColumnDo> page(GenTableColumnDo genTableColumnDo);

}
