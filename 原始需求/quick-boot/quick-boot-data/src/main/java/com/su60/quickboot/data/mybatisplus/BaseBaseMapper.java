package com.su60.quickboot.data.mybatisplus;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.cursor.Cursor;

/**
 * 基础Mapper
 *
 * @param <T> 泛型
 * @author luyanan
 * @since 2023/09/13
 **/
public interface BaseBaseMapper<T> extends BaseMapper<T> {

	/**
	 * 普通游标查询
	 */
	Cursor<T> selectCursor(@Param("ew") Wrapper<T> wrapper);

	/**
	 * PK 游标分页查询
	 */
	Cursor<T> selectByPkCursor(@Param("lastId") Long lastId,
							   @Param("size") Integer size,
							   @Param("ew") Wrapper<T> wrapper);
}
