package com.su60.quickboot.data.datascope;


/**
 * 表名匹配工具
 */
public class TableMatchUtil {

	/**
	 * 判断实际表名是否匹配注解配置
	 */
	public static boolean match(String actual, String[] patterns) {
		for (String p : patterns) {
			if (p.contains("*")) {
				if (actual.matches(p.replace("*", ".*"))) {
					return true;
				}
			} else if (actual.equalsIgnoreCase(p)) {
				return true;
			}
		}
		return false;
	}
}
