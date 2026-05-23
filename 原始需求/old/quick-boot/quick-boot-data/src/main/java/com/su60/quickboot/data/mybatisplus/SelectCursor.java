package com.su60.quickboot.data.mybatisplus;

import com.baomidou.mybatisplus.core.injector.AbstractMethod;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.toolkit.sql.SqlScriptUtils;
import org.apache.ibatis.cursor.Cursor;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.mapping.SqlSource;

import static org.apache.ibatis.ognl.OgnlRuntime.getMethod;

/**
 * 游标查询
 * @author luyanan
 * @since 2025/11/21
 */
public class SelectCursor extends AbstractMethod {

	public SelectCursor() {
		super("selectCursor");
	}

	@Override
	public MappedStatement injectMappedStatement(Class<?> mapperClass, Class<?> modelClass,
												 TableInfo tableInfo) {

		// SQL：和 selectList 类似，用 wrapper 生成动态 SQL
		String sql = SqlScriptUtils.convertChoose(
				"ew != null and ew.sqlSelect != null",
				"${ew.sqlSelect}",
				tableInfo.getAllSqlSelect()
		);

		String where = SqlScriptUtils.convertWhere(
				SqlScriptUtils.convertIf("${ew.customSqlSegment}",
						"ew != null and ew.nonEmptyOfWhere", true)
		);

		String sqlTemplate = String.format("SELECT %s FROM %s %s",
				sql,
				tableInfo.getTableName(),
				where
		);

		// 返回类型 Cursor
		SqlSource sqlSource = languageDriver.createSqlSource(configuration, sqlTemplate, modelClass);

		return this.addSelectMappedStatementForOther(mapperClass, sqlSource, Cursor.class);
	}
}
