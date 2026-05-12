package com.su60.quickboot.generator.mapper;

import com.su60.quickboot.generator.entity.GenTableEntity;
import com.su60.quickboot.data.mybatisplus.BaseBaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * <p>
 * 代码生成业务表 Mapper 接口
 * </p>
 *
 * @author luyanan
 * @since 2024/10/15
 */
@Mapper
public interface GenTableMapper extends BaseBaseMapper<GenTableEntity> {

	/**
	 * 创建表
	 *
	 * @param sql
	 * @return
	 * @since 2024/10/23
	 */
	@Update("${sql}")
	void createTable(@Param("sql") String sql);
}

