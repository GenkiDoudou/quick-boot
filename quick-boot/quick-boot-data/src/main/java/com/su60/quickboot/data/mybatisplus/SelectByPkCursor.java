package com.su60.quickboot.data.mybatisplus;

import com.baomidou.mybatisplus.core.injector.AbstractMethod;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import org.apache.ibatis.mapping.SqlSource;

public class SelectByPkCursor extends AbstractMethod {

	public SelectByPkCursor() {
		super("selectByPkCursor");
	}

	@Override
	public org.apache.ibatis.mapping.MappedStatement injectMappedStatement(
			Class<?> mapperClass,
			Class<?> modelClass,
			TableInfo tableInfo) {
		String sqlMethodSql = "<script> %s SELECT %s FROM %s   %s and  %s > #{lastId} %s %s  limit #{size}</script>";
		String sql = String.format(sqlMethodSql, sqlFirst(), sqlSelectColumns(tableInfo, true), tableInfo.getTableName(),
				sqlWhereEntityWrapper(true, tableInfo), tableInfo.getKeyColumn(), sqlOrderBy(tableInfo), sqlComment());
		SqlSource sqlSource = super.createSqlSource(configuration, sql, modelClass);
		return this.addSelectMappedStatementForTable(mapperClass, methodName, sqlSource, tableInfo);
	}
}
