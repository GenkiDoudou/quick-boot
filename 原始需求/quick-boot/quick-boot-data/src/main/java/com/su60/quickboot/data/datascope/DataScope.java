package com.su60.quickboot.data.datascope;

/**
 * 数据权限范围
 * ⚠️ 由【角色】决定
 */
public enum DataScope {

	/** 所有数据 */
	ALL,

	/** 自定义部门 */
	CUSTOM,

	/** 本部门 */
	DEPT,

	/** 本部门及下级 */
	DEPT_AND_CHILD,

	/** 仅本人 */
	SELF
}
