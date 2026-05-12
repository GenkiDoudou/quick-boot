package com.su60.quickboot.data.generator.query;

import com.su60.quickboot.data.generator.DataMap;
import com.su60.quickboot.data.generator.TableInfo;

/**
 * 代码生成拦截器
 *
 * @author luyanan
 * @since 2024/10/28
 */
public interface GeneratorInterceptor {


	/**
	 * 执行过滤
	 *
	 * @param tableInfo 表信息
	 * @param dataMap   数据map
	 * @return
	 * @since 2024/10/28
	 */
	void execute(TableInfo tableInfo, DataMap dataMap);
}
