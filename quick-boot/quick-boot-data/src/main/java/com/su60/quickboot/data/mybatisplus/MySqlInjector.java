package com.su60.quickboot.data.mybatisplus;

import com.baomidou.mybatisplus.core.injector.AbstractMethod;
import com.baomidou.mybatisplus.core.injector.DefaultSqlInjector;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.extension.injector.methods.InsertBatchSomeColumn;

import java.util.List;

/**
 * @author luyanan
 * @since 2024/03/21
 **/
public class MySqlInjector extends DefaultSqlInjector {
	@Override
	public List<AbstractMethod> getMethodList(Class<?> mapperClass, TableInfo tableInfo) {
		List<AbstractMethod> methodList = super.getMethodList(mapperClass, tableInfo);
		methodList.add(new InsertBatchSomeColumn());
		// 注入自定义 SelectCursor
		methodList.add(new SelectCursor());

		methodList.add(new SelectByPkCursor());

		return methodList;
	}

}
