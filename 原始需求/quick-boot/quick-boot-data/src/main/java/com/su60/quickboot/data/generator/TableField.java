package com.su60.quickboot.data.generator;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.HashMap;
import java.util.Map;

/**
 * 表字段
 *
 * @author luyanan
 * @since 2024/06/23
 **/
@Data
@Accessors(chain = true)
public class TableField {


	/**
	 * 列名
	 *
	 * @since 2024/06/23
	 */

	private String columnName;

	/**
	 * 字段类型
	 *
	 * @since 2024/06/23
	 */

	private String columnType;

	/**
	 * 字段注释
	 *
	 * @since 2024/06/23
	 */

	private String columnComment;

	/**
	 * 属性名
	 *
	 * @since 2024/06/23
	 */

	private String attrName;

	/**
	 * 属性类型
	 *
	 * @since 2024/06/23
	 */

	private String attrType;

	/**
	 * 包路径
	 *
	 * @since 2024/06/23
	 */

	private String packageName;

	/**
	 * 是否主键
	 *
	 * @since 2024/06/23
	 */

	private String isPk;

	/**
	 * 是否必填
	 *
	 * @since 2024/06/23
	 */

	private String isRequired;


	/**
	 * 扩展内容
	 *
	 * @since 2024/10/30
	 */

	private Map<String, Object> ext = new HashMap<>();
}
