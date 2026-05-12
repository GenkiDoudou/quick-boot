package com.su60.quickboot.data.generator.query;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 数据库类型
 *
 * @author luyanan
 * @since 2024/06/24
 **/
@AllArgsConstructor
@Getter
public enum DbType {

	/**
	 * MYSQL
	 */
	MYSQL("mysql", "MySql数据库"),
	;
	/**
	 * 数据库名称
	 */
	private String db;
	/**
	 * 描述
	 */
	private String desc;


	/**
	 * 获取数据库类型
	 *
	 * @param dbType 数据库类型字符串
	 * @return 数据类型
	 */
	public static DbType getDbType(String dbType) {
		for (DbType type : DbType.values()) {
			if (type.db.equalsIgnoreCase(dbType)) {
				return type;
			}
		}
		return MYSQL;
	}

	/**
	 * 根据url 判断数据库类型
	 *
	 * @param url url
	 * @return 数据库类型
	 * @since 2024/06/26
	 */
	public static DbType getDbTypeByUrl(String url) {
		return Arrays.stream(DbType.values()).filter(a -> url.contains(a.getDb()))
				.findFirst().orElseThrow(() -> new NullPointerException("未知的数据库类型:" + url));
	}

}
