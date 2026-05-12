package com.su60.quickboot.data.generator;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 字段类型转换
 *
 * @author luyanan
 * @since 2024/06/25
 **/
@Data
@Accessors(chain = true)
public class FieldType {


	/**
	 * 字段类型
	 *
	 * @since 2024/06/25
	 */

	private String columnType;

	/**
	 * 属性类型
	 *
	 * @since 2024/06/25
	 */

	private String attrType;


	/**
	 * 包名
	 *
	 * @since 2024/06/25
	 */

	private String packageName;
}
