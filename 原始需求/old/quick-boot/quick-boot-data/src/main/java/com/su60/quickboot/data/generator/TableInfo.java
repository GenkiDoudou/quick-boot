package com.su60.quickboot.data.generator;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 表信息
 *
 * @author luyanan
 * @since 2024/06/23
 **/
@Data
@Accessors(chain = true)
public class TableInfo {


	/**
	 * 表名
	 *
	 * @since 2024/06/23
	 */

	private String tableName;

	/**
	 * 类名
	 *
	 * @since 2024/06/23
	 */

	private String className;

	/**
	 * 表注释
	 *
	 * @since 2024/06/23
	 */

	private String tableComment;

	/**
	 * 表字段信息
	 *
	 * @since 2024/06/26
	 */

	private List<TableField> tableFields;
}
