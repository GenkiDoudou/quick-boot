package com.su60.quickboot.data.generator.query;


import cn.hutool.core.util.StrUtil;

/**
 * mysql 表信息查询
 *
 * @author luyanan
 * @since 2024/06/24
 **/
public class MysqlTableQuery implements ITableQuery {
	@Override
	public DbType dbType() {
		return DbType.MYSQL;
	}

	@Override
	public String tablesSql(String tableName) {
		StringBuilder sql = new StringBuilder();
		sql.append("select table_name, table_comment from information_schema.tables ");
		sql.append("where table_schema = (select database()) ");
		//表名查询
		if (StrUtil.isNotBlank(tableName)) {
			sql.append("and table_name = '").append(tableName).append("' ");
		}

		// 排除定时任务表和flyway表
		sql.append(" and table_name not  like  'QRTZ_%' and TABLE_NAME not like 'flyway_%' ");
		sql.append("order by table_name asc");
		return sql.toString();
	}

	@Override
	public String tableName() {
		return "table_name";
	}

	@Override
	public String tableComment() {
		return "table_comment";
	}

	@Override
	public String tableFieldsSql(String tableName) {
		return "select column_name, data_type, column_comment, column_key, is_nullable from information_schema.columns "
				+ "where table_name = '" + tableName + "' and table_schema = (select database()) order by ordinal_position";
	}

	@Override
	public String fieldName() {
		return "column_name";
	}

	@Override
	public String fieldType() {
		return "data_type";
	}

	@Override
	public String fieldComment() {
		return "column_comment";
	}

	@Override
	public String fieldKey() {
		return "column_key";
	}

	@Override
	public String fieldIsNullable() {
		return "is_nullable";
	}
}
