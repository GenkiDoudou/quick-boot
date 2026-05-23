package com.su60.quickboot.data.generator.query;


/**
 * 表信息查询
 *
 * @author luyanan
 * @since 2024/06/24
 **/
public interface ITableQuery {

	/**
	 * 数据库类型
	 *
	 * @return 数据库类型
	 * @since 2024/06/24
	 */
	DbType dbType();


	/**
	 * 获取表的sql
	 *
	 * @param tableName 表名
	 * @return 表sql
	 * @since 2024/06/24
	 */
	String tablesSql(String tableName);


	/**
	 * 表名称
	 *
	 * @return 表名称
	 * @since 2024/06/24
	 */
	String tableName();


	/**
	 * 表注释
	 *
	 * @return 表注释
	 * @since 2024/06/24
	 */
	String tableComment();


	/**
	 * 获取表字段信息
	 *
	 * @param tableName 表名
	 * @return 获取表字段信息
	 * @since 2024/06/24
	 */
	String tableFieldsSql(String tableName);

	/**
	 * 字段名
	 *
	 * @return 字段名
	 * @since 2024/06/24
	 */
	String fieldName();


	/**
	 * 字段类型
	 *
	 * @return 字段类型
	 * @since 2024/06/24
	 */
	String fieldType();


	/**
	 * 字段注释
	 *
	 * @return 字段注释
	 * @since 2024/06/24
	 */
	String fieldComment();


	/**
	 * 主键
	 *
	 * @return 主键
	 * @since 2024/06/24
	 */
	String fieldKey();

	/**
	 * 是否为空
	 *
	 * @return 是否为空
	 * @since 2024/07/08
	 */

	String fieldIsNullable();


}
